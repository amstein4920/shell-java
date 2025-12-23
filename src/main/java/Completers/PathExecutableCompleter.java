package Completers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class PathExecutableCompleter implements Completer {

    private final List<Path> pathDirs;

    public PathExecutableCompleter() {
        String path = System.getenv("PATH");

        // Probably can't be empty or null, but it is possible there is a problem with
        // containerization
        if (path == null || path.isEmpty()) {
            this.pathDirs = List.of();
        } else {
            this.pathDirs = Arrays.stream(path.split(File.pathSeparator))
                    .map(Paths::get)
                    .filter(Files::isDirectory)
                    .toList();
        }
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String prefix = line.word();

        for (Path dir : pathDirs) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, prefix + "*")) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p) && Files.isExecutable(p)) {
                        candidates.add(new Candidate(p.getFileName().toString()));
                    }
                }
            } catch (IOException ignored) {

            }
        }
    }
}
