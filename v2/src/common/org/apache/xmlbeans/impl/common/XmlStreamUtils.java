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

package org.apache.xmlbeans.impl.common;

import javax.xml.stream.XMLStreamReader;

/**
 * debugging utilities for XmlStreamReader
 */
public final class XmlStreamUtils
{
  public static String printEvent(XMLStreamReader xmlr)
  {
    StringBuffer b = new StringBuffer();
    b.append("EVENT:[" + xmlr.getLocation().getLineNumber() + "][" +
             xmlr.getLocation().getColumnNumber() + "] ");
    b.append(getName(xmlr.getEventType()));
    b.append(" [");
    switch (xmlr.getEventType()) {
      case XMLStreamReader.START_ELEMENT:
        b.append("<");
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(" ");
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"" + xmlr.getNamespaceURI(i) + "\"");
          } else {
            b.append("xmlns:" + n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append("\"");
          }
        }

        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
          b.append(" ");
          printName(xmlr.getAttributePrefix(i),
                    xmlr.getAttributeNamespace(i),
                    xmlr.getAttributeLocalName(i),
                    b);
          b.append("=\"");
          b.append(xmlr.getAttributeValue(i));
          b.append("\"");
        }

        b.append(">");
        break;
      case XMLStreamReader.END_ELEMENT:
        b.append("</");
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(" ");
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"" + xmlr.getNamespaceURI(i) + "\"");
          } else {
            b.append("xmlns:" + n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append("\"");
          }
        }
        b.append(">");
        break;
      case XMLStreamReader.SPACE:
      case XMLStreamReader.CHARACTERS:
        //b.append(xmlr.getText());
        int start = xmlr.getTextStart();
        int length = xmlr.getTextLength();
        b.append(new String(xmlr.getTextCharacters(),
                            start,
                            length));
        break;
      case XMLStreamReader.PROCESSING_INSTRUCTION:
        String target = xmlr.getPITarget();
        if (target == null) target = "";
        String data = xmlr.getPIData();
        if (data == null) data = "";
        b.append("<?");
        b.append(target + " " + data);
        b.append("?>");
        break;
      case XMLStreamReader.CDATA:
        b.append("<![CDATA[");
        if (xmlr.hasText())
          b.append(xmlr.getText());
        b.append("]]>");
        break;

      case XMLStreamReader.COMMENT:
        b.append("<!--");
        if (xmlr.hasText())
          b.append(xmlr.getText());
        b.append("-->");
        break;
      case XMLStreamReader.ENTITY_REFERENCE:
        b.append(xmlr.getLocalName() + "=");
        if (xmlr.hasText())
          b.append("[" + xmlr.getText() + "]");
        break;
      case XMLStreamReader.START_DOCUMENT:
        b.append("<?xml");
        b.append(" version='" + xmlr.getVersion() + "'");
        b.append(" encoding='" + xmlr.getCharacterEncodingScheme() + "'");
        if (xmlr.isStandalone())
          b.append(" standalone='yes'");
        else
          b.append(" standalone='no'");
        b.append("?>");
        break;

    }
    b.append("]");
    return b.toString();
  }


  private static void printName(String prefix,
                                String uri,
                                String localName,
                                StringBuffer b)
  {
    if (uri != null && !("".equals(uri))) b.append("['" + uri + "']:");
    if (prefix != null) b.append(prefix + ":");
    if (localName != null) b.append(localName);
  }

  private static void printName(XMLStreamReader xmlr, StringBuffer b)
  {
    if (xmlr.hasName()) {
      String prefix = xmlr.getPrefix();
      String uri = xmlr.getNamespaceURI();
      String localName = xmlr.getLocalName();
      printName(prefix, uri, localName, b);
    }
  }

  public static String getName(int eventType)
  {
    switch (eventType) {
      case XMLStreamReader.START_ELEMENT:
        return "START_ELEMENT";
      case XMLStreamReader.END_ELEMENT:
        return "END_ELEMENT";
      case XMLStreamReader.PROCESSING_INSTRUCTION:
        return "PROCESSING_INSTRUCTION";
      case XMLStreamReader.CHARACTERS:
        return "CHARACTERS";
      case XMLStreamReader.SPACE:
        return "SPACE";
      case XMLStreamReader.COMMENT:
        return "COMMENT";
      case XMLStreamReader.START_DOCUMENT:
        return "START_DOCUMENT";
      case XMLStreamReader.END_DOCUMENT:
        return "END_DOCUMENT";
      case XMLStreamReader.ENTITY_REFERENCE:
        return "ENTITY_REFERENCE";
      case XMLStreamReader.ATTRIBUTE:
        return "ATTRIBUTE";
      case XMLStreamReader.DTD:
        return "DTD";
      case XMLStreamReader.CDATA:
        return "CDATA";
      case XMLStreamReader.NAMESPACE:
        return "NAMESPACE";
      case XMLStreamReader.START_ENTITY:
        return "START_ENTITY";
      case XMLStreamReader.END_ENTITY:
        return "END_ENTITY";

    }
    return "UNKNOWN_EVENT_TYPE";
  }

  public static int getType(String val)
  {
    if (val.equals("START_ELEMENT"))
      return XMLStreamReader.START_ELEMENT;
    if (val.equals("SPACE"))
      return XMLStreamReader.SPACE;
    if (val.equals("END_ELEMENT"))
      return XMLStreamReader.END_ELEMENT;
    if (val.equals("PROCESSING_INSTRUCTION"))
      return XMLStreamReader.PROCESSING_INSTRUCTION;
    if (val.equals("CHARACTERS"))
      return XMLStreamReader.CHARACTERS;
    if (val.equals("COMMENT"))
      return XMLStreamReader.COMMENT;
    if (val.equals("START_DOCUMENT"))
      return XMLStreamReader.START_DOCUMENT;
    if (val.equals("END_DOCUMENT"))
      return XMLStreamReader.END_DOCUMENT;
    if (val.equals("ATTRIBUTE"))
      return XMLStreamReader.ATTRIBUTE;
    if (val.equals("DTD"))
      return XMLStreamReader.DTD;
    if (val.equals("CDATA"))
      return XMLStreamReader.CDATA;
    if (val.equals("NAMESPACE"))
      return XMLStreamReader.NAMESPACE;
    if (val.equals("START_ENTITY"))
      return XMLStreamReader.START_ENTITY;
    if (val.equals("END_ENTITY"))
      return XMLStreamReader.END_ENTITY;
    return -1;
  }


}
