import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public enum RedirectOperation {
    OUT(">", false, true),
    OUT1("1>", false, true),
    OUT_APPEND(">>", true, true),
    OUT1_APPEND("1>>", true, true),
    ERR("2>", false, false),
    ERR_APPEND("2>>", true, false);

    private final String token;
    private final boolean append;
    private final boolean stdout;

    RedirectOperation(String token, boolean append, boolean stdout) {
        this.token = token;
        this.append = append;
        this.stdout = stdout;
    }

    public void apply(String fileName) throws FileNotFoundException {
        PrintStream ps = append
                ? new PrintStream(new FileOutputStream(fileName, true))
                : new PrintStream(fileName);
        if (stdout) {
            System.setOut(ps);
        } else {
            System.setErr(ps);
        }
    }

    public static RedirectOperation fromToken(String token) {
        for (RedirectOperation operation : values()) {
            if (operation.token.equals(token))
                return operation;
        }
        return null;
    }

    public static String[] redirectOutputs(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            if (i < arguments.length - 1) {
                RedirectOperation operation = RedirectOperation.fromToken(arguments[i]);
                if (operation != null) {
                    try {
                        operation.apply(arguments[i + 1]);
                    } catch (FileNotFoundException e) {
                        System.err.println("Redirect failed: " + e.getMessage());
                    }

                    return Arrays.copyOfRange(arguments, 0, i);
                }
            }
        }
        return arguments;
    }
}