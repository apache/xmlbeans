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

package compile.scomp.detailed;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.*;

import java.io.File;
import java.util.*;

import compile.scomp.common.CompileCommon;

import javax.xml.namespace.QName;


public class DetailedCompTests extends TestCase
{
    public DetailedCompTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(DetailedCompTests.class); }


    /**
     * This test requires laxDoc.xsd to be compiled and
     * on the classpath ahead of time, otherwise documentation
     * element processing would not occur
     * @throws Exception
     */
    public void testLaxDocProcessing() throws Exception
    {
        QName q = new QName("urn:lax.Doc.Compilation", "ItemRequest");
        ArrayList err = new ArrayList();
        XmlOptions xm_opt = new XmlOptions().setErrorListener(err);
        xm_opt.setSavePrettyPrint();

        XmlObject xObj = XmlObject.Factory.parse(
                new File(CompileCommon.fileLocation+"/detailed/laxDoc.xsd"));
        XmlObject[] schemas = new XmlObject[]{xObj};


        // ensure exception is thrown when
        // xmloptions flag is not set
        boolean valDocEx = false;
        try{
            SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null,
                schemas, null, XmlBeans.getBuiltinTypeSystem(), null, xm_opt);
            Assert.assertTrue("STS was null", sts != null);
        }catch(XmlException xmlEx){
            valDocEx = true;
            System.err.println("Expected Error: "+xmlEx.getMessage());
        } catch(Exception e){
            throw e;
        }

        //check exception was thrown
        if(!valDocEx)
            throw new Exception("Documentation processing " +
                    "should have thrown and error");
        // validate error code
        valDocEx = false;
        for (Iterator iterator = err.iterator(); iterator.hasNext();) {
            XmlError xErr = (XmlError)iterator.next();
            //System.out.println("ERROR: '"+ xErr+"'");
            //any one of these are possible
            if(xErr.getErrorCode().compareTo("cvc-complex-type.4") == 0 ||
                    xErr.getErrorCode().compareTo("cvc-complex-type.2.3") == 0 ||
                    xErr.getErrorCode().compareTo("cvc-complex-type.2.4c") == 0)
                valDocEx = true;
        }

        if (!valDocEx)
            throw new Exception("Expected Error code did not validate");

        //reset errors
        err.clear();

        //ensure no exception when error
        xm_opt = xm_opt.setCompileNoValidation();
        try {
            SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null,
                    schemas, null, XmlBeans.getBuiltinTypeSystem(), null,
                    xm_opt);

            if(!err.isEmpty())
                throw new Exception("Error listener should be empty");

            for (Iterator iterator = err.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());
            }

            SchemaGlobalElement sge = sts.findElement(q);
            System.out.println("QName: " + sge.getName());
            System.out.println("Type: " + sge.getType());


        } catch (Exception e) {
            throw e;
        }

    }

}
