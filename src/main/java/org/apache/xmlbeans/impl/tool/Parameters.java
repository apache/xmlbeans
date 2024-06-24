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

import org.apache.xmlbeans.SchemaCodePrinter;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Parameters {
    private File baseDir;
    private File[] xsdFiles;
    private File[] wsdlFiles;
    private File[] javaFiles;
    private File[] configFiles;
    private URL[] urlFiles;
    private File[] classpath;
    private File outputJar;
    private String name;
    private File srcDir;
    private File classesDir;
    private String memoryInitialSize;
    private String memoryMaximumSize;
    private String compiler;
    private boolean nojavac;
    private boolean quiet;
    private boolean verbose;
    private boolean download;
    private Collection<XmlError> errorListener;
    private boolean noUpa;
    private boolean noPvr;
    private boolean noAnn;
    private boolean noVDoc;
    private boolean noExt;
    private boolean debug;
    private boolean copyAnn;
    private String sourceCodeEncoding;
    private boolean incrementalSrcGen;
    private String repackage;
    private List<Extension> extensions = Collections.emptyList();
    private Set<String> mdefNamespaces = Collections.emptySet();
    private String catalogFile;
    private SchemaCodePrinter schemaCodePrinter;
    private EntityResolver entityResolver;
    private Set<XmlOptions.BeanMethod> partialMethods = Collections.emptySet();

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File[] getXsdFiles() {
        return xsdFiles;
    }

    public void setXsdFiles(File... xsdFiles) {
        this.xsdFiles = xsdFiles == null ? null : xsdFiles.clone();
    }

    public File[] getWsdlFiles() {
        return wsdlFiles;
    }

    public void setWsdlFiles(File... wsdlFiles) {
        this.wsdlFiles = wsdlFiles == null ? null : wsdlFiles.clone();
    }

    public File[] getJavaFiles() {
        return javaFiles;
    }

    public void setJavaFiles(File... javaFiles) {
        this.javaFiles = javaFiles == null ? null : javaFiles.clone();
    }

    public File[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(File... configFiles) {
        this.configFiles = configFiles == null ? null : configFiles.clone();
    }

    public URL[] getUrlFiles() {
        return urlFiles;
    }

    public void setUrlFiles(URL... urlFiles) {
        this.urlFiles = urlFiles == null ? null : urlFiles.clone();
    }

    public File[] getClasspath() {
        return classpath;
    }

    public void setClasspath(File... classpath) {
        this.classpath = classpath == null ? null : classpath.clone();
    }

    public File getOutputJar() {
        return outputJar;
    }

    public void setOutputJar(File outputJar) {
        this.outputJar = outputJar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classesDir) {
        this.classesDir = classesDir;
    }

    public boolean isNojavac() {
        return nojavac;
    }

    public void setNojavac(boolean nojavac) {
        this.nojavac = nojavac;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public boolean isNoUpa() {
        return noUpa;
    }

    public void setNoUpa(boolean noUpa) {
        this.noUpa = noUpa;
    }

    public boolean isNoPvr() {
        return noPvr;
    }

    public void setNoPvr(boolean noPvr) {
        this.noPvr = noPvr;
    }

    public boolean isNoAnn() {
        return noAnn;
    }

    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    public void setNoAnn(boolean noAnn) {
        this.noAnn = noAnn;
    }

    public boolean isNoVDoc() {
        return noVDoc;
    }

    public void setNoVDoc(boolean newNoVDoc) {
        this.noVDoc = newNoVDoc;
    }

    public boolean isNoExt() {
        return noExt;
    }

    public void setNoExt(boolean newNoExt) {
        this.noExt = newNoExt;
    }

    public boolean isIncrementalSrcGen() {
        return incrementalSrcGen;
    }

    public void setIncrementalSrcGen(boolean incrSrcGen) {
        this.incrementalSrcGen = incrSrcGen;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setSourceCodeEncoding(String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    public String getMemoryInitialSize() {
        return memoryInitialSize;
    }

    public void setMemoryInitialSize(String memoryInitialSize) {
        this.memoryInitialSize = memoryInitialSize;
    }

    public String getMemoryMaximumSize() {
        return memoryMaximumSize;
    }

    public void setMemoryMaximumSize(String memoryMaximumSize) {
        this.memoryMaximumSize = memoryMaximumSize;
    }

    public String getCompiler() {
        return compiler;
    }

    public void setCompiler(String compiler) {
        this.compiler = compiler;
    }

    public Collection<XmlError> getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(Collection<XmlError> errorListener) {
        this.errorListener = errorListener;
    }

    public String getRepackage() {
        return repackage;
    }

    public void setRepackage(String newRepackage) {
        repackage = newRepackage;
    }

    public boolean isCopyAnn() {
        return copyAnn;
    }

    public void setCopyAnn(boolean newCopyAnn) {
        copyAnn = newCopyAnn;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
    }

    public Set<String> getMdefNamespaces() {
        return mdefNamespaces;
    }

    public void setMdefNamespaces(Set<String> mdefNamespaces) {
        this.mdefNamespaces = mdefNamespaces;
    }

    public String getCatalogFile() {
        return catalogFile;
    }

    public void setCatalogFile(String catalogPropFile) {
        this.catalogFile = catalogPropFile;
    }

    public SchemaCodePrinter getSchemaCodePrinter() {
        return schemaCodePrinter;
    }

    public void setSchemaCodePrinter(SchemaCodePrinter schemaCodePrinter) {
        this.schemaCodePrinter = schemaCodePrinter;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public Set<XmlOptions.BeanMethod> getPartialMethods() {
        return partialMethods;
    }

    public void setPartialMethods(Set<XmlOptions.BeanMethod> partialMethods) {
        this.partialMethods = partialMethods;
    }
}
