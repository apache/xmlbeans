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

package org.apache.xmlbeans;

import org.apache.xmlbeans.XmlCursor.XmlBookmark;

/**
 * A subclass of XmlBookmark that holds line number information.
 * If a document is parsed with line numbers
 * enabled, these bookmarks will be placed at appropriate locations
 * within the document.
 * 
 * @see XmlOptions#setLoadLineNumbers 
 */
public class XmlLineNumber extends XmlBookmark
{
    /**
     * Constructs a line number with no column or offset information.
     * @param line the line number - the first line is 1
     */ 
    public XmlLineNumber ( int line ) { this( line, -1, -1 ); }
    
    /**
     * Constructs a line number and column with no file offset information.
     * @param line the line number - the first line is 1
     * @param line the column number - the first column is 1
     */
    public XmlLineNumber ( int line, int column ) { this( line, column, -1 ); }
    
    /**
     * Constructs a line number and column with no file offset information.
     * @param line the line number - the first line is 1
     * @param line the column number - the first column is 1
     * @param line the file character offset - the first character in the file is 0
     */
    public XmlLineNumber ( int line, int column, int offset )
    {
        super( false );
        
        _line = line;
        _column = column;
        _offset = offset;
    }
    
    /**
     * Returns the 1-based line number, or -1 if not known.
     */ 
    public int getLine   ( ) { return _line;   }
    
    /**
     * Returns the 1-based column number, or -1 if not known.
     */ 
    public int getColumn ( ) { return _column; }
    
    /**
     * Returns the 0-based file offset number, or -1 if not known.
     */ 
    public int getOffset ( ) { return _offset; }

    private int _line, _column, _offset;
}
