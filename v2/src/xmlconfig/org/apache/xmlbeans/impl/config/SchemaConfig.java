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

package org.apache.xmlbeans.impl.config;

import javax.xml.namespace.QName;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;

import com.bea.x2002.x09.xbean.config.ConfigDocument.Config;
import com.bea.x2002.x09.xbean.config.Nsconfig;
import com.bea.x2002.x09.xbean.config.Qnameconfig;

public class SchemaConfig
{
    private Map _packageMap;
    private Map _prefixMap;
    private Map _suffixMap;
    private Map _qnameMap;

    private SchemaConfig()
    {
        _packageMap = Collections.EMPTY_MAP;
        _prefixMap = Collections.EMPTY_MAP;
        _suffixMap = Collections.EMPTY_MAP;
        _qnameMap = Collections.EMPTY_MAP;
    }
    
    public static SchemaConfig forConfigDocuments(Config[] configs)
    {
        return new SchemaConfig(configs);
    }
    
    private SchemaConfig(Config[] configs)
    {
        _packageMap = new LinkedHashMap();
        _prefixMap = new LinkedHashMap();
        _suffixMap = new LinkedHashMap();
        _qnameMap = new LinkedHashMap();
        for (int i = 0; i < configs.length; i++)
        {
            Config config = configs[i];
            Nsconfig[] nsa = config.getNamespaceArray();
            for (int j = 0; j < nsa.length; j++)
            {
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getPackage(), _packageMap);
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getPrefix(), _prefixMap);
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getSuffix(), _suffixMap);
            }
            
            Qnameconfig[] qnc = config.getQnameArray();
            for (int j = 0; j < qnc.length; j++)
            {
                _qnameMap.put(qnc[j].getName(), qnc[j].getJavaname());
            }
        }
    }

    private static void recordNamespaceSetting(Object key, String value, Map result)
    {
        if (value == null)
            return;
        else if (key == null)
            result.put("", value);
        else if (key instanceof String && "##any".equals(key))
            result.put(key, value);
        else if (key instanceof List)
        {
            for (Iterator i = ((List)key).iterator(); i.hasNext(); )
            {
                String uri = (String)i.next();
                if ("##local".equals(uri))
                    uri = "";
                result.put(uri, value);
            }
        }
    }
    
    private String lookup(Map map, String uri)
    {
        if (uri == null)
            uri = "";
        String result = (String)map.get(uri);
        if (result != null)
            return result;
        return (String)map.get("##any");
    }

    public String lookupPackageForNamespace(String uri)
    {
        return lookup(_packageMap, uri);
    }

    public String lookupPrefixForNamespace(String uri)
    {
        return lookup(_prefixMap, uri);
    }

    public String lookupSuffixForNamespace(String uri)
    {
        return lookup(_suffixMap, uri);
    }

    public String lookupJavanameForQName(QName qname)
    {
        return (String)_qnameMap.get(qname);
    }
}
