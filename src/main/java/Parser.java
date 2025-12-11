import java.util.ArrayList;
import java.util.List;

public class Parser {

    public String[] parse(String inputString) {
        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;

        for (char c : inputString.toCharArray()) {
            switch (c) {
                case '\\':
                    if (escaped || singleQuoted) {
                        builder.append(c);
                        escaped = false;
                    } else {
                        escaped = true;
                    }
                    break;
                case ' ':
                    if (escaped && doubleQuoted) {
                        builder.append('\\');
                    }
                    if (singleQuoted || doubleQuoted || escaped) {
                        builder.append(c);
                    } else if (builder.length() > 0) {
                        result.add(builder.toString());
                        builder.setLength(0);
                    }
                    escaped = false;
                    break;
                case '\'':
                    if (escaped && doubleQuoted) {
                        builder.append('\\');
                    }
                    if (doubleQuoted || escaped) {
                        builder.append(c);
                    } else {
                        singleQuoted = !singleQuoted;
                    }
                    escaped = false;
                    break;
                case '"':
                    if (singleQuoted || escaped) {
                        builder.append(c);
                    } else {
                        doubleQuoted = !doubleQuoted;
                    }
                    escaped = false;
                    break;
                default:
                    if (doubleQuoted && escaped) {
                        builder.append('\\');
                    }
                    builder.append(c);
                    escaped = false;
            }
        }

        if (builder.length() > 0) {
            result.add(builder.toString());
        }

        return result.toArray(new String[0]);
    }
}
