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
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;

final class MarshalResult implements XMLStreamReader
{
    private XmlTypeVisitor currVisitor;
    private final Stack visitorStack = new Stack();
    private final MarshalContext context;
    private int currentEventType = XMLStreamReader.START_ELEMENT;

    MarshalResult(RuntimeBindingProperty property, Object obj,
                  MarshalContext context)
    {
        currVisitor = createVisitor(property, obj, context);
        this.context = context;
    }

    protected static XmlTypeVisitor createVisitor(RuntimeBindingProperty property,
                                                  Object obj,
                                                  MarshalContext context)
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
        int retval = -1;

        final int curr_state = currVisitor.getState();
        switch (curr_state) {
            case XmlTypeVisitor.START:
                {
                    final int next_state = currVisitor.advance();
                    switch (next_state) {
                        case XmlTypeVisitor.CONTENT:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = START_ELEMENT;
                            break;
                        case XmlTypeVisitor.CHARS:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = CHARACTERS;
                            break;
                        case XmlTypeVisitor.END:
                            retval = END_ELEMENT;
                            break;
                        default:
                            throw new AssertionError("bad state: " + curr_state);
                    }
                }
                break;
            case XmlTypeVisitor.CHARS:
                {
                    currVisitor = popVisitor();
                    final int next_state = currVisitor.advance();
                    switch (next_state) {
                        case XmlTypeVisitor.CONTENT:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = START_ELEMENT;
                            break;
                        case XmlTypeVisitor.CHARS:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = CHARACTERS;
                        case XmlTypeVisitor.END:
                            retval = END_ELEMENT;
                            break;
                        default:
                            throw new AssertionError("bad state: " + curr_state);
                    }
                }
                break;
            case XmlTypeVisitor.END:
                {
                    currVisitor = popVisitor();
                    final int next_state = currVisitor.advance();
                    switch (next_state) {
                        case XmlTypeVisitor.CONTENT:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = START_ELEMENT;
                            break;
                        case XmlTypeVisitor.CHARS:
                            pushVisitor(currVisitor);
                            currVisitor = currVisitor.getCurrentChild();
                            retval = CHARACTERS;
                        case XmlTypeVisitor.END:
                            retval = END_ELEMENT;
                            break;
                         default:
                            throw new AssertionError("bad state: " + curr_state);
                    }
                }
                break;
            default:
                throw new AssertionError("unknown state: " + curr_state);
        }

        assert retval != -1;
        currentEventType = retval;
        return retval;
    }

    private void pushVisitor(XmlTypeVisitor v)
    {
        visitorStack.push(v);
        context.getNamespaceContext().openScope();
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

//        return !visitorStack.isEmpty();

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
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isStartElement()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isEndElement()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isCharacters()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isWhiteSpace()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributeValue(String s, String s1)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int getAttributeCount()
    {
        return currVisitor.getAttributeCount();
    }

    public QName getAttributeName(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributeNamespace(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributeLocalName(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributePrefix(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributeType(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getAttributeValue(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isAttributeSpecified(int i)
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int getNamespaceCount()
    {
        return context.getNamespaceContext().getCurrentScopeNamespaceCount();
    }


    public String getNamespacePrefix(int i)
    {
        return context.getNamespaceContext().getCurrentScopeNamespacePrefix(i);
    }

    public String getNamespaceURI(int i)
    {
        return context.getNamespaceContext().getCurrentScopeNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public int getEventType()
    {
        return currentEventType;
    }

    public String getText()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");

    }

    public char[] getTextCharacters()
    {
        CharSequence seq = currVisitor.getCharData();
        return seq.toString().toCharArray();
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2)
        throws XMLStreamException
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
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
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean hasText()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
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
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean isStandalone()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public boolean standaloneSet()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getCharacterEncodingScheme()
    {
        throw new UnsupportedOperationException("UNIMPLEMENTED");
    }

    public String getPITarget()
    {
        throw new IllegalStateException();
    }

    public String getPIData()
    {
        throw new IllegalStateException();
    }


    private void warn(String s)
    {
        System.err.println("WARNING: " + s);
    }
}
