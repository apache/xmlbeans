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

package zipcompare;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;

public class ZipCompare
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: zipcompare [file1] [file2]");
            System.exit(1);
        }

        ZipFile file1;
        try { file1 = new ZipFile(args[0]); }
        catch (IOException e) { System.out.println("Could not open zip file " + args[0] + ": " + e); System.exit(1); return; }

        ZipFile file2;
        try { file2 = new ZipFile(args[1]); }
        catch (IOException e) { System.out.println("Could not open zip file " + args[0] + ": " + e); System.exit(1); return; }

        System.out.println("Comparing " + args[0] + " with " + args[1] + ":");

        Set set1 = new LinkedHashSet();
        for (Enumeration e = file1.entries(); e.hasMoreElements(); )
            set1.add(((ZipEntry)e.nextElement()).getName());

        Set set2 = new LinkedHashSet();
        for (Enumeration e = file2.entries(); e.hasMoreElements(); )
            set2.add(((ZipEntry)e.nextElement()).getName());

        int errcount = 0;
        int filecount = 0;
        for (Iterator i = set1.iterator(); i.hasNext(); )
        {
            String name = (String)i.next();
            if (!set2.contains(name))
            {
                System.out.println(name + " not found in " + args[1]);
                errcount += 1;
                continue;
            }
            try
            {
                set2.remove(name);
                if (!streamsEqual(file1.getInputStream(file1.getEntry(name)), file2.getInputStream(file2.getEntry(name))))
                {
                    System.out.println(name + " does not match");
                    errcount += 1;
                    continue;
                }
            }
            catch (Exception e)
            {
                System.out.println(name + ": IO Error " + e);
                e.printStackTrace();
                errcount += 1;
                continue;
            }
            filecount += 1;
        }
        for (Iterator i = set2.iterator(); i.hasNext(); )
        {
            String name = (String)i.next();
            System.out.println(name + " not found in " + args[0]);
            errcount += 1;
        }
        System.out.println(filecount + " entries matched");
        if (errcount > 0)
        {
            System.out.println(errcount + " entries did not match");
            System.exit(1);
        }
        System.exit(0);
    }

    static boolean streamsEqual(InputStream stream1, InputStream stream2) throws IOException
    {
        byte[] buf1 = new byte[4096];
        byte[] buf2 = new byte[4096];
        boolean done1 = false;
        boolean done2 = false;

        try
        {
        while (!done1)
        {
            int off1 = 0;
            int off2 = 0;

            while (off1 < buf1.length)
            {
                int count = stream1.read(buf1, off1, buf1.length - off1);
                if (count < 0)
                {
                    done1 = true;
                    break;
                }
                off1 += count;
            }
            while (off2 < buf2.length)
            {
                int count = stream2.read(buf2, off2, buf2.length - off2);
                if (count < 0)
                {
                    done2 = true;
                    break;
                }
                off2 += count;
            }
            if (off1 != off2 || done1 != done2)
                return false;
            for (int i = 0; i < off1; i++)
            {
                if (buf1[i] != buf2[i])
                    return false;
            }
        }
        return true;
        }
        finally { stream1.close(); stream2.close(); }
    }
}
