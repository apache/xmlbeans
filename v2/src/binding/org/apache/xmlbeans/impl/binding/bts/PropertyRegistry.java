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

package org.apache.xmlbeans.impl.binding.bts;

final class PropertyRegistry
{

  public static BindingProperty forNode(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {
    final BindingProperty retval;
    if (node instanceof org.apache.xml.xmlbeans.bindingConfig.QnameProperty) {
      org.apache.xml.xmlbeans.bindingConfig.QnameProperty qnode =
        (org.apache.xml.xmlbeans.bindingConfig.QnameProperty) node;
      QNameProperty qnp = new QNameProperty();
      fillQNamePropertyFromNode(qnp, qnode);
      retval = qnp;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty) {
      org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty qnode =
        (org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty) node;
      SimpleContentProperty scp = new SimpleContentProperty();
      fillSimpleContentPropertyFromNode(scp, qnode);
      retval = scp;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty) {
      org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty qnode =
        (org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty) node;
      GenericXmlProperty scp = new GenericXmlProperty();
      fillGenericXmlPropertyFromNode(scp, qnode);
      retval = scp;
    } else {
      throw new AssertionError("unknown type " + node.getClass());
    }
    return retval;
  }

  private static void fillPropertyFromNode(BindingProperty prop,
                                           org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {
    prop.setTypeName(BindingTypeName.forPair(
      JavaTypeName.forString(node.getJavatype()),
      XmlTypeName.forString(node.getXmlcomponent())));
    prop.setGetter(BindingFileUtils.create(node.getGetter()));
    prop.setSetter(BindingFileUtils.create(node.getSetter()));
    prop.setIssetter(BindingFileUtils.create(node.getIssetter()));
    prop.setField(node.getField());
    String collection = node.getCollection();
    if (collection != null)
      prop.setCollection(JavaTypeName.forString(collection));

    final org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory factory =
      node.getFactory();
    if (factory != null) {
      prop.setJavaInstanceFactory(FactoryRegistry.forNode(factory));
    }
  }

  private static void fillQNamePropertyFromNode(QNameProperty prop,
                                                org.apache.xml.xmlbeans.bindingConfig.QnameProperty node)
  {
    fillPropertyFromNode(prop, node);
    prop.setQName(node.getQname());
    prop.setAttribute(node.getAttribute());
    prop.setMultiple(node.getMultiple());
    prop.setNillable(node.getNillable());
    prop.setOptional(node.getOptional());
    prop.setDefault(node.getDefault());
  }

  private static void fillSimpleContentPropertyFromNode(SimpleContentProperty prop,
                                                        org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty node)
  {
    fillPropertyFromNode(prop, node);
  }

  private static void fillGenericXmlPropertyFromNode(GenericXmlProperty prop,
                                                     org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty node)
  {
    fillPropertyFromNode(prop, node);
  }

  private static org.apache.xml.xmlbeans.bindingConfig.BindingProperty writeProperty(BindingProperty prop,
                                                                                     org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {
    node.setJavatype(prop.getTypeName().getJavaName().toString());
    node.setXmlcomponent(prop.getTypeName().getXmlName().toString());
    if (prop.getFieldName() != null)
      node.setField(prop.getFieldName());
    if (prop.getGetterName() != null) {
      TypeRegistry.writeMethodName(node.addNewGetter(), prop.getGetterName());
    }
    if (prop.getSetterName() != null) {
      TypeRegistry.writeMethodName(node.addNewSetter(), prop.getSetterName());
    }
    if (prop.getIssetterName() != null) {
      TypeRegistry.writeMethodName(node.addNewIssetter(), prop.getIssetterName());
    }
    if (prop.getCollectionClass() != null)
      node.setCollection(prop.getCollectionClass().toString());

    final JavaInstanceFactory jif = prop.getJavaInstanceFactory();
    if (jif != null) {
      org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory jif_node =
        node.addNewFactory();
      FactoryRegistry.writeAJavaInstanceFactory(jif, jif_node);
    }

    return node;
  }

  static org.apache.xml.xmlbeans.bindingConfig.BindingProperty writeQNameProperty(QNameProperty qprop,
                                                                                  org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingProperty) node.changeType(org.apache.xml.xmlbeans.bindingConfig.QnameProperty.type);

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode =
      (org.apache.xml.xmlbeans.bindingConfig.QnameProperty) writeProperty(qprop, node);

    qpNode.setQname(qprop.getQName());
    if (qprop.isAttribute())
      qpNode.setAttribute(true);
    if (qprop.isMultiple())
      qpNode.setMultiple(true);
    if (qprop.isOptional())
      qpNode.setOptional(true);
    if (qprop.isNillable())
      qpNode.setNillable(true);

    return qpNode;
  }


  static org.apache.xml.xmlbeans.bindingConfig.BindingProperty writeSimpleContentProperty(SimpleContentProperty gprop,
                                                                                          org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingProperty) node.changeType(org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty.type);

    org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty gnode =
      (org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty) writeProperty(gprop, node);

    return gnode;
  }

  static org.apache.xml.xmlbeans.bindingConfig.BindingProperty writeGenericXmlProperty(GenericXmlProperty gprop,
                                                                                       org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
  {

    node = (org.apache.xml.xmlbeans.bindingConfig.BindingProperty) node.changeType(org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty.type);

    org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty gnode =
      (org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty) writeProperty(gprop, node);

    return gnode;
  }
}
