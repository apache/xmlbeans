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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.internal.xmlbeans.wsdlsubst.DefinitionsDocument;
import org.apache.internal.xmlbeans.wsdlsubst.TImport;
import org.apache.xmlbeans.impl.common.SequencedHashMap;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
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

        URL source = null;
        URL target = null;

        try
        {
            source = new URL(args[0]);
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
                URL dir = new File(".").getCanonicalFile().toURL();
                String lastPart = source.getPath();
                lastPart = lastPart.substring(lastPart.lastIndexOf('/') + 1);
                target = CodeGenUtil.resolve(dir, lastPart);
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
                target = new URL(args[1]);
                if (!target.getProtocol().equals("file"))
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
                    target = new File(args[1]).getCanonicalFile().toURL();
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
            URL source = (URL)i.next();
            URL target = (URL)uriMap.get(source);
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
     * Copies the schema or wsdl at the source URL to the target URL, along
     * with any relative references.  The target URL should be a file URL.
     * If doCopy is false, the file copies are not actually done; the map
     * returned just describes the copies that would have been done.
     *
     * @param source an arbitrary URL describing a source Schema or WSDL
     * @param target a file URL describing a target filename
     * @return a map of all the source/target URLs needed to copy
     * the file along with all its relative referents.
     */
    public static Map findAllRelative(URL source, URL target)
    {
        Map result = new SequencedHashMap();
        result.put(source, target);

        LinkedList process = new LinkedList();
        process.add(source);

        while (!process.isEmpty())
        {
            URL nextSource = (URL)process.removeFirst();
            URL nextTarget = (URL)result.get(nextSource);
            Map nextResults = findRelativeInOne(nextSource, nextTarget);
            for (Iterator i = nextResults.keySet().iterator(); i.hasNext(); )
            {
                URL newSource = (URL)i.next();
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
                    "http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst" 
            ));

    private static Map findRelativeInOne(URL source, URL target)
    {
        try
        {
            XmlObject xobj = XmlObject.Factory.parse(source, loadOptions);
            XmlCursor xcur = xobj.newCursor();
            xcur.toFirstChild();

            Map result = new SequencedHashMap();

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

    private static void putNewMapping(Map result, URL origSource, URL origTarget, String literalURL)
    {
        if (literalURL == null)
            return;

        URL url = null;
        try
        {
            url = new URL(literalURL);
        }
        catch(MalformedURLException mue)
        {
            url = null;
        }

        //if the string can be parsed into a URL, it's absolute
        if (url != null)
            return;

        URL newSource = CodeGenUtil.resolve(origSource, literalURL);
        URL newTarget = CodeGenUtil.resolve(origTarget, literalURL);

        if (newSource != null && newTarget != null)
            result.put(newSource, newTarget);
    }

    private static void putMappingsFromSchema(Map result, URL source, URL target, SchemaDocument.Schema schema)
    {
        ImportDocument.Import[] imports = schema.getImportArray();
        for (int i = 0; i < imports.length; i++)
            putNewMapping(result, source, target, imports[i].getSchemaLocation());

        IncludeDocument.Include[] includes = schema.getIncludeArray();
        for (int i = 0; i < includes.length; i++)
            putNewMapping(result, source, target, includes[i].getSchemaLocation());
    }

    private static void putMappingsFromWsdl(Map result, URL source, URL target, DefinitionsDocument.Definitions wdoc)
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
