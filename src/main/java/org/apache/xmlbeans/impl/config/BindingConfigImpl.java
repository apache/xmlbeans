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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument.Config;
import org.apache.xmlbeans.impl.xb.xmlconfig.*;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

/**
 * An implementation of BindingConfig
 */
public class BindingConfigImpl extends BindingConfig {
    private final Map _packageMap = new LinkedHashMap();
    private final Map _prefixMap = new LinkedHashMap();
    private final Map _suffixMap = new LinkedHashMap();
    // uri prefix -> package
    private final Map<Object, String> _packageMapByUriPrefix = new LinkedHashMap<>();
    // uri prefix -> name prefix
    private final Map<Object, String> _prefixMapByUriPrefix = new LinkedHashMap<>();
    // uri prefix -> name suffix
    private final Map<Object, String> _suffixMapByUriPrefix = new LinkedHashMap<>();
    private final Map<QName, String> _qnameTypeMap = new LinkedHashMap<>();
    private final Map<QName, String> _qnameDocTypeMap = new LinkedHashMap<>();
    private final Map<QName, String> _qnameElemMap = new LinkedHashMap<>();
    private final Map<QName, String> _qnameAttMap = new LinkedHashMap<>();

    private final List<InterfaceExtensionImpl> _interfaceExtensions = new ArrayList<>();
    private final List<PrePostExtensionImpl> _prePostExtensions = new ArrayList<>();
    private final Map<QName, UserTypeImpl> _userTypes = new LinkedHashMap<>();

    public static BindingConfig forConfigDocuments(Config[] configs, File[] javaFiles, File[] classpath) {
        return new BindingConfigImpl(configs, javaFiles, classpath);
    }

    private BindingConfigImpl(Config[] configs, File[] javaFiles, File[] classpath) {
        for (Config config : configs) {
            Nsconfig[] nsa = config.getNamespaceArray();
            for (Nsconfig nsconfig : nsa) {
                recordNamespaceSetting(nsconfig.getUri(), nsconfig.getPackage(), _packageMap);
                recordNamespaceSetting(nsconfig.getUri(), nsconfig.getPrefix(), _prefixMap);
                recordNamespaceSetting(nsconfig.getUri(), nsconfig.getSuffix(), _suffixMap);
                recordNamespacePrefixSetting(nsconfig.getUriprefix(), nsconfig.getPackage(), _packageMapByUriPrefix);
                recordNamespacePrefixSetting(nsconfig.getUriprefix(), nsconfig.getPrefix(), _prefixMapByUriPrefix);
                recordNamespacePrefixSetting(nsconfig.getUriprefix(), nsconfig.getSuffix(), _suffixMapByUriPrefix);
            }

            Qnameconfig[] qnc = config.getQnameArray();
            for (Qnameconfig qnameconfig : qnc) {
                List applyto = qnameconfig.xgetTarget().xgetListValue();
                QName name = qnameconfig.getName();
                String javaname = qnameconfig.getJavaname();
                for (Object o : applyto) {
                    Qnametargetenum a = (Qnametargetenum) o;
                    switch (a.getEnumValue().intValue()) {
                        case Qnametargetenum.INT_TYPE:
                            _qnameTypeMap.put(name, javaname);
                            break;
                        case Qnametargetenum.INT_DOCUMENT_TYPE:
                            _qnameDocTypeMap.put(name, javaname);
                            break;
                        case Qnametargetenum.INT_ACCESSOR_ELEMENT:
                            _qnameElemMap.put(name, javaname);
                            break;
                        case Qnametargetenum.INT_ACCESSOR_ATTRIBUTE:
                            _qnameAttMap.put(name, javaname);
                            break;
                    }
                }
            }

            Extensionconfig[] ext = config.getExtensionArray();
            for (Extensionconfig extensionconfig : ext) {
                recordExtensionSetting(javaFiles, classpath, extensionconfig);
            }

            Usertypeconfig[] utypes = config.getUsertypeArray();
            for (Usertypeconfig utype : utypes) {
                recordUserTypeSetting(javaFiles, classpath, utype);
            }
        }

        secondPhaseValidation();
        //todo normalize();
    }

    void addInterfaceExtension(InterfaceExtensionImpl ext) {
        if (ext == null) {
            return;
        }

        _interfaceExtensions.add(ext);
    }

    void addPrePostExtension(PrePostExtensionImpl ext) {
        if (ext == null) {
            return;
        }

        _prePostExtensions.add(ext);
    }

    void secondPhaseValidation() {
        // validate interface methods collisions
        Map<InterfaceExtension.MethodSignature, InterfaceExtension.MethodSignature> methodSignatures = new HashMap<>();

        for (InterfaceExtensionImpl extension : _interfaceExtensions) {

            InterfaceExtensionImpl.MethodSignatureImpl[] methods = (InterfaceExtensionImpl.MethodSignatureImpl[]) extension.getMethods();
            for (InterfaceExtensionImpl.MethodSignatureImpl ms : methods) {
                if (methodSignatures.containsKey(ms)) {

                    InterfaceExtensionImpl.MethodSignatureImpl ms2 = (InterfaceExtensionImpl.MethodSignatureImpl) methodSignatures.get(ms);
                    if (!ms.getReturnType().equals(ms2.getReturnType())) {
                        BindingConfigImpl.error("Colliding methods '" + ms.getSignature() + "' in interfaces " +
                                                ms.getInterfaceName() + " and " + ms2.getInterfaceName() + ".", null);
                    }

                    return;
                }

                // store it into hashmap
                methodSignatures.put(ms, ms);
            }
        }

        // validate that PrePostExtension-s do not intersect
        for (int i = 0; i < _prePostExtensions.size() - 1; i++) {
            PrePostExtensionImpl a = _prePostExtensions.get(i);
            for (int j = 1; j < _prePostExtensions.size(); j++) {
                PrePostExtensionImpl b = _prePostExtensions.get(j);
                if (a.hasNameSetIntersection(b)) {
                    BindingConfigImpl.error("The applicable domain for handler '" + a.getHandlerNameForJavaSource() +
                                            "' intersects with the one for '" + b.getHandlerNameForJavaSource() + "'.", null);
                }
            }
        }
    }

    private static void recordNamespaceSetting(Object key, String value, Map<Object, String> result) {
        if (value == null) {
            return;
        }
        if (key == null) {
            result.put("", value);
        } else if (key instanceof String && "##any".equals(key)) {
            result.put(key, value);
        } else if (key instanceof List) {
            // map uris to value
            ((List<?>) key).forEach(o -> result.put("##local".equals(o) ? "" : o, value));
        }
    }

    private static void recordNamespacePrefixSetting(List list, String value, Map<Object, String> result) {
        if (value == null) {
            return;
        }
        if (list == null) {
            return;
        }
        list.forEach(o -> result.put(o, value));
    }

    private void recordExtensionSetting(File[] javaFiles, File[] classpath, Extensionconfig ext) {
        NameSet xbeanSet = null;
        Object key = ext.getFor();


        if (key instanceof String && "*".equals(key)) {
            xbeanSet = NameSet.EVERYTHING;
        } else if (key instanceof List) {
            NameSetBuilder xbeanSetBuilder = new NameSetBuilder();
            for (Object o : (List) key) {
                String xbeanName = (String) o;
                xbeanSetBuilder.add(xbeanName);
            }
            xbeanSet = xbeanSetBuilder.toNameSet();
        }

        if (xbeanSet == null) {
            error("Invalid value of attribute 'for' : '" + key + "'.", ext);
        }

        Extensionconfig.Interface[] intfXO = ext.getInterfaceArray();
        Extensionconfig.PrePostSet ppXO = ext.getPrePostSet();

        Parser loader = new Parser(javaFiles, classpath);

        if (intfXO.length > 0 || ppXO != null) {
            for (Extensionconfig.Interface anInterface : intfXO) {
                addInterfaceExtension(InterfaceExtensionImpl.newInstance(loader, xbeanSet, anInterface));
            }

            addPrePostExtension(PrePostExtensionImpl.newInstance(loader, xbeanSet, ppXO));
        }
    }

    private void recordUserTypeSetting(File[] javaFiles, File[] classpath, Usertypeconfig usertypeconfig) {
        Parser loader = new Parser(javaFiles, classpath);
        UserTypeImpl userType = UserTypeImpl.newInstance(loader, usertypeconfig);
        _userTypes.put(userType.getName(), userType);
    }


    private String lookup(Map map, Map mapByUriPrefix, String uri) {
        if (uri == null) {
            uri = "";
        }
        String result = (String) map.get(uri);
        if (result != null) {
            return result;
        }
        if (mapByUriPrefix != null) {
            result = lookupByUriPrefix(mapByUriPrefix, uri);
            if (result != null) {
                return result;
            }
        }

        return (String) map.get("##any");
    }

    private String lookupByUriPrefix(Map mapByUriPrefix, String uri) {
        if (uri == null) {
            return null;
        }
        if (!mapByUriPrefix.isEmpty()) {
            String uriprefix = null;
            for (Object o : mapByUriPrefix.keySet()) {
                String nextprefix = (String) o;
                if (uriprefix != null && nextprefix.length() < uriprefix.length()) {
                    continue;
                }
                if (uri.startsWith(nextprefix)) {
                    uriprefix = nextprefix;
                }
            }

            if (uriprefix != null) {
                return (String) mapByUriPrefix.get(uriprefix);
            }
        }
        return null;
    }

    //package methods
    static void warning(String s, XmlObject xo) {
        StscState.get().error(s, XmlError.SEVERITY_WARNING, xo);
    }

    static void error(String s, XmlObject xo) {
        StscState.get().error(s, XmlError.SEVERITY_ERROR, xo);
    }

    //public methods

    public String lookupPackageForNamespace(String uri) {
        return lookup(_packageMap, _packageMapByUriPrefix, uri);
    }

    public String lookupPrefixForNamespace(String uri) {
        return lookup(_prefixMap, _prefixMapByUriPrefix, uri);
    }

    public String lookupSuffixForNamespace(String uri) {
        return lookup(_suffixMap, _suffixMapByUriPrefix, uri);
    }

    /**
     * @deprecated replaced with {@link #lookupJavanameForQName(QName, int)}
     */
    public String lookupJavanameForQName(QName qname) {
        String result = _qnameTypeMap.get(qname);
        return result != null ? result : _qnameDocTypeMap.get(qname);
    }

    public String lookupJavanameForQName(QName qname, int kind) {
        switch (kind) {
            case QNAME_TYPE:
                return _qnameTypeMap.get(qname);
            case QNAME_DOCUMENT_TYPE:
                return _qnameDocTypeMap.get(qname);
            case QNAME_ACCESSOR_ELEMENT:
                return _qnameElemMap.get(qname);
            case QNAME_ACCESSOR_ATTRIBUTE:
                return _qnameAttMap.get(qname);
        }
        return null;
    }

    public UserType lookupUserTypeForQName(QName qname) {
        return qname == null ? null : _userTypes.get(qname);
    }

    public InterfaceExtension[] getInterfaceExtensions() {
        return _interfaceExtensions.toArray(new InterfaceExtension[0]);
    }

    public InterfaceExtension[] getInterfaceExtensions(String fullJavaName) {
        return _interfaceExtensions.stream().
            filter(i -> i.contains(fullJavaName)).
            toArray(InterfaceExtension[]::new);
    }

    public PrePostExtension[] getPrePostExtensions() {
        return _prePostExtensions.toArray(new PrePostExtension[0]);
    }

    public PrePostExtension getPrePostExtension(String fullJavaName) {
        return _prePostExtensions.stream().
            filter(p -> p.contains(fullJavaName)).
            findFirst().orElse(null);
    }
}
