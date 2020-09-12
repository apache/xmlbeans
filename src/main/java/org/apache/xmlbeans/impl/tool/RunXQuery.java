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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RunXQuery {
    public static void printUsage() {
        System.out.println("Run an XQuery against an XML instance");
        System.out.println("Usage:");
        System.out.println("xquery [-verbose] [-pretty] [-q <query> | -qf query.xq] [file.xml]*");
        System.out.println(" -q <query> to specify a query on the command-line");
        System.out.println(" -qf <query> to specify a file containing a query");
        System.out.println(" -pretty pretty-prints the results");
        System.out.println(" -license prints license information");
        System.out.println(" the query is run on each XML file specified");
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        Set<String> flags = new HashSet<>();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("version");
        flags.add("verbose");
        flags.add("pretty");

        CommandLine cl =
            new CommandLine(
                args, flags,
                Arrays.asList("q", "qf"));

        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null) {
            printUsage();
            System.exit(0);
            return;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0) {
            for (String badopt : badopts) {
                System.out.println("Unrecognized option: " + badopt);
            }
            printUsage();
            System.exit(0);
            return;
        }

        if (cl.getOpt("license") != null) {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null) {
            CommandLine.printVersion();
            System.exit(0);
            return;
        }

        args = cl.args();

        if (args.length == 0) {
            printUsage();
            System.exit(0);
            return;
        }

        boolean verbose = cl.getOpt("verbose") != null;
        boolean pretty = cl.getOpt("pretty") != null;

        //
        // Get and compile the query
        //

        String query = cl.getOpt("q");
        String queryfile = cl.getOpt("qf");

        if (query == null && queryfile == null) {
            System.err.println("No query specified");
            System.exit(0);
            return;
        }

        if (query != null && queryfile != null) {
            System.err.println("Specify -qf or -q, not both.");
            System.exit(0);
            return;
        }

        try {
            if (queryfile != null) {
                File queryFile = new File(queryfile);
                FileInputStream is = new FileInputStream(queryFile);
                InputStreamReader r = new InputStreamReader(is, StandardCharsets.ISO_8859_1);

                StringBuilder sb = new StringBuilder();

                for (; ; ) {
                    int ch = r.read();

                    if (ch < 0) {
                        break;
                    }

                    sb.append((char) ch);
                }

                r.close();
                is.close();

                query = sb.toString();
            }
        } catch (Throwable e) {
            System.err.println("Cannot read query file: " + e.getMessage());
            System.exit(1);
            return;
        }

        if (verbose) {
            System.out.println("Compile Query:");
            System.out.println(query);
            System.out.println();
        }

        try {
            query = XmlBeans.compileQuery(query);
        } catch (Exception e) {
            System.err.println("Error compiling query: " + e.getMessage());
            System.exit(1);
            return;
        }

        //
        // Get the instance
        //

        File[] files = cl.getFiles();

        for (File file : files) {
            XmlObject x;

            try {
                if (verbose) {
                    InputStream is = new FileInputStream(file);

                    for (; ; ) {
                        int ch = is.read();

                        if (ch < 0) {
                            break;
                        }

                        System.out.write(ch);
                    }

                    is.close();

                    System.out.println();
                }

                x = XmlObject.Factory.parse(file);
            } catch (Throwable e) {
                System.err.println("Error parsing instance: " + e.getMessage());
                System.exit(1);
                return;
            }

            if (verbose) {
                System.out.println("Executing Query...");
                System.err.println();
            }

            XmlObject[] result;

            try {
                result = x.execQuery(query);
            } catch (Throwable e) {
                System.err.println("Error executing query: " + e.getMessage());
                System.exit(1);
                return;
            }

            if (verbose) {
                System.out.println("Query Result:");
            }

            XmlOptions opts = new XmlOptions();
            opts.setSaveOuter();
            if (pretty) {
                opts.setSavePrettyPrint();
            }

            for (XmlObject xmlObject : result) {
                xmlObject.save(System.out, opts);
                System.out.println();
            }
        }
    }
}
