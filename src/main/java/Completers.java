import org.jline.reader.Completer;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

public class Completers {
    public static Completer getCompleter() {
        Completer builtinsCompleter = new StringsCompleter("exit", "echo", "pwd", "cd", "type");
        Completer executablesCompleter = new ArgumentCompleter(
                new StringsCompleter(PathExecutables.getExecutablesOnPath()), new NullCompleter());

        Completer completer = new AggregateCompleter(builtinsCompleter,
                executablesCompleter);

        return completer;
    }
}
