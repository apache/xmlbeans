/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.marshal.util.collections.ArrayIterator;
import org.apache.xmlbeans.impl.marshal.util.collections.EmptyIterator;
import org.apache.xmlbeans.impl.marshal.util.collections.ReflectiveArrayIterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;


final class MarshalResult implements XMLStreamReader
{
    private final MarshallerImpl context;

    //state fields
    private XmlTypeVisitor currVisitor;
    private final Stack visitorStack = new Stack();
    private int currentEventType = XMLStreamReader.START_ELEMENT;
    private boolean initedAttributes = false;


    private static final String ATTRIBUTE_XML_TYPE = "CDATA";

    MarshalResult(RuntimeBindingProperty property, Object obj,
                  MarshallerImpl context)
    {
        currVisitor = createVisitor(property, obj, context);
        this.context = context;
    }

    protected static XmlTypeVisitor createVisitor(RuntimeBindingProperty property,
                                                  Object obj,
                                                  MarshallerImpl context)
    {
        BindingType btype = property.getType();

        //TODO: cleanup instanceof
        if (btype instanceof ByNameBean) {
            return new ByNameTypeVisitor(property, obj, context);
        } else if (btype instanceof SimpleBindingType) {
            return new SimpleTypeVisitor(property, obj, context);
        } else if (btype instanceof BuiltinBindingType) {
            return new SimpleTypeVisitor(property, obj, context);
        }

        throw new AssertionError("UNIMP TYPE: " + btype);
    }

    public Object getProperty(String s)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int next() throws XMLStreamException
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

        return (currentEventType = advanceToNext());
    }


    private int advanceToNext()
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
        context.getNamespaceContext().openScope();
        initedAttributes = false;
    }

    private XmlTypeVisitor popVisitor()
    {
        context.getNamespaceContext().closeScope();
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
        return currVisitor.getAttributeCount();
    }

    public QName getAttributeName(int i)
    {
        initAttributes();
        return currVisitor.getAttributeName(i);
    }

    public String getAttributeNamespace(int i)
    {
        initAttributes();
        return getAttributeName(i).getNamespaceURI();
    }

    public String getAttributeLocalName(int i)
    {
        initAttributes();
        return getAttributeName(i).getLocalPart();
    }

    public String getAttributePrefix(int i)
    {
        initAttributes();
        return getAttributeName(i).getPrefix();
    }

    public String getAttributeType(int i)
    {
        attributeRangeCheck(i);
        return ATTRIBUTE_XML_TYPE;
    }

    public String getAttributeValue(int i)
    {
        initAttributes();
        return currVisitor.getAttributeValue(i);
    }

    public boolean isAttributeSpecified(int i)
    {
        initAttributes();

        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int getNamespaceCount()
    {
        initAttributes();
        return context.getNamespaceContext().getCurrentScopeNamespaceCount();
    }


    public String getNamespacePrefix(int i)
    {
        initAttributes();
        return context.getNamespaceContext().getCurrentScopeNamespacePrefix(i);
    }

    public String getNamespaceURI(int i)
    {
        initAttributes();
        return context.getNamespaceContext().getCurrentScopeNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext()
    {
        return context.getNamespaceContext();
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
        for(int i = 0 ; i < len ; i++) {
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

          final int len = seq.length();
          char[] val = new char[len];
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
            case COMMENT:
            case DTD:
            case ENTITY_REFERENCE:
            case CHARACTERS:
                return true;
            default:
                return false;
        }
    }

    public Location getLocation()
    {
        return EmptyLocation.getInstance();
    }

    public QName getName()
    {
        return currVisitor.getName();
    }

    public String getLocalName()
    {
        return getName().getLocalPart();
    }

    public boolean hasName()
    {
        return ((currentEventType == XMLStreamReader.START_ELEMENT) ||
            (currentEventType == XMLStreamReader.END_ELEMENT));
    }

    public String getNamespaceURI()
    {
        return getName().getNamespaceURI();
    }

    public String getPrefix()
    {
        return getName().getPrefix();
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

    static Iterator getCollectionIterator(Object value)
    {
        //TODO & FIXME: refactor this into seperate classes
        if (value == null) {
            return EmptyIterator.getInstance();
        } else if (value instanceof Collection) {
            return ((Collection)value).iterator();
        } else if (value instanceof Object[]) {
            return new ArrayIterator((Object[])value);
        } else if (value.getClass().isArray()) {
            return new ReflectiveArrayIterator(value);
        } else {
            throw new AssertionError("bad type: " + value.getClass());
        }
    }

    private void initAttributes()
    {
        if (!initedAttributes) {
            currVisitor.initAttributes();
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


}
