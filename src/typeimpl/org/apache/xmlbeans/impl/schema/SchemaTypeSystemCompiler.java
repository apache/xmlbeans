/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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
import com.bea.x2002.x09.xbean.config.ConfigDocument.Config;
import com.bea.x2002.x09.xbean.config.ConfigDocument;
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
