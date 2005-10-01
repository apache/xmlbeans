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

import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument.Config;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.apache.xmlbeans.impl.xb.xmlconfig.Nsconfig;
import org.apache.xmlbeans.impl.xb.xmlconfig.Qnameconfig;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.schema.StscState;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * An implementation of BindingConfig
 */
public class BindingConfigImpl extends BindingConfig
{
    private Map _packageMap;
    private Map _prefixMap;
    private Map _suffixMap;
    private Map _packageMapByUriPrefix; // uri prefix -> package
    private Map _prefixMapByUriPrefix;  // uri prefix -> name prefix
    private Map _suffixMapByUriPrefix;  // uri prefix -> name suffix
    private Map _qnameMap;

    private List _interfaceExtensions;
    private List _prePostExtensions;

    private BindingConfigImpl()
    {
        _packageMap = Collections.EMPTY_MAP;
        _prefixMap = Collections.EMPTY_MAP;
        _suffixMap = Collections.EMPTY_MAP;
        _packageMapByUriPrefix = Collections.EMPTY_MAP;
        _prefixMapByUriPrefix = Collections.EMPTY_MAP;
        _suffixMapByUriPrefix = Collections.EMPTY_MAP;
        _qnameMap = Collections.EMPTY_MAP;
        _interfaceExtensions = new ArrayList();
        _prePostExtensions = new ArrayList();
    }

    public static BindingConfig forConfigDocuments(Config[] configs, File[] javaFiles, File[] classpath)
    {
        return new BindingConfigImpl(configs, javaFiles, classpath);
    }

    private BindingConfigImpl(Config[] configs, File[] javaFiles, File[] classpath)
    {
        _packageMap = new LinkedHashMap();
        _prefixMap = new LinkedHashMap();
        _suffixMap = new LinkedHashMap();
        _packageMapByUriPrefix = new LinkedHashMap();
        _prefixMapByUriPrefix = new LinkedHashMap();
        _suffixMapByUriPrefix = new LinkedHashMap();
        _qnameMap = new LinkedHashMap();
        _interfaceExtensions = new ArrayList();
        _prePostExtensions = new ArrayList();

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
                recordExtensionSetting(javaFiles, classpath, ext[j]);
            }
        }

        secondPhaseValidation();
        //todo normalize();
    }

    void addInterfaceExtension(InterfaceExtensionImpl ext)
    {
        if (ext==null)
            return;

        _interfaceExtensions.add(ext);
    }

    void addPrePostExtension(PrePostExtensionImpl ext)
    {
        if (ext==null)
            return;

        _prePostExtensions.add(ext);
    }

    void secondPhaseValidation()
    {
        // validate interface methods collisions
        Map methodSignatures = new HashMap();

        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtensionImpl interfaceExtension = (InterfaceExtensionImpl) _interfaceExtensions.get(i);

            InterfaceExtensionImpl.MethodSignatureImpl[] methods = (InterfaceExtensionImpl.MethodSignatureImpl[])interfaceExtension.getMethods();
            for (int j = 0; j < methods.length; j++)
            {
                InterfaceExtensionImpl.MethodSignatureImpl ms = methods[j];

                if (methodSignatures.containsKey(methods[j]))
                {

                    InterfaceExtensionImpl.MethodSignatureImpl ms2 = (InterfaceExtensionImpl.MethodSignatureImpl) methodSignatures.get(methods[j]);
                    BindingConfigImpl.error("Colliding methods '" + ms.getSignature() + "' in interfaces " +
                        ms.getInterfaceName() + " and " + ms2.getInterfaceName() + ".", null);

                    return;
                }

                // store it into hashmap
                methodSignatures.put(methods[j], methods[j]);
            }
        }

        // validate that PrePostExtension-s do not intersect
        for (int i = 0; i < _prePostExtensions.size() - 1; i++)
        {
            PrePostExtensionImpl a = (PrePostExtensionImpl) _prePostExtensions.get(i);
            for (int j = 1; j < _prePostExtensions.size(); j++)
            {
                PrePostExtensionImpl b = (PrePostExtensionImpl) _prePostExtensions.get(j);
                if (a.hasNameSetIntersection(b))
                    BindingConfigImpl.error("The applicable domain for handler '" + a.getHandlerNameForJavaSource() +
                        "' intersects with the one for '" + b.getHandlerNameForJavaSource() + "'.", null);
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

    private void recordExtensionSetting(File[] javaFiles, File[] classpath, Extensionconfig ext)
    {
        NameSet xbeanSet = null;
        Object key = ext.getFor();


        if (key instanceof String && "*".equals(key))
            xbeanSet = NameSet.EVERYTHING;
        else if (key instanceof List)
        {
            NameSetBuilder xbeanSetBuilder = new NameSetBuilder();
            for (Iterator i = ((List) key).iterator(); i.hasNext();)
            {
                String xbeanName = (String) i.next();
                xbeanSetBuilder.add(xbeanName);
            }
            xbeanSet = xbeanSetBuilder.toNameSet();
        }

        if (xbeanSet == null)
            error("Invalid value of attribute 'for' : '" + key + "'.", ext);

        Extensionconfig.Interface[] intfXO = ext.getInterfaceArray();
        Extensionconfig.PrePostSet ppXO    = ext.getPrePostSet(); 

        if (intfXO.length > 0 || ppXO != null)
        {
            JamClassLoader jamLoader = getJamLoader(javaFiles, classpath);
            for (int i = 0; i < intfXO.length; i++)
            {
                addInterfaceExtension(InterfaceExtensionImpl.newInstance(jamLoader, xbeanSet, intfXO[i]));
            }

            addPrePostExtension(PrePostExtensionImpl.newInstance(jamLoader, xbeanSet, ppXO));
        }
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

    public InterfaceExtension[] getInterfaceExtensions()
    {
        return (InterfaceExtension[])_interfaceExtensions.toArray(new InterfaceExtension[_interfaceExtensions.size()]);
    }

    public InterfaceExtension[] getInterfaceExtensions(String fullJavaName)
    {
        List result = new ArrayList();
        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtensionImpl intfExt = (InterfaceExtensionImpl) _interfaceExtensions.get(i);
            if (intfExt.contains(fullJavaName))
                result.add(intfExt);
        }

        return (InterfaceExtension[])result.toArray(new InterfaceExtension[result.size()]);
    }

    public PrePostExtension[] getPrePostExtensions()
    {
        return (PrePostExtension[])_prePostExtensions.toArray(new PrePostExtension[_prePostExtensions.size()]);
    }

    public PrePostExtension getPrePostExtension(String fullJavaName)
    {
        for (int i = 0; i < _prePostExtensions.size(); i++)
        {
            PrePostExtensionImpl prePostExt = (PrePostExtensionImpl) _prePostExtensions.get(i);
            if (prePostExt.contains(fullJavaName))
                return prePostExt;
        }
        return null;
    }

    private JamClassLoader getJamLoader(File[] javaFiles, File[] classpath)
    {
        JamServiceFactory jf = JamServiceFactory.getInstance();
        JamServiceParams params = jf.createServiceParams();
        params.set14WarningsEnabled(false);
        // BUGBUG(radup) This is here because the above doesn't do the trick
        params.setShowWarnings(false);

        // process the included sources
        if (javaFiles!=null)
            for (int i = 0; i < javaFiles.length; i++)
                params.includeSourceFile(javaFiles[i]);

        //params.setVerbose(DirectoryScanner.class);

        // add the sourcepath and classpath, if specified
        params.addClassLoader(this.getClass().getClassLoader());
        if (classpath != null)
            for (int i = 0; i < classpath.length; i++)
                params.addClasspath(classpath[i]);

        // create service, get classes, return compiler
        JamService service;
        try
        {
            service = jf.createService(params);
        }
        catch (IOException ioe)
        {
            error("Error when accessing .java files.", null);
            return null;
        }

//        JClass[] cls = service.getAllClasses();
//        for (int i = 0; i < cls.length; i++)
//        {
//            JClass cl = cls[i];
//            System.out.println("CL: " + cl + " " + cl.getQualifiedName());
//            JMethod[] methods = cl.getMethods();
//            for (int j = 0; j < methods.length; j++)
//            {
//                JMethod method = methods[j];
//                System.out.println("    " + method.getQualifiedName());
//            }
//        }

        return service.getClassLoader();
    }
}
