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
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.net.URI;

import org.w3.x2001.xmlSchema.SchemaDocument.Schema;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument.Config;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.config.SchemaConfig;

import java.util.Collection;

public class SchemaTypeSystemCompiler
{
    public static class Parameters
    {
        private String name;
        private Schema[] schemas;
        private Config[] configs;
        private SchemaTypeLoader linkTo;
        private XmlOptions options;
        private Collection errorListener;
        private boolean javaize;
        private URI baseURI;
        private Map sourcesToCopyMap;

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
    }

    public static SchemaTypeSystem compile(Parameters params)
    {
        return compileImpl(params.getName(), params.getSchemas(), params.getConfigs(), params.getLinkTo(), params.getOptions(), params.getErrorListener(), params.isJavaize(), params.getBaseURI(), params.getSourcesToCopyMap());
    }
    
    /* package!!! */ static SchemaTypeSystemImpl compileImpl(
        String name, Schema[] schemas, Config[] configs,
        SchemaTypeLoader linkTo, XmlOptions options, Collection outsideErrors, boolean javaize, URI baseURI, Map sourcesToCopyMap)
    {
        if (linkTo == null)
            throw new IllegalArgumentException("Must supply linkTo");

        XmlErrorWatcher errorWatcher = new XmlErrorWatcher(outsideErrors);

        // construct the state
        StscState state = StscState.start();
        boolean validate = (options == null || !options.hasOption(XmlOptions.COMPILE_NO_VALIDATION));
        try
        {
            state.setErrorListener(errorWatcher);
            state.setSchemaConfig(SchemaConfig.forConfigDocuments(configs));
            state.setOptions(options);
            state.setGivenTypeSystemName(name);
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


}
