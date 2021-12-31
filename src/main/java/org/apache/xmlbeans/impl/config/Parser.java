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

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.javaparser.ParserConfiguration.LanguageLevel.BLEEDING_EDGE;

class Parser {
    private final File[] javaFiles;
    private final File[] classpath;
    private final ParserConfiguration pc;
    private final ProjectRoot projectRoot;
    private final CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();

    public Parser(File[] javaFiles, File[] classpath) {
        this.javaFiles = (javaFiles != null) ? javaFiles.clone() : new File[0];
        this.classpath = (classpath != null) ? classpath.clone() : new File[0];

        pc = new ParserConfiguration();
        pc.setLanguageLevel(BLEEDING_EDGE);

        URL[] urls = Stream.of(this.classpath).map(Parser::fileToURL).filter(Objects::nonNull).toArray(URL[]::new);
        combinedTypeSolver.add(new ClassLoaderTypeSolver(new URLClassLoader(urls, getClass().getClassLoader())));
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        pc.setSymbolResolver(symbolSolver);

        if (this.javaFiles.length > 0) {
            ChildSolverCollectionStrategy solver = new ChildSolverCollectionStrategy(pc, combinedTypeSolver);

            Stream.of(this.javaFiles)
                .map(f -> f.isDirectory() ? f : f.getParentFile())
                .map(File::toPath)
                .distinct()
                .forEach(solver::collect);

            projectRoot = solver.collectAll();
        } else {
            projectRoot = null;
        }
    }

    public ClassOrInterfaceDeclaration loadSource(String className) {
        final String fileName = className.replace('.','/') +".java";
        if (projectRoot == null) {
            // TODO: check if this is called, when no sources are specified
//            ParseResult<CompilationUnit> blub = new JavaParser(pc).parse(fileName);
//            boolean suc = blub.isSuccessful();
            return null;
        } else {
            return projectRoot.getSourceRoots().stream().map(sr -> parseOrNull(sr, fileName))
                .filter(Objects::nonNull)
                .filter(ParseResult::isSuccessful)
                .map(ParseResult::getResult)
                .map(Optional::get)
                .flatMap(cu -> cu.getTypes().stream())
                .filter(ClassOrInterfaceDeclaration.class::isInstance)
                .filter(t -> className.equals(t.getFullyQualifiedName().orElse(null)))
                .map(ClassOrInterfaceDeclaration.class::cast)
                .findFirst().orElse(null);
        }
    }

    private static URL fileToURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ignored) {
            return null;
        }
    }

    private ParseResult<CompilationUnit> parseOrNull(SourceRoot sr, String fileName) {
        try {
            return sr.tryToParse("", fileName, pc);
        } catch (IOException ignroed) {
            return null;
        }
    }
}
