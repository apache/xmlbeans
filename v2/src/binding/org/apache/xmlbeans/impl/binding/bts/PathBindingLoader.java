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
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.JavaName;
import org.apache.xmlbeans.impl.schema.FileResourceLoader;
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * A binding loader impl with the ability to chain together a path
 * of other loaders.
 */ 
public class PathBindingLoader implements BindingLoader
{
    private final Collection loaderPath;
    public static final PathBindingLoader EMPTY_LOADER = new PathBindingLoader(Collections.EMPTY_LIST);
    
    public static BindingLoader forPath(BindingLoader[] path)
    {
        return forPath(Arrays.asList(path));
    }
    
    public static BindingLoader forPath(Collection path)
    {
        IdentityHashMap seen = new IdentityHashMap();
        
        List flattened = new ArrayList(path.size());
        for (Iterator i = path.iterator(); i.hasNext(); )
            addToPath(flattened, seen, (BindingLoader)i.next());
        
        if (flattened.size() == 0)
            return EMPTY_LOADER;
        
        if (flattened.size() == 1)
            return (BindingLoader)flattened.get(0);
        
        return new PathBindingLoader(flattened);
    }
    
    private static void addToPath(List path, IdentityHashMap seen, BindingLoader loader)
    {
        if (seen.containsKey(loader))
            return;
        
        if (loader instanceof PathBindingLoader)
            for (Iterator j = ((PathBindingLoader)path).loaderPath.iterator(); j.hasNext(); )
                addToPath(path, seen, (BindingLoader)j.next());
        else
            path.add(loader);
    }
    
    private PathBindingLoader(List path)
    {
        loaderPath = Collections.unmodifiableList(path);
    }
    
    public BindingType getBindingType(BindingTypeName btName)
    {
        BindingType result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).getBindingType(btName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingTypeName lookupPojoFor(XmlName xName)
    {
        BindingTypeName result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).lookupPojoFor(xName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingTypeName lookupXmlObjectFor(XmlName xName)
    {
        BindingTypeName result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).lookupXmlObjectFor(xName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingTypeName lookupTypeFor(JavaName jName)
    {
        BindingTypeName result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).lookupTypeFor(jName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingTypeName lookupElementFor(JavaName jName)
    {
        BindingTypeName result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).lookupElementFor(jName);
            if (result != null)
                return result;
        }
        return null;
    }
    
    public static final String STANDARD_PATH = "org/apache/xmlbeans/binding-config.xml";
    
    public static BindingLoader forClassLoader(ClassLoader loader)
    {
        Enumeration i;

        try
        {
            i = loader.getResources(STANDARD_PATH);
        }
        catch (IOException e)
        {
            throw (IllegalStateException)(new IllegalStateException().initCause(e));
        }

        URL resource = null;

        List files = new ArrayList();
        
        try
        {
            while (i.hasMoreElements())
            {
                resource = (URL)i.nextElement();
                files.add(BindingFile.forDoc(BindingConfigDocument.Factory.parse(resource)));
            }
        }
        catch (Exception e)
        {
            throw (IllegalStateException)(new IllegalStateException("Problem resolving " + resource).initCause(e));
        }
        
        return forPath(files);
    }
    
    public static BindingLoader forClasspath(File[] jarsOrDirs)
    {
        List files = new ArrayList();
        
        try
        {
            for (int i = 0; i < jarsOrDirs.length; i++)
            {
                FileResourceLoader rl = new FileResourceLoader(jarsOrDirs[i]);
                InputStream resource = rl.getResourceAsStream(STANDARD_PATH);
                files.add(BindingFile.forDoc(BindingConfigDocument.Factory.parse(resource)));
            }
        }
        catch (Exception e)
        {
            throw (IllegalStateException)(new IllegalStateException("Problem resolving files").initCause(e));
        }
        
        return forPath(files);
    }
        
}

