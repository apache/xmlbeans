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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.internal.xmlbeans.wsdlsubst.DefinitionsDocument;
import org.apache.internal.xmlbeans.wsdlsubst.TImport;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.io.File;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.ImportDocument;
import org.w3.x2001.xmlSchema.IncludeDocument;

public class SchemaCopy
{

    public static void main(String[] args)
    {
        if (args.length < 1 || args.length > 2)
        {
            System.out.println("Usage: schemacopy sourceurl [targetfile]");
            return;
        }

        URI source = null;
        URI target = null;

        try
        {
            source = new URI(args[0]);
            source.toURL(); // to trigger exception
        }
        catch (Exception e)
        {
            System.err.println("Badly formed URL " + source);
            return;
        }

        if (args.length < 2)
        {
            try
            {
                URI dir = new File(".").getCanonicalFile().toURI();
                String lastPart = source.getPath();
                lastPart = lastPart.substring(lastPart.lastIndexOf('/') + 1);
                target = CodeGenUtil.resolve(dir, URI.create(lastPart));
            }
            catch (Exception e)
            {
                System.err.println("Cannot canonicalize current directory");
                return;
            }
        }
        else
        {
            try
            {
                target = new URI(args[1]);
                if (!target.isAbsolute())
                    target = null;
                else if (!target.getScheme().equals("file"))
                    target = null;
            }
            catch (Exception e)
            {
                target = null;
            }

            if (target == null)
            {
                try
                {
                    target = new File(target).getCanonicalFile().toURI();
                }
                catch (Exception e)
                {
                    System.err.println("Cannot canonicalize current directory");
                    return;
                }
            }
        }

        Map thingsToCopy = findAllRelative(source, target);
        copyAll(thingsToCopy, true);
    }

    private static void copyAll(Map uriMap, boolean stdout)
    {
        for (Iterator i = uriMap.keySet().iterator(); i.hasNext(); )
        {
            URI source = (URI)i.next();
            URI target = (URI)uriMap.get(source);
            try
            {
                IOUtil.copyCompletely(source, target);
            }
            catch (Exception e)
            {
                if (stdout)
                    System.out.println("Could not copy " + source + " -> " + target);
                continue;
            }
            if (stdout)
            System.out.println("Copied " + source + " -> " + target);
        }
    }


    /**
     * Copies the schema or wsdl at the source URI to the target URI, along
     * with any relative references.  The target URI should be a file URI.
     * If doCopy is false, the file copies are not actually done; the map
     * returned just describes the copies that would have been done.
     *
     * @param source an arbitrary URI describing a source Schema or WSDL
     * @param target a file URI describing a target filename
     * @return a map of all the source/target URIs needed to copy
     * the file along with all its relative referents.
     */
    public static Map findAllRelative(URI source, URI target)
    {
        Map result = new LinkedHashMap();
        result.put(source, target);

        LinkedList process = new LinkedList();
        process.add(source);

        while (!process.isEmpty())
        {
            URI nextSource = (URI)process.removeFirst();
            URI nextTarget = (URI)result.get(nextSource);
            Map nextResults = findRelativeInOne(nextSource, nextTarget);
            for (Iterator i = nextResults.keySet().iterator(); i.hasNext(); )
            {
                URI newSource = (URI)i.next();
                if (result.containsKey(newSource))
                    continue;
                result.put(newSource, nextResults.get(newSource));
                process.add(newSource);
            }
        }

        return result;
    }
    
    private static final XmlOptions loadOptions = new XmlOptions().
            setLoadSubstituteNamespaces(Collections.singletonMap(
                    "http://www.apache.org/internal/xmlbeans/wsdlsubst", "http://schemas.xmlsoap.org/wsdl/"
            ));

    private static Map findRelativeInOne(URI source, URI target)
    {
        try
        {
            URL sourceURL = source.toURL();
            XmlObject xobj = XmlObject.Factory.parse(sourceURL, loadOptions);
            XmlCursor xcur = xobj.newCursor();
            xcur.toFirstChild();

            Map result = new LinkedHashMap();
            if (xobj instanceof SchemaDocument)
                putMappingsFromSchema(result, source, target, ((SchemaDocument)xobj).getSchema());
            else if (xobj instanceof DefinitionsDocument)
                putMappingsFromWsdl(result, source, target, ((DefinitionsDocument)xobj).getDefinitions());
            return result;
        }
        catch (Exception e)
        {
            // any exceptions parsing the given URL?  Then skip this file silently
        }
        return Collections.EMPTY_MAP;
    }

    private static void putNewMapping(Map result, URI origSource, URI origTarget, String literalURI)
    {
        try
        {
            if (literalURI == null)
                return;
            URI newRelative = new URI(literalURI);
            if (newRelative.isAbsolute())
                return;
            URI newSource = CodeGenUtil.resolve(origSource, newRelative);
            URI newTarget = CodeGenUtil.resolve(origTarget, newRelative);
            result.put(newSource, newTarget);
        }
        catch (URISyntaxException e)
        {
            // uri syntax problem? do nothing silently.
        }
    }

    private static void putMappingsFromSchema(Map result, URI source, URI target, SchemaDocument.Schema schema)
    {
        ImportDocument.Import[] imports = schema.getImportArray();
        for (int i = 0; i < imports.length; i++)
            putNewMapping(result, source, target, imports[i].getSchemaLocation());

        IncludeDocument.Include[] includes = schema.getIncludeArray();
        for (int i = 0; i < includes.length; i++)
            putNewMapping(result, source, target, includes[i].getSchemaLocation());
    }

    private static void putMappingsFromWsdl(Map result, URI source, URI target, DefinitionsDocument.Definitions wdoc)
    {
        XmlObject[] types = wdoc.getTypesArray();
        for (int i = 0; i < types.length; i++)
        {
            SchemaDocument.Schema[] schemas = (SchemaDocument.Schema[])types[i].selectPath("declare namespace xs='http://www.w3.org/2001/XMLSchema' xs:schema");
            for (int j = 0; j < schemas.length; j++)
                putMappingsFromSchema(result, source, target, schemas[j]);
        }

        TImport[] imports = wdoc.getImportArray();
        for (int i = 0; i < imports.length; i++)
            putNewMapping(result, source, target, imports[i].getLocation());
    }
}
