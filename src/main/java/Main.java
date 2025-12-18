import java.io.PrintStream;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();

        // I want to turn off escaping so that the custom behaivor in ShellParser.java
        // is maintained.
        DefaultParser jlineParser = new DefaultParser();

        jlineParser.setEscapeChars(null);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(jlineParser)
                .completer(Completers.getCompleter())
                .build();
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
