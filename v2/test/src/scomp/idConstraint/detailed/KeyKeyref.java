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

package scomp.idConstraint.detailed;

import scomp.common.BaseCase;
import xbean.scomp.idConstraint.constraint.*;
import org.apache.xmlbeans.XmlString;

/**
 * @owner: ykadiysk
 * Date: Aug 9, 2004
 * Time: 10:26:32 AM
 */
public class KeyKeyref extends BaseCase {

    public void testUnique() throws Throwable {
        String input =
                "<productList xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <product>" +
                " <department name=\"Marketing\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</productList>";

        ProductListDocument doc = ProductListDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * field combo not unique in instance  (a and c are the same)
     *
     * @throws Throwable
     */
    public void testUniqueIllegal() throws Throwable {
        ProductListDocument doc = ProductListDocument.Factory.newInstance();
        ProductListType products = ProductListType.Factory.newInstance();
        ProductType a = products.addNewProduct();
        ProductType.Department dep = ProductType.Department.Factory.newInstance();
        XmlString val = XmlString.Factory.newInstance();
        val.setStringValue("FooDep");
        dep.setName(val);
        a.setDepartmentArray(new ProductType.Department[]{dep});
        a.setId("0");

        ProductType b = products.addNewProduct();
        b.setDepartmentArray(new ProductType.Department[]{dep});
        b.setId("1");

        ProductType c = products.addNewProduct();
        c.setDepartmentArray(new ProductType.Department[]{dep});
        c.setId("0");
        doc.setProductList(products);

        System.out.println(doc.xmlText());

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"c-props-correct"};
        assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * Selector selects a non-unique field in instance
     * Only one dept can appear in the first product
     *
     * @throws Throwable
     */
    public void testUniqueIllegal2() throws Throwable {
        String input =
                "<productList xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <product>" +
                " <department name=\"Marketing\"/>" +
                " <department name=\"Management\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</productList>";

        ProductListDocument doc = ProductListDocument.Factory.parse(input);

        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }


    public void testKey() throws Throwable {
        String input =
                "<KeyProductList xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <product>" +
                " <department name=\"Marketing\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</KeyProductList>";

        KeyProductListDocument doc = KeyProductListDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * null key in instance:missing dept in first product
     *
     * @throws Throwable
     */
    public void testKeyIllegal() throws Throwable {
        String input =
                "<KeyProductList xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <product>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</KeyProductList>";

        KeyProductListDocument doc = KeyProductListDocument.Factory.parse(input);

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));

    }


    public void testKeyRef() throws Throwable {
        String input =
                "<CompanyDB xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                "<KeyProductList >" +
                " <product>" +
                " <department name=\"Marketing\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</KeyProductList>" +
                "<order>" +
                "   <customerName>Bozo</customerName>" +
                "   <item deptId=\"7345\">" +
                "   <SKU>FooBarChart</SKU>" +
                "  </item>" +
                "</order>" +
                "<order>" +
                "   <customerName>Bozo</customerName>" +
                "   <item deptId=\"7345\">" +
                "        <SKU>FooBarChart</SKU>" +
                "  </item>" +
                "</order>" +
                "</CompanyDB>";

        CompanyDBDocument doc = CompanyDBDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    //ref non-existing key
    public void testKeyRefIllegal() throws Throwable {

        String input =
                "<CompanyDB xmlns=\"http://xbean/scomp/idConstraint/Constraint\">" +
                "<KeyProductList >" +
                " <product>" +
                " <department name=\"Marketing\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "<product>" +
                " <department name=\"7345\"/>" +
                " <id>FooBarChart</id>" +
                "</product>" +
                "</KeyProductList>" +
                "<order>" +
                "   <customerName>Bozo</customerName>" +
                "   <item deptId=\"7345\">" +
                "   <SKU>FooBarChart</SKU>" +
                "  </item>" +
                "</order>" +
                "<order>" +
                "   <customerName>Marketing</customerName>" +
                "   <item deptId=\"7345\">" +
                "   <SKU>FooBarChart</SKU>" +
                "  </item>" +
                "</order>" +
                "</CompanyDB>";

        CompanyDBDocument doc = CompanyDBDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));


    }

}
