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
package common;

import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Common {
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String P = File.separator;

    public static String FWROOT = getRootFile();
    public static String CASEROOT = getCaseLocation();
    public static String XBEAN_CASE_ROOT = getCaseLocation() + P + "xbean";
    public static String SCOMP_CASE_ROOT = XBEAN_CASE_ROOT + P + "compile" + P + "scomp" + P;

    //location of files under "cases folder"
    public static String OUTPUTROOT = FWROOT + P + "build" + P + "test" + P + "output";


    public final List<XmlError> errorList = new LinkedList<>();
    public final XmlOptions xmOpts = new XmlOptions();

    public Common() {
        xmOpts.setErrorListener(errorList);
    }

    /**
     * If System.property for 'xbean.rootdir' == null
     * use '.' as basePath
     * '.' should be where the build.xml file lives
     */
    public static String getRootFile() throws IllegalStateException {
        String baseDir = System.getProperty("xbean.rootdir");
        if (baseDir == null) {
            return new File(".").getAbsolutePath();
        } else {
            return new File(baseDir).getAbsolutePath();
        }
    }

    /**
     * If System.property for 'cases.location' == null
     * use '.' as basePath and add src/test/resources.
     * should be where the build.xml file lives
     */
    public static String getCaseLocation() throws IllegalStateException {
        String baseDir = System.getProperty("cases.location");
        if (baseDir == null) {
            return new File("." + P + "src" + P + "test" + P + "resources").getAbsolutePath();
        } else {
            return new File(baseDir).getAbsolutePath();
        }
    }

    /**
     * Gets a case file from under CASEROOT with location passed in as strPath
     *
     * @return file Object for references location
     */
    public static File xbeanCase(String strPath) {
        return (new File(CASEROOT, strPath));
    }

    /**
     * Creates directory under output directory as noted by strPath
     *
     * @return File Object specified by strPath
     */
    public static File xbeanOutput(String strPath) {
        File result = new File(OUTPUTROOT, strPath);
        result.mkdirs();
        return result;
    }

    /**
     * Recursively deletes files under specified directory
     */
    public static void deltree(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (String s : Objects.requireNonNull(dir.list())) {
                    deltree(new File(dir, s));
                }
            }
            if (!dir.delete()) {
                System.out.println("Could not delete " + dir);
            }
            //throw new IllegalStateException("Could not delete " + dir);
        }
    }

    /**
     * check list of errors/warnings/msgs and print them. Return true if errors found
     */
    public static boolean hasSevereError(List<XmlError> errors) {
        boolean errFound = errors.stream().anyMatch(e -> e.getSeverity() == XmlError.SEVERITY_ERROR);
        errors.clear();
        return errFound;
    }

    /**
     * Validate schemas to instance based on the docType
     */
    public static void validateInstance(String[] schemas, String[] instances, QName docType) throws Exception {
        SchemaTypeLoader stl = makeSchemaTypeLoader(schemas);
        XmlOptions options = new XmlOptions();

        if (docType != null) {
            SchemaType docSchema = stl.findDocumentType(docType);
            assertNotNull(docSchema);
            options.setDocumentType(docSchema);
        }

        for (int i = 0; i < instances.length; i++) {
            XmlObject x = stl.parse(instances[i], null, options);

            //if (!startOnDocument) {
            //    XmlCursor c = x.newCursor();
            //    c.toFirstChild();
            //    x = c.getObject();
            //    c.dispose();
            //}

            List<XmlError> xel = new ArrayList<>();

            options.setErrorListener(xel);

            boolean isValid = x.validate(options);

            if (!isValid) {
                StringBuilder errorTxt = new StringBuilder("Invalid doc, expected a valid doc: ");
                errorTxt.append("Instance(").append(i).append("): ");
                errorTxt.append(x.xmlText());
                errorTxt.append("Errors: ");
                for (XmlError xmlError : xel) {
                    errorTxt.append(xmlError).append("\n");
                }
                System.err.println(errorTxt);
                throw new Exception("Instance not valid\n" + errorTxt);
            }
        }
    }

    /**
     * Convenience method to create a SchemaTypeLoader from a set of xsds
     */
    public static SchemaTypeLoader makeSchemaTypeLoader(String[] schemas)
        throws Exception {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++) {
            schemaDocs[i] =
                XmlObject.Factory.parse(schemas[i]);
        }

        return XmlBeans.loadXsd(schemaDocs);
    }

    /**
     * Convenience class for creating tests in a multithreaded env
     */
    public static abstract class TestThread extends Thread {
        protected Throwable _throwable;
        protected boolean _result;
        protected XmlOptions xm;
        protected List<XmlError> errors = new ArrayList<>();

        public TestThread() {
            xm = new XmlOptions();
            xm.setErrorListener(errors);
            xm.setValidateOnSet();
        }

        public Throwable getException() {
            return _throwable;
        }

        public boolean getResult() {
            return _result;
        }


    }


}
