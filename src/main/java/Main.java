import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        // This scanner is tied to System.in and used for the entire lifecycle of the application.
        // From this, I don't believe there is ever reason to close it.
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        // int exitCode = 0;

        String[] pathDirs = System.getenv("PATH").split(File.pathSeparator);

        do {
            System.out.print("$ ");

            String input = scanner.nextLine();
            String[] parts = input.split(" ");

            processCommand(parts, pathDirs);
        } while (true);
    }

    private static void processCommand(String[] inputParts, String[] pathDirs) {
        String command = inputParts[0];
        String[] arguments = Arrays.copyOfRange(inputParts, 1, inputParts.length);

        switch (command.toUpperCase()) {
            case "EXIT":
                System.exit(Integer.parseInt(arguments[0].trim()));
            case "ECHO":
                System.out.println(String.join(" ", arguments));
                return;
            case "TYPE":
                boolean isBuiltin = Builtin.isBuiltin(arguments[0].trim());
                if (isBuiltin) {
                    System.out.println(arguments[0] + " is a shell builtin");
                    return;
                }
                for (String dir : pathDirs) {
                    File file = new File(dir, arguments[0]);
                    if (file.exists() && file.canExecute()) {
                        System.out.println(arguments[0] + " is " + file.getAbsolutePath());
                        return;
                    }
                }

                System.out.println(arguments[0] + ": not found");
                return;
            default:
                for (String dir : pathDirs) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        ProcessBuilder processBuilder = new ProcessBuilder(inputParts);
                        try {
                            Process process = processBuilder.start();

                            captureProcessOutput(process);

                            process.waitFor(); // Returns process code, but don't have use for right this moment

                        } catch (IOException | InterruptedException e) {
                            System.err.println("Error in execution of process: " + e.getMessage());
                        }

                        return;
                    }
                }

                System.out.println(command + ": command not found");
        }
    }

    private static void captureProcessOutput(Process process) {
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error in process output: " + e.getMessage());
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error in process error: " + e.getMessage());
            }
        });

        outputThread.start();
        errorThread.start();

        try {
            outputThread.join();
            errorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted.");
        }
    }
}
