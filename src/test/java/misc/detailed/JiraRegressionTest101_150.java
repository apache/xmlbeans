package misc.detailed;

import misc.common.JiraTestBase;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class JiraRegressionTest101_150 extends JiraTestBase
{
    /**
     * [XMLBEANS-103]   XMLBeans - QName thread cache, cause memory leaks
     */
    @Test
    public void test_jira_xmlbeans102a() throws Exception{
        // set the parameters similar to those in the bug
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(JIRA_CASES + "xmlbeans_102.xsd")});
        params.setOutputJar(new File(outputroot+P+"xmlbeans_102.jar"));
        File outputDir = new File(outputroot + P + "xmlbeans_102");
        outputDir.mkdirs();
        params.setClassesDir(outputDir);
        params.setSrcDir(outputDir);
        // compile schema
        SchemaCompiler.compile(params);
        // check for jar - compilation success
        if(!(new File(outputroot + P + "xmlbeans_102.jar").exists()) )
            throw new Exception("Jar File was not found");
        //cleanup
        deltree(outputroot);
    }

    /*
    * [XMLBEANS-102]: scomp - infinite loop during jar for specific xsd and params for netui_config.xsd
    */
    @Test
    public void test_jira_xmlbeans102b() {
        //Assert.fail("test_jira_xmlbeans102: Infinite loop after completion of parsing" );

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setOutputJar(new File(schemaCompOutputDirPath + "jira102.jar"));
        params.setClassesDir(schemaCompClassesDir);

        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_102_netui-config.xsd_")});
        List errors = new ArrayList();
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors)) {
            fail("test_jira_xmlbeans102() : Errors found when executing scomp");
        }

    }

    /**
     * NPE while initializing a type system w/ a type that extends
     * an a complex type from a different type system
     */
    @Test
    public void test_jira_xmlbeans105() throws Exception {
        //run untyped parse
        XmlObject obj = XmlObject.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        //run Typed Parse
        jira.xmlbeans105.ResourceUnknownFaultDocument rud =
                jira.xmlbeans105.ResourceUnknownFaultDocument.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        // / we know the instance is invalid
        // make sure the error message is what is expected
        rud.validate(xmOpts);
        assertEquals("More Errors than expected", 1, errorList.size());
        assertEquals("Did not receive the expected error code: " + ((XmlError) errorList.get(0)).getErrorCode(), 0, ((XmlError) errorList.get(0)).getErrorCode().compareToIgnoreCase("cvc-complex-type.2.4a"));

    }
}
