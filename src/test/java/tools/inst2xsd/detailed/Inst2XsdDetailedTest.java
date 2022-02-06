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
import static tools.inst2xsd.common.Inst2XsdCommon.*;
import static tools.inst2xsd.common.Inst2XsdTestBase.SCHEMA_CASES_DIR;
import static tools.inst2xsd.common.Inst2XsdTestBase.runSchemaBuild;

@Disabled
public class Inst2XsdDetailedTest {

    //TODO: move to checkin
    @Test
    void test_ns_duplicate_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS.xml"));
        runSchemaBuild(inst, getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS_ss0.xsd")));
    }

    //TODO: move to checkin
    @Test
    void test_ns_duplicate_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS.xml"));
        runSchemaBuild(inst, getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS_vb0.xsd")));

    }

    //TODO: move to checkin  QName:bug
    @Test
    void test_examples_xmlnews() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews.xml"));

        runSchemaBuild(inst, getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_rd0.xsd")));
        runSchemaBuild(inst, getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_vb0.xsd")));
        runSchemaBuild(inst, getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_ss0.xsd")));
    }

    //TODO: move to checkin QName:bug
    @Test
    void test_examples_slashdotrdf() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdot.rdf.xml"));

        runSchemaBuild(inst, getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_rd1.xsd")),
        });
        runSchemaBuild(inst, getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_vb1.xsd")),
        });
        runSchemaBuild(inst, getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_ss1.xsd")),
        });
    }

    //TODO: move to checkin QName:bug
    @Test
    void test_examples_xsl() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_example.xml"));

        runSchemaBuild(inst, getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_rd1.xsd")),
        });
        runSchemaBuild(inst, getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_vb1.xsd")),
        });
        runSchemaBuild(inst, getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_ss1.xsd")),
        });
    }

    //TODO: move to checkin QName:bug
    @Test
    void test_examples_rss092() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092.xml"));

        runSchemaBuild(inst, getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_rd0.xsd")));
        runSchemaBuild(inst, getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_vb0.xsd")));
        runSchemaBuild(inst, getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_ss0.xsd")));
    }

    @Test
    void test_types_comments() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "comments.xml"));

        runSchemaBuild(inst, getRussianOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "comments_rd0.xsd")));
        runSchemaBuild(inst, getVenetianOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "comments_vb0.xsd")));
        runSchemaBuild(inst, getSalamiOptions(),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "comments_ss0.xsd")));
    }

    @Test
    void test_types_commentschoice() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "commentschoice.xml"));
        Inst2XsdOptions opts = getRussianOptions();
        opts.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);

        runSchemaBuild(inst, opts,
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "commentschoice_eNrd0.xsd")));

        opts.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "commentschoice_eNvb0.xsd")));

        opts.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "commentschoice_eNss0.xsd")));
    }

    /** This case fails validation because comment is not recognized */
    @Test
    void test_types_innercomment() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "innercomment.xml"));

        runSchemaBuild(inst, getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "innercomment_vb0.xsd")));
    }

    @Test
    void test_types_nillable() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil.xml"));

        runSchemaBuild(inst, getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_rd1.xsd"))
        });
        runSchemaBuild(inst, getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_vb1.xsd"))
        });
        runSchemaBuild(inst, getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "types" + P + "xsinil_ss1.xsd"))
        });
    }


}
