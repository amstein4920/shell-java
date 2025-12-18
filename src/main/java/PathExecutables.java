import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PathExecutables {
    public static Set<String> getExecutablesOnPath() {
        String path = System.getenv("PATH");

        // Probably not ever going to be null, but could maybe have some
        // containerization issues
        if (path == null) {
            return Collections.emptySet();
        }

        Set<String> executables = new HashSet<>();
        String[] dirs = path.split(File.pathSeparator);

        for (String dir : dirs) {
            File directoryFile = new File(dir);

            if (!directoryFile.isDirectory())
                continue;

            File[] files = directoryFile.listFiles();
            if (files == null)
                continue;

            for (File file : files) {
                if (file.isFile() && file.canExecute()) {
                    executables.add(file.getName());
                }
            }
        }

        return executables;
    }
}
