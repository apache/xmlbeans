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

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class StreamInstanceValidator
{
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    public static void printUsage()
    {
        System.out.println("Validates a schema defintion and instances within the schema.");
        System.out.println("Usage: validate [switches] schema.xsd instance.xml");
        System.out.println("Switches:");
        System.out.println("    -dl    enable network downloads for imports and includes");
        System.out.println("    -nopvr disable particle valid (restriction) rule");
        System.out.println("    -noupa diable unique particle attributeion rule");
        System.out.println("    -license prints license information");
    }

    public static void main(String[] args)
    {
        Set flags = new HashSet();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("version");
        flags.add("dl");
        flags.add("noupr");
        flags.add("noupa");

        CommandLine cl = new CommandLine(args, flags, Collections.EMPTY_SET);
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null)
        {
            printUsage();
            System.exit(0);
            return;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0)
        {
            for (int i = 0; i < badopts.length; i++)
                System.out.println("Unrecognized option: " + badopts[i]);
            printUsage();
            System.exit(0);
            return;
        }

        if (cl.getOpt("license") != null) {
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

        if (cl.args().length == 0) {
            printUsage();
            return;
        }

        boolean dl = (cl.getOpt("dl") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);

        File[] schemaFiles = cl.filesEndingWith(".xsd");
        File[] instanceFiles = cl.filesEndingWith(".xml");

        List sdocs = new ArrayList();

        final XmlOptions options = (new XmlOptions()).setLoadLineNumbers();
        for (int i = 0; i < schemaFiles.length; i++) {
            try {
                sdocs.add(
                    XmlObject.Factory.parse(
                        schemaFiles[i], options.setLoadMessageDigest()));
            }
            catch (Exception e) {
                System.err.println(schemaFiles[i] + " not loadable: " + e);
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

        try {
            sLoader = XmlBeans.loadXsd(schemas, schemaOptions);
        }
        catch (Exception e) {
            if (compErrors.isEmpty() || !(e instanceof XmlException)) {
                e.printStackTrace(System.err);
            }
            System.out.println("Schema invalid");
            for (Iterator i = compErrors.iterator(); i.hasNext();)
                System.out.println(i.next());
            return;
        }

        validateFiles(instanceFiles, sLoader, options);

    }

    public static void validateFiles(File[] instanceFiles,
                                     SchemaTypeLoader sLoader,
                                     final XmlOptions options)
    {
        final ValidatingXMLStreamReader vsr = new ValidatingXMLStreamReader();
        final Collection errors = new ArrayList();

        for (int i = 0; i < instanceFiles.length; i++) {
            final File file = instanceFiles[i];
            final String path = file.getPath();
            long time = 0;

            errors.clear();

            try {
                final FileInputStream fis = new FileInputStream(file);
                final XMLStreamReader rdr =
                    XML_INPUT_FACTORY.createXMLStreamReader(path, fis);

                //advance to first start element.
                while(!rdr.isStartElement()) {
                    rdr.next();
                }

                time = System.currentTimeMillis();
                vsr.init(rdr, true, null, sLoader, options, errors);
                
                while (vsr.hasNext()) {
                    vsr.next();
                }

                time = (System.currentTimeMillis() - time);
                vsr.close();
                fis.close();
            }
            catch (XMLStreamException xse) {
                final Location loc = xse.getLocation();
                XmlError e = XmlError.forLocation(xse.getMessage(), path,
                                                  loc.getLineNumber(),
                                                  loc.getColumnNumber(),
                                                  loc.getCharacterOffset());
                errors.add(e);
            }
            catch (Exception e) {
                System.err.println("error for file: " + file + ": " + e);
                e.printStackTrace(System.err);
                continue;
            }


            if (errors.isEmpty()) {
                System.out.println(file + " valid. (" + time + " ms)");
            } else {
                System.out.println(file + " NOT valid (" + time + " ms):");
                for (Iterator it = errors.iterator(); it.hasNext();) {
                    XmlError err = (XmlError)it.next();
                    System.out.println(stringFromError(err, path));
                }
            }

        }
    }

    private static String stringFromError(XmlError err,
                                          final String path)
    {
        String s = XmlError.severityAsString(err.getSeverity()) +
            ": " +
            //err.getSourceName()
            path
            + ":" + err.getLine() + ":" + err.getColumn() + " " +
            err.getMessage() + " ";
        return s;
    }
}
