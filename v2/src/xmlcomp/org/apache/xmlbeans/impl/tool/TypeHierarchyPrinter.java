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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.QNameHelper;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.io.File;

import org.w3.x2001.xmlSchema.SchemaDocument;

public class TypeHierarchyPrinter
{
    public static void main(String[] args) throws Exception
    {
        CommandLine cl = new CommandLine(args, Collections.EMPTY_SET);
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }
        if (cl.args().length == 0)
        {
            System.out.println("Prints inheritance hierarchy of types defined in a schema.");
            System.out.println("Usage: xsdtree [-noanon] [-nopvr] [-noupa] [-license] file1.xsd file2.xsd ...");
            return;
        }
        
        boolean noanon = (cl.getOpt("noanon") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);
        
        File[] schemaFiles = cl.getFiles();
        
        // step 1: load all the files
        List sdocs = new ArrayList();
        for (int i = 0; i < schemaFiles.length; i++)
        {
            try
            {
                sdocs.add(
                    SchemaDocument.Factory.parse(
                        schemaFiles[i], (new XmlOptions()).setLoadLineNumbers()));
            }
            catch (Exception e)
            {
                System.err.println( schemaFiles[i] + " not loadable: " + e );
            }
        }
        

        XmlObject[] schemas = (XmlObject[])sdocs.toArray(new XmlObject[0]);
        
        // step 2: compile all the schemas
        SchemaTypeSystem typeSystem;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        schemaOptions.setCompileDownloadUrls();
        if (nopvr)
            schemaOptions.setCompileNoPvrRule();
        if (noupa)
            schemaOptions.setCompileNoUpaRule();
        
        try
        {
            typeSystem = XmlBeans.compileXsd(schemas, XmlBeans.getBuiltinTypeSystem(), schemaOptions);
        }
        catch (XmlException e)
        {
            System.out.println("Schema invalid");
            if (compErrors.isEmpty())
                System.out.println(e.getMessage());
            else for (Iterator i = compErrors.iterator(); i.hasNext(); )
                System.out.println(i.next());
            return;
        }
        
        // step 3: go through all the types, and note their base types and namespaces
        Map prefixes = new HashMap();
        prefixes.put("http://www.w3.org/XML/1998/namespace", "xml");
        prefixes.put("http://www.w3.org/2001/XMLSchema", "xs");
        System.out.println("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
        
        // This will be a map of (base SchemaType -> Collection of directly dervied types)
        Map childTypes = new HashMap();
        
        // breadthfirst traversal of the type containment tree
        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(typeSystem.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(typeSystem.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(typeSystem.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType sType = (SchemaType)allSeenTypes.get(i);
            
            // recurse through nested anonymous types as well
            if (!noanon)
                allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
            
            // we're not interested in document types, attribute types, or chasing the base type of anyType
            if (sType.isDocumentType() || sType.isAttributeType() || sType == XmlObject.type)
                continue;
            
            // assign a prefix to the namespace of this type if needed
            noteNamespace(prefixes, sType);
            
            // enter this type in the list of children of its base type
            Collection children = (Collection)childTypes.get(sType.getBaseType());
            if (children == null)
            {
                children = new ArrayList();
                childTypes.put(sType.getBaseType(), children);
                
                // the first time a builtin type is seen, add it too (to get a complete tree up to anyType)
                if (sType.getBaseType().isBuiltinType())
                    allSeenTypes.add(sType.getBaseType());
            }
            children.add(sType);
        }
        
        // step 4: print the tree, starting from xs:anyType (i.e., XmlObject.type)
        List typesToPrint = new ArrayList();
        typesToPrint.add(XmlObject.type);
        StringBuffer spaces = new StringBuffer();
        while (!typesToPrint.isEmpty())
        {
            SchemaType sType = (SchemaType)typesToPrint.remove(typesToPrint.size() - 1);
            if (sType == null)
                spaces.setLength(Math.max(0, spaces.length() - 2));
            else
            {
                System.out.println(spaces + "+-" + QNameHelper.readable(sType, prefixes) + notes(sType));
                Collection children = (Collection)childTypes.get(sType);
                if (children != null && children.size() > 0)
                {
                    spaces.append(typesToPrint.size() == 0 || typesToPrint.get(typesToPrint.size() - 1) == null ? "  " : "| ");
                    typesToPrint.add(null);
                    typesToPrint.addAll(children);
                }
            }
        }
    }
    
    private static String notes(SchemaType sType)
    {
        if (sType.isBuiltinType())
            return " (builtin)";
        
        if (sType.isSimpleType())
        {
            switch (sType.getSimpleVariety())
            {
                case SchemaType.LIST:
                    return " (list)";
                case SchemaType.UNION:
                    return " (union)";
                default:
                    if (sType.getEnumerationValues() != null)
                        return " (enumeration)";
                    return "";
            }
        }
        
        switch (sType.getContentType())
        {
            case SchemaType.MIXED_CONTENT:
                return " (mixed)";
            case SchemaType.SIMPLE_CONTENT:
                return " (complex)";
            default:
                return "";
        }
    }
    
    private static void noteNamespace(Map prefixes, SchemaType sType)
    {
        String namespace = QNameHelper.namespace(sType);
        if (namespace.equals("") || prefixes.containsKey(namespace))
            return;
        
        String base = QNameHelper.suggestPrefix(namespace);
        String result = base;
        for (int n = 0; prefixes.containsValue(result); n += 1)
        {
            result = base + n;
        }
        
        prefixes.put(namespace, result);
        System.out.println("xmlns:" + result + "=\"" + namespace + "\"");
    }
}
