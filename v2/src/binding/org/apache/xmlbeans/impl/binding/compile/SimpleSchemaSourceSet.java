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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.common.XmlErrorContext;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleSchemaSourceSet implements SchemaSourceSet
{
    private TylarLoader tylarLoader;
    private SchemaTypeSystem schemaTypeSystem;

    public SchemaTypeSystem getSchemaTypeSystem()
    {
        return schemaTypeSystem;
    }

    public TylarLoader getTylarLoader()
    {
        return tylarLoader;
    }

    public void compileSchemaToBinaries(File classesDir)
    {
        SchemaTypeSystemImpl impl = (SchemaTypeSystemImpl)schemaTypeSystem;
        impl.saveToDirectory(classesDir);
    }

    private SimpleSchemaSourceSet(SchemaTypeSystem schemaTypeSystem, TylarLoader tylarLoader)
    {
        this.tylarLoader = tylarLoader;
        this.schemaTypeSystem = schemaTypeSystem;
    }
    
    private static SchemaTypeLoader schemaLoader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());
    
    private static SchemaDocument parseSchemaFile(File file) throws IOException, XmlException
    {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setLoadMessageDigest();
        return (SchemaDocument)schemaLoader.parse(file, SchemaDocument.type, options);
    }
    
    private static TylarLoader defaultTylarLoader(TylarLoader supplied)
    {
        if (supplied != null)
            return supplied;
        return SimpleTylarLoader.forBuiltins();
    }
    
    public static SchemaSourceSet forFile(File xsdFilename, TylarLoader tylarLoader) throws IOException, XmlException
    {
        tylarLoader = defaultTylarLoader(tylarLoader);
        SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[] { parseSchemaFile(xsdFilename) }, XmlBeans.getBuiltinTypeSystem(), null);
        return new SimpleSchemaSourceSet(sts, tylarLoader);
    }
    
    public static SchemaSourceSet forFileArray(File[] xsdFiles, TylarLoader tylarLoader) throws IOException, XmlException
    {
        tylarLoader = defaultTylarLoader(tylarLoader);
        XmlObject[] parsedSchemas = new XmlObject[xsdFiles.length];
        for (int i = 0; i < xsdFiles.length; i++)
            parsedSchemas[i] = parseSchemaFile(xsdFiles[i]);
        SchemaTypeSystem sts = XmlBeans.compileXsd(parsedSchemas, XmlBeans.getBuiltinTypeSystem(), null);
        return new SimpleSchemaSourceSet(sts, tylarLoader);
    }
        
    public static SchemaSourceSet forSchemaGenerator(SchemaCodeResult generator, TylarLoader tylarLoader)
    {
        try
        {
            String[] namespaces = generator.getTargetNamespaces();
            Collection schemas = new ArrayList();
            for (int i = 0; i < namespaces.length; i++)
            {
                ByteArrayOutputStream inMemoryBuffer = new ByteArrayOutputStream();
                generator.printSchema(namespaces[i], inMemoryBuffer);
                inMemoryBuffer.close();
                ByteArrayInputStream input = new ByteArrayInputStream(inMemoryBuffer.toByteArray());
                schemas.add(SchemaDocument.Factory.parse(input));
            }
            XmlObject [] sources = (XmlObject[])schemas.toArray(new XmlObject[schemas.size()]);
            SchemaTypeSystem sts = XmlBeans.compileXsd(sources, tylarLoader.getSchemaTypeLoader(), null);
            return new SimpleSchemaSourceSet(sts, tylarLoader);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (XmlException e)
        {
            throw (IllegalStateException)new IllegalStateException().initCause(e);
        }
    }
}
