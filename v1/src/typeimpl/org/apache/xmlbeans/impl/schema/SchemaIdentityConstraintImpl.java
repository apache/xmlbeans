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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.XPath;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Collections;

public class SchemaIdentityConstraintImpl implements SchemaIdentityConstraint
{
    private SchemaTypeSystem _typeSystem;
    private String _selector;
    private String[] _fields;
    private SchemaIdentityConstraint.Ref _key;
    private QName _name;
    private int _type;
    private XmlObject _parse;
    private Map _nsMap = Collections.EMPTY_MAP;
    private String _parseTNS;
    private boolean _chameleon;

    // Lazily computed paths
    private volatile XPath _selectorPath;
    private volatile XPath[] _fieldPaths;

    public SchemaIdentityConstraintImpl(SchemaTypeSystem sys) {
        _typeSystem = sys;
    }

    public String getSelector() {
        return _selector;
    }

    public Object getSelectorPath() {
        XPath p = _selectorPath;
        if (p == null) {
            try {
                buildPaths();
                p = _selectorPath;
            }
            catch (XPath.XPathCompileException e) {
                assert false: "Failed to compile xpath. Should be caught by compiler " + e;
                return null;
            }
        }
        return p;
    }

    public void setNSMap(Map nsMap) {
        _nsMap = nsMap;
    }

    public Map getNSMap() {
        return Collections.unmodifiableMap(_nsMap);
    }

    public void setSelector(String selector) {
        assert selector != null;
        _selector = selector;
    }

    public void setFields(String[] fields) {
        assert fields != null && fields.length > 0;
        _fields = fields;
    }

    public String[] getFields() {
        String[] fields = new String[_fields.length];
        System.arraycopy(_fields, 0, fields, 0, fields.length);
        return fields;
    }

    public Object getFieldPath(int index) {
        XPath[] p = _fieldPaths;
        if (p == null) {
            try {
                buildPaths();
                p = _fieldPaths;
            }
            catch (XPath.XPathCompileException e) {
                assert false: "Failed to compile xpath. Should be caught by compiler " + e;
                return null;
            }
        }
        return p[index];
    }

    public void buildPaths() throws XPath.XPathCompileException {
        // TODO: Need the namespace map - requires store support
        _selectorPath = XPath.compileXPath(_selector, _nsMap);

        _fieldPaths = new XPath[_fields.length];
        for (int i = 0 ; i < _fieldPaths.length ; i++)
            _fieldPaths[i] = XPath.compileXPath(_fields[i], _nsMap);
    }

    public void setReferencedKey(SchemaIdentityConstraint.Ref key) {
        _key = key;
    }

    public SchemaIdentityConstraint getReferencedKey() {
        return _key.get();
    }

    public void setConstraintCategory(int type) {
        assert type >= CC_KEY && type <= CC_UNIQUE;
        _type = type;
    }

    public int getConstraintCategory() {
        return _type;
    }

    public void setName(QName name) {
        assert name != null;
        _name = name;
    }

    public QName getName() {
        return _name;
    }

    public int getComponentType() {
        return IDENTITY_CONSTRAINT;
    }

    public SchemaTypeSystem getTypeSystem() {
        return _typeSystem;
    }

    public void setParseContext(XmlObject o, String targetNamespace, boolean chameleon) {
        _parse = o;
        _parseTNS = targetNamespace;
        _chameleon = chameleon;
    }

    public XmlObject getParseObject() {
        return _parse;
    }

    public String getTargetNamespace() {
        return _parseTNS;
    }

    public String getChameleonNamespace() {
        return _chameleon ? _parseTNS : null;
    }


    /**
     * Only applicable to keyrefs. Other types are implicitly resolved.
     */
    public boolean isResolved() {
        return getConstraintCategory() != CC_KEYREF || _key != null;
    }

    private SchemaIdentityConstraint.Ref _selfref = new SchemaIdentityConstraint.Ref(this);

    public SchemaIdentityConstraint.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }
}
