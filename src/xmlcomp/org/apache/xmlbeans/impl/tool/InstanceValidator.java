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

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;

public class InstanceValidator
{
    public static void main(String[] args)
    {
        CommandLine cl = new CommandLine(args, Collections.EMPTY_SET);
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }
        if (cl.args().length == 0)
        {
            System.out.println("Validates a schema defintion and instances within the schema.");
            System.out.println("Usage: validate [switches] schema.xsd instance.xml");
            System.out.println("Switches:");
            System.out.println("    -dl    enable network downloads for imports and includes");
            System.out.println("    -nopvr disable particle valid (restriction) rule");
            System.out.println("    -noupa diable unique particle attributeion rule");
            System.out.println("    -license prints license information");
            return;
        }
        
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null)
        {
            CommandLine.printVersion();
            System.exit(0);
            return;
        }

        boolean dl = (cl.getOpt("dl") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);
        
        File[] schemaFiles = cl.filesEndingWith(".xsd");
        File[] instanceFiles = cl.filesEndingWith(".xml");
        
        List sdocs = new ArrayList();
        
        
        for (int i = 0; i < schemaFiles.length; i++)
        {
            try
            {
                sdocs.add(
                    XmlObject.Factory.parse(
                        schemaFiles[i], (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest()));
            }
            catch (Exception e)
            {
                System.err.println( schemaFiles[i] + " not loadable: " + e );
            }
        }

        XmlObject[] schemas = (XmlObject[])sdocs.toArray(new XmlObject[0]);

        SchemaTypeLoader sLoader;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        if (dl)
            schemaOptions.setCompileDownloadUrls();
        if (nopvr)
            schemaOptions.setCompileNoPvrRule();
        if (noupa)
            schemaOptions.setCompileNoUpaRule();
        
        try
        {
            sLoader = XmlBeans.loadXsd(schemas, schemaOptions);
        }
        catch (Exception e)
        {
            if (compErrors.isEmpty() || !(e instanceof XmlException))
            {
                e.printStackTrace(System.err);
            }
            System.out.println("Schema invalid");
            for (Iterator i = compErrors.iterator(); i.hasNext(); )
                System.out.println(i.next());
            return;
        }
        
        for (int i = 0; i < instanceFiles.length; i++)
        {
            XmlObject xobj;
            
            try
            {
                xobj =
                    sLoader.parse( instanceFiles[i], null, (new XmlOptions()).setLoadLineNumbers() );
            }
            catch (Exception e)
            {
                System.err.println(instanceFiles[i] + " not loadable: " + e);
                e.printStackTrace(System.err);
                continue;
            }

            Collection errors = new ArrayList();

            if (xobj.schemaType() == XmlObject.type)
            {
                System.out.println(instanceFiles[i] + " NOT valid.  ");
                System.out.println("  Document type not found." );
            }
            else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
                System.out.println(instanceFiles[i] + " valid.");
            else
            {
                System.out.println(instanceFiles[i] + " NOT valid.");
                for (Iterator it = errors.iterator(); it.hasNext(); )
                {
                    System.out.println(it.next());
                }
            }
        }
    }
}
