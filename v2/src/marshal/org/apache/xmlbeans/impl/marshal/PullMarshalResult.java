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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.JaxrpcEnumType;
import org.apache.xmlbeans.impl.binding.bts.ListArrayType;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.SimpleContentBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.binding.bts.WrappedArrayType;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.marshal.util.AttributeHolder;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;


abstract class PullMarshalResult
    extends MarshalResult
    implements XMLStreamReader
{
    private final BindingTypeVisitor bindingTypeVisitor =
        new BindingTypeVisitor(this);
    private final ScopedNamespaceContext namespaceContext;
    private final Stack visitorStack = new Stack();

    private XmlTypeVisitor currVisitor;
    private int currentEventType = XMLStreamReader.START_ELEMENT;
    private boolean initedAttributes = false;
    private AttributeHolder attributeHolder;

    private static final String ATTRIBUTE_XML_TYPE = "CDATA";


    //TODO: REVIEW: consider ways to reduce the number of parameters here
    PullMarshalResult(BindingLoader loader,
                      RuntimeBindingTypeTable tbl,
                      NamespaceContext root_nsctx,
                      RuntimeBindingProperty property,
                      Object obj,
                      XmlOptions options)
        throws XmlException
    {
        super(loader, tbl, options);
        namespaceContext = new ScopedNamespaceContext(root_nsctx);
        namespaceContext.openScope();
        currVisitor = createInitialVisitor(property, obj);
    }

    //reset to initial state but with new property and object
    protected void reset(RuntimeBindingProperty property, Object obj)
        throws XmlException
    {
        resetPrefixCount();
        namespaceContext.clear();
        namespaceContext.openScope();
        visitorStack.clear();
        currVisitor = createInitialVisitor(property, obj);
        currentEventType = XMLStreamReader.START_ELEMENT;
        initedAttributes = false;
        if (attributeHolder != null) attributeHolder.clear();
    }

    protected XmlTypeVisitor createInitialVisitor(RuntimeBindingProperty property,
                                                  Object obj)
        throws XmlException
    {
        return createVisitor(property, obj);
    }

    protected XmlTypeVisitor createVisitor(RuntimeBindingProperty property,
                                           Object obj)
        throws XmlException
    {
        assert property != null;

        BindingType btype = property.getRuntimeBindingType().getBindingType();
        final BindingTypeVisitor type_visitor = bindingTypeVisitor;
        type_visitor.setParentObject(obj);
        type_visitor.setRuntimeBindingProperty(property);
        btype.accept(type_visitor);
        return type_visitor.getXmlTypeVisitor();
    }


    public Object getProperty(String s)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int next()
        throws XMLStreamException
    {
        switch (currVisitor.getState()) {
            case XmlTypeVisitor.START:
                break;
            case XmlTypeVisitor.CHARS:
            case XmlTypeVisitor.END:
                currVisitor = popVisitor();
                break;
            default:
                throw new AssertionError("invalid: " + currVisitor.getState());
        }

        try {
            return (currentEventType = advanceToNext());
        }
        catch (XmlException e) {
            //TODO: consider passing Location to exception ctor
            XMLStreamException xse = new XMLStreamException(e);
            xse.initCause(e);
            throw xse;
        }
    }

    private int advanceToNext()
        throws XmlException
    {
        final int next_state = currVisitor.advance();
        switch (next_state) {
            case XmlTypeVisitor.CONTENT:
                pushVisitor(currVisitor);
                currVisitor = currVisitor.getCurrentChild();
                return START_ELEMENT;
            case XmlTypeVisitor.CHARS:
                pushVisitor(currVisitor);
                currVisitor = currVisitor.getCurrentChild();
                return CHARACTERS;
            case XmlTypeVisitor.END:
                return END_ELEMENT;
            default:
                throw new AssertionError("bad state: " + next_state);
        }
    }

    private void pushVisitor(XmlTypeVisitor v)
    {
        visitorStack.push(v);
        namespaceContext.openScope();
        initedAttributes = false;
    }

    private XmlTypeVisitor popVisitor()
    {
        namespaceContext.closeScope();
        final XmlTypeVisitor tv = (XmlTypeVisitor)visitorStack.pop();
        return tv;
    }



    public void require(int i, String s, String s1)
        throws XMLStreamException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getElementText() throws XMLStreamException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int nextTag() throws XMLStreamException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean hasNext() throws XMLStreamException
    {
        if (visitorStack.isEmpty()) {
            return (currVisitor.getState() != XmlTypeVisitor.END);
        } else {
            return true;
        }
    }

    public void close() throws XMLStreamException
    {
        //TODO: consider freeing memory
    }

    public String getNamespaceURI(String s)
    {
        if (s == null)
            throw new IllegalArgumentException("prefix cannot be null");

        return getNamespaceContext().getNamespaceURI(s);
    }

    public boolean isStartElement()
    {
        return currentEventType == START_ELEMENT;
    }

    public boolean isEndElement()
    {
        return currentEventType == END_ELEMENT;
    }

    public boolean isCharacters()
    {
        return currentEventType == CHARACTERS;
    }

    public boolean isWhiteSpace()
    {
        if (!isCharacters()) return false;
        CharSequence seq = currVisitor.getCharData();
        return XmlWhitespace.isAllSpace(seq);
    }

    public String getAttributeValue(String uri, String lname)
    {
        initAttributes();

        //TODO: do better than this basic and slow implementation
        for (int i = 0, len = getAttributeCount(); i < len; i++) {
            final QName aname = getAttributeName(i);

            if (lname.equals(aname.getLocalPart())) {
                if (uri == null || uri.equals(aname.getNamespaceURI()))
                    return getAttributeValue(i);
            }
        }
        return null;
    }

    public int getAttributeCount()
    {
        initAttributes();
        if (attributeHolder == null)
            return 0;
        else
            return attributeHolder.getAttributeCount();
    }

    public QName getAttributeName(int i)
    {
        initAttributes();
        assert attributeHolder != null;
        return attributeHolder.getAttributeName(i);
    }

    public String getAttributeNamespace(int i)
    {
        initAttributes();
        assert attributeHolder != null;
        return attributeHolder.getAttributeNamespace(i);
    }

    public String getAttributeLocalName(int i)
    {
        initAttributes();
        assert attributeHolder != null;
        return attributeHolder.getAttributeLocalName(i);
    }

    public String getAttributePrefix(int i)
    {
        initAttributes();
        assert attributeHolder != null;
        return attributeHolder.getAttributePrefix(i);
    }

    public String getAttributeType(int i)
    {
        attributeRangeCheck(i);
        return ATTRIBUTE_XML_TYPE;
    }

    public String getAttributeValue(int i)
    {
        initAttributes();
        assert attributeHolder != null;
        return attributeHolder.getAttributeValue(i);
    }

    public boolean isAttributeSpecified(int i)
    {
        initAttributes();

        assert attributeHolder != null;
        return attributeHolder.isAttributeSpecified(i);
    }

    public int getNamespaceCount()
    {
        initAttributes();
        return namespaceContext.getCurrentScopeNamespaceCount();
    }


    public String getNamespacePrefix(int i)
    {
        initAttributes();
        return namespaceContext.getCurrentScopeNamespacePrefix(i);
    }

    public String getNamespaceURI(int i)
    {
        initAttributes();
        return namespaceContext.getCurrentScopeNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext()
    {
        return namespaceContext;
    }

    protected void bindNamespace(String prefix, String uri)
        throws XmlException
    {
        namespaceContext.bindNamespace(prefix, uri);
    }

    protected void addAttribute(String lname,
                                String value)
        throws XmlException
    {
        addAttribute(null, lname, null, value);
    }

    public int getEventType()
    {
        return currentEventType;
    }

    public String getText()
    {
        CharSequence seq = currVisitor.getCharData();
        return seq.toString();
    }

    public char[] getTextCharacters()
    {
        CharSequence seq = currVisitor.getCharData();
        if (seq instanceof String) {
            return seq.toString().toCharArray();
        }

        final int len = seq.length();
        char[] val = new char[len];
        for (int i = 0; i < len; i++) {
            val[i] = seq.charAt(i);
        }
        return val;
    }


    public int getTextCharacters(int sourceStart,
                                 char[] target,
                                 int targetStart,
                                 int length)
        throws XMLStreamException
    {
        if (length < 0)
            throw new IndexOutOfBoundsException("negative length: " + length);

        if (targetStart < 0)
            throw new IndexOutOfBoundsException("negative targetStart: " + targetStart);

        final int target_length = target.length;
        if (targetStart >= target_length)
            throw new IndexOutOfBoundsException("targetStart(" + targetStart + ") past end of target(" + target_length + ")");

        if ((targetStart + length) > target_length) {
            throw new IndexOutOfBoundsException("insufficient data in target(length is " + target_length + ")");
        }

        CharSequence seq = currVisitor.getCharData();
        if (seq instanceof String) {
            final String s = seq.toString();
            s.getChars(sourceStart, sourceStart + length, target, targetStart);
            return length;
        }

        //TODO: review this code
        int cnt = 0;
        for (int src_idx = sourceStart, dest_idx = targetStart; cnt < length; cnt++) {
            target[dest_idx++] = seq.charAt(src_idx++);
        }
        return cnt;
    }

    public int getTextStart()
    {
        return 0;
    }

    public int getTextLength()
    {
        return currVisitor.getCharData().length();
    }

    public String getEncoding()
    {
        return null;
    }

    public boolean hasText()
    {
        //we'll likely never return some of these but just in case...
        switch (currentEventType) {
            case CHARACTERS:
            case COMMENT:
            case SPACE:
            case ENTITY_REFERENCE:
            case DTD:
                return true;
            default:
                return false;
        }
    }

    public Location getLocation()
    {
        //TODO: something better than this, like give the object instance
        return EmptyLocation.getInstance();
    }

    public QName getName()
    {
        return currVisitor.getName();
    }

    public String getLocalName()
    {
        return currVisitor.getLocalPart();
    }

    public boolean hasName()
    {
        return ((currentEventType == XMLStreamReader.START_ELEMENT) ||
            (currentEventType == XMLStreamReader.END_ELEMENT));
    }

    public String getNamespaceURI()
    {
        return currVisitor.getNamespaceURI();
    }

    public String getPrefix()
    {
        return currVisitor.getPrefix();
    }

    public String getVersion()
    {
        return null;
    }

    public boolean isStandalone()
    {
        return false;
    }

    public boolean standaloneSet()
    {
        return false;
    }

    public String getCharacterEncodingScheme()
    {
        return null;
    }

    public String getPITarget()
    {
        throw new IllegalStateException();
    }

    public String getPIData()
    {
        throw new IllegalStateException();
    }

    protected void initAttributes()
    {
        if (!initedAttributes) {
            try {
                if (attributeHolder != null) {
                    attributeHolder.clear();
                }
                currVisitor.initAttributes();
            }
            catch (XmlException e) {
                //public attribute interfaces of XMLStreamReader
                //force us into this behavior
                throw new XmlRuntimeException(e);
            }
            initedAttributes = true;
        }
    }

    private void attributeRangeCheck(int i)
    {
        final int att_cnt = getAttributeCount();
        if (i >= att_cnt) {
            String msg = "index" + i + " invalid. " +
                " attribute count is " + att_cnt;
            throw new IndexOutOfBoundsException(msg);
        }
    }


    public String toString()
    {
        return "org.apache.xmlbeans.impl.marshal.MarshalResult{" +
            "currentEvent=" + XmlStreamUtils.printEvent(this) +
            ", visitorStack=" + (visitorStack == null ? null : "size:" + visitorStack.size() + visitorStack) +
            ", currVisitor=" + currVisitor +
            "}";
    }

    protected void addAttribute(String namespaceURI,
                                String localPart,
                                String prefix,
                                String value)
    {
        if (attributeHolder == null) {
            attributeHolder = new AttributeHolder();
        }
        attributeHolder.add(namespaceURI, localPart, prefix, value);
    }

    private static final class BindingTypeVisitor
        implements org.apache.xmlbeans.impl.binding.bts.BindingTypeVisitor
    {
        private final PullMarshalResult marshalResult;

        private Object parentObject;
        private RuntimeBindingProperty runtimeBindingProperty;

        private XmlTypeVisitor xmlTypeVisitor;

        public BindingTypeVisitor(PullMarshalResult marshalResult)
        {
            this.marshalResult = marshalResult;
        }

        public void setParentObject(Object parentObject)
        {
            this.parentObject = parentObject;
        }

        public void setRuntimeBindingProperty(RuntimeBindingProperty runtimeBindingProperty)
        {
            this.runtimeBindingProperty = runtimeBindingProperty;
        }

        public XmlTypeVisitor getXmlTypeVisitor()
        {
            return xmlTypeVisitor;
        }

        public void visit(BuiltinBindingType builtinBindingType)
            throws XmlException
        {
            xmlTypeVisitor = new SimpleTypeVisitor(runtimeBindingProperty,
                                                   parentObject,
                                                   marshalResult);
        }

        public void visit(ByNameBean byNameBean)
            throws XmlException
        {
            xmlTypeVisitor = new ByNameTypeVisitor(runtimeBindingProperty,
                                                   parentObject,
                                                   marshalResult);
        }

        public void visit(SimpleContentBean simpleContentBean)
            throws XmlException
        {
            xmlTypeVisitor = new SimpleContentTypeVisitor(runtimeBindingProperty,
                                                          parentObject,
                                                          marshalResult);
        }

        public void visit(SimpleBindingType simpleBindingType)
            throws XmlException
        {
            xmlTypeVisitor = new SimpleTypeVisitor(runtimeBindingProperty,
                                                   parentObject,
                                                   marshalResult);
        }

        public void visit(JaxrpcEnumType jaxrpcEnumType)
            throws XmlException
        {
            xmlTypeVisitor = new SimpleTypeVisitor(runtimeBindingProperty,
                                                   parentObject,
                                                   marshalResult);
        }

        public void visit(SimpleDocumentBinding simpleDocumentBinding)
            throws XmlException
        {
            throw new AssertionError("unexpected type: " + simpleDocumentBinding);
        }

        public void visit(WrappedArrayType wrappedArrayType)
            throws XmlException
        {
            xmlTypeVisitor = new WrappedArrayTypeVisitor(runtimeBindingProperty,
                                                         parentObject,
                                                         marshalResult);
        }

        public void visit(ListArrayType listArrayType)
            throws XmlException
        {
            xmlTypeVisitor = new SimpleTypeVisitor(runtimeBindingProperty,
                                                   parentObject,
                                                   marshalResult);
        }

    }

}
