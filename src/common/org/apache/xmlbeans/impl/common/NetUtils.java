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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;


/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id$
 */

public class NetUtils {

    /**
     * Array containing the safe characters set as defined by RFC 1738
     */
    private static BitSet safeCharacters;


    private static final char[] hexadecimal =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'A', 'B', 'C', 'D', 'E', 'F'};

    static {
        safeCharacters = new BitSet(256);
        int i;
        // 'lowalpha' rule
        for (i = 'a'; i <= 'z'; i++) {
            safeCharacters.set(i);
        }
        // 'hialpha' rule
        for (i = 'A'; i <= 'Z'; i++) {
            safeCharacters.set(i);
        }
        // 'digit' rule
        for (i = '0'; i <= '9'; i++) {
            safeCharacters.set(i);
        }

        // 'safe' rule
        safeCharacters.set('$');
        safeCharacters.set('-');
        safeCharacters.set('_');
        safeCharacters.set('.');
        safeCharacters.set('+');

        // 'extra' rule
        safeCharacters.set('!');
        safeCharacters.set('*');
        safeCharacters.set('\'');
        safeCharacters.set('(');
        safeCharacters.set(')');
        safeCharacters.set(',');

        // special characters common to http: file: and ftp: URLs ('fsegment' and 'hsegment' rules)
        safeCharacters.set('/');
        safeCharacters.set(':');
        safeCharacters.set('@');
        safeCharacters.set('&');
        safeCharacters.set('=');
    }

    /**
     * Decode a path.
     *
     * <p>Interprets %XX (where XX is hexadecimal number) as UTF-8 encoded bytes.
     * <p>The validity of the input path is not checked (i.e. characters that were not encoded will
     * not be reported as errors).
     * <p>This method differs from URLDecoder.decode in that it always uses UTF-8 (while URLDecoder
     * uses the platform default encoding, often ISO-8859-1), and doesn't translate + characters to spaces.
     *
     * @param path the path to decode
     * @return the decoded path
     */
    public static String decodePath(String path) {
        StringBuffer translatedPath = new StringBuffer(path.length());
        byte[] encodedchars = new byte[path.length() / 3];
        int i = 0;
        int length = path.length();
        int encodedcharsLength = 0;
        while (i < length) {
            if (path.charAt(i) == '%') {
                // we must process all consecutive %-encoded characters in one go, because they represent
                // an UTF-8 encoded string, and in UTF-8 one character can be encoded as multiple bytes
                while (i < length && path.charAt(i) == '%') {
                    if (i + 2 < length) {
                        try {
                            byte x = (byte)Integer.parseInt(path.substring(i + 1, i + 3), 16);
                            encodedchars[encodedcharsLength] = x;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("NetUtils.decodePath: illegal hex characters in pattern %" + path.substring(i + 1, i + 3));
                        }
                        encodedcharsLength++;
                        i += 3;
                    } else {
                        throw new IllegalArgumentException("NetUtils.decodePath: % character should be followed by 2 hexadecimal characters.");
                    }
                }
                try {
                    String translatedPart = new String(encodedchars, 0, encodedcharsLength, "UTF-8");
                    translatedPath.append(translatedPart);
                } catch (UnsupportedEncodingException e) {
                    // the situation that UTF-8 is not supported is quite theoretical, so throw a runtime exception
                    throw new RuntimeException("Problem in decodePath: UTF-8 encoding not supported.");
                }
                encodedcharsLength = 0;
            } else {
                // a normal character
                translatedPath.append(path.charAt(i));
                i++;
            }
        }
        return translatedPath.toString();
    }

    /**
     * Encode a path as required by the URL specificatin (<a href="http://www.ietf.org/rfc/rfc1738.txt">
     * RFC 1738</a>). This differs from <code>java.net.URLEncoder.encode()</code> which encodes according
     * to the <code>x-www-form-urlencoded</code> MIME format.
     *
     * @param path the path to encode
     * @return the encoded path
     */
    public static String encodePath(String path) {
       // taken from org.apache.catalina.servlets.DefaultServlet ;)

        /**
         * Note: This code portion is very similar to URLEncoder.encode.
         * Unfortunately, there is no way to specify to the URLEncoder which
         * characters should be encoded. Here, ' ' should be encoded as "%20"
         * and '/' shouldn't be encoded.
         */

        int maxBytesPerChar = 10;
        StringBuffer rewrittenPath = new StringBuffer(path.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(buf, "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
            writer = new OutputStreamWriter(buf);
        }

        for (int i = 0; i < path.length(); i++) {
            int c = path.charAt(i);
            if (safeCharacters.get(c)) {
                rewrittenPath.append((char)c);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write(c);
                    writer.flush();
                } catch(IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    byte toEncode = ba[j];
                    rewrittenPath.append('%');
                    int low = (toEncode & 0x0f);
                    int high = ((toEncode & 0xf0) >> 4);
                    rewrittenPath.append(hexadecimal[high]);
                    rewrittenPath.append(hexadecimal[low]);
                }
                buf.reset();
            }
        }

        return rewrittenPath.toString();
    }

    /**
     * Returns the path of the given resource.
     *
     * @param uri The URI of the resource
     * @return the resource path
     */
    public static String getPath(String uri) {
        int i = uri.lastIndexOf('/');
        if (i > -1) {
            return uri.substring(0, i);
        }
        i = uri.indexOf(':');
        return (i > -1) ? uri.substring(i + 1, uri.length()) : "";
    }

    /**
     * Remove path and file information from a filename returning only its
     * extension  component
     *
     * @param uri The filename
     * @return The filename extension (with starting dot!)
     */
    public static String getExtension(String uri) {
        int dot = uri.lastIndexOf('.');
        if (dot > -1) {
            uri = uri.substring(dot);
            int slash = uri.lastIndexOf('/');
            if (slash > -1) {
                return null;
            } else {
                int sharp = uri.lastIndexOf('#');
                if (sharp > -1) {
                    // uri starts with dot already
                    return uri.substring(0, sharp);
                } else {
                    int mark = uri.lastIndexOf('?');
                    if (mark > -1) {
                        // uri starts with dot already
                        return uri.substring(0, mark);
                    } else {
                        return uri;
                    }
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Absolutize a relative resource path on the given absolute base path.
     *
     * @param path The absolute base path
     * @param resource The relative resource path
     * @return The absolutized resource path
     */
    public static String absolutize(String path, String resource) {
        if (path == null || path.length() == 0) {
            // Base path is empty
            return resource;
        }

        if (resource == null || resource.length() == 0) {
            // Resource path is empty
            return path;
        }

        if (resource.charAt(0) == '/') {
            // Resource path is already absolute
            return resource;
        }

        int length = path.length() - 1;
        boolean slash = (path.charAt(length) == '/');

        StringBuffer b = new StringBuffer();
        b.append(path);
        if (!slash) {
            b.append('/');
        }
        b.append(resource);
        return b.toString();
    }

    /**
     * Relativize an absolute resource on a given absolute path.
     *
     * @param path The absolute path
     * @param absoluteResource The absolute resource
     * @return the resource relative to the given path
     */
    public static String relativize(String path, String absoluteResource) {
        if (path == null || "".equals(path)) {
            return absoluteResource;
        }

        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }

        if (absoluteResource.startsWith(path)) {
            // resource is direct descendant
            return absoluteResource.substring(path.length());
        } else {
            // resource is not direct descendant
            int index = StringUtils.matchStrings(path, absoluteResource);
            if (index > 0 && path.charAt(index-1) != '/') {
                index = path.substring(0, index).lastIndexOf('/');
                index++;
            }
            String pathDiff = path.substring(index);
            String resource = absoluteResource.substring(index);
            int levels = StringUtils.count(pathDiff, '/');
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < levels; i++) {
                b.append("../");
            }
            b.append(resource);
            return b.toString();
        }
    }

    /**
     * Normalize a uri containing ../ and ./ paths.
     *
     * @param uri The uri path to normalize
     * @return The normalized uri
     */
    public static String normalize(String uri) {
        String[] dirty = StringUtils.split(uri, '/');
        int length = dirty.length;
        String[] clean = new String[length];

        boolean path;
        boolean finished;
        while (true) {
            path = false;
            finished = true;
            for (int i = 0, j = 0; (i < length) && (dirty[i] != null); i++) {
                if (".".equals(dirty[i])) {
                    // ignore
                } else if ("..".equals(dirty[i])) {
                    clean[j++] = dirty[i];
                    if (path) finished = false;
                } else {
                    if ((i+1 < length) && ("..".equals(dirty[i+1]))) {
                        i++;
                    } else {
                        clean[j++] = dirty[i];
                        path = true;
                    }
                }
            }
            if (finished) {
                break;
            } else {
                dirty = clean;
                clean = new String[length];
            }
        }

        StringBuffer b = new StringBuffer(uri.length());
        for (int i = 0; (i < length) && (clean[i] != null); i++) {
            b.append(clean[i]);
            if ((i+1 < length) && (clean[i+1] != null)) {
                b.append("/");
            }
        }
        return b.toString();
    }

    /**
     * Remove parameters from a uri.
     * Resulting Map will have either String for single value attributes,
     * or String arrays for multivalue attributes.
     *
     * @param uri The uri path to deparameterize.
     * @param parameters The map that collects parameters.
     * @return The cleaned uri
     */
    public static String deparameterize(String uri, Map parameters) {
        int i = uri.lastIndexOf('?');
        if (i == -1) {
            return uri;
        }

        String[] params = StringUtils.split(uri.substring(i + 1), '&');
        for (int j = 0; j < params.length; j++) {
            String p = params[j];
            int k = p.indexOf('=');
            if (k == -1) {
                break;
            }
            String name = p.substring(0, k);
            String value = p.substring(k + 1);
            Object values = parameters.get(name);
            if (values == null) {
                parameters.put(name, value);
            } else if (values.getClass().isArray()) {
                String[] v1 = (String[])values;
                String[] v2 = new String[v1.length + 1];
                System.arraycopy(v1, 0, v2, 0, v1.length);
                v2[v1.length] = value;
                parameters.put(name, v2);
            } else {
                parameters.put(name, new String[]{values.toString(), value});
            }
        }
        return uri.substring(0, i);
    }

    /**
     * Add parameters stored in the Map to the uri string.
     * Map can contain Object values which will be converted to the string,
     * or Object arrays, which will be treated as multivalue attributes.
     *
     * @param uri The uri to add parameters into
     * @param parameters The map containing parameters to be added
     * @return The uri with added parameters
     */
    public static String parameterize(String uri, Map parameters) {
        if (parameters.size() == 0) {
            return uri;
        }

        StringBuffer buffer = new StringBuffer(uri);
        if (uri.indexOf('?') == -1) {
            buffer.append('?');
        } else {
            buffer.append('&');
        }

        for (Iterator i = parameters.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getValue().getClass().isArray()) {
                Object[] value = (Object[])entry.getValue();
                for (int j = 0; j < value.length; j++) {
                    if (j > 0) {
                        buffer.append('&');
                    }
                    buffer.append(entry.getKey());
                    buffer.append('=');
                    buffer.append(value[j]);
                }
            } else {
                buffer.append(entry.getKey());
                buffer.append('=');
                buffer.append(entry.getValue());
            }
            if (i.hasNext()) {
                buffer.append('&');
            }
        }
        return buffer.toString();
    }



    /**
     * Remove any authorisation details from a URI
     */
    public static String removeAuthorisation(String uri) {
        if (uri.indexOf("@")!=-1 && (uri.startsWith("ftp://") || uri.startsWith("http://"))) {
            return uri.substring(0, uri.indexOf(":")+2)+uri.substring(uri.indexOf("@")+1);
        }

        return uri;
    }
}
