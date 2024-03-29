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

import org.apache.xmlbeans.*;

import java.io.File;
import java.util.*;

public class InstanceValidator {
    public static void printUsage() {
        System.out.println("Validates the specified instance against the specified schema.");
        System.out.println("Contrast with the svalidate tool, which validates using a stream.");
        System.out.println("Usage: validate [-dl] [-nopvr] [-noupa] [-license] schema.xsd instance.xml");
        System.out.println("Options:");
        System.out.println("    -dl - permit network downloads for imports and includes (default is off)");
        System.out.println("    -noupa - do not enforce the unique particle attribution rule");
        System.out.println("    -nopvr - do not enforce the particle valid (restriction) rule");
        System.out.println("    -strict - performs strict(er) validation");
        System.out.println("    -partial - allow partial schema type system");
        System.out.println("    -license - prints license information");
    }

    public static void main(String[] args) {
        System.exit(extraMain(args));
    }

    /**
     * Use this method to avoid calling {@link java.lang.System#exit(int)}
     *
     * @param args are command line options
     * @return exitCode
     */
    public static int extraMain(String[] args) {
        Set<String> flags = new HashSet<>();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("version");
        flags.add("dl");
        flags.add("noupa");
        flags.add("nopvr");
        flags.add("strict");
        flags.add("partial");

        CommandLine cl = new CommandLine(args, flags, Collections.EMPTY_SET);

        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null || args.length < 1) {
            printUsage();
            return 0;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0) {
            for (int i = 0; i < badopts.length; i++) {
                System.out.println("Unrecognized option: " + badopts[i]);
            }
            printUsage();
            return 0;
        }

        if (cl.getOpt("license") != null) {
            CommandLine.printLicense();
            return 0;
        }

        if (cl.getOpt("version") != null) {
            CommandLine.printVersion();
            return 0;
        }

        if (cl.args().length == 0) {
            return 0;
        }

        boolean dl = (cl.getOpt("dl") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);
        boolean strict = (cl.getOpt("strict") != null);
        boolean partial = (cl.getOpt("partial") != null);

        File[] schemaFiles = cl.filesEndingWith(".xsd");
        File[] instanceFiles = cl.filesEndingWith(".xml");
        File[] jarFiles = cl.filesEndingWith(".jar");

        List<XmlObject> sdocs = new ArrayList<>();


        for (File schemaFile : schemaFiles) {
            try {
                sdocs.add(XmlObject.Factory.parse(schemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest()));
            } catch (Exception e) {
                System.err.println(schemaFile + " not loadable: " + e);
            }
        }

        XmlObject[] schemas = (XmlObject[]) sdocs.toArray(new XmlObject[0]);

        SchemaTypeLoader sLoader = null;
        Collection<XmlError> compErrors = new ArrayList<>();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        if (dl) {
            schemaOptions.setCompileDownloadUrls();
        }
        if (nopvr) {
            schemaOptions.setCompileNoPvrRule();
        }
        if (noupa) {
            schemaOptions.setCompileNoUpaRule();
        }
        if (partial) {
            schemaOptions.setCompilePartialTypesystem();
        }

        if (jarFiles != null && jarFiles.length > 0) {
            sLoader = XmlBeans.typeLoaderForResource(XmlBeans.resourceLoaderForPath(jarFiles));
        }

        int returnCode = 0;

        try {
            if (schemas != null && schemas.length > 0) {
                sLoader = XmlBeans.compileXsd(schemas, sLoader, schemaOptions);
            }
        } catch (Exception e) {
            if (compErrors.isEmpty() || !(e instanceof XmlException)) {
                e.printStackTrace(System.err);
            }
            System.out.println("Schema invalid:" + (partial ? " couldn't recover from errors" : ""));
            for (XmlError compError : compErrors) {
                System.out.println(compError);
            }

            returnCode = 10;
            return returnCode;
        }

        // recovered from errors, print out errors
        if (partial && !compErrors.isEmpty()) {
            returnCode = 11;
            System.out.println("Schema invalid: partial schema type system recovered");
            for (XmlError compError : compErrors) {
                System.out.println(compError);
            }
        }

        if (sLoader == null) {
            sLoader = XmlBeans.getContextTypeLoader();
        }

        for (File instanceFile : instanceFiles) {
            XmlObject xobj;

            try {
                XmlOptions xo = new XmlOptions();
                xo.setLoadLineNumbersEndElement();
                xobj = sLoader.parse(instanceFile, null, xo);
            } catch (Exception e) {
                System.err.println(instanceFile + " not loadable: " + e);
                e.printStackTrace(System.err);
                continue;
            }

            Collection<XmlError> errors = new ArrayList<>();

            if (xobj.schemaType() == XmlObject.type) {
                System.out.println(instanceFile + " NOT valid.  ");
                System.out.println("  Document type not found.");
            } else if (xobj.validate(strict ?
                new XmlOptions().setErrorListener(errors).setValidateStrict() :
                new XmlOptions().setErrorListener(errors))) {
                System.out.println(instanceFile + " valid.");
            } else {
                returnCode = 1;
                System.out.println(instanceFile + " NOT valid.");
                for (XmlError error : errors) {
                    System.out.println(error);
                }
            }
        }

        return returnCode;
    }
}
