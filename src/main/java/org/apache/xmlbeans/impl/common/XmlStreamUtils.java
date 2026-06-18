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

package org.apache.xmlbeans.impl.common;

import javax.xml.stream.XMLStreamReader;

/**
 * debugging utilities for XmlStreamReader
 */
public final class XmlStreamUtils
{
  public static String printEvent(XMLStreamReader xmlr)
  {
    StringBuilder b = new StringBuilder();
    b.append("EVENT:[")
            .append(xmlr.getLocation().getLineNumber())
            .append("][")
            .append(xmlr.getLocation().getColumnNumber())
            .append("] ")
            .append(getName(xmlr.getEventType()))
            .append(" [");
    switch (xmlr.getEventType()) {
      case XMLStreamReader.START_ELEMENT:
        b.append('<');
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(' ');
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"").append(xmlr.getNamespaceURI(i)).append('\"');
          } else {
            b.append("xmlns:").append(n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append('\"');
          }
        }

        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
          b.append(' ');
          printName(xmlr.getAttributePrefix(i),
                    xmlr.getAttributeNamespace(i),
                    xmlr.getAttributeLocalName(i),
                    b);
          b.append("=\"");
          b.append(xmlr.getAttributeValue(i));
          b.append('\"');
        }

        b.append(">");
        break;
      case XMLStreamReader.END_ELEMENT:
        b.append("</");
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(' ');
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"").append(xmlr.getNamespaceURI(i)).append('\"');
          } else {
            b.append("xmlns:").append(n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append('\"');
          }
        }
        b.append('>');
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
        b.append(target).append(' ').append(data);
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
        b.append(xmlr.getLocalName()).append('=');
        if (xmlr.hasText())
          b.append('[').append(xmlr.getText()).append(']');
        break;
      case XMLStreamReader.START_DOCUMENT:
        b.append("<?xml");
        b.append(" version='").append(xmlr.getVersion()).append('\'');
        b.append(" encoding='").append(xmlr.getCharacterEncodingScheme()).append('\'');
        if (xmlr.isStandalone())
          b.append(" standalone='yes'");
        else
          b.append(" standalone='no'");
        b.append("?>");
        break;

    }
    b.append(']');
    return b.toString();
  }


  private static void printName(String prefix,
                                String uri,
                                String localName,
                                StringBuilder b)
  {
    if (uri != null && !(uri.isEmpty())) b.append("['").append(uri).append("']:");
    if (prefix != null && !(prefix.isEmpty())) b.append(prefix).append(':');
    if (localName != null) b.append(localName);
  }

  private static void printName(XMLStreamReader xmlr, StringBuilder b)
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
    }
    return "UNKNOWN_EVENT_TYPE";
  }

  public static int getType(String val)
  {
      switch (val) {
          case "START_ELEMENT":
              return XMLStreamReader.START_ELEMENT;
          case "SPACE":
              return XMLStreamReader.SPACE;
          case "END_ELEMENT":
              return XMLStreamReader.END_ELEMENT;
          case "PROCESSING_INSTRUCTION":
              return XMLStreamReader.PROCESSING_INSTRUCTION;
          case "CHARACTERS":
              return XMLStreamReader.CHARACTERS;
          case "COMMENT":
              return XMLStreamReader.COMMENT;
          case "START_DOCUMENT":
              return XMLStreamReader.START_DOCUMENT;
          case "END_DOCUMENT":
              return XMLStreamReader.END_DOCUMENT;
          case "ATTRIBUTE":
              return XMLStreamReader.ATTRIBUTE;
          case "DTD":
              return XMLStreamReader.DTD;
          case "CDATA":
              return XMLStreamReader.CDATA;
          case "NAMESPACE":
              return XMLStreamReader.NAMESPACE;
      }
      return -1;
  }


}
