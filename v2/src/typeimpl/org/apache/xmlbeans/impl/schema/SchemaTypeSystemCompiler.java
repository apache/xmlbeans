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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.net.URI;

import org.w3.x2001.xmlSchema.SchemaDocument.Schema;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument.Config;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.config.SchemaConfig;

import java.util.Collection;
import java.io.File;

public class SchemaTypeSystemCompiler
{
    public static class Parameters
    {
        private SchemaTypeSystem existingSystem;
        private String name;
        private Schema[] schemas;
        private Config[] configs;
        private File[] javaFiles;
        private SchemaTypeLoader linkTo;
        private XmlOptions options;
        private Collection errorListener;
        private boolean javaize;
        private URI baseURI;
        private Map sourcesToCopyMap;
        private File schemasDir;
        private File[] classpath;

        public SchemaTypeSystem getExistingTypeSystem()
        {
            return existingSystem;
        }

        public void setExistingTypeSystem(SchemaTypeSystem system)
        {
            this.existingSystem = system;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public SchemaDocument.Schema[] getSchemas()
        {
            return schemas;
        }

        public void setSchemas(SchemaDocument.Schema[] schemas)
        {
            this.schemas = schemas;
        }

        public ConfigDocument.Config[] getConfigs()
        {
            return configs;
        }

        public void setConfigs(ConfigDocument.Config[] configs)
        {
            this.configs = configs;
        }

        public File[] getJavaFiles()
        {
            return javaFiles;
        }

        public void setJavaFiles(File[] javaFiles)
        {
            this.javaFiles = javaFiles;
        }

        public SchemaTypeLoader getLinkTo()
        {
            return linkTo;
        }

        public void setLinkTo(SchemaTypeLoader linkTo)
        {
            this.linkTo = linkTo;
        }

        public XmlOptions getOptions()
        {
            return options;
        }

        public void setOptions(XmlOptions options)
        {
            this.options = options;
        }

        public Collection getErrorListener()
        {
            return errorListener;
        }

        public void setErrorListener(Collection errorListener)
        {
            this.errorListener = errorListener;
        }

        public boolean isJavaize()
        {
            return javaize;
        }

        public void setJavaize(boolean javaize)
        {
            this.javaize = javaize;
        }

        public URI getBaseURI()
        {
            return baseURI;
        }

        public void setBaseURI(URI baseURI)
        {
            this.baseURI = baseURI;
        }

        public Map getSourcesToCopyMap()
        {
            return sourcesToCopyMap;
        }

        public void setSourcesToCopyMap(Map sourcesToCopyMap)
        {
            this.sourcesToCopyMap = sourcesToCopyMap;
        }

        public File getSchemasDir()
        {
            return schemasDir;
        }

        public void setSchemasDir(File schemasDir)
        {
            this.schemasDir = schemasDir;
        }

        public File[] getClasspath()
        {
            return classpath;
        }

        public void setClasspath(File[] classpath)
        {
            this.classpath = classpath;
        }
    }

    public static SchemaTypeSystem compile(Parameters params)
    {
        return compileImpl(params.getExistingTypeSystem(), params.getName(),
            params.getSchemas(), params.getConfigs(), params.getJavaFiles(), params.getLinkTo(),
            params.getOptions(), params.getErrorListener(), params.isJavaize(),
            params.getBaseURI(), params.getSourcesToCopyMap(), params.getSchemasDir(), params.getClasspath());
    }
    
    /* package */ static SchemaTypeSystemImpl compileImpl( SchemaTypeSystem system, String name,
        Schema[] schemas, Config[] configs, File[] javaFiles, SchemaTypeLoader linkTo,
        XmlOptions options, Collection outsideErrors, boolean javaize,
        URI baseURI, Map sourcesToCopyMap, File schemasDir, File[] classpath)
    {
        if (linkTo == null)
            throw new IllegalArgumentException("Must supply linkTo");

        XmlErrorWatcher errorWatcher = new XmlErrorWatcher(outsideErrors);
        boolean incremental = system != null;

        // construct the state
        StscState state = StscState.start();
        boolean validate = (options == null || !options.hasOption(XmlOptions.COMPILE_NO_VALIDATION));
        try
        {
            state.setErrorListener(errorWatcher);
            state.setSchemaConfig(SchemaConfig.forConfigDocuments(configs, javaFiles, classpath));
            state.setOptions(options);
            state.setGivenTypeSystemName(name);
            state.setSchemasDir(schemasDir);
            if (baseURI != null)
                state.setBaseUri(baseURI);

            // construct the classpath (you always get the builtin types)
            linkTo = SchemaTypeLoaderImpl.build(new SchemaTypeLoader[] { BuiltinSchemaTypeSystem.get(), linkTo }, null, null);
            state.setImportingTypeLoader(linkTo);

            List validSchemas = new ArrayList(schemas.length);

            // load all the xsd files into it
            if (validate)
            {
                for (int i = 0; i < schemas.length; i++)
                {
                    if (schemas[i].validate(new XmlOptions().setErrorListener(errorWatcher)))
                        validSchemas.add(schemas[i]);
                }
            }
            else
            {
                validSchemas.addAll(Arrays.asList(schemas));
            }

            Schema[] startWith = (Schema[])validSchemas.toArray(new Schema[validSchemas.size()]);

            if (incremental)
            {
                Set namespaces = new HashSet();
                startWith = getSchemasToRecompile((SchemaTypeSystemImpl)system, startWith, namespaces);
                state.initFromTypeSystem((SchemaTypeSystemImpl)system, namespaces);
            }
            else
            {
                state.setDependencies(new SchemaDependencies());
            }

            // deal with imports and includes
            StscImporter.SchemaToProcess[] schemasAndChameleons = StscImporter.resolveImportsAndIncludes(startWith);

            // call the translator so that it may also perform magic
            StscTranslator.addAllDefinitions(schemasAndChameleons);

            // call the resolver to do its magic
            StscResolver.resolveAll();
            
            // call the checker to check both restrictions and defaults
            StscChecker.checkAll();

            // call the javaizer to do its magic
            StscJavaizer.javaizeAllTypes(javaize);

            // construct the loader out of the state
            state.get().sts().loadFromStscState(state);

            // fill in the source-copy map
            if (sourcesToCopyMap != null)
                sourcesToCopyMap.putAll(state.sourceCopyMap());

            // if any errors, return null
            if (errorWatcher.hasError())
                return null;

            return state.get().sts();
        }
        finally
        {
            StscState.end();
        }
    }

    /**
     * Get the list of Schemas to be recompiled, based on the list of Schemas that
     * were modified.
     * We make use of the depencency information that we stored in the typesystem
     * and of the entity resolvers that have been set up
     */
    private static Schema[] getSchemasToRecompile(SchemaTypeSystemImpl system,
        Schema[] modified, Set namespaces)
    {
        Set modifiedFiles = new HashSet();
        Map haveFile = new HashMap();
        for (int i = 0; i < modified.length; i++)
        {
            String fileURL = modified[i].documentProperties().getSourceName();
            if (fileURL == null)
                throw new IllegalArgumentException("One of the Schema files passed in" +
                    " doesn't have the source set, which prevents it to be incrementally" +
                    " compiled");
            modifiedFiles.add(fileURL);
            haveFile.put(fileURL, modified[i]);
        }
        SchemaDependencies dep = system.getDependencies();
        List nss = dep.getNamespacesTouched(modifiedFiles);
        namespaces.addAll(dep.computeTransitiveClosure(nss));
        List needRecompilation = dep.getFilesTouched(namespaces);
        StscState.get().setDependencies(new SchemaDependencies(dep, namespaces));
        List result = new ArrayList();
        for (int i = 0; i < needRecompilation.size(); i++)
        {
            String url = (String) needRecompilation.get(i);
            Schema have = (Schema) haveFile.get(url);
            if (have != null)
                result.add(have);
            else
            {
                // We have to load the file from the entity resolver
                try
                {
                    XmlObject xdoc = StscImporter.DownloadTable.
                        downloadDocument(StscState.get().getS4SLoader(), null, url);
                    XmlOptions voptions = new XmlOptions();
                    voptions.setErrorListener(StscState.get().getErrorListener());
                    if (!(xdoc instanceof SchemaDocument) || !xdoc.validate(voptions))
                    {
                        StscState.get().error("Referenced document is not a valid schema, URL = " + url, XmlErrorCodes.CANNOT_FIND_RESOURCE, null);
                        continue;
                    }

                    SchemaDocument sDoc = (SchemaDocument)xdoc;

                    result.add(sDoc.getSchema());
                }
                catch (java.net.MalformedURLException mfe)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "MalformedURLException", url, mfe.getMessage() }, null);
                    continue;
                }
                catch (java.io.IOException ioe)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "IOException", url, ioe.getMessage() }, null);
                    continue;
                }
                catch (XmlException xmle)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "XmlException", url, xmle.getMessage() }, null);
                    continue;
                }
            }
        }
        return (Schema[]) result.toArray(new Schema[result.size()]);
    }


}
