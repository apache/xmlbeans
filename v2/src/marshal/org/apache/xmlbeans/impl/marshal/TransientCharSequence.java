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

final class TransientCharSequence
    implements CharSequence
{
    private char[] chars;
    private int count;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    //max buffer size we will reuse.
    private static final int MAX_BUFFER_SIZE = 256 * 1024;

    TransientCharSequence()
    {
        chars = new char[DEFAULT_BUFFER_SIZE];
    }

    public int length()
    {
        return count;
    }

    public char charAt(int index)
    {
        assert index < count;
        assert chars != null;
        return chars[index];
    }

    public CharSequence subSequence(int start, int end)
    {
        assert end < count;
        assert chars != null;
        //we can optimize this later if this method is actually used.
        return new String(chars, 0, count);
    }

    public String toString()
    {
        assert chars != null;
        return new String(chars, 0, count);
    }

    public void append(char[] src, int src_offset, int src_length)
    {
        assert chars != null;
        final int new_len = count + src_length;
        ensureCapacity(new_len);
        System.arraycopy(src, src_offset, chars, count, src_length);
        count = new_len;
    }

    public void clear()
    {
        count = 0;
        if (chars.length > MAX_BUFFER_SIZE) {
            chars = new char[DEFAULT_BUFFER_SIZE];
        }
    }


    private void ensureCapacity(int requested)
    {
        assert requested >= 0;
        
        if (requested < chars.length) return;

        int new_capacity = ((chars.length * 3) + 1) / 2;
        if (requested > new_capacity) {
            new_capacity = requested;
        }

        char new_chars[] = new char[new_capacity];
        System.arraycopy(chars, 0, new_chars, 0, count);
        chars = new_chars;
    }

}
