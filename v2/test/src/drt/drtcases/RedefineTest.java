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
package drtcases;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.example.prod.NewSizeDocument;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 * Date: May 4, 2004
 */
public class RedefineTest extends TestCase
{
    public RedefineTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(RedefineTest.class); }

    public void testRedefine()
    {
        try
        {
            String xml = "<newSize xmlns='http://example.org/prod'>7</newSize>";
            NewSizeDocument nsDoc = NewSizeDocument.Factory.parse(xml);

            boolean valid = nsDoc.validate();

            if (!valid)
                print(nsDoc);

            Assert.assertTrue(valid);

            Assert.assertTrue(nsDoc.getNewSize()==7);

            nsDoc.setNewSize(20);

            List errors = new ArrayList();
            XmlOptions options = new XmlOptions();
            options.setErrorListener(errors);

            valid = nsDoc.validate(options);

            if (valid || errors.size()!=1)
                print(nsDoc);

            Assert.assertTrue(!valid);

            Assert.assertTrue(errors.size()==1);
        }
        catch (XmlException e)
        {
            Assert.assertTrue(false);
        }
    }

    private static void print(XmlObject xo)
    {
        List errors = new ArrayList();
        XmlOptions options = new XmlOptions();
        options.setErrorListener(errors);

        System.out.println("Doc:\n" + xo + "\nValid: " + xo.validate(options));

        for (int i = 0; i < errors.size(); i++)
        {
            XmlError xmlError = (XmlError) errors.get(i);
            System.out.println(xmlError);
        }
    }
}
