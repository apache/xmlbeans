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

public final class FactoryRegistry
{

  private static org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory writeJavaInstanceFactory(JavaInstanceFactory javaInstanceFactory, org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
  {
    return node;
  }

  private static org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory writeParentInstanceFactory(ParentInstanceFactory parentInstanceFactory, org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory) node.changeType(org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory.type);

    node = writeJavaInstanceFactory(parentInstanceFactory, node);

    org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory pifNode =
      (org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory) node;

    if (parentInstanceFactory.getCreateObjectMethod() != null) {
      TypeRegistry.writeMethodName(pifNode.addNewCreateObjectMethod(),
                                   parentInstanceFactory.getCreateObjectMethod());
    }

    return pifNode;
  }

  static void writeAJavaInstanceFactory(final JavaInstanceFactory jif,
                                        org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory jif_node)
  {
    if (jif instanceof ParentInstanceFactory) {
      ParentInstanceFactory pif = (ParentInstanceFactory) jif;
      writeParentInstanceFactory(pif, jif_node);
    } else {
      throw new AssertionError("unknown type: " + jif.getClass());
    }
  }

  public static JavaInstanceFactory forNode(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
  {
    assert node != null;

    if (node instanceof org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory) {
      ParentInstanceFactory pif = new ParentInstanceFactory();
      fillFactoryFromNode(pif, node);
      return pif;
    } else {
      throw new AssertionError("unknown node type " + node.getClass());
    }

  }

  private static void fillFactoryFromNode(ParentInstanceFactory parentInstanceFactory,
                                  org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
  {
    org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory pifNode =
      (org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory) node;
    parentInstanceFactory.setCreateObjectMethod(BindingFileUtils.create(pifNode.getCreateObjectMethod()));
  }

}
