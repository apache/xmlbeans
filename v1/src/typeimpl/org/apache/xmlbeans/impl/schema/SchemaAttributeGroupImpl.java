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

import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeSystem;

public class SchemaAttributeGroupImpl implements SchemaAttributeGroup
{
    private SchemaTypeSystem _typeSystem;
    private QName _name;
    private XmlObject _parseObject;
    private String _parseTNS;
    private boolean _chameleon;
    private boolean _redefinition;

    public SchemaAttributeGroupImpl(SchemaTypeSystem typeSystem)
    {
        _typeSystem = typeSystem;
    }

    public SchemaAttributeGroupImpl(SchemaTypeSystem typeSystem, QName name)
    {
        this(typeSystem);
        _name = name;
    }

    public void init(QName name, String targetNamespace, boolean chameleon, boolean redefinition, XmlObject x)
    {
        assert _name == null || name.equals( _name );

        _name = name;
        _parseTNS = targetNamespace;
        _chameleon = chameleon;
        _redefinition = redefinition;
        _parseObject = x;
    }

    public SchemaTypeSystem getTypeSystem()
    {
        return _typeSystem;
    }

    public int getComponentType()
        { return SchemaComponent.ATTRIBUTE_GROUP; }

    public QName getName()
        { return _name; }

    public XmlObject getParseObject()
        { return _parseObject; }

    public String getTargetNamespace()
        { return _parseTNS; }

    public String getChameleonNamespace()
        { return _chameleon ? _parseTNS : null; }

    private SchemaAttributeGroup.Ref _selfref = new SchemaAttributeGroup.Ref(this);
    
    public SchemaAttributeGroup.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }

    public boolean isRedefinition()
        { return _redefinition; }
}
