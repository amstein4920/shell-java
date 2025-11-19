import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static File currentDir = new File(System.getProperty("user.dir"));

    public static void main(String[] args) throws Exception {

        // This scanner is tied to System.in and used for the entire lifecycle of the
        // application.
        // From this, I don't believe there is ever reason to close it.
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        // int exitCode = 0;

        String[] pathDirs = System.getenv("PATH").split(File.pathSeparator);

        do {
            System.out.print("$ ");

            String input = scanner.nextLine();
            String[] parts = parse(input);

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
            case "PWD":
                System.out.println(currentDir);
                return;
            case "CD":
                if ("~".equals(arguments[0])) {
                    currentDir = new File(System.getenv("HOME"));
                    return;
                }
                File newDir = new File(arguments[0]);
                if (!newDir.isAbsolute()) {
                    newDir = new File(currentDir, arguments[0]);
                }
                try {
                    newDir = newDir.getCanonicalFile();
                } catch (IOException e) {
                    System.err.println("Unable to parse file path: " + e.getMessage());
                }
                if (!newDir.isDirectory()) {
                    System.out.println("cd: " + arguments[0] + ": No such file or directory");
                    return;
                }

                currentDir = newDir;
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

    private static String[] parse(String inputString) {
        Pattern pattern = Pattern.compile("'([^']*)'|([^\\s]+)");
        Matcher matcher = pattern.matcher(inputString);

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int end = -1;

        while (matcher.find()) {
            String quoted = matcher.group(1);
            String unquoted = matcher.group(2);

            boolean adjacentToEnd = (matcher.start() == end);
            String subString = (quoted != null ? quoted : unquoted);

            if (unquoted != null) {
                subString = subString.replace("''", "");
            }

            if (subString.isEmpty()) {
                // Do nothing
            } else if (adjacentToEnd) {
                current.append(subString);
            } else {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                }
                current.setLength(0);
                current.append(subString);
            }

            end = matcher.end();
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
