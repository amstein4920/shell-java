import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Buffer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.Widget;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import Completers.Completers;

public class Main {
    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();

        // I want to turn off escaping so that the custom behaivor in ShellParser.java
        // is maintained.
        DefaultParser jlineParser = new DefaultParser();
        jlineParser.setEscapeChars(null);

        Completer completer = Completers.getCompleter();

        DoubleTabState state = new DoubleTabState();

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(jlineParser)
                .completer(completer)
                .build();

        Widget doubleTabWidget = () -> {
            Buffer buffer = reader.getBuffer();
            String currentBuffer = buffer.toString();

            ParsedLine parsedLine = reader.getParser().parse(currentBuffer, buffer.cursor());

            List<Candidate> candidates = new ArrayList<>();
            completer.complete(reader, parsedLine, candidates);

            List<Candidate> matches = candidates.stream()
                    .filter(c -> c.value().toLowerCase(Locale.ROOT)
                            .startsWith(parsedLine.word().toLowerCase(Locale.ROOT)))
                    .distinct()
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                // Write bell sound
                terminal.writer().print("\u0007");
                terminal.flush();
                state.lastWasTab = false;
                return true;
            }

            // If only one match, complete like normal
            if (matches.size() == 1) {
                Candidate c = matches.get(0);

                buffer.backspace(parsedLine.word().length());
                buffer.write(c.value() + " ");

                state.lastWasTab = false;
                return true;
            }

            boolean sameBuffer = currentBuffer.equals(state.lastBuffer);

            if (!state.lastWasTab || !sameBuffer) {
                // First Tab
                terminal.writer().print("\u0007");
                terminal.flush();

                state.lastWasTab = true;
                state.lastBuffer = currentBuffer;
                return true;
            }

            // Second Tab
            terminal.writer().println();
            matches.stream()
                    .map(Candidate::value)
                    .distinct()
                    .sorted()
                    .forEach(c -> terminal.writer().print(c + "  "));

            terminal.writer().println();
            terminal.flush();

            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);

            state.lastWasTab = false;
            return true;
        };

        reader.getWidgets().put("double-tab", doubleTabWidget);

        KeyMap<Binding> keyMap = reader.getKeyMaps().get(LineReader.MAIN);
        keyMap.bind(new Reference("double-tab"), "\t");

        do {
            String input = reader.readLine("$ ");

            Parser parser = new Parser();
            String[] arguments = parser.parse(input); // [command, args?]
            PrintStream console = System.out;
            arguments = RedirectOperation.redirectOutputs(arguments);
            Command command = Commands.get(arguments[0]);
            if (command != null) {
                command.execute(arguments);
            } else {
                new Executable().execute(arguments);
            }
            System.setOut(console);
        } while (true);
    }
}

class DoubleTabState {
    boolean lastWasTab = false;
    String lastBuffer = "";
}