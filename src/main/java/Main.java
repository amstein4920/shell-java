import java.io.PrintStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        // This scanner is tied to System.in and used for the entire lifecycle of the
        // application.
        // From this, I don't believe there is ever reason to close it.
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("$ ");

            String input = scanner.nextLine();

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
