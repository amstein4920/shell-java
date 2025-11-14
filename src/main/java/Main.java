import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        repl: do {
        System.out.print("$ ");

        String command = scanner.nextLine();

        switch (command.toUpperCase()) {
            case "EXIT":
                break repl;
            default:
                System.out.println(command + ": command not found");
        }
    } while (true);

        scanner.close();
    }
}
