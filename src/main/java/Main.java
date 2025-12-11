import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
            arguments = redirectOutputs(arguments);
            Command command = Commands.get(arguments[0]);
            if (command != null) {
                command.execute(arguments);
            } else {
                new Executable().execute(arguments);
            }
            System.setOut(console);
        } while (true);
    }

    private static String[] redirectOutputs(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            if (i < arguments.length - 1) {
                if (Arrays.asList(new String[] { ">", "1>" }).contains(arguments[i])) {
                    PrintStream newPS;
                    try {
                        newPS = new PrintStream(arguments[i + 1]);
                        System.setOut(newPS);
                        arguments = Arrays.copyOfRange(arguments, 0, i);
                    } catch (FileNotFoundException e) {
                    }
                } else if (Arrays.asList(new String[] { ">>", "1>>" }).contains(arguments[i])) {
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(new File(arguments[i + 1]), true);
                        PrintStream newPS = new PrintStream(fos);
                        System.setOut(newPS);
                        arguments = Arrays.copyOfRange(arguments, 0, i);
                    } catch (FileNotFoundException e) {
                        System.err.println("Output File Couldn't Be Created: " + e.getMessage());
                    }
                } else if ("2>".equals(arguments[i])) {
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
        return arguments;
    }
}
