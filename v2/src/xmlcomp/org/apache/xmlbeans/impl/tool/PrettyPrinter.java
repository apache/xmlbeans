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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class PrettyPrinter
{
    private static final int DEFAULT_INDENT = 2;

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
            indent = DEFAULT_INDENT;
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

    public static String indent(String xmldoc)
        throws IOException, XmlException
    {
        StringWriter sw = new StringWriter();
        XmlObject doc = XmlObject.Factory.parse(xmldoc, (new XmlOptions()).setLoadLineNumbers());
        doc.save(sw, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(DEFAULT_INDENT));
        sw.close();
        return sw.getBuffer().toString();
    }
}
