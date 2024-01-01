/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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
        HashSet<String> expectedSet = new HashSet<>();
        expectedSet.add("test1.xsd");
        expectedSet.add("test2.xsd");
        assertEquals(expectedSet, files.stream().map(File::getName).collect(Collectors.toSet()));
    }
}
