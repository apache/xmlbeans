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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;


public abstract class PushedInputStream extends InputStream
{
    private static int defaultBufferSize = 2048;
    protected byte buf[];
    protected int writepos;
    protected int readpos;
    protected int markpos = -1;
    protected int marklimit;
    protected OutputStream outputStream = new InternalOutputStream();

    /**
     * Called when more bytes need to be written into this stream
     * (as an OutputStream).
     *
     * This method must write at least one byte if the stream is
     * not ended, and it must not write any bytes if the stream has
     * already ended.
     */
    protected abstract void fill(int requestedBytes) throws IOException;

    /**
     * Returns the linked output stream.
     *
     * This is the output stream that must be written to whenever
     * the fill method is called.
     */
    public final OutputStream getOutputStream()
    {
        return outputStream;
    }

    public PushedInputStream()
    {
        this(defaultBufferSize);
    }

    public PushedInputStream(int size)
    {
        if (size < 0)
        {
            throw new IllegalArgumentException("Negative initial buffer size");
        }
        buf = new byte[size];
    }

    /**
     * Makes room for cb more bytes of data
     */
    private void shift(int cb)
    {
        int savepos = readpos;
        if (markpos > 0)
        {
            if (readpos - markpos > marklimit)
                markpos = -1;
            else
                savepos = markpos;
        }

        int size = writepos - savepos;

        if (savepos > 0 && buf.length - size >= cb && size <= cb)
        {
            System.arraycopy(buf, savepos, buf, 0, size);
        }
        else
        {
            int newcount = size + cb;
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, savepos, newbuf, 0, size);
            buf = newbuf;
        }

        if (savepos > 0)
        {
            readpos -= savepos;
            if (markpos > 0)
                markpos -= savepos;
            writepos -= savepos;
        }
    }

    public synchronized int read() throws IOException
    {
        if (readpos >= writepos)
        {
            fill(1);
            if (readpos >= writepos)
                return -1;
        }
        return buf[readpos++] & 0xff;
    }

    /**
     * Read characters into a portion of an array, reading from the underlying
     * stream at most once if necessary.
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
        int avail = writepos - readpos;
        if (avail < len)
        {
            fill(len - avail);
            avail = writepos - readpos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(buf, readpos, b, off, cnt);
        readpos += cnt;
        return cnt;
    }

    public synchronized long skip(long n) throws IOException
    {
        if (n <= 0)
            return 0;

        long avail = writepos - readpos;

        if (avail < n)
        {
            // Fill in buffer to save bytes for reset
            long req = n - avail;
            if (req > Integer.MAX_VALUE)
                req = Integer.MAX_VALUE;
            fill((int)req);
            avail = writepos - readpos;
            if (avail <= 0)
                return 0;
        }

        long skipped = (avail < n) ? avail : n;
        readpos += skipped;
        return skipped;
    }

    public synchronized int available()
    {
        return writepos - readpos;
    }

    public synchronized void mark(int readlimit)
    {
        marklimit = readlimit;
        markpos = readpos;
    }

    public synchronized void reset() throws IOException
    {
        if (markpos < 0)
            throw new IOException("Resetting to invalid mark");
        readpos = markpos;
    }

    public boolean markSupported()
    {
        return true;
    }

    private class InternalOutputStream extends OutputStream
    {
        public synchronized void write(int b) throws IOException
        {
            if (writepos + 1 > buf.length)
            {
                shift(1);
            }
            buf[writepos] = (byte)b;
            writepos += 1;
        }

        public synchronized void write(byte b[], int off, int len)
        {
            if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0))
                throw new IndexOutOfBoundsException();
            else if (len == 0)
                return;

            if (writepos + len > buf.length)
                shift(len);

            System.arraycopy(b, off, buf, writepos, len);
            writepos += len;
        }
    }
}
