/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package misc.common;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;

/**
 *
 */
public class JiraTestBase extends TestCase
{
    public static final String P = File.separator;

    public static String fwroot = getRootFile();
    public static String caseroot = fwroot + P + "test" + P + "cases";
    //location of files under "cases folder"
    public static String JIRA_CASES = caseroot + P + "xbean" + P + "misc" +
                                        P +
                                        "jira" +
                                        P;
    public static File outputroot = new File(fwroot,
                                             "build" + P + "test" + P +
                                             "jiraoutput");


    public static String scompTestFilesRoot = fwroot + P + "test" + P + "cases" + P + "xbean" + P + "misc" + P + "jira" + P;
    public static String schemaCompOutputDirPath = fwroot + P + "build" + P + "test" + P + "output" + P + "jira" + P;
    public static File schemaCompOutputDirFile = null;
    public static File schemaCompSrcDir = null;
    public static File schemaCompClassesDir = null;

    public static final int THREAD_COUNT = 150;
    public static final int ITERATION_COUNT = 2;
    public LinkedList errorList;
    public XmlOptions xmOpts;

    public JiraTestBase(String name){
        super(name);
        errorList = new LinkedList();
        xmOpts = new XmlOptions();
        xmOpts.setErrorListener(errorList);
    }

    /**
     * If System.property for 'xbean.rootdir' == null use '.' as basePath '.'
     * should be where the build.xml file lives
     *
     * @return
     * @throws IllegalStateException
     */
    public static String getRootFile() throws IllegalStateException
    {
        String baseDir = System.getProperty("xbean.rootdir");
        if (baseDir == null) {
            return new File(".").getAbsolutePath();
        } else {
            return new File(baseDir).getAbsolutePath();
        }
    }

    public static File xbeanCase(String str)
    {
        return (new File(caseroot + JIRA_CASES, str));
    }

    public static void deltree(File dir)
    {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++) {
                    deltree(new File(dir, list[i]));
                }
            }
            if (!dir.delete()) {
                System.out.println("Could not delete " + dir);
            }
            //throw new IllegalStateException("Could not delete " + dir);
        }
    }

    /*
    * Helper Methods
    *
    */
    public static XmlObject compileXsdString(String XsdAsString) {
        XmlObject xobj = null;
        try {
            xobj = XmlObject.Factory.parse(XsdAsString);
        } catch (XmlException xme) {
            if (!xme.getErrors().isEmpty()) {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();) {
                    System.out.println("Parse Errors :" + itr.next());
                }
            }
        } finally {
            Assert.assertNotNull(xobj);
            return xobj;
        }
    }

    public static XmlObject compileXsdFile(String XsdFilePath) {
        XmlObject xobj = null;
        try {
            xobj = XmlObject.Factory.parse(new File(XsdFilePath));
        } catch (XmlException xme) {
            if (!xme.getErrors().isEmpty()) {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();) {
                    System.out.println("Parse Errors :" + itr.next());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            ioe.getMessage();
        } finally {
            Assert.assertNotNull(xobj);
            return xobj;
        }
    }


    public boolean printOptionErrMsgs(Collection errors) {
        // check list of errors/warnings/msgs and print them. Return true if errors found
        boolean errFound = false;
        if (!errors.isEmpty()) {
            for (Iterator i = errors.iterator(); i.hasNext();) {
                XmlError eacherr = (XmlError) i.next();
                int errSeverity = eacherr.getSeverity();
                if (errSeverity == XmlError.SEVERITY_ERROR) {
                    System.out.println("Err Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                    errFound = true;
                } else if (errSeverity == XmlError.SEVERITY_WARNING) {
                    System.out.println("Warning Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                } else if (errSeverity == XmlError.SEVERITY_INFO) {
                    System.out.println("Info Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                }
            }
            errors.clear();
        }
        return errFound;
    }

    public void validateInstance(String[] schemas, String[] instances, QName docType) throws Exception {
        SchemaTypeLoader stl = makeSchemaTypeLoader(schemas);
        XmlOptions options = new XmlOptions();

        if (docType != null) {
            SchemaType docSchema = stl.findDocumentType(docType);
            Assert.assertTrue(docSchema != null);
            options.put(XmlOptions.DOCUMENT_TYPE, docSchema);
        }

        for (int i = 0; i < instances.length; i++) {
            XmlObject x =
                    stl.parse((String) instances[i], null, options);

            //if (!startOnDocument) {
            //    XmlCursor c = x.newCursor();
            //    c.toFirstChild();
            //    x = c.getObject();
            //    c.dispose();
            //}

            List xel = new ArrayList();

            options.put(XmlOptions.ERROR_LISTENER, xel);

            boolean isValid = x.validate(options);

            if (!isValid) {
                StringBuffer errorTxt = new StringBuffer("Invalid doc, expected a valid doc: ");
                errorTxt.append("Instance(" + i + "): ");
                errorTxt.append(x.xmlText());
                errorTxt.append("Errors: ");
                for (int j = 0; j < xel.size(); j++)
                    errorTxt.append(xel.get(j) + "\n");
                System.err.println(errorTxt.toString());
                throw new Exception("Instance not valid\n" + errorTxt.toString());
            }
        }
    }

    public SchemaTypeLoader makeSchemaTypeLoader(String[] schemas)
            throws Exception {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++) {
            schemaDocs[i] =
                    XmlObject.Factory.parse(schemas[i]);
        }

        return XmlBeans.loadXsd(schemaDocs);
    }

    /**
     * For Testing jira issue 46
     */
    public static class RegexThread extends TestThread
    {
        private xmlbeans46.UsPhoneNumberDocument phone;
        Random rand;

        public RegexThread()
        {
            super();
            phone = xmlbeans46.UsPhoneNumberDocument.Factory.newInstance();
            rand = new Random();
        }

        /**
         * Validates a type that uses the following pattern
         * <xs:restriction base="xs:string">
         * <xs:pattern value="\d{3}\-\d{3}\-\d{4}"/>
         * </xs:restriction>
         */
        public void run()
        {
            try {

                for (int i = 0; i < 9; i++) {
                    int pre = rand.nextInt(999);
                    int mid = rand.nextInt(999);
                    int post = rand.nextInt(9999);
                    String testVal = ((pre > 100) ? String.valueOf(pre) : "128") + "-" +
                            ((mid > 100) ? String.valueOf(mid) : "256") + "-" +
                            ((post > 1000) ? String.valueOf(post) : "1024");

                    String xmlData = "<xb:usPhoneNumber xmlns:xb=\"http://xmlbeans_46\">" +
                            testVal +
                            "</xb:usPhoneNumber>";
                    //cannot repro using this method
                    //phone.setUsPhoneNumber(testVal);
                    //if (!phone.validate(xm)) {
                    //    _throwable = new Throwable("Multi Threaded Regular " +
                    //            "Expression did not validate - " + testVal);
                    //    if (errors != null && errors.size() > 0)
                    //        System.err.println("ERROR: " + errors);
                    //}

                    boolean validated = parseAndValidate(xmlData);
                    if (!validated) {
                        System.out.println("Not Valid!!!");
                    }
                    System.out.println("Validated " + testVal + " successfully ");
                }
                _result = true;

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        private boolean parseAndValidate(String val) throws XmlException
        {
            xmlbeans46.UsPhoneNumberDocument xml = xmlbeans46.UsPhoneNumberDocument.Factory.parse(val);
            return validate(xml);
        }

        private boolean validate(xmlbeans46.UsPhoneNumberDocument rdd)
        {
            Collection errors = new ArrayList();
            XmlOptions validateOptions = new XmlOptions();
            validateOptions.setErrorListener(errors);
            boolean valid = rdd.validate(validateOptions);
            if (!valid) {
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    XmlError xmlError = (XmlError) iterator.next();
                    System.out.println("XML Error - " + xmlError.getMessage() + " at\n" + xmlError.getCursorLocation().xmlText());
                }

            }
            return valid;
        }
    }


    /**
     * For Testing jira issue 84
     */
    public static class XPathThread extends TestThread
    {
        public XPathThread()
        {
            super();
        }

        public void run()
        {

            try {
                for (int i = 0; i < ITERATION_COUNT; i++) {
                    switch (i % 2) {
                        case 0:
                            runStatusXPath();
                            break;
                        case 1:
                            runDocXPath();
                            break;
                        default:
                            System.out.println("Val: " + i);
                            break;
                    }

                }
                _result = true;

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void runStatusXPath()
        {
            try {
                System.out.println("Testing Status");
                String statusDoc = "<statusreport xmlns=\"http://openuri.org/enumtest\">\n" +
                        "  <status name=\"first\" target=\"all\">all</status>\n" +
                        "  <status name=\"second\" target=\"all\">few</status>\n" +
                        "  <status name=\"third\" target=\"none\">most</status>\n" +
                        "  <status name=\"first\" target=\"none\">none</status>\n" +
                        "</statusreport>";
                XmlObject path = XmlObject.Factory.parse(statusDoc, xm);
                XmlObject[] resSet = path.selectPath("//*:status");
                Assert.assertTrue(resSet.length + "", resSet.length == 4);
                resSet = path.selectPath("//*:status[@name='first']");
                Assert.assertTrue(resSet.length == 2);

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void runDocXPath()
        {
            try {
                System.out.println("Testing Doc");
                String docDoc = "<?xml version=\"1.0\"?>\n" +
                        "<doc xmlns:ext=\"http://somebody.elses.extension\">\n" +
                        "  <a test=\"test\" />\n" +
                        "  <b attr1=\"a1\" attr2=\"a2\"   \n" +
                        "  xmlns:java=\"http://xml.apache.org/xslt/java\">\n" +
                        "    <a>\n" +
                        "    </a> \n" +
                        "  </b>\n" +
                        "</doc><!-- -->  ";
                XmlObject path = XmlObject.Factory.parse(docDoc, xm);
                XmlObject[] resSet = path.selectPath("//a");
                Assert.assertTrue(resSet.length == 2);
                resSet = path.selectPath("//b[@attr2]");
                Assert.assertTrue(resSet.length == 1);

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }
    }

    public static abstract class TestThread extends Thread
    {
        protected Throwable _throwable;
        protected boolean _result;
        protected XmlOptions xm;
        protected ArrayList errors;

        public TestThread()
        {
            xm = new XmlOptions();
            ArrayList errors = new ArrayList();
            xm.setErrorListener(errors);
            xm.setValidateOnSet();
        }

        public Throwable getException()
        {
            return _throwable;
        }

        public boolean getResult()
        {
            return _result;
        }


    }


}
