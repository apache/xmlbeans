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

import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.apache.xmlbeans.impl.schema.ResourceLoader;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.common.XmlErrorPrinter;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.common.XmlErrorContext;
import org.apache.xmlbeans.impl.values.XmlListImpl;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument;

import java.io.File;
import java.util.*;
import java.net.URI;

import org.w3.x2001.xmlSchema.SchemaDocument;

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
            System.out.println("    -compiler - path to external java compiler");
            System.out.println("    -jar - path to jar utility");
            System.out.println("    -ms - initial memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_START + "')");
            System.out.println("    -mx - maximum memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_MAX + "')");
            System.out.println("    -debug - compile with debug symbols");
            System.out.println("    -quiet - print fewer informational messages");
            System.out.println("    -verbose - print more informational messages");
            System.out.println("    -version - prints version information");
            System.out.println("    -license - prints license information");
            System.out.println("    -allowmdef \"[ns] [ns] [ns]\" - ignores multiple defs in given namespaces");
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
        CommandLine cl = new CommandLine(args, opts);

        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null)
        {
            CommandLine.printVersion();
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
        boolean nojavac = (cl.getOpt("srconly") != null);
        boolean debug = (cl.getOpt("debug") != null);
        boolean jaxb = (cl.getOpt("jaxb") != null);

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
            src = SchemaCodeGenerator.createDir(tempdir, "src");
        if (classes == null)
            classes = SchemaCodeGenerator.createDir(tempdir, "classes");

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
        params.setDebug(debug);
        params.setErrorListener(err);
        params.setRepackage(repackage);
        params.setExtensions(extensions);
        params.setJaxb(jaxb);
        params.setMdefNamespaces(mdefNamespaces);

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
        private boolean debug;
        private String repackage;
        private List extensions = Collections.EMPTY_LIST;
        private boolean jaxb;
        private Set mdefNamespaces = Collections.EMPTY_SET;

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


        public void setJaxb(boolean jaxb)
        {
            this.jaxb = jaxb;
        }

        public boolean getJaxb()
        {
            return this.jaxb;
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

    }

    private static SchemaTypeSystem loadTypeSystem(
        String name, File[] xsdFiles,
        File[] wsdlFiles, File[] configFiles, ResourceLoader cpResourceLoader,
        boolean download, boolean noUpa, boolean noPvr, Set mdefNamespaces,
        File baseDir, Map sourcesToCopyMap, Collection outerErrorListener)
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
                    options.setLoadSubstituteNamespaces(MAP_COMPATIBILITY_CONFIG_URIS);

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
                            XmlObject[] schemas = types[j].selectPath("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\" xs:schema");
                            if (schemas.length == 0)
                            {
                                StscState.addWarning(errorListener, "The WSDL " + wsdlFiles[i] + " did not have any schema documents in namespace 'http://www.w3.org/2001/XMLSchema'", XmlErrorContext.GENERIC_ERROR, wsdldoc);
                                continue;
                            }

                            for (int k = 0; k < schemas.length; k++)
                            {
                                if (schemas[k] instanceof SchemaDocument.Schema &&
                                    schemas[k].validate(new XmlOptions().setErrorListener(errorListener)))
                                {
                                    count++;
                                    scontentlist.add(schemas[k]);
                                }
                            }
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
                    options.setLoadSubstituteNamespaces(MAP_COMPATIBILITY_CONFIG_URIS);

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
        if (mdefNamespaces != null)
            opts.setCompileMdefNamespaces(mdefNamespaces);
        opts.setCompileNoValidation(); // already validated here

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
        Collection outerErrorListener = params.getErrorListener();
        String repackage = params.getRepackage();
        List extensions = params.getExtensions();
        boolean jaxb = params.getJaxb();
        Set mdefNamespaces = params.getMdefNamespaces();

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

        // build the in-memory type system
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);
        SchemaTypeSystem system = loadTypeSystem(name, xsdFiles, wsdlFiles, configFiles, cpResourceLoader, download, noUpa, noPvr, mdefNamespaces, baseDir, sourcesToCopyMap, errorListener);
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
            result &= SchemaCodeGenerator.compileTypeSystem(system, srcDir, javaFiles, sourcesToCopyMap, classpath, classesDir, outputJar, nojavac, jaxb, errorListener, repackage, verbose, sourcefiles);
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
        {
            System.out.println("BUILD FAILED");
        }
        else {
            // call schema compiler extension if registered
            runExtensions(extensions, system, classesDir);
        }

        if (cpResourceLoader != null)
            cpResourceLoader.close();
        return result;
    }

    private static void runExtensions(List extensions, SchemaTypeSystem system, File classesDir)
    {
        if (extensions != null && extensions.size() > 0)
        {
            SchemaCompilerExtension sce = null;
            Iterator i = extensions.iterator();
            Map extensionParms = null;
            String classesDirName = null;
            try
            {
                classesDirName = classesDir.getCanonicalPath();
            }
            catch(java.io.IOException e)
            {
                System.out.println("WARNING: Unable to get the path for schema jar file");
                classesDirName = classesDir.getAbsolutePath();
            }

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
                    break;
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("ILLEGAL ACCESS Exception when attempting to instantiate schema compiler extension: " + extension.getClassName().getName());
                    System.out.println("EXTENSION Class was not run");
                    break;
                }

                System.out.println("Running Extension: " + sce.getExtensionName());
                extensionParms = new HashMap();
                Iterator parmsi = extension.getParams().iterator();
                while (parmsi.hasNext())
                {
                    Extension.Param p = (Extension.Param) parmsi.next();
                    extensionParms.put(p.getName(), p.getValue());
                }
                extensionParms.put("classesDir", classesDirName);
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

    private static final String CONFIG_URI = "http://xml.apache.org/xmlbeans/2004/02/xbean/config";
    private static final String COMPATIBILITY_CONFIG_URI = "http://www.bea.com/2002/09/xbean/config";
    private static final Map MAP_COMPATIBILITY_CONFIG_URIS;
    static
    {
        MAP_COMPATIBILITY_CONFIG_URIS = new HashMap();
        MAP_COMPATIBILITY_CONFIG_URIS.put(COMPATIBILITY_CONFIG_URI, CONFIG_URI);
    }
}
