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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.util.Collections;
import java.io.File;
import java.io.IOException;

public class PrettyPrinter
{
    public static void main(String[] args)
    {
        CommandLine cl = new CommandLine(args, Collections.singleton("indent"));
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }
        
        if (cl.args().length == 0)
        {
            System.out.println("Pretty prints XML files.");
            System.out.println("Usage: xpretty [switches] file.xml");
            System.out.println("Switches:");
            System.out.println("    -indent #   use the given indent");
            System.out.println("    -license prints license information");
            return;
        }
        
        String indentStr = cl.getOpt("indent");
        int indent;
        if (indentStr == null)
            indent = 2;
        else
            indent = Integer.parseInt(indentStr);
        
        File[] files = cl.getFiles();
        
        for (int i = 0; i < files.length; i++)
        {
            XmlObject doc;
            try
            {
                doc = XmlObject.Factory.parse(files[i], (new XmlOptions()).setLoadLineNumbers());
            }
            catch (Exception e)
            {
                System.err.println(files[i] + " not loadable: " + e.getMessage());
                continue;
            }
            
            try
            {
                doc.save(System.out, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(indent));
            }
            catch (IOException e)
            {
                System.err.println("Unable to pretty print " + files[i] + ": " + e.getMessage());
            }
        }
    }
}
