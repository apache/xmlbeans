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

package org.apache.xmlbeans.xml.stream.events;

import org.apache.xmlbeans.xml.stream.XMLEvent;

public class ElementTypeNames {
  public static String getName(int val) { 
    switch(val) {
    case XMLEvent.XML_EVENT: return ("XML_EVENT");
    case XMLEvent.START_ELEMENT: return ("START_ELEMENT");
    case XMLEvent.END_ELEMENT: return ("END_ELEMENT");
    case XMLEvent.PROCESSING_INSTRUCTION: return ("PROCESSING_INSTRUCTION");
    case XMLEvent.CHARACTER_DATA: return ("CHARACTER_DATA");
    case XMLEvent.COMMENT: return ("COMMENT");
    case XMLEvent.SPACE: return ("SPACE");
    case XMLEvent.NULL_ELEMENT: return ("NULL_ELEMENT");
    case XMLEvent.START_DOCUMENT: return ("START_DOCUMENT");
    case XMLEvent.END_DOCUMENT: return ("END_DOCUMENT");
    case XMLEvent.START_PREFIX_MAPPING: return ("START_PREFIX_MAPPING");
    case XMLEvent.CHANGE_PREFIX_MAPPING: return ("CHANGE_PREFIX_MAPPING");
    case XMLEvent.END_PREFIX_MAPPING: return ("END_PREFIX_MAPPING");
    case XMLEvent.ENTITY_REFERENCE: return ("ENTITY_REFERENCE");
    default: return "";
    }
  }
  public static int getType(String val) {
    if (val.equals("XML_EVENT")) 
      return XMLEvent.XML_EVENT;
    if (val.equals ("START_ELEMENT")) return 
        XMLEvent.START_ELEMENT; 
    if (val.equals ("END_ELEMENT")) return XMLEvent.END_ELEMENT;
    if (val.equals ("PROCESSING_INSTRUCTION"))
      return XMLEvent.PROCESSING_INSTRUCTION; 
    if (val.equals ("CHARACTER_DATA"))
      return XMLEvent.CHARACTER_DATA; 
    if (val.equals ("COMMENT"))
      return XMLEvent.COMMENT; 
    if (val.equals ("SPACE"))
      return XMLEvent.SPACE; 
    if (val.equals ("NULL_ELEMENT"))
      return XMLEvent.NULL_ELEMENT; 
    if (val.equals ("START_DOCUMENT"))
      return XMLEvent.START_DOCUMENT; 
    if (val.equals ("END_DOCUMENT"))
      return XMLEvent.END_DOCUMENT; 
    if (val.equals ("START_PREFIX_MAPPING"))
      return XMLEvent.START_PREFIX_MAPPING; 
    if (val.equals ("CHANGE_PREFIX_MAPPING"))
      return XMLEvent.CHANGE_PREFIX_MAPPING;
    if (val.equals ("ENTITY_REFERENCE"))
      return XMLEvent.ENTITY_REFERENCE; 
    if (val.equals ("END_PREFIX_MAPPING"))
      return XMLEvent.END_PREFIX_MAPPING;

    return XMLEvent.NULL_ELEMENT;
  }
}
