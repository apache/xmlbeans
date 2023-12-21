package org.apache.xmlbeans.impl.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileUtilTest {

    private Path root;

    @BeforeEach
    public void setup() throws IOException {
        root = Files.createTempDirectory("test");
    }

    @AfterEach
    public void cleanup() throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void testFindNoSubdirs() throws IOException {
        Files.createFile(root.resolve("test1.xsdconfig"));
        Files.createFile(root.resolve("test1.xsd"));
        Files.createFile(root.resolve("test1.txt"));
        Path dir = Files.createDirectory(root.resolve("test"));
        Files.createFile(dir.resolve("test2.xsdconfig"));
        Files.createFile(dir.resolve("test2.xsd"));
        Files.createFile(dir.resolve("test2.txt"));

        Collection<File> files = FileUtil.find(root.toFile(), Pattern.compile(".*\\.xsd"), false);

        assertEquals(1, files.size());
        assertEquals("test1.xsd", files.iterator().next().getName());
    }

    @Test
    public void testFindSubdirs() throws IOException {
        Files.createFile(root.resolve("test1.xsdconfig"));
        Files.createFile(root.resolve("test1.xsd"));
        Files.createFile(root.resolve("test1.txt"));
        Path dir = Files.createDirectory(root.resolve("test"));
        Files.createFile(dir.resolve("test2.xsdconfig"));
        Files.createFile(dir.resolve("test2.xsd"));
        Files.createFile(dir.resolve("test2.txt"));

        Collection<File> files = FileUtil.find(root.toFile(), Pattern.compile(".*\\.xsd"), true);

        assertEquals(2, files.size());
        assertEquals(Set.of("test1.xsd", "test2.xsd"), files.stream().map(File::getName).collect(Collectors.toSet()));
    }
}