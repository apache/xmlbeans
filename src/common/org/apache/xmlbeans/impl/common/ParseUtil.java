package org.apache.xmlbeans.impl.common;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 25, 2003
 */
public class ParseUtil
{
    public static String trimInitialPlus(String xml)
    {
        if (xml!=null && xml.charAt(0) == '+') {
            return xml.substring(1);
        } else {
            return xml;
        }
    }
}
