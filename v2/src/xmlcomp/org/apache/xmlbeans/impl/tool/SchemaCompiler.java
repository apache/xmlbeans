/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.apache.xmlbeans.impl.schema.ResourceLoader;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.common.XmlErrorPrinter;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.common.XmlErrorContext;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.values.XmlListImpl;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SimpleValue;
import com.bea.x2002.x09.xbean.config.ConfigDocument;

import java.io.File;
import java.util.*;
import java.net.URI;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.xml.sax.EntityResolver;

public class SchemaCompiler
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Compiles a schema into XML Bean classes and metadata.");
            System.out.println("Usage: scomp [opts] [dirs]* [schema.xsd]* [service.wsdl]* [config.xsdconfig]*");
            System.out.println("Options include:");
            System.out.println("    -cp [a;b;c] - classpath");
            System.out.println("    -d [dir] - target binary directory for .class and .xsb files");
            System.out.println("    -src [dir] - target directory for generated .java files");
            System.out.println("    -srconly - do not compile .java files or jar the output.");
            System.out.println("    -out [result.jar] - the name of the output jar");
            System.out.println("    -dl - permit network downloads for imports and includes (default is off)");
            System.out.println("    -noupa - do not enforce the unique particle attribution rule");
            System.out.println("    -nopvr - do not enforce the particle valid (restriction) rule");
            System.out.println("    -noann - ignore annotations");
            System.out.println("    -compiler - path to external java compiler");
            System.out.println("    -jar - path to jar utility");
            System.out.println("    -ms - initial memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_START + "')");
            System.out.println("    -mx - maximum memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_MAX + "')");
            System.out.println("    -debug - compile with debug symbols");
            System.out.println("    -quiet - print fewer informational messages");
            System.out.println("    -verbose - print more informational messages");
            System.out.println("    -license - prints license information");
            System.out.println("    -allowmdef \"[ns] [ns] [ns]\" - ignores multiple defs in given namespaces");
            System.out.println("    -catalog [file] -  catalog file for org.apache.xml.resolver.tools.CatalogResolver. (Note: needs resolver.jar from http://xml.apache.org/commons/components/resolver/index.html)");
            /* Undocumented feature - pass in one schema compiler extension and related parameters
            System.out.println("    -repackage - repackage specification");
            System.out.println("    -extension - registers a schema compiler extension");
            System.out.println("    -extensionParms - specify parameters for the compiler extension");
            */
            System.out.println();
            System.out.println("If you require a different java compiler, use the XMLBean Ant task instead.");
            System.exit(0);
            return;
        }

        Set opts = new HashSet();
        opts.add("out");
        opts.add("name");
        opts.add("src");
        opts.add("d");
        opts.add("cp");
        opts.add("compiler");
        opts.add("jar");
        opts.add("ms");
        opts.add("mx");
        opts.add("repackage");
        opts.add("extension");
        opts.add("extensionParms");
        opts.add("allowmdef");
        opts.add("catalog");
        CommandLine cl = new CommandLine(args, opts);

        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        args = cl.args();
        boolean verbose = (cl.getOpt("verbose") != null);
        boolean quiet = (cl.getOpt("quiet") != null);
        if (verbose)
            quiet = false;

        String outputfilename = cl.getOpt("out");

        String repackage = cl.getOpt("repackage");

        String name = cl.getOpt("name");

        boolean download = (cl.getOpt("dl") != null);
        boolean noUpa = (cl.getOpt("noupa") != null);
        boolean noPvr = (cl.getOpt("nopvr") != null);
        boolean noAnn = (cl.getOpt("noann") != null);
        boolean nojavac = (cl.getOpt("srconly") != null);
        boolean debug = (cl.getOpt("debug") != null);

        String allowmdef = cl.getOpt("allowmdef");
        Set mdefNamespaces = (allowmdef == null ? Collections.EMPTY_SET :
                new HashSet(Arrays.asList(XmlListImpl.split_list(allowmdef))));

        List extensions = new ArrayList();
        if (cl.getOpt("extension") != null) {
            try {
                Extension e = new Extension();
                e.setClassName(Class.forName(cl.getOpt("extension"), false, Thread.currentThread().getContextClassLoader()));
                extensions.add(e);
            } catch (ClassNotFoundException e) {
                System.err.println("Could not find extension class: " + cl.getOpt("extension") + "  Is it on your classpath?");
                System.exit(1);
            }
        }

        if (extensions.size() > 0)
        {
            // example: -extensionParms typeMappingFileLocation=d:\types
            if (cl.getOpt("extensionParms") != null) {
                Extension e = (Extension) extensions.get(0);
                // extensionParms are delimited by ';'
                StringTokenizer parmTokens = new StringTokenizer(cl.getOpt("extensionParms"), ";");
                while (parmTokens.hasMoreTokens()) {
                    // get name value pair for each extension parms and stick into extension parms
                    String nvPair = parmTokens.nextToken();
                    int index = nvPair.indexOf('=');
                    if (index < 0)
                    {
                        System.err.println("extensionParms should be name=value;name=value");
                        System.exit(1);
                    }
                    String n = nvPair.substring(0, index);
                    String v = nvPair.substring(index + 1);
                    Extension.Param param = e.createParam();
                    param.setName(n);
                    param.setValue(v);
                }
            }
        }

        String classesdir = cl.getOpt("d");
        File classes = null;
        if (classesdir != null)
            classes = new File(classesdir);

        String srcdir = cl.getOpt("src");
        File src = null;
        if (srcdir != null)
            src = new File(srcdir);
        if (nojavac && srcdir == null && classes != null)
            src = classes;

        // create temp directory
        File tempdir = null;
        if (src == null || classes == null)
        {
            try
            {
                tempdir = SchemaCodeGenerator.createTempDir();
            }
            catch (java.io.IOException e)
            {
                System.err.println("Error creating temp dir " + e);
                System.exit(1);
            }
        }

        File jarfile = null;
        if (outputfilename == null && classes == null && !nojavac)
            outputfilename = "xmltypes.jar";
        if (outputfilename != null)
            jarfile = new File(outputfilename);

        if (src == null)
            src = IOUtil.createDir(tempdir, "src");
        if (classes == null)
            classes = IOUtil.createDir(tempdir, "classes");

        File[] classpath = null;
        String cpString = cl.getOpt("cp");
        if (cpString != null)
        {
            String[] cpparts = cpString.split(File.pathSeparator);
            List cpList = new ArrayList();
            for (int i = 0; i < cpparts.length; i++)
                cpList.add(new File(cpparts[i]));
            classpath = (File[])cpList.toArray(new File[cpList.size()]);
        }
        else
        {
            classpath = CodeGenUtil.systemClasspath();
        }

        String compiler = cl.getOpt("compiler");
        String jar = cl.getOpt("jar");

        String memoryInitialSize = cl.getOpt("ms");
        String memoryMaximumSize = cl.getOpt("mx");

        File[] xsdFiles = cl.filesEndingWith(".xsd");
        File[] wsdlFiles = cl.filesEndingWith(".wsdl");
        File[] javaFiles = cl.filesEndingWith(".java");
        File[] configFiles = cl.filesEndingWith(".xsdconfig");

        if (xsdFiles.length + wsdlFiles.length == 0)
        {
            System.err.println("Could not find any xsd or wsdl files to process.");
            System.exit(1);
        }
        File baseDir = cl.getBaseDir();
        URI baseURI = baseDir == null ? null : baseDir.toURI();

        XmlErrorPrinter err = new XmlErrorPrinter(verbose, baseURI);

        String catString = cl.getOpt("catalog");

        Parameters params = new Parameters();
        params.setBaseDir(baseDir);
        params.setXsdFiles(xsdFiles);
        params.setWsdlFiles(wsdlFiles);
        params.setJavaFiles(javaFiles);
        params.setConfigFiles(configFiles);
        params.setClasspath(classpath);
        params.setOutputJar(jarfile);
        params.setName(name);
        params.setSrcDir(src);
        params.setClassesDir(classes);
        params.setCompiler(compiler);
        params.setJar(jar);
        params.setMemoryInitialSize(memoryInitialSize);
        params.setMemoryMaximumSize(memoryMaximumSize);
        params.setNojavac(nojavac);
        params.setQuiet(quiet);
        params.setVerbose(verbose);
        params.setDownload(download);
        params.setNoUpa(noUpa);
        params.setNoPvr(noPvr);
        params.setNoAnn(noAnn);
        params.setDebug(debug);
        params.setErrorListener(err);
        params.setRepackage(repackage);
        params.setExtensions(extensions);
        params.setMdefNamespaces(mdefNamespaces);
        params.setCatalogFile(catString);

        boolean result = compile(params);

        if (tempdir != null)
            SchemaCodeGenerator.tryHardToDelete(tempdir);

        if (!result)
            System.exit(1);

        System.exit(0);
    }

    public static class Parameters
    {
        private File baseDir;
        private File[] xsdFiles;
        private File[] wsdlFiles;
        private File[] javaFiles;
        private File[] configFiles;
        private File[] classpath;
        private File outputJar;
        private String name;
        private File srcDir;
        private File classesDir;
        private String memoryInitialSize;
        private String memoryMaximumSize;
        private String compiler;
        private String jar;
        private boolean nojavac;
        private boolean quiet;
        private boolean verbose;
        private boolean download;
        private Collection errorListener;
        private boolean noUpa;
        private boolean noPvr;
        private boolean noAnn;
        private boolean debug;
        private String repackage;
        private List extensions = Collections.EMPTY_LIST;
        private Set mdefNamespaces = Collections.EMPTY_SET;
        private String catalogFile;

        public File getBaseDir()
        {
            return baseDir;
        }

        public void setBaseDir(File baseDir)
        {
            this.baseDir = baseDir;
        }

        public File[] getXsdFiles()
        {
            return xsdFiles;
        }

        public void setXsdFiles(File[] xsdFiles)
        {
            this.xsdFiles = xsdFiles;
        }

        public File[] getWsdlFiles()
        {
            return wsdlFiles;
        }

        public void setWsdlFiles(File[] wsdlFiles)
        {
            this.wsdlFiles = wsdlFiles;
        }

        public File[] getJavaFiles()
        {
            return javaFiles;
        }

        public void setJavaFiles(File[] javaFiles)
        {
            this.javaFiles = javaFiles;
        }

        public File[] getConfigFiles()
        {
            return configFiles;
        }

        public void setConfigFiles(File[] configFiles)
        {
            this.configFiles = configFiles;
        }

        public File[] getClasspath()
        {
            return classpath;
        }

        public void setClasspath(File[] classpath)
        {
            this.classpath = classpath;
        }

        public File getOutputJar()
        {
            return outputJar;
        }

        public void setOutputJar(File outputJar)
        {
            this.outputJar = outputJar;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public File getSrcDir()
        {
            return srcDir;
        }

        public void setSrcDir(File srcDir)
        {
            this.srcDir = srcDir;
        }

        public File getClassesDir()
        {
            return classesDir;
        }

        public void setClassesDir(File classesDir)
        {
            this.classesDir = classesDir;
        }

        public boolean isNojavac()
        {
            return nojavac;
        }

        public void setNojavac(boolean nojavac)
        {
            this.nojavac = nojavac;
        }

        public boolean isQuiet()
        {
            return quiet;
        }

        public void setQuiet(boolean quiet)
        {
            this.quiet = quiet;
        }

        public boolean isVerbose()
        {
            return verbose;
        }

        public void setVerbose(boolean verbose)
        {
            this.verbose = verbose;
        }

        public boolean isDownload()
        {
            return download;
        }

        public void setDownload(boolean download)
        {
            this.download = download;
        }

        public boolean isNoUpa()
        {
            return noUpa;
        }

        public void setNoUpa(boolean noUpa)
        {
            this.noUpa = noUpa;
        }

        public boolean isNoPvr()
        {
            return noPvr;
        }

        public void setNoPvr(boolean noPvr)
        {
            this.noPvr = noPvr;
        }

        public boolean isNoAnn()
        {
            return noAnn;
        }

        public void setNoAnn(boolean noAnn)
        {
            this.noAnn = noAnn;
        }

        public boolean isDebug()
        {
            return debug;
        }

        public void setDebug(boolean debug)
        {
            this.debug = debug;
        }

        public String getMemoryInitialSize()
        {
            return memoryInitialSize;
        }

        public void setMemoryInitialSize(String memoryInitialSize)
        {
            this.memoryInitialSize = memoryInitialSize;
        }

        public String getMemoryMaximumSize()
        {
            return memoryMaximumSize;
        }

        public void setMemoryMaximumSize(String memoryMaximumSize)
        {
            this.memoryMaximumSize = memoryMaximumSize;
        }

        public String getCompiler()
        {
            return compiler;
        }

        public void setCompiler(String compiler)
        {
            this.compiler = compiler;
        }

        public String getJar()
        {
            return jar;
        }

        public void setJar(String jar)
        {
            this.jar = jar;
        }

        public Collection getErrorListener()
        {
            return errorListener;
        }

        public void setErrorListener(Collection errorListener)
        {
            this.errorListener = errorListener;
        }

        public String getRepackage()
        {
            return repackage;
        }

        public void setRepackage(String newRepackage)
        {
            repackage = newRepackage;
        }

        public List getExtensions() {
            return extensions;
        }

        public void setExtensions(List extensions) {
            this.extensions = extensions;
        }

        public Set getMdefNamespaces()
        {
            return mdefNamespaces;
        }

        public void setMdefNamespaces(Set mdefNamespaces)
        {
            this.mdefNamespaces = mdefNamespaces;
        }

        public String getCatalogFile()
        {
            return catalogFile;
        }

        public void setCatalogFile(String catalogPropFile)
        {
            this.catalogFile = catalogPropFile;
        }

    }

    private static SchemaTypeSystem loadTypeSystem(
        String name, File[] xsdFiles,
        File[] wsdlFiles, File[] configFiles, ResourceLoader cpResourceLoader,
        boolean download, boolean noUpa, boolean noPvr, boolean noAnn,
        Set mdefNamespaces, File baseDir, Map sourcesToCopyMap,
        Collection outerErrorListener, File schemasDir, EntityResolver entResolver)
    {
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);

        // For parsing XSD and WSDL files, we should use the SchemaDocument
        // classloader rather than the thread context classloader.  This is
        // because in some situations (such as when being invoked by ant
        // underneath the ide) the context classloader is potentially weird
        // (because of the design of ant).

        SchemaTypeLoader loader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());

        // step 1, parse all the XSD files.
        ArrayList scontentlist = new ArrayList();
        if (xsdFiles != null)
        {
            for (int i = 0; i < xsdFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadMessageDigest();
                    options.setEntityResolver(entResolver);

                    XmlObject schemadoc = loader.parse(xsdFiles[i], null, options);
                    if (!(schemadoc instanceof SchemaDocument))
                    {
                        StscState.addError(errorListener, "Document " + xsdFiles[i] + " is not a schema file", XmlErrorContext.CANNOT_LOAD_XSD_FILE, schemadoc);
                    }
                    else
                    {
                        StscState.addInfo(errorListener, "Loading schema file " + xsdFiles[i]);
                        XmlOptions opts = new XmlOptions().setErrorListener(errorListener);
                        if (schemadoc.validate(opts))
                            scontentlist.add(((SchemaDocument)schemadoc).getSchema());
                    }
                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, "Cannot load file " + xsdFiles[i] + ": " + e, XmlErrorContext.CANNOT_LOAD_XSD_FILE, xsdFiles[i]);
                }
            }
        }

        // step 2, parse all WSDL files
        if (wsdlFiles != null)
        {
            for (int i = 0; i < wsdlFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadSubstituteNamespaces(Collections.singletonMap(
                            "http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"
                    ));
                    options.setEntityResolver(entResolver);

                    XmlObject wsdldoc = loader.parse(wsdlFiles[i], null, options);

                    if (!(wsdldoc instanceof org.apache.internal.xmlbeans.wsdlsubst.DefinitionsDocument))
                        StscState.addError(errorListener, "Document " + wsdlFiles[i] + " is not a wsdl file", XmlErrorContext.CANNOT_LOAD_XSD_FILE, wsdldoc);
                    else
                    {
                        if (wsdlContainsEncoded(wsdldoc))
                            StscState.addWarning(errorListener, "The WSDL " + wsdlFiles[i] + " uses SOAP encoding. SOAP encoding is not compatible with literal XML Schema.", XmlErrorContext.CANNOT_LOAD_XSD_FILE, wsdldoc);
                        StscState.addInfo(errorListener, "Loading wsdl file " + wsdlFiles[i]);
                        XmlObject[] types = ((org.apache.internal.xmlbeans.wsdlsubst.DefinitionsDocument)wsdldoc).getDefinitions().getTypesArray();
                        int count = 0;
                        for (int j = 0; j < types.length; j++)
                        {
                            // explicit cast for paranoia
                            SchemaDocument.Schema[] schemas = (SchemaDocument.Schema[])types[j].selectPath("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\" xs:schema");
                            for (int k = 0; k < schemas.length; k++)
                            {
                                if (schemas[k].validate(new XmlOptions().setErrorListener(errorListener)))
                                    scontentlist.add(schemas[k]);
                            }
                            count += schemas.length;
                        }
                        StscState.addInfo(errorListener, "Processing " + count + " schema(s) in " + wsdlFiles[i].toString());
                    }
                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, "Cannot load file " + wsdlFiles[i] + ": " + e, XmlErrorContext.CANNOT_LOAD_XSD_FILE, wsdlFiles[i]);
                }
            }
        }

        SchemaDocument.Schema[] sdocs = (SchemaDocument.Schema[])scontentlist.toArray(new SchemaDocument.Schema[scontentlist.size()]);

        // now the config files.
        ArrayList cdoclist = new ArrayList();
        if (configFiles != null)
        {
            for (int i = 0; i < configFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.put( XmlOptions.LOAD_LINE_NUMBERS );
                    options.setEntityResolver(entResolver);

                    XmlObject configdoc = loader.parse(configFiles[i], null, options);
                    if (!(configdoc instanceof ConfigDocument))
                        StscState.addError(errorListener, "Document " + configFiles[i] + " is not an xsd config file", XmlErrorContext.CANNOT_LOAD_XSD_FILE, configdoc);
                    else
                    {
                        StscState.addInfo(errorListener, "Loading config file " + configFiles[i]);
                        if (configdoc.validate(new XmlOptions().setErrorListener(errorListener)))
                            cdoclist.add(((ConfigDocument)configdoc).getConfig());
                    }
                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, "Cannot load xsd config file " + configFiles[i] + ": " + e, XmlErrorContext.CANNOT_LOAD_XSD_CONFIG_FILE, configFiles[i]);
                }
            }
        }
        ConfigDocument.Config[] cdocs = (ConfigDocument.Config[])cdoclist.toArray(new ConfigDocument.Config[cdoclist.size()]);

        SchemaTypeLoader linkTo = SchemaTypeLoaderImpl.build(null, cpResourceLoader, null);

        URI baseURI = null;
        if (baseDir != null)
            baseURI = baseDir.toURI();

        XmlOptions opts = new XmlOptions();
        if (download)
            opts.setCompileDownloadUrls();
        if (noUpa)
            opts.setCompileNoUpaRule();
        if (noPvr)
            opts.setCompileNoPvrRule();
        if (noAnn)
            opts.setCompileNoAnnotations();
        if (mdefNamespaces != null)
            opts.setCompileMdefNamespaces(mdefNamespaces);
        opts.setCompileNoValidation(); // already validated here
        opts.setEntityResolver(entResolver);

        // now pass it to the main compile function
        SchemaTypeSystemCompiler.Parameters params = new SchemaTypeSystemCompiler.Parameters();
        params.setName(name);
        params.setSchemas(sdocs);
        params.setConfigs(cdocs);
        params.setLinkTo(linkTo);
        params.setOptions(opts);
        params.setErrorListener(errorListener);
        params.setJavaize(true);
        params.setBaseURI(baseURI);
        params.setSourcesToCopyMap(sourcesToCopyMap);
        params.setSchemasDir(schemasDir);
        return SchemaTypeSystemCompiler.compile(params);
    }

    public static boolean compile(Parameters params)
    {
        File baseDir = params.getBaseDir();
        File[] xsdFiles = params.getXsdFiles();
        File[] wsdlFiles = params.getWsdlFiles();
        File[] javaFiles = params.getJavaFiles();
        File[] configFiles = params.getConfigFiles();
        File[] classpath = params.getClasspath();
        File outputJar = params.getOutputJar();
        String name = params.getName();
        File srcDir = params.getSrcDir();
        File classesDir = params.getClassesDir();
        String compiler = params.getCompiler();
        String jar = params.getJar();
        String memoryInitialSize = params.getMemoryInitialSize();
        String memoryMaximumSize = params.getMemoryMaximumSize();
        boolean nojavac = params.isNojavac();
        boolean debug = params.isDebug();
        boolean verbose = params.isVerbose();
        boolean quiet = params.isQuiet();
        boolean download = params.isDownload();
        boolean noUpa = params.isNoUpa();
        boolean noPvr = params.isNoPvr();
        boolean noAnn = params.isNoAnn();
        Collection outerErrorListener = params.getErrorListener();
        String repackage = params.getRepackage();
        List extensions = params.getExtensions();
        Set mdefNamespaces = params.getMdefNamespaces();

        EntityResolver cmdLineEntRes = ResolverUtil.resolverForCatalog(params.getCatalogFile());

        if (srcDir == null || classesDir == null)
            throw new IllegalArgumentException("src and class gen directories may not be null.");

        long start = System.currentTimeMillis();

        // Calculate the usenames based on the relativized filenames on the filesystem
        if (baseDir == null)
            baseDir = new File(System.getProperty("user.dir"));

        ResourceLoader cpResourceLoader = null;

        Map sourcesToCopyMap = new HashMap();

        if (classpath != null)
            cpResourceLoader = new PathResourceLoader(classpath);

        boolean result = true;

        File schemasDir = IOUtil.createDir(classesDir, "schema/src");

        // build the in-memory type system
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);
        SchemaTypeSystem system = loadTypeSystem(name, xsdFiles, wsdlFiles, configFiles, cpResourceLoader,
            download, noUpa, noPvr, noAnn, mdefNamespaces, baseDir, sourcesToCopyMap, errorListener,
            schemasDir, cmdLineEntRes);
        if (errorListener.hasError())
            result = false;
        long finish = System.currentTimeMillis();
        if (!quiet)
            System.out.println("Time to build schema type system: " + ((double)(finish - start) / 1000.0) + " seconds" );

        // now code generate and compile the JAR
        if (result && system != null) // todo: don't check "result" here if we want to compile anyway, ignoring invalid schemas
        {
            start = System.currentTimeMillis();

            // generate source and .xsb
            List sourcefiles = new ArrayList();
            result &= SchemaCodeGenerator.compileTypeSystem(system, srcDir, javaFiles, sourcesToCopyMap,
                classpath, classesDir, outputJar, nojavac, errorListener, repackage, verbose,
                sourcefiles, schemasDir);
            result &= !errorListener.hasError();

            if (result)
            {
                finish = System.currentTimeMillis();
                if (!quiet)
                    System.out.println("Time to generate code: " + ((double)(finish - start) / 1000.0) + " seconds" );
            }

            // compile source
            if (result && !nojavac)
            {
                start = System.currentTimeMillis();

                if (javaFiles != null)
                    sourcefiles.addAll(java.util.Arrays.asList(javaFiles));
                if (!CodeGenUtil.externalCompile(sourcefiles, classesDir, classpath, debug, compiler, memoryInitialSize, memoryMaximumSize, quiet, verbose))
                    result = false;

                finish = System.currentTimeMillis();
                if (result && !params.isQuiet())
                    System.out.println("Time to compile code: " + ((double)(finish - start) / 1000.0) + " seconds" );

                // jar classes and .xsb
                if (result && outputJar != null)
                {
                    if (!CodeGenUtil.externalJar(classesDir, outputJar, jar, quiet, verbose))
                        result = false;

                    if (result && !params.isQuiet())
                        System.out.println("Compiled types to: " + outputJar);
                }
            }
        }

        if (!result && !quiet)
            System.out.println("BUILD FAILED");
        else {
            // call schema compiler extension if registered
            runExtensions(extensions, system);
        }

        if (cpResourceLoader != null)
            cpResourceLoader.close();
        return result;
    }

    private static void runExtensions(List extensions, SchemaTypeSystem system)
    {
        if (extensions != null && extensions.size() > 0)
        {
            SchemaCompilerExtension sce = null;
            Iterator i = extensions.iterator();
            while (i.hasNext())
            {
                Extension extension = (Extension) i.next();
                try
                {
                    sce = (SchemaCompilerExtension) extension.getClassName().newInstance();
                }
                catch (InstantiationException e)
                {
                    System.out.println("UNABLE to instantiate schema compiler extension:" + extension.getClassName().getName());
                    System.out.println("EXTENSION Class was not run");
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("ILLEGAL ACCESS Exception when attempting to instantiate schema compiler extension: " + extension.getClassName().getName());
                    System.out.println("EXTENSION Class was not run");
                }
                System.out.println("Running Schema Compiler Extension: " + sce.getExtensionName());
                Map extensionParms = new HashMap();
                Iterator parmsi = extension.getParams().iterator();
                while (parmsi.hasNext())
                {
                    Extension.Param p = (Extension.Param) parmsi.next();
                    extensionParms.put(p.getName(), p.getValue());
                }
                sce.schemaCompilerExtension(system, extensionParms);
            }
        }
    }


    private static boolean wsdlContainsEncoded(XmlObject wsdldoc)
    {
        // search for any <soap:body use="encoded"/> etc.
        XmlObject[] useAttrs = wsdldoc.selectPath(
                "declare namespace soap='http://schemas.xmlsoap.org/wsdl/soap/' " +
                ".//soap:body/@use|.//soap:header/@use|.//soap:fault/@use");
        for (int i = 0; i < useAttrs.length; i++)
        {
            if ("encoded".equals(((SimpleValue)useAttrs[i]).getStringValue()))
                return true;
        }
        return false;
    }


}
