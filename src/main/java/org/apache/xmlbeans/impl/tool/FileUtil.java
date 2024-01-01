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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FileUtil {

    private FileUtil() {}

    /**
     * @param base Directory where to look for files
     * @param pattern Regex pattern the files must match to be included
     * @param searchSubdirectories If true, search all subdirectories. Otherwise, only search base
     * @return Collection of files in base and possibly subdirectories that match pattern
     */
    static Collection<File> find(File base, Pattern pattern, boolean searchSubdirectories) {
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
