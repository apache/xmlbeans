/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.impl.regex.RegularExpression;
import org.apache.xmlbeans.impl.regex.Match;

import java.util.ArrayList;


/**
 * A collection of <code>String</code> handling utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version StringUtils.java,v 1.1 2003/03/09 00:09:43
 */
public class StringUtils {

    /**
     * Counts the occurrence of the given char in the string.
     *
     * @param str The string to be tested
     * @param c the char to be counted
     * @return the occurrence of the character in the string.
     */
    public static int count(String str, char c) {
        int index = 0;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) index++;
        }
        return index;
    }

    /**
     * Matches two strings.
     *
     * @param a The first string
     * @param b The second string
     * @return the index where the two strings stop matching starting from 0
     */
    public static int matchStrings(String a, String b) {
        int i;
        char[] ca = a.toCharArray();
        char[] cb = b.toCharArray();
        int len = ( ca.length < cb.length ) ? ca.length : cb.length;

        for (i = 0; i < len; i++) {
            if (ca[i] != cb[i]) break;
        }

        return i;
    }


    public static boolean matches(String source, String pattern)
    {
        RegularExpression re = new RegularExpression(pattern);
        return re.matches(source);
    }


    /**
     * Splits a string by the delimiter character passed in.  Faster than using patterns.
     */
    public static String[] split(String source, char delim)
    {
        ArrayList result = new ArrayList(3);
        int pos = 0;
        int start = 0;
        int len = source.length();

        while (true)
        {
            while (pos < len && delim == source.charAt(pos))
                pos++;
            if (pos >= len)
                break;
            start = pos;
            while (pos < len && delim != source.charAt(pos))
                pos++;
            result.add(source.substring(start, pos));
        }

        //If no delimiter found, return the whole string
        if (result.size() == 0)
            result.add(source);

        return (String[]) result.toArray(new String[result.size()]);
    }


    /**
     * Behaves exactly like JDK 1.4 String.split()
     */
    public static String[] split(String source, String delimPattern)
    {
        ArrayList result = new ArrayList(3);
        RegularExpression re = new RegularExpression(delimPattern);
        Match matcher = new Match();

        if (re.matches(source, matcher))
        {
            int groups = matcher.getNumberOfGroups();
            int start = 0;
            for (int i = 0; i < groups; i++)
            {
                result.add(source.substring(start, matcher.getBeginning(i)));
                start = matcher.getEnd(i);
            }
            result.add(source.substring(start));
        }
        else
        {
            //If no delimiter found, return the whole string
            result.add(source);
        }

        return (String[]) result.toArray(new String[result.size()]);
    }


    /**
     * Replaces the replacement string in the target StringBuffer using the Match parameter.
     * It assumes that the pattern doesn't have nested captured groups.
     */
    public static void replaceAll(Match matcher, StringBuffer target, String replace)
    {
        int groups = matcher.getNumberOfGroups();
        if (groups == 0)
            return;

        for (int i = 0; i < groups; i++)
            target.replace(matcher.getBeginning(i), matcher.getEnd(i), replace);
    }


    /**
     * Behaves exactly like JDK 1.4's String.replaceAll()
     */
    public static String replaceAll(String source, String pattern, String replace)
    {
        RegularExpression re = new RegularExpression(pattern);
        Match matcher = new Match();

        if (re.matches(source, matcher))
        {
            StringBuffer sb = new StringBuffer(source);
            replaceAll(matcher, sb, replace);

            return new String(sb);
        }

        //nothing to replace
        return source;
    }
}
