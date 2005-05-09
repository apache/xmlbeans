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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.impl.common.XmlErrorContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.config.SchemaConfig;
import org.apache.xmlbeans.impl.config.ExtensionHolder;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlStringImpl;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.util.HexBin;

import java.util.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.io.File;


import javax.xml.namespace.QName;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.xml.sax.EntityResolver;


/**
 * This class represents the state of the SchemaTypeSystemCompiler as it's
 * going.
 */
public class StscState
{
    private String _givenStsName;
    private Collection _errorListener;
    private SchemaTypeSystemImpl _target;
    private SchemaConfig _config;
    private Map _compatMap;
    private boolean _doingDownloads;
    private byte[] _digest = null;
    private boolean _noDigest = false;

    private SchemaTypeLoader _importingLoader;

    private Map _redefinedGlobalTypes        = new LinkedHashMap();
    private Map _redefinedModelGroups        = new LinkedHashMap();
    private Map _redefinedAttributeGroups    = new LinkedHashMap();
        
    private Map _globalTypes        = new LinkedHashMap();
    private Map _globalElements     = new LinkedHashMap();
    private Map _globalAttributes   = new LinkedHashMap();
    private Map _modelGroups        = new LinkedHashMap();
    private Map _attributeGroups    = new LinkedHashMap();
    private Map _documentTypes      = new LinkedHashMap();
    private Map _attributeTypes     = new LinkedHashMap();
    private Map _typesByClassname   = new LinkedHashMap();
    private Map _misspelledNames    = new HashMap();
    private Set _processingGroups   = new LinkedHashSet();
    private Map _idConstraints      = new LinkedHashMap();
    private Set _namespaces         = new HashSet();
    private boolean _noUpa;
    private boolean _noPvr;
    private Set _mdefNamespaces     = buildDefaultMdefNamespaces();
    private EntityResolver _entityResolver;

    private static Set buildDefaultMdefNamespaces()
    {
        // namespaces which are known to appear in WSDLs redundantly
        return new HashSet(
                Arrays.asList( new String[] {
                    "http://www.openuri.org/2002/04/soap/conversation/",
                }));
    }

    /**
     * Used to store the new target namespace for a chameleon
     * included schema.
     */
    public static final Object CHAMELEON_INCLUDE_URI = new Object();

    /**
     * Only constructed via StscState.start().
     */
    private StscState()
    {
    }

    /**
     * Initializer for schematypepath
     */
    public void setImportingTypeLoader(SchemaTypeLoader loader)
    {
        _importingLoader = loader;
    }

    /**
     * Initializer for error handling.
     */
    public void setErrorListener(Collection errorListener)
        { _errorListener = errorListener; }

    /**
     * Passes an error on to the current XmlErrorContext.
     */
    public void error(String message, int code, XmlObject loc)
        { addError(_errorListener, message, code, loc); }

    /**
     * Passes a warning on to the current XmlErrorContext.
     */
    public void warning(String message, int code, XmlObject loc)
    {
        // it's OK for XMLSchema.xsd itself to have reserved type names
        if (code == XmlErrorContext.RESERVED_TYPE_NAME &&
                loc.documentProperties().getSourceName() != null &&
                loc.documentProperties().getSourceName().indexOf("XMLSchema.xsd") > 0)
            return;

        addWarning(_errorListener, message, code, loc);
    }

    /**
     * Passes a warning on to the current XmlErrorContext.
     */
    public void info(String message)
        { addInfo(_errorListener, message); }

    public static void addError(Collection errorListener, String message, int code, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
              message,
              XmlError.SEVERITY_ERROR,
              location);
        errorListener.add(err);
    }

    public static void addError(Collection errorListener, String message, int code, File location)
    {
        XmlError err =
            XmlError.forLocation(
              message,
              XmlError.SEVERITY_ERROR,
              location.toURI().toString(), 0, 0, 0);
        errorListener.add(err);
    }

    public static void addWarning(Collection errorListener, String message, int code, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
              message,
              XmlError.SEVERITY_WARNING,
              location);
        errorListener.add(err);
    }

    public static void addInfo(Collection errorListener, String message)
    {
        XmlError err = XmlError.forMessage(message, XmlError.SEVERITY_INFO);
        errorListener.add(err);
    }
    
    public void setGivenTypeSystemName(String name)
        { _givenStsName = name; }

    /**
     * Initializer for references to the SchemaTypeLoader
     */
    public void setTargetSchemaTypeSystem(SchemaTypeSystemImpl target)
        { _target = target; }
    
    /**
     * Accumulates a schema digest...
     */
    public void addSchemaDigest(byte[] digest)
    {
        if (_noDigest)
            return;
        
        if (digest == null)
        {
            _noDigest = true;
            _digest = null;
            return;
        }
        
        if (_digest == null)
            _digest = new byte[128/8]; // 128 bits.
        int len = _digest.length;
        if (digest.length < len)
            len = digest.length;
        for (int i = 0; i < len; i++)
            _digest[i] ^= digest[i];
    }
    
    /**
     * The SchemaTypeSystem which we're building types on behalf of.
     */
    public SchemaTypeSystemImpl sts()
    {
        if (_target != null)
            return _target;
        
        String name = _givenStsName;
        if (name == null && _digest != null)
            name = "s" + new String(HexBin.encode(_digest));
        
        _target = new SchemaTypeSystemImpl(name);
        return _target;
    }

    /**
     * True if the given URI is a local file
     */
    public boolean shouldDownloadURI(String uriString)
    {
        if (_doingDownloads)
            return true;

        if (uriString == null)
            return false;

        try
        {
            URI uri = new URI(uriString);
            return uri.getScheme().equalsIgnoreCase("file");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Initializer for compatMap.
     */
    public void setOptions(XmlOptions options)
    {
        if (options == null)
        {
            return; // defaults are all false. 
        }
        
        _compatMap = (Map)options.get(XmlOptions.COMPILE_SUBSTITUTE_NAMES);
        _noUpa = options.hasOption(XmlOptions.COMPILE_NO_UPA_RULE) ? true :
                !"true".equals(System.getProperty("xmlbean.uniqueparticleattribution", "true"));
        _noPvr = options.hasOption(XmlOptions.COMPILE_NO_PVR_RULE) ? true :
                !"true".equals(System.getProperty("xmlbean.particlerestriction", "true"));
        _doingDownloads = options.hasOption(XmlOptions.COMPILE_DOWNLOAD_URLS) ? true :
                "true".equals(System.getProperty("xmlbean.downloadurls", "false"));
        _entityResolver = (EntityResolver)options.get(XmlOptions.ENTITY_RESOLVER);
        if (_entityResolver != null)
            _doingDownloads = true;
        
        if (options.hasOption(XmlOptions.COMPILE_MDEF_NAMESPACES))
        {
            _mdefNamespaces.addAll((Collection)options.get(XmlOptions.COMPILE_MDEF_NAMESPACES));
            
            String local = "##local";
            
            if (_mdefNamespaces.contains(local))
            {
                _mdefNamespaces.remove(local);
                _mdefNamespaces.add("");
            }
        }
    }
    
    /**
     * May return null if there is no custom entity resolver.
     */ 
    public EntityResolver getEntityResolver()
    {
        return _entityResolver;
    }
    
    /**
     * True if no unique particle attribution option is set
     */
    public boolean noUpa()
    {
        return _noUpa;
    }
    
    /**
     * True if no particle valid (restriciton) option is set
     */
    public boolean noPvr()
    {
        return _noPvr;
    }
    

    /**
     * Intercepts XML names and translates them
     * through the compat map, if any.
     *
     * Also looks for a default namespace for global definitions.
     */
    private QName compatName(QName name, String chameleonNamespace)
    {
        // first check for a chameleonNamespace namespace
        if (name.getNamespaceURI().length() == 0 && chameleonNamespace != null && chameleonNamespace.length() > 0)
            name = new QName(chameleonNamespace, name.getLocalPart());

        if (_compatMap == null)
            return name;

        QName subst = (QName)_compatMap.get(name);
        if (subst == null)
            return name;
        return subst;
    }

    /**
     * Initializer for the schema config object.
     */
    public void setSchemaConfig(SchemaConfig config)
        throws IllegalArgumentException
    {
        _config = config;
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getPackageOverride(String namespace)
    {
        return _config.lookupPackageForNamespace(namespace);
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getJavaPrefix(String namespace)
    {
        return _config.lookupPrefixForNamespace(namespace);
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getJavaSuffix(String namespace)
    {
        return _config.lookupSuffixForNamespace(namespace);
    }

    /**
     * Looks up configured java name for the given qname.
     */
    public String getJavaname(QName qname)
    {
        return _config.lookupJavanameForQName(qname);
    }

    /**
     * Gets configured extension set, null if javaName is not contained in any extension.
     */
    public ExtensionHolder getExtensionHolder(String javaName)
    {
        return _config.extensionHolderFor(javaName);
    }

    /**
     * Gets configured extension set.
     */
    public ExtensionHolder getExtensionHolder()
    {
        return _config.getExtensionHolder();
    }

    /* SPELLINGS ======================================================*/

    private static String crunchName(QName name)
    {
        // lowercase, and drop namespace.
        return name.getLocalPart().toLowerCase();
    }

    void addSpelling(QName name, SchemaComponent comp)
    {
        _misspelledNames.put(crunchName(name), comp);
    }

    SchemaComponent findSpelling(QName name)
    {
        return (SchemaComponent)_misspelledNames.get(crunchName(name));
    }

    /* NAMESPACES ======================================================*/

    void addNamespace(String targetNamespace)
    {
        _namespaces.add(targetNamespace);
    }

    String[] getNamespaces()
    {
        return (String[])_namespaces.toArray(new String[_namespaces.size()]);
    }

    boolean linkerDefinesNamespace(String namespace)
    {
        return _importingLoader.isNamespaceDefined(namespace);
    }

    /* TYPES ==========================================================*/

    SchemaTypeImpl findGlobalType(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_globalTypes.get(name);
        if (result == null)
            result = (SchemaTypeImpl)_importingLoader.findType(name);
        return result;
    }
    
    SchemaTypeImpl findRedefinedGlobalType(QName name, String chameleonNamespace, QName redefinedName)
    {
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinedName))
        {
            return (SchemaTypeImpl)_redefinedGlobalTypes.get(name);
            // BUGBUG: should also link against _importingLoader.findRedefinedType
        }
        SchemaTypeImpl result = (SchemaTypeImpl)_globalTypes.get(name);
        if (result == null)
            result = (SchemaTypeImpl)_importingLoader.findType(name);
        return result;
    }

    void addGlobalType(SchemaTypeImpl type, boolean redefined)
    {
        if (type != null)
        {
            QName name = type.getName();
            
            if (redefined)
            {
                if (_redefinedGlobalTypes.containsKey(name))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate global type: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                    _redefinedGlobalTypes.put(name, type);
            }
            else
            {
                if (_globalTypes.containsKey(name))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate global type: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                {
                    _globalTypes.put(name, type);
                    addSpelling(name, type);
                }
            }
        }
    }

    private boolean ignoreMdef(QName name)
    {
        return _mdefNamespaces.contains(name.getNamespaceURI());
    }

    SchemaType[] globalTypes()
        { return (SchemaType[])_globalTypes.values().toArray(new SchemaType[_globalTypes.size()]); }

    SchemaType[] redefinedGlobalTypes()
        { return (SchemaType[])_redefinedGlobalTypes.values().toArray(new SchemaType[_redefinedGlobalTypes.size()]); }
    
    /* DOCUMENT TYPES =================================================*/

    SchemaTypeImpl findDocumentType(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_documentTypes.get(name);
        if (result == null)
            result = (SchemaTypeImpl)_importingLoader.findDocumentType(name);
        return result;
    }

    void addDocumentType(SchemaTypeImpl type, QName name)
    {
        if (_documentTypes.containsKey(name))
        {
            if (!ignoreMdef(name))
                error("Duplicate global element: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_ELEMENT, null);
        }
        else
            _documentTypes.put(name, type);
    }

    SchemaType[] documentTypes()
        { return (SchemaType[])_documentTypes.values().toArray(new SchemaType[_documentTypes.size()]); }

    /* ATTRIBUTE TYPES =================================================*/

    SchemaTypeImpl findAttributeType(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_attributeTypes.get(name);
        if (result == null)
            result = (SchemaTypeImpl)_importingLoader.findAttributeType(name);
        return result;
    }

    void addAttributeType(SchemaTypeImpl type, QName name)
    {
        if (_attributeTypes.containsKey(name))
        {
            if (!ignoreMdef(name))
                error("Duplicate global attribute: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_ATTRIBUTE, null);
        }
        else
            _attributeTypes.put(name, type);
    }

    SchemaType[] attributeTypes()
        { return (SchemaType[])_attributeTypes.values().toArray(new SchemaType[_attributeTypes.size()]); }

    /* ATTRIBUTES =====================================================*/

    SchemaGlobalAttributeImpl findGlobalAttribute(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaGlobalAttributeImpl result = (SchemaGlobalAttributeImpl)_globalAttributes.get(name);
        if (result == null)
            result = (SchemaGlobalAttributeImpl)_importingLoader.findAttribute(name);
        return result;
    }

    void addGlobalAttribute(SchemaGlobalAttributeImpl attribute)
    {
        if (attribute != null)
        {
            QName name = attribute.getName();
            _globalAttributes.put(name, attribute);
            addSpelling(name, attribute);
        }
    }

    SchemaGlobalAttribute[] globalAttributes()
        { return (SchemaGlobalAttribute[])_globalAttributes.values().toArray(new SchemaGlobalAttribute[_globalAttributes.size()]); }

    /* ELEMENTS =======================================================*/

    SchemaGlobalElementImpl findGlobalElement(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaGlobalElementImpl result = (SchemaGlobalElementImpl)_globalElements.get(name);
        if (result == null)
            result = (SchemaGlobalElementImpl)_importingLoader.findElement(name);
        return result;
    }

    void addGlobalElement(SchemaGlobalElementImpl element)
    {
        if (element != null)
        {
            QName name = element.getName();
            _globalElements.put(name, element);
            addSpelling(name, element);
        }
    }

    SchemaGlobalElement[] globalElements()
        { return (SchemaGlobalElement[])_globalElements.values().toArray(new SchemaGlobalElement[_globalElements.size()]); }

    /* ATTRIBUTE GROUPS ===============================================*/

    SchemaAttributeGroupImpl findAttributeGroup(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaAttributeGroupImpl result = (SchemaAttributeGroupImpl)_attributeGroups.get(name);
        if (result == null)
            result = (SchemaAttributeGroupImpl)_importingLoader.findAttributeGroup(name);
        return result;
    }

    SchemaAttributeGroupImpl findRedefinedAttributeGroup(QName name, String chameleonNamespace, QName redefinitionFor)
    {
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinitionFor))
        {
            return (SchemaAttributeGroupImpl)_redefinedAttributeGroups.get(name);
            // BUGBUG: should also link against _importingLoader.findRedefinedAttributeGroup
        }
        SchemaAttributeGroupImpl result = (SchemaAttributeGroupImpl)_attributeGroups.get(name);
        if (result == null)
            result = (SchemaAttributeGroupImpl)_importingLoader.findAttributeGroup(name);
        return result;
    }

    void addAttributeGroup(SchemaAttributeGroupImpl attributeGroup, boolean redefined)
    {
        if (attributeGroup != null)
        {
            QName name = attributeGroup.getName();
            if (redefined)
            {
                if (_redefinedAttributeGroups.containsKey(name))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate attribute group: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                    _redefinedAttributeGroups.put(name, attributeGroup);
                
            }
            else
            {
                if (_attributeGroups.containsKey( name ))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate attribute group: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                {
                    _attributeGroups.put(attributeGroup.getName(), attributeGroup);
                    addSpelling(attributeGroup.getName(), attributeGroup);
                }
            }
        }
    }

    SchemaAttributeGroup[] attributeGroups()
        { return (SchemaAttributeGroup[])_attributeGroups.values().toArray(new SchemaAttributeGroup[_attributeGroups.size()]); }

    SchemaAttributeGroup[] redefinedAttributeGroups()
        { return (SchemaAttributeGroup[])_redefinedAttributeGroups.values().toArray(new SchemaAttributeGroup[_redefinedAttributeGroups.size()]); }

    /* MODEL GROUPS ===================================================*/

    SchemaModelGroupImpl findModelGroup(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaModelGroupImpl result = (SchemaModelGroupImpl)_modelGroups.get(name);
        if (result == null)
            result = (SchemaModelGroupImpl)_importingLoader.findModelGroup(name);
        return result;
    }

    SchemaModelGroupImpl findRedefinedModelGroup(QName name, String chameleonNamespace, QName redefinitionFor)
    {
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinitionFor))
        {
            return (SchemaModelGroupImpl)_redefinedModelGroups.get(name);
            // BUGBUG: should also link against _importingLoader.findRedefinedModelGroup
        }
        SchemaModelGroupImpl result = (SchemaModelGroupImpl)_modelGroups.get(name);
        if (result == null)
            result = (SchemaModelGroupImpl)_importingLoader.findModelGroup(name);
        return result;
    }

    void addModelGroup(SchemaModelGroupImpl modelGroup, boolean redefined)
    {
        if (modelGroup != null)
        {
            QName name = modelGroup.getName();
            if (redefined)
            {
                if (_redefinedModelGroups.containsKey(name))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate model group: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                    _redefinedModelGroups.put(name, modelGroup);
            }
            else
            {
                if (_modelGroups.containsKey(name))
                {
                    if (!ignoreMdef(name))
                        error("Duplicate model group: " + QNameHelper.pretty(name), XmlErrorContext.DUPLICATE_GLOBAL_TYPE, null);
                }
                else
                {
                    _modelGroups.put(modelGroup.getName(), modelGroup);
                    addSpelling(modelGroup.getName(), modelGroup);
                }
            }
        }
    }

    SchemaModelGroup[] modelGroups()
        { return (SchemaModelGroup[])_modelGroups.values().toArray(new SchemaModelGroup[_modelGroups.size()]); }
    
    SchemaModelGroup[] redefinedModelGroups()
        { return (SchemaModelGroup[])_redefinedModelGroups.values().toArray(new SchemaModelGroup[_redefinedModelGroups.size()]); }

    /* IDENTITY CONSTRAINTS ===========================================*/

    SchemaIdentityConstraintImpl findIdConstraint(QName name, String chameleonNamespace)
    {
        name = compatName(name, chameleonNamespace);
        return (SchemaIdentityConstraintImpl)_idConstraints.get(name);
    }

    void addIdConstraint(SchemaIdentityConstraintImpl idc)
    {
        if (idc != null)
        {
            _idConstraints.put(idc.getName(), idc);
            addSpelling(idc.getName(), idc);
        }
    }

    SchemaIdentityConstraintImpl[] idConstraints()
        { return (SchemaIdentityConstraintImpl[])_idConstraints.values().toArray(new SchemaIdentityConstraintImpl[_idConstraints.size()]); }

    /* RECURSION AVOIDANCE ============================================*/
    boolean isProcessing(Object obj)
    {
        return _processingGroups.contains(obj);
    }

    void startProcessing(Object obj)
    {
        assert(!_processingGroups.contains(obj));
        _processingGroups.add(obj);
    }

    void finishProcessing(Object obj)
    {
        assert(_processingGroups.contains(obj));
        _processingGroups.remove(obj);
    }

    Object[] getCurrentProcessing()
    {
        return _processingGroups.toArray();
    }

    /* JAVAIZATION ====================================================*/

    Map typesByClassname()
        { return Collections.unmodifiableMap(_typesByClassname); }

    void addClassname(String classname, SchemaType type)
        { _typesByClassname.put(classname, type); }



    /**
     * Stack management if (heaven help us) we ever need to do
     * nested compilation of schema type system.
     */
    private static final class StscStack
    {
        StscState current;
        ArrayList stack = new ArrayList();
        final StscState push()
        {
            stack.add(current);
            current = new StscState();
            return current;
        }
        final void pop()
        {
            current = (StscState)stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
    }

    private static ThreadLocal tl_stscStack = new ThreadLocal();

    public static StscState start()
    {
        StscStack stscStack = (StscStack) tl_stscStack.get();

        if (stscStack==null)
        {
            stscStack = new StscStack();
            tl_stscStack.set(stscStack);
        }
        return stscStack.push();
    }

    public static StscState get()
    {
        return ((StscStack) tl_stscStack.get()).current;
    }

    public static void end()
    {
        StscStack stscStack = (StscStack) tl_stscStack.get();
        stscStack.pop();
        if (stscStack.stack.size()==0)
            tl_stscStack.set(null);            // this is required to release all the references in this classloader
                                               // which will enable class unloading and avoid OOM in PermGen
    }

    private final static XmlValueRef XMLSTR_PRESERVE = buildString("preserve");
    private final static XmlValueRef XMLSTR_REPLACE = buildString("preserve");
    private final static XmlValueRef XMLSTR_COLLAPSE = buildString("preserve");

    static final SchemaType[] EMPTY_ST_ARRAY = new SchemaType[0];
    static final SchemaType.Ref[] EMPTY_STREF_ARRAY = new SchemaType.Ref[0];

    private final static XmlValueRef[] FACETS_NONE = new XmlValueRef[]
        { null, null, null, null, null, null, null, null, null,
          null, null, null };

    private final static boolean[] FIXED_FACETS_NONE = new boolean[]
        { false, false, false, false, false, false, false, false, false,
          false, false, false };

    private final static XmlValueRef[] FACETS_WS_COLLAPSE = new XmlValueRef[]
        { null, null, null, null, null, null, null, null, null,
          build_wsstring(SchemaType.WS_COLLAPSE), null, null };

    private final static boolean[] FIXED_FACETS_WS = new boolean[]
        { false, false, false, false, false, false, false, false, false,
          true, false, false };

    final static XmlValueRef[] FACETS_UNION = FACETS_NONE;
    final static boolean[] FIXED_FACETS_UNION = FIXED_FACETS_NONE;
    final static XmlValueRef[] FACETS_LIST = FACETS_WS_COLLAPSE;
    final static boolean[] FIXED_FACETS_LIST = FIXED_FACETS_WS;

    static XmlValueRef build_wsstring(int wsr)
    {
        switch (wsr)
        {
            case SchemaType.WS_PRESERVE:
                return XMLSTR_PRESERVE;
            case SchemaType.WS_REPLACE:
                return XMLSTR_REPLACE;
            case SchemaType.WS_COLLAPSE:
                return XMLSTR_COLLAPSE;
        }
        return null;
    }

    static XmlValueRef buildString(String str)
    {
        if (str == null)
            return null;

        try
        {
            XmlStringImpl i = new XmlStringImpl();
            i.set(str);
            i.setImmutable();
            return new XmlValueRef(i);
        }
        catch (XmlValueOutOfRangeException e)
        {
            return null;
        }
    }

    public void notFoundError(QName itemName, int code, XmlObject loc)
    {
        String basicMessage;

        switch (code)
        {
            case XmlErrorContext.TYPE_NOT_FOUND:
                basicMessage = "Type " + QNameHelper.pretty(itemName) + " not found.";
                break;
            case XmlErrorContext.ELEMENT_REF_NOT_FOUND:
                basicMessage = "Element " + QNameHelper.pretty(itemName) + " not found.";
                break;
            case XmlErrorContext.ATTRIBUTE_REF_NOT_FOUND:
                basicMessage = "Attribute " + QNameHelper.pretty(itemName) + " not found.";
                break;
            case XmlErrorContext.MODEL_GROUP_NOT_FOUND:
                basicMessage = "Model group " + QNameHelper.pretty(itemName) + " not found.";
                break;
            case XmlErrorContext.ATTRIBUTE_GROUP_NOT_FOUND:
                basicMessage = "Attribute group " + QNameHelper.pretty(itemName) + " not found.";
                break;
            case XmlErrorContext.IDC_NOT_FOUND:
                basicMessage = "Identity constraint '" + QNameHelper.pretty(itemName) + "' not found.";
                break;
            default:
                assert(false);
                basicMessage = "Definition " + QNameHelper.pretty(itemName) + " not found.";
                break;
        }

        String helpfulMessage = "";
        SchemaComponent foundComponent = findSpelling(itemName);
        QName name;
        if (foundComponent != null)
        {
            name = foundComponent.getName();
            if (name != null)
            {
                String sourceName = null;
                switch (foundComponent.getComponentType())
                {
                    case SchemaComponent.TYPE:
                        sourceName = ((SchemaType)foundComponent).getSourceName();
                        break;
                    case SchemaComponent.ELEMENT:
                        sourceName = ((SchemaGlobalElement)foundComponent).getSourceName();
                        break;
                    case SchemaComponent.ATTRIBUTE:
                        sourceName = ((SchemaGlobalAttribute)foundComponent).getSourceName();
                        break;
                }
                String source = "";
                if (sourceName != null)
                {
                    source = " (in " +  sourceName.substring(sourceName.lastIndexOf('/') + 1) + ")";
                }
                
                if (name.equals(itemName))
                {
                    switch (foundComponent.getComponentType())
                    {
                        case SchemaComponent.TYPE:
                            helpfulMessage = "  Do you mean to refer to the type with that name" + source + "?";
                            break;
                        case SchemaComponent.ELEMENT:
                            helpfulMessage = "  Do you mean to refer to the element with that name" + source + "?";
                            break;
                        case SchemaComponent.ATTRIBUTE:
                            helpfulMessage = "  Do you mean to refer to the attribute with that name" + source + "?";
                            break;
                        case SchemaComponent.ATTRIBUTE_GROUP:
                            helpfulMessage = "  Do you mean to refer to the attribute group with that name" + source + "?";
                            break;
                        case SchemaComponent.MODEL_GROUP:
                            helpfulMessage = "  Do you mean to refer to the model group with that name" + source + "?";
                            break;
                    }
                }
                else
                {
                    switch (foundComponent.getComponentType())
                    {
                        case SchemaComponent.TYPE:
                            helpfulMessage = "  Do you mean to refer to the type named " + QNameHelper.pretty(name) + source + "?";
                            break;
                        case SchemaComponent.ELEMENT:
                            helpfulMessage = "  Do you mean to refer to the element named " + QNameHelper.pretty(name) + source + "?";
                            break;
                        case SchemaComponent.ATTRIBUTE:
                            helpfulMessage = "  Do you mean to refer to the attribute named " + QNameHelper.pretty(name) + source + "?";
                            break;
                        case SchemaComponent.ATTRIBUTE_GROUP:
                            helpfulMessage = "  Do you mean to refer to the attribute group named " + QNameHelper.pretty(name) + source + "?";
                            break;
                        case SchemaComponent.MODEL_GROUP:
                            helpfulMessage = "  Do you mean to refer to the model group named " + QNameHelper.pretty(name) + source + "?";
                            break;
                    }
                }
            }
        }

        error(basicMessage + helpfulMessage, code, loc);
    }

    /**
     * Produces the "sourceName" (to be used within the schema project
     * source file copies) from the URI of the original source.
     *
     * Returns null if none.
     */
    public String sourceNameForUri(String uri)
    {
        return (String)_sourceForUri.get(uri);
    }

    /**
     * Returns the whole sourceCopyMap, mapping URI's that have
     * been read to "sourceName" local names that have been used
     * to tag the types.
     */
    public Map sourceCopyMap()
    {
        return Collections.unmodifiableMap(_sourceForUri);
    }

    /**
     * The base URI to use for nice filenames when saving sources.
     */
    public void setBaseUri(URI uri)
    {
        _baseURI = uri;
    }

    private final static String PROJECT_URL_PREFIX = "project://local";
    
    public String relativize(String uri)
    {
        return relativize(uri, false);
    }
    
    public String computeSavedFilename(String uri)
    {
        return relativize(uri, true);
    }
    
    private String relativize(String uri, boolean forSavedFilename)
    {
        if (uri == null)
            return null;

        // deal with things that do not look like absolute uris
        if (uri.startsWith("/"))
        {
            uri = PROJECT_URL_PREFIX + uri.replace('\\', '/');
        }
        else
        {
            // looks like a URL?
            int colon = uri.indexOf(':');
            if (colon <= 1 || !uri.substring(0, colon).matches("^\\w+$"))
                uri = PROJECT_URL_PREFIX + "/" + uri.replace('\\', '/');
        }

        // now relativize against that...
        if (_baseURI != null)
        {
            try
            {
                URI relative = _baseURI.relativize(new URI(uri));
                if (!relative.isAbsolute())
                    return relative.toString();
                else
                    uri = relative.toString();
            }
            catch (URISyntaxException e)
            {
            }
        }
        
        if (!forSavedFilename)
            return uri;

        int lastslash = uri.lastIndexOf('/');
        String dir = QNameHelper.hexsafe(lastslash == -1 ? "" : uri.substring(0, lastslash));
        return dir + "/" + uri.substring(lastslash + 1);
    }

    /**
     * Notes another URI that has been consumed during compilation
     * (this is the URI that is in the document .NAME property)
     */
    public void addSourceUri(String uri, String nameToUse)
    {
        if (uri == null)
            return;

        if (nameToUse == null)
            nameToUse = computeSavedFilename(uri);

        _sourceForUri.put(uri, nameToUse);
    }

    /**
     * Returns the error listener being filled in during this compilation
     */
    public Collection getErrorListener()
    {
        return _errorListener;
    }

    /**
     * Returns the schema type loader to use for processing s4s
     */
    public SchemaTypeLoader getS4SLoader()
    {
        return _s4sloader;
    }

    Map _sourceForUri = new HashMap();
    URI _baseURI = URI.create(PROJECT_URL_PREFIX + "/");
    SchemaTypeLoader _s4sloader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());
}
