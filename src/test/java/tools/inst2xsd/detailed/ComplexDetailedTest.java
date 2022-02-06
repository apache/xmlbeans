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
package tools.inst2xsd.detailed;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static common.Common.P;
import static org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions.*;
import static tools.inst2xsd.common.Inst2XsdCommon.*;
import static tools.inst2xsd.common.Inst2XsdTestBase.SCHEMA_CASES_DIR;
import static tools.inst2xsd.common.Inst2XsdTestBase.runSchemaBuild;

@Disabled
public class ComplexDetailedTest {

    @Test
    void test_complex_enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum.xml"));

        runSchemaBuild(inst, getRussianOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_rd0.xsd")));
        runSchemaBuild(inst, getVenetianOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_vb0.xsd")));
        runSchemaBuild(inst, getSalamiOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_ss0.xsd")));
    }

    @Test
    void test_complex_enum_never() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum.xml"));

        Inst2XsdOptions opts = getVenetianOptions();
        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum_vb_eN0.xsd")));

        opts = getVenetianOptions();
        opts.setSimpleContentTypes(SIMPLE_CONTENT_TYPES_STRING);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum_vb_scs_enum0.xsd")));
    }

    @Test
    void test_complex_qname_enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName.xml"));

        Inst2XsdOptions opts = getVenetianOptions();
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName_vb_enum0.xsd")));

        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName_vb_eN0.xsd")));
    }

    @Test
    void test_complex_nestedNSArray() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedNSArray.xml"));

        runSchemaBuild(inst, getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd3.xsd"))
        });
        runSchemaBuild(inst, getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb3.xsd"))
        });
        runSchemaBuild(inst, getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss3.xsd"))
        });
    }

    @Test
    void test_example_po() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po.xml"));

        Inst2XsdOptions rdEN = getRussianOptions();
        rdEN.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, rdEN,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_rd_eN0.xsd")));

        runSchemaBuild(inst, getVenetianOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb0.xsd")));
        runSchemaBuild(inst, getSalamiOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_ss0.xsd")));

        Inst2XsdOptions opts = getVenetianOptions();
        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_eN0.xsd")));

        opts.setDesign(DESIGN_SALAMI_SLICE);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_ss_eN0.xsd")));

        opts.setDesign(DESIGN_VENETIAN_BLIND);
        opts.setSimpleContentTypes(SIMPLE_CONTENT_TYPES_STRING);
        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_scs0.xsd")));
        opts = null;
        opts = getVenetianOptions();
        opts.setSimpleContentTypes(SIMPLE_CONTENT_TYPES_STRING);
        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_scs_enum0.xsd")));
    }

    /**
     * java.lang.IllegalStateException: Not on a container
     */
    @Test
    void test_complex_attrenum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum.xml"));

        runSchemaBuild(inst, getRussianOptions(),
            new XmlObject[]{
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd0.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd1.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd2.xsd"))
            });

        runSchemaBuild(inst, getVenetianOptions(),
            new XmlObject[]{
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb0.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb1.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb2.xsd"))
            });


        runSchemaBuild(inst, getSalamiOptions(),
            new XmlObject[]{
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss0.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss1.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss2.xsd"))
            });

        Inst2XsdOptions opts = getRussianOptions();
        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            new XmlObject[]{
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum0.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum1.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum2.xsd"))
            });

        opts = null;
        opts = getVenetianOptions();
        opts.setUseEnumerations(ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
            new XmlObject[]{
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum0.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum1.xsd")),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum2.xsd"))
            });
    }

}
