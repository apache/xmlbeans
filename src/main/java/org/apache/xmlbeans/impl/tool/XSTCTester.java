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
import org.apache.xmlbeans.impl.xb.ltgfmt.FileDesc;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestsDocument;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class XSTCTester {
    public static void printUsage() {
        System.out.println("Usage: xstc [-showpass] [-errcode] foo_LTGfmt.xml ...");
    }

    public static void main(String[] args) throws IOException {
        Set<String> flags = new HashSet<>();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("version");
        flags.add("showpass");
        flags.add("errcode");

        long start = System.currentTimeMillis();

        CommandLine cl = new CommandLine(args, flags, Collections.EMPTY_SET);
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null) {
            printUsage();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null) {
            CommandLine.printVersion();
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

        if (cl.args().length == 0) {
            printUsage();
            return;
        }

        boolean showpass = (cl.getOpt("showpass") != null);
        boolean errcode = (cl.getOpt("errcode") != null);

        File[] allFiles = cl.getFiles();
        Collection<File> ltgFiles = new ArrayList<>();
        Harness harness = new XMLBeanXSTCHarness();

        for (File allFile : allFiles) {
            if (allFile.getName().contains("LTG")) {
                ltgFiles.add(allFile);
            }
        }

        File resultsFile = new File("out.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(resultsFile.toPath(), StandardCharsets.ISO_8859_1))) {
            writer.println("<html>");
            writer.println("<style>td {border-bottom: 1px solid black} xmp {white-space: normal; word-wrap: break-word; word-break: break-all} </style>");
            writer.println("<body>");

            writer.println("<script language='JavaScript' type='text/javascript'>");
            writer.println("var w;");
            writer.println("function openWindow(schema, instance) {");
            writer.println("  if (w == null) {");
            writer.println("    w = window.open('about:blank', 'xstc');");
            writer.println("  }");
            writer.println("  if (w.closed) {");
            writer.println("    w = window.open('about:blank', 'xstc');");
            writer.println("  }");
            writer.println("  w.document.open();");
            writer.println("  w.document.write(\"<frameset rows=*,*><frame src='\" + schema + \"'><frame src='\" + instance + \"'></frameset>\");");
            writer.println("  w.document.close();");
            writer.println("  w.focus();");
            writer.println("}");
            writer.println("</script>");

            writer.println("<h1>XML Schema Test Collection Results</h1>");
            writer.println("<p>Run on " + (new XmlCalendar(new Date())) + "</p>");
            writer.println("<p>Values in schema or instance valid columns are results from compiling or validating respectively.");
            writer.println("Red or orange background mean the test failed.</p>");
            writer.println("<table style='border: 1px solid black' cellpadding=0 cellspacing=0>");
            writer.println("<tr><td witdh=10%>id</td><td width=70%>Description</td><td width=10%>sch v</td><td width=10%>ins v</td></tr>");
            int failures = 0;
            int cases = 0;
            for (File ltgFile : ltgFiles) {
                System.out.println("Processing test cases in " + ltgFile);
                Collection<String> ltgErrors = new ArrayList<>();
                TestCase[] testCases = parseLTGFile(ltgFile, ltgErrors);
                final Collection<TestCaseResult> results = new ArrayList<>();
                if (testCases != null) {
                    for (TestCase testCase : testCases) {
                        TestCaseResult result = new TestCaseResult();
                        result.testCase = testCase;
                        harness.runTestCase(result);
                        cases += 1;
                        if (!result.succeeded(errcode)) {
                            failures += 1;
                        } else if (!showpass) {
                            continue;
                        }
                        results.add(result);
                    }
                }
                writer.println("<tr><td colspan=4 bgcolor=skyblue>" + ltgFile + "</td></tr>");
                if (!ltgErrors.isEmpty()) {
                    writer.println("<tr><td>Errors within the LTG file:");
                    writer.println("<xmp>");
                    for (String ltgError : ltgErrors) {
                        writer.println(ltgError);
                    }
                    writer.println("</xmp>");
                    writer.println("</td></tr>");
                } else {
                    if (results.size() == 0) {
                        writer.println("<tr><td colspan=4 bgcolor=green>Nothing to report</td></tr>");
                    }
                }
                for (TestCaseResult result : results) {
                    summarizeResultAsHTMLTableRows(result, writer, errcode);
                }
            }
            writer.println("<tr><td colspan=4>Summary: " + failures + " failures out of " + cases + " cases run.</td></tr>");
            writer.println("</table>");
        }

        long finish = System.currentTimeMillis();
        System.out.println("Time run tests: " + ((double) (finish - start) / 1000.0) + " seconds");

        // Launch results
        System.out.println("Results output to " + resultsFile);
        String osName = SystemProperties.getProperty("os.name");
        assert (osName != null);
        if (osName.toLowerCase(Locale.ROOT).contains("windows")) {
            Runtime.getRuntime().exec("cmd /c start iexplore \"" + resultsFile.getAbsolutePath() + "\"");
        } else {
            Runtime.getRuntime().exec("mozilla file://" + resultsFile.getAbsolutePath());
        }
    }

    public static class TestCase {
        private File ltgFile;
        private String id;
        private String origin;
        private String description;
        private File schemaFile;
        private File instanceFile;
        private File resourceFile;
        private boolean svExpected;
        private boolean ivExpected;
        private boolean rvExpected;
        private String errorCode;

        public File getLtgFile() {
            return ltgFile;
        }

        public String getId() {
            return id;
        }

        public String getOrigin() {
            return origin;
        }

        public String getDescription() {
            return description;
        }

        public File getSchemaFile() {
            return schemaFile;
        }

        public File getInstanceFile() {
            return instanceFile;
        }

        public File getResourceFile() {
            return resourceFile;
        }

        public boolean isSvExpected() {
            return svExpected;
        }

        public boolean isIvExpected() {
            return ivExpected;
        }

        public boolean isRvExpected() {
            return rvExpected;
        }

        public String getErrorCode() {
            return errorCode;
        }

    }

    public static class TestCaseResult {
        private TestCase testCase;
        private boolean svActual;
        private final Collection<XmlError> svMessages = new ArrayList<>();
        private boolean ivActual;
        private final Collection<XmlError> ivMessages = new ArrayList<>();
        private boolean crash;

        public TestCase getTestCase() {
            return testCase;
        }

        public boolean isSvActual() {
            return svActual;
        }

        public void setSvActual(boolean svActual) {
            this.svActual = svActual;
        }

        public boolean isIvActual() {
            return ivActual;
        }

        public void setIvActual(boolean ivActual) {
            this.ivActual = ivActual;
        }

        public Collection<XmlError> getSvMessages() {
            return Collections.unmodifiableCollection(svMessages);
        }

        public void addSvMessages(Collection<XmlError> svMessages) {
            this.svMessages.addAll(svMessages);
        }

        public Collection<XmlError> getIvMessages() {
            return Collections.unmodifiableCollection(ivMessages);
        }

        public void addIvMessages(Collection<XmlError> ivMessages) {
            this.ivMessages.addAll(ivMessages);
        }

        public void setCrash(boolean crash) {
            this.crash = crash;
        }

        public boolean isCrash() {
            return crash;
        }

        public boolean succeeded(boolean errcode) {
            boolean success = !crash &&
                              (isIvActual() == testCase.isIvExpected()) &&
                              (isSvActual() == testCase.isSvExpected());
            if (errcode && testCase.getErrorCode() != null) {
                success &= errorReported(testCase.getErrorCode(), svMessages) || errorReported(testCase.getErrorCode(), ivMessages);
            }
            return success;
        }

    }

    public interface Harness {
        void runTestCase(TestCaseResult result);
    }

    public static String makeHTMLLink(File file, boolean value) {
        if (file == null) {
            return "&nbsp;";
        }
        URI uri = file.getAbsoluteFile().toURI();
        return "<a href=\"" + uri + "\" target=_blank>" + value + "</a>";
    }

    private static final Pattern leadingSpace = Pattern.compile("^\\s+", Pattern.MULTILINE);

    public static String makeHTMLDescription(TestCase testCase) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a class=noline href='javascript:openWindow(\"");
        if (testCase.getSchemaFile() == null) {
            sb.append("about:No schema");
        } else {
            sb.append(testCase.getSchemaFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
        }

        sb.append("\", \"");
        if (testCase.getInstanceFile() == null) {
            sb.append("about:No instance");
        } else {
            sb.append(testCase.getInstanceFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
        }
        sb.append("\")'><xmp>");
        sb.append(leadingSpace.matcher(testCase.getDescription()).replaceAll(""));
        sb.append("</xmp></a>");
        return sb.toString();
    }

    public static void summarizeResultAsHTMLTableRows(TestCaseResult result, PrintWriter out, boolean errcode) {
        TestCase testCase = result.getTestCase();

        boolean errorRow = errcode && testCase.getErrorCode() != null;
        boolean messagesRow = !result.getIvMessages().isEmpty() || !result.getSvMessages().isEmpty();

        boolean sRight = testCase.getSchemaFile() == null || testCase.isSvExpected() == result.isSvActual();
        boolean iRight = testCase.getInstanceFile() == null || testCase.isIvExpected() == result.isIvActual();
        boolean codeRight = true;
        if (errorRow) {
            codeRight = (errorReported(testCase.getErrorCode(), result.svMessages) || errorReported(testCase.getErrorCode(), result.ivMessages));
        }

        out.println(result.isCrash() ? "<tr bgcolor=black color=white>" : "<tr>");
        int idRowSpan = 1 + (errorRow ? 1 : 0) + (messagesRow ? 1 : 0);
        out.println("<td rowspan=" + idRowSpan + " valign=top>" + testCase.getId() + "</td>");
        out.println("<td valign=top>" + makeHTMLDescription(testCase) + "</td>");
        String sLinks;
        if (testCase.getResourceFile() == null) {
            sLinks = makeHTMLLink(testCase.getSchemaFile(), result.isSvActual());
        } else {
            sLinks = makeHTMLLink(testCase.getSchemaFile(), result.isSvActual()) + "<br>" + makeHTMLLink(testCase.getResourceFile(), result.isSvActual());
        }

        out.println((sRight ? "<td valign=top>" : result.isSvActual() ? "<td bgcolor=orange valign=top>" : "<td bgcolor=red valign=top>") + sLinks + "</td>");
        out.println((iRight ? "<td valign=top>" : result.isIvActual() ? "<td bgcolor=orange valign=top>" : "<td bgcolor=red valign=top>") + makeHTMLLink(testCase.getInstanceFile(), result.isIvActual()) + "</td>");
        out.println("</tr>");
        if (errorRow) {
            out.println("<tr>");
            out.println((codeRight ? "<td colspan=4 valid=top>" : "<td colspan=4 bgcolor=orange valign=top>") + "expected error: " + testCase.getErrorCode() + "</td>");
            out.println("</tr>");
        }
        if (messagesRow) {
            if (!result.succeeded(errcode)) {
                out.println("<tr><td colspan=4 bgcolor=yellow><xmp>");
            } else {
                out.println("<tr><td colspan=4><xmp>");
            }
            for (XmlError s : result.getSvMessages()) {
                out.println(s);
            }
            for (XmlError xmlError : result.getIvMessages()) {
                out.println(xmlError);
            }
            out.println("</xmp></tr></td>");
        }
    }

    public static TestCase[] parseLTGFile(File ltgFile, Collection<String> outerErrors) {
        Collection<XmlError> errors = new ArrayList<>();
        try {
            XmlOptions ltgOptions = new XmlOptions();
            ltgOptions.setLoadSubstituteNamespaces(Collections.singletonMap("", "http://www.bea.com/2003/05/xmlbean/ltgfmt"));
            ltgOptions.setErrorListener(errors);
            ltgOptions.setLoadLineNumbers();
            TestsDocument doc = TestsDocument.Factory.parse(ltgFile, ltgOptions);
            if (!doc.validate(ltgOptions)) {
                throw new Exception("Document " + ltgFile + " not valid.");
            }

            org.apache.xmlbeans.impl.xb.ltgfmt.TestCase[] testCases = doc.getTests().getTestArray();

            Collection<TestCase> result = new ArrayList<>();
            for (org.apache.xmlbeans.impl.xb.ltgfmt.TestCase testCase : testCases) {
                TestCase newCase = new TestCase();
                newCase.ltgFile = ltgFile;
                newCase.id = testCase.getId();
                newCase.origin = testCase.getOrigin();
                newCase.description = testCase.getDescription();
                FileDesc[] filedescs = testCase.getFiles().getFileArray();
                testCase.getOrigin();
                for (FileDesc filedesc : filedescs) {
                    String dir = filedesc.getFolder();
                    String filename = filedesc.getFileName();
                    File theFile = new File(ltgFile.getParentFile(), dir + "/" + filename);
                    if (!theFile.exists() || !theFile.isFile() || !theFile.canRead()) {
                        outerErrors.add(XmlError.forObject("Can't read file " + theFile, filedesc).toString());
                        continue;
                    }

                    switch (filedesc.getRole().intValue()) {
                        case FileDesc.Role.INT_INSTANCE:
                            if (newCase.instanceFile != null) {
                                outerErrors.add(XmlError.forObject("More than one instance file speicifed - ignoring all but last", filedesc).toString());
                            }
                            newCase.instanceFile = theFile;
                            newCase.ivExpected = filedesc.getValidity();
                            break;

                        case FileDesc.Role.INT_SCHEMA:
                            if (newCase.schemaFile != null) {
                                outerErrors.add(XmlError.forObject("More than one schema file speicifed - ignoring all but last", filedesc).toString());
                            }
                            newCase.schemaFile = theFile;
                            newCase.svExpected = filedesc.getValidity();
                            break;

                        case FileDesc.Role.INT_RESOURCE:
                            if (newCase.resourceFile != null) {
                                outerErrors.add(XmlError.forObject("More than one resource file speicifed - ignoring all but last", filedesc).toString());
                            }
                            newCase.resourceFile = theFile;
                            newCase.rvExpected = filedesc.getValidity();
                            break;

                        default:
                            throw new XmlException(XmlError.forObject("Unexpected file role", filedesc));
                    }

                    if (filedesc.getCode() != null) {
                        newCase.errorCode = filedesc.getCode().getID();
                    }
                }
                result.add(newCase);
            }
            return result.toArray(new TestCase[0]);
        } catch (Exception e) {
            if (errors.isEmpty()) {
                outerErrors.add(e.getMessage());
            } else {
                for (XmlError error : errors) {
                    outerErrors.add(error.toString());
                }
            }
            return null;
        }
    }

    public static boolean errorReported(String errorCode, Collection<XmlError> set) {
        if (errorCode == null || set == null || set.size() == 0) {
            return false;
        }

        for (XmlError xmlError : set) {
            if (errorCode.equals(xmlError.getErrorCode())) {
                return true;
            }
        }

        return false;
    }

}
