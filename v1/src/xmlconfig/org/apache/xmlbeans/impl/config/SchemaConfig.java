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

package org.apache.xmlbeans.impl.config;

import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument.Config;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.Extensionconfig;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.Nsconfig;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.Qnameconfig;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.schema.StscState;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemaConfig
{
    private Map _packageMap;
    private Map _prefixMap;
    private Map _suffixMap;
    private Map _packageMapByUriPrefix; // uri prefix -> package
    private Map _prefixMapByUriPrefix;  // uri prefix -> name prefix
    private Map _suffixMapByUriPrefix;  // uri prefix -> name suffix
    private Map _qnameMap;
    private ExtensionHolder _extensionHolder;

    private SchemaConfig()
    {
        _packageMap = Collections.EMPTY_MAP;
        _prefixMap = Collections.EMPTY_MAP;
        _suffixMap = Collections.EMPTY_MAP;
        _packageMapByUriPrefix = Collections.EMPTY_MAP;
        _prefixMapByUriPrefix = Collections.EMPTY_MAP;
        _suffixMapByUriPrefix = Collections.EMPTY_MAP;
        _qnameMap = Collections.EMPTY_MAP;
        _extensionHolder = null;
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
        _packageMapByUriPrefix = new LinkedHashMap();
        _prefixMapByUriPrefix = new LinkedHashMap();
        _suffixMapByUriPrefix = new LinkedHashMap();
        _qnameMap = new LinkedHashMap();
        _extensionHolder = new ExtensionHolder();

        for (int i = 0; i < configs.length; i++)
        {
            Config config = configs[i];
            Nsconfig[] nsa = config.getNamespaceArray();
            for (int j = 0; j < nsa.length; j++)
            {
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getPackage(), _packageMap);
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getPrefix(), _prefixMap);
                recordNamespaceSetting(nsa[j].getUri(), nsa[j].getSuffix(), _suffixMap);
                recordNamespacePrefixSetting(nsa[j].getUriprefix(), nsa[j].getPackage(), _packageMapByUriPrefix);
                recordNamespacePrefixSetting(nsa[j].getUriprefix(), nsa[j].getPrefix(), _prefixMapByUriPrefix);
                recordNamespacePrefixSetting(nsa[j].getUriprefix(), nsa[j].getSuffix(), _suffixMapByUriPrefix);
            }
            
            Qnameconfig[] qnc = config.getQnameArray();
            for (int j = 0; j < qnc.length; j++)
            {
                _qnameMap.put(qnc[j].getName(), qnc[j].getJavaname());
            }

            Extensionconfig[] ext = config.getExtensionArray();
            for (int j = 0; j < ext.length; j++)
            {
                recordExtensionSetting(ext[j]);
            }
        }

        _extensionHolder.secondPhaseValidation();
        //todo _extensionHolder.normalize();
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

    private static void recordNamespacePrefixSetting(List list, String value, Map result)
    {
        if (value == null)
            return;
        else if (list == null)
            return;
        for (Iterator i = list.iterator(); i.hasNext(); )
        {
            result.put(i.next(), value);
        }
    }

    private void recordExtensionSetting(Extensionconfig ext)
    {
        NameSet xbeanSet = null;
        Object key = ext.getFor();


        if (key instanceof String && "*".equals(key))
            xbeanSet = NameSet.EVERYTHING;
        else if (key instanceof List)
        {
            NameSetBuilder xbeanSetBuilder = new NameSetBuilder();
            for (Iterator i = ((List)key).iterator(); i.hasNext(); )
            {
                String xbeanName = (String)i.next();
                xbeanSetBuilder.add(xbeanName);
            }
            xbeanSet = xbeanSetBuilder.toNameSet();
        }

        if (xbeanSet==null)
            error("Invalid value of attribute 'for' : '" + key + "'.", ext);

        Extensionconfig.Interface[] intfXO = ext.getInterfaceArray();

        for (int i = 0; i < intfXO.length; i++)
        {
            _extensionHolder.addInterfaceExtension(InterfaceExtension.newInstance(xbeanSet, intfXO[i]));
        }

        _extensionHolder.addPrePostExtension(PrePostExtension.newInstance(xbeanSet, ext.getPrePostSet()));
    }


    private String lookup(Map map, Map mapByUriPrefix, String uri)
    {
        if (uri == null)
            uri = "";
        String result = (String)map.get(uri);
        if (result != null)
            return result;
        if (mapByUriPrefix != null)
        {
            result = lookupByUriPrefix(mapByUriPrefix, uri);
            if (result != null)
                return result;
        }

        return (String)map.get("##any");
    }

    private String lookupByUriPrefix(Map mapByUriPrefix, String uri)
    {
        if (uri == null)
            return null;
        if (!mapByUriPrefix.isEmpty())
        {
            String uriprefix = null;
            Iterator i = mapByUriPrefix.keySet().iterator();
            while (i.hasNext())
            {
                String nextprefix = (String)i.next();
                if (uriprefix != null && nextprefix.length() < uriprefix.length())
                    continue;
                if (uri.startsWith(nextprefix))
                    uriprefix = nextprefix;
            }

            if (uriprefix != null)
                return (String)mapByUriPrefix.get(uriprefix);
        }
        return null;
    }

    //package methods
    static void warning(String s, XmlObject xo)
    {
        StscState.get().error(s, XmlError.SEVERITY_WARNING, xo);
    }

    static void error(String s, XmlObject xo)
    {
        StscState.get().error(s, XmlError.SEVERITY_ERROR, xo);
    }

    //public methods
    public String lookupPackageForNamespace(String uri)
    {
        return lookup(_packageMap, _packageMapByUriPrefix, uri);
    }

    public String lookupPrefixForNamespace(String uri)
    {
        return lookup(_prefixMap, _prefixMapByUriPrefix, uri);
    }

    public String lookupSuffixForNamespace(String uri)
    {
        return lookup(_suffixMap, _suffixMapByUriPrefix, uri);
    }

    public String lookupJavanameForQName(QName qname)
    {
        return (String)_qnameMap.get(qname);
    }

    public ExtensionHolder extensionHolderFor(String fullJavaName)
    {
        return _extensionHolder.extensionHolderFor(fullJavaName);
    }

    public ExtensionHolder getExtensionHolder()
    {
        return _extensionHolder;
    }
}
