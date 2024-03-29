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



package xmlcursor.common;



public interface Common {
    String XMLFRAG_BEGINTAG = "<xml-fragment>";
    String XMLFRAG_ENDTAG = "</xml-fragment>";

    String XML_FOO = "<foo></foo>";
    String XML_FOO_1ATTR = "<foo attr0=\"val0\"></foo>";
    String XML_FOO_TEXT = "<foo>text</foo>";
    String XML_FOO_1ATTR_TEXT = "<foo attr0=\"val0\">text</foo>";
    String XML_FOO_2ATTR = "<foo attr0=\"val0\" attr1=\"val1\"></foo>";
    String XML_FOO_2ATTR_TEXT = "<foo attr0=\"val0\" attr1=\"val1\">text</foo>";
    String XML_FOO_5ATTR_TEXT = "<foo attr0=\"val0\" attr1=\"val1\"  attr2=\"val2\"  attr3=\"val3\"  attr4=\"val4\">text</foo>";
    String XML_FOO_BAR = "<foo><bar></bar></foo>";
    String XML_FOO_BAR_TEXT = "<foo><bar>text</bar></foo>";
    String XML_FOO_BAR_TEXT_EXT = "<foo><bar>text</bar>extended</foo>";
    String XML_FOO_BAR_WS_TEXT = "<foo><bar> text </bar> ws \\r\\n </foo>";
    String XML_FOO_BAR_WS_ONLY = "<foo> <bar> </bar> </foo>";
    String XML_FOO_NS = "<foo xmlns=\"http://www.foo.org\"></foo>";
    String XML_FOO_NS_PREFIX = "<foo xmlns:edi='http://ecommerce.org/schema'><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units='Euro'>32.18</edi:price></foo>";
    String XML_FOO_BAR_NESTED_SIBLINGS = "<foo attr0=\"val0\"><bar>text0<zed>nested0</zed></bar><bar>text1<zed>nested1</zed></bar></foo>";
    String XML_FOO_PROCINST = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo>text</foo>";
    String XML_FOO_COMMENT = "<!-- comment text --><foo>text</foo>";
    String XML_FOO_DIGITS = "<foo xmlns=\"http://www.foo.org\" attr0=\"val0\">01234</foo>";
    String XML_TEXT_MIDDLE = "<foo><bar>text</bar>extended<goo>text1</goo></foo>";

    String XML_ATTR_TEXT = "<foo x=\"y\">ab</foo> ";



    String CLM_NS = "http://www.tranxml.org/TranXML/Version4.0";
    String CLM_XSI_NS = "xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\"";
    String CLM_NS_XQUERY_DEFAULT = "declare default element namespace \"" + CLM_NS + "\"; ";

    String TRANXML_FILE_CLM = "xbean/xmlcursor/CarLocationMessage.xml";
    String TRANXML_FILE_XMLCURSOR_PO = "xbean/xmlcursor/po.xml";


    String XML_SCHEMA_TYPE_SUFFIX = "http://www.w3.org/2001/XMLSchema";
    String TRANXML_SCHEMA_TYPE_SUFFIX = CLM_NS;

    static String wrapInXmlFrag(String text) {
        return XMLFRAG_BEGINTAG + text + XMLFRAG_ENDTAG;
    }

}

