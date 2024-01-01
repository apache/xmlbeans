package org.apache.xmlbeans.impl.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

    private FileUtil() {}

    /**
     * @param base Directory where to look for files
     * @param pattern Regex pattern the files must match to be included
     * @param searchSubdirectories If true, search all subdirectories. Otherwise, only search base
     * @return Collection of files in base and possibly subdirectories that match pattern
     */
    public static Collection<File> find(File base, Pattern pattern, boolean searchSubdirectories) {
        try (Stream<Path> pathStream = Files.find(base.toPath(), searchSubdirectories ? Integer.MAX_VALUE : 1, (path, atts) -> {
            String name = path.getFileName().toString();
            return !name.endsWith(".xsdconfig") && pattern.matcher(name).matches();
        })) {
            return pathStream.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
