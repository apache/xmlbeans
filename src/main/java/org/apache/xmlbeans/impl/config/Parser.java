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
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

class Parser {
    final File[] javaFiles;
    final File[] classpath;


    public Parser(File[] javaFiles, File[] classpath) {
        this.javaFiles = (javaFiles != null) ? javaFiles.clone() : new File[0];
        this.classpath = (classpath != null) ? classpath.clone() : new File[0];
    }

    public ClassOrInterfaceDeclaration loadSource(String className) {
        final String fileName = className.replace('.','/') +".java";
        for (File f : javaFiles) {
            final String filePath = f.getPath();
            if (filePath.replace('\\','/').endsWith(fileName)) {
                // remove filename from path - don't use replace because of different path separator
                final String rootPath = filePath.substring(0, filePath.length()-fileName.length());
                final String startPackage = className.indexOf('.') == -1 ? "" : className.substring(0, className.lastIndexOf('.'));
                final String simpleName = startPackage.isEmpty() ? className : className.substring(startPackage.length()+1);
                SourceRoot sourceRoot = new SourceRoot(new File(rootPath).toPath());
                try {
                    ParseResult<CompilationUnit> pcu = sourceRoot.tryToParse(startPackage, simpleName+".java");
                    ClassOrInterfaceDeclaration cls = pcu.getResult().flatMap(cu -> cu.getTypes().stream()
                        .filter(matchType(className))
                        .map(t -> (ClassOrInterfaceDeclaration) t).findFirst()).orElse(null);
                    return cls;
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static Predicate<TypeDeclaration<?>> matchType(String className) {
        return (t) -> t instanceof  ClassOrInterfaceDeclaration &&
                      t.getFullyQualifiedName().map(fqn -> fqn.equals(className)).orElse(false);
    }
}
