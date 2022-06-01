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

package org.apache.xmlbeans.impl.config;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Collection strategy which works on a subdirectory and determines the root source directory
 * for further lookups
 */
class ChildSolverCollectionStrategy implements CollectionStrategy {
    private static final Logger LOG = LogManager.getLogger(ChildSolverCollectionStrategy.class);

    private final ParserConfiguration config;
    private final PathMatcher javaMatcher = getPathMatcher("glob:**.java");
    private final PathMatcher jarMatcher = getPathMatcher("glob:**.jar");
    private final List<Path> roots = new ArrayList<>();
    private final CombinedTypeSolver combinedTypeSolver;

    ChildSolverCollectionStrategy(ParserConfiguration config, CombinedTypeSolver combinedTypeSolver) {
        this.config = config;
        this.combinedTypeSolver = combinedTypeSolver;
    }

    @Override
    public ParserConfiguration getParserConfiguration() {
        return config;
    }

    @Override
    public ProjectRoot collect(Path path) {
        try {
            Files.walkFileTree(path, new FileVisitor());
        } catch (IOException e) {
            LOG.atWarn().withThrowable(e).log("Unable to walk {}",path);
        }
        return roots.isEmpty() ? null : new ProjectRoot(roots.get(roots.size()-1), config);
    }

    public ProjectRoot collectAll() {
        Path root = null;
        for (Path p : roots) {
            if (root == null) {
                root = p;
            } else {
                root = commonRoot(root, p);
                if (root == null) {
                    break;
                }
            }
        }

        if (root == null) {
            throw new IllegalStateException("Unable to construct a common project root - giving up.");
        }

        ProjectRoot pr = new ProjectRoot(root, config);
        roots.forEach(pr::addSourceRoot);
        return pr;
    }

    private static Path commonRoot(Path path1, Path path2) {
        List<Path> l1 = new ArrayList<>();
        path1.toAbsolutePath().iterator().forEachRemaining(l1::add);
        List<Path> l2 = new ArrayList<>();
        path2.toAbsolutePath().iterator().forEachRemaining(l2::add);
        l1.retainAll(l2);
        return l1.isEmpty() ? null : l1.get(l1.size()-1);
    }

    private CombinedTypeSolver getSolver() {
        return combinedTypeSolver;
    }

    private class FileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (javaMatcher.matches(file)) {
                if (roots.stream().map(Path::toAbsolutePath).noneMatch(file.toAbsolutePath()::startsWith)) {
                    getRoot(file).ifPresent(r -> {
                        getSolver().add(new JavaParserTypeSolver(r, getParserConfiguration()));
                        roots.add(r);
                    });
                }
            } else if (jarMatcher.matches(file)) {
                getSolver().add(new JarTypeSolver(file));
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return Files.isHidden(dir) ? SKIP_SUBTREE : CONTINUE;
        }
    }
}
