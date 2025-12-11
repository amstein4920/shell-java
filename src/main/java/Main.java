import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
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

            for (int i = 0; i < arguments.length; i++) {
                if (i < arguments.length - 1) {
                    if (Arrays.asList(new String[] { ">", "1>" }).contains(arguments[i])) {
                        PrintStream newPS;
                        try {
                            newPS = new PrintStream(arguments[i + 1]);
                            System.setOut(newPS);
                            arguments = Arrays.copyOfRange(arguments, 0, i);
                        } catch (FileNotFoundException e) {
                            System.err.println("Output File Couldn't Be Created: " + e.getMessage());
                        }
                    } else if (Arrays.asList(new String[] { "2>" }).contains(arguments[i])) {
                        PrintStream newPS;
                        try {
                            newPS = new PrintStream(arguments[i + 1]);
                            System.setErr(newPS);
                            arguments = Arrays.copyOfRange(arguments, 0, i);
                        } catch (FileNotFoundException e) {
                            System.err.println("Error File Couldn't Be Created: " + e.getMessage());
                        }
                    }
                }
            }
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
