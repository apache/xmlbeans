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

import org.apache.xmlbeans.XmlException;

public interface BindingTypeVisitor
{
    void visit(BuiltinBindingType builtinBindingType)
        throws XmlException;

    void visit(ByNameBean byNameBean)
        throws XmlException;

    void visit(SimpleContentBean simpleContentBean)
        throws XmlException;

    void visit(SimpleBindingType simpleBindingType)
        throws XmlException;

    void visit(JaxrpcEnumType jaxrpcEnumType)
        throws XmlException;

    void visit(SimpleDocumentBinding simpleDocumentBinding)
        throws XmlException;

    void visit(WrappedArrayType wrappedArrayType)
        throws XmlException;

    void visit(SoapArrayType soapArrayType)
        throws XmlException;

    void visit(ListArrayType listArrayType)
        throws XmlException;
}
