import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int exitCode = 0;

        repl: do {
            System.out.print("$ ");

            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String command = parts[0];
            String[] arguments = Arrays.copyOfRange(parts, 1, parts.length);


            switch (command.toUpperCase()) {
                case "EXIT":
                    exitCode = Integer.parseInt(arguments[0].trim());
                    break repl;
                case "ECHO":
                    System.out.println(String.join(" ", arguments));
                    break;
                default:
                    System.out.println(command + ": command not found");
            }
        } while (true);
        scanner.close();
        System.exit(exitCode);
    }
}
