import java.io.File;
import java.io.IOException;
import java.util.Arrays;

interface Command {
    void execute(String[] arguments);
}

public class Commands {

    static File currentDir = new File(System.getProperty("user.dir"));

    public static Command get(String name) {
        return switch (name) {
            case "exit" -> new Exit();
            case "echo" -> new Echo();
            case "pwd" -> new Pwd();
            case "cd" -> new Cd();
            case "type" -> new Type();
            default -> null;
        };
    }

    public static String getPath(String argument) {
        String[] pathDirs = System.getenv("PATH").split(File.pathSeparator);

        for (String dir : pathDirs) {
            File file = new File(dir, argument);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}

class Exit implements Command {
    public void execute(String[] arguments) {
        if (arguments.length > 1) {
            System.exit(Integer.parseInt(arguments[1].trim()));
        }
        System.exit(0);
    }
}

class Echo implements Command {
    public void execute(String[] arguments) {
        System.out.println(String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length)));
    }
}

class Pwd implements Command {
    public void execute(String[] arguments) {
        System.out.println(Commands.currentDir);
    }
}

class Cd implements Command {
    public void execute(String[] arguments) {
        if ("~".equals(arguments[1])) {
            Commands.currentDir = new File(System.getenv("HOME"));
            return;
        }
        File newDir = new File(arguments[1]);
        if (!newDir.isAbsolute()) {
            newDir = new File(Commands.currentDir, arguments[1]);
        }
        try {
            newDir = newDir.getCanonicalFile();
        } catch (IOException e) {
            System.err.println("Unable to parse file path: " + e.getMessage());
        }
        if (!newDir.isDirectory()) {
            System.out.println("cd: " + arguments[1] + ": No such file or directory");
            return;
        }

        Commands.currentDir = newDir;
    }
}

class Type implements Command {
    public void execute(String[] arguments) {
        Command command = Commands.get(arguments[1]);
        if (command != null) {
            System.out.println(arguments[1] + " is a shell builtin");
        } else {
            String path = Commands.getPath(arguments[1]);
            if (path != null) {
                System.out.println(arguments[1] + " is " + path);
            } else {
                System.out.println(arguments[1] + ": not found");
            }
        }
    }
}

class Executable implements Command {
    public void execute(String[] arguments) {
        if (Commands.getPath(arguments[0]) != null) {
            try {
                Process process = Runtime.getRuntime().exec(arguments);
                process.getInputStream().transferTo(System.out);
                process.getErrorStream().transferTo(System.err);
            } catch (Exception e) {
                System.err.println("Error in execution of process: " + e.getMessage());
            }
        } else {
            System.out.println(arguments[0] + ": command not found");
        }
    }
}