/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class XmlEncodingSniffer
{
    private String      _xmlencoding;
    private String      _javaencoding;
    private InputStream _stream;
    private Reader      _reader;

    /**
     * Sniffs the given XML stream for encoding information.
     *
     * After a sniffer is constructed, it can return either a stream
     * (which is a buffered stream wrapper of the original) or a reader
     * (which applies the proper encoding).
     *
     * @param stream           The stream to sniff
     * @param encodingOverride The XML (IANA) name for the overriding encoding
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public XmlEncodingSniffer(InputStream stream, String encodingOverride)
        throws IOException, UnsupportedEncodingException
    {
        _stream = stream;
        
        if (encodingOverride != null)
            _xmlencoding = EncodingMap.getJava2IANAMapping(encodingOverride);

        if (_xmlencoding == null)
            _xmlencoding = encodingOverride;

        if (_xmlencoding == null)
        {
            SniffedXmlInputStream sniffed = new SniffedXmlInputStream(_stream);
            _xmlencoding = sniffed.getXmlEncoding();
            assert(_xmlencoding != null);
            _stream = sniffed;
        }

        _javaencoding = EncodingMap.getIANA2JavaMapping(_xmlencoding);
        
        // we allow you to use Java's encoding names in XML even though you're
        // not supposed to.
        
        if (_javaencoding == null)
            _javaencoding = _xmlencoding;
    }

    /**
     * Sniffs the given XML stream for encoding information.
     *
     * After a sniffer is constructed, it can return either a reader
     * (which is a buffered stream wrapper of the original) or a stream
     * (which applies the proper encoding).
     *
     * @param reader           The reader to sniff
     * @param encodingDefault  The Java name for the default encoding to apply, UTF-8 if null.
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public XmlEncodingSniffer(Reader reader, String encodingDefault)
            throws IOException, UnsupportedEncodingException
    {
        if (encodingDefault == null)
            encodingDefault = "UTF-8";
        
        SniffedXmlReader sniffedReader = new SniffedXmlReader(reader);
        _reader = sniffedReader;
        _xmlencoding = sniffedReader.getXmlEncoding();

        if (_xmlencoding == null)
        {
            _xmlencoding = EncodingMap.getJava2IANAMapping(encodingDefault);
            if (_xmlencoding != null)
                _javaencoding = encodingDefault;
            else
                _xmlencoding = encodingDefault;
        }

        if (_xmlencoding == null)
            _xmlencoding = "UTF-8";
        
        // we allow you to use Java's encoding names in XML even though you're
        // not supposed to.
        
        _javaencoding = EncodingMap.getIANA2JavaMapping(_xmlencoding);
        
        if (_javaencoding == null)
            _javaencoding = _xmlencoding;
    }

    public String getXmlEncoding()
    {
        return _xmlencoding;
    }

    public String getJavaEncoding()
    {
        return _javaencoding;
    }

    public InputStream getStream()
            throws UnsupportedEncodingException
    {
        if (_stream != null)
        {
            InputStream is = _stream;
            _stream = null;
            return is;
        }

        if (_reader != null)
        {
            InputStream is = new ReaderInputStream( _reader, _javaencoding );
            _reader = null;
            return is;
        }

        return null;
    }


    public Reader getReader ( )
        throws UnsupportedEncodingException
    {
        if (_reader != null)
        {
            Reader reader = _reader;
            _reader = null;
            return reader;
        }

        if (_stream != null)
        {
            Reader reader = new InputStreamReader( _stream, _javaencoding );
            _stream = null;
            return reader;
        }

        return null;
    }
}
