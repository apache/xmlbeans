
import tools.inst2xsd.common.Inst2XsdTestBase;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;

/**
 * @author jacobd
 *         Date: Aug 9, 2004
 */
public class Inst2XsdDetailedTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedTest(String name) {
        super(name);
    }

    //TODO: move to checkin
    public void test_ns_duplicate_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS.xml"));
        log("-= Salami =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS_ss0.xsd")));
    }

    //TODO: move to checkin
    public void test_ns_duplicate_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS.xml"));
        log("-= venetian =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS_vb0.xsd")));

    }


   








    //TODO: move to checkin  QName:bug
    public void test_examples_xmlnews() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xmlnews_ss0.xsd")));
    }

    //TODO: move to checkin QName:bug
    public void test_examples_slashdotrdf() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdot.rdf.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_rd1.xsd")),
        });
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_vb1.xsd")),
        });
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotrdf_ss1.xsd")),
        });
    }

    //TODO: move to checkin QName:bug
    public void test_examples_xsl() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_example.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_rd0.xsd")),
        });
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_vb1.xsd")),
        });
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "xsl_ss1.xsd")),
        });
    }

    //TODO: move to checkin QName:bug
    public void test_examples_rss092() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss092_ss0.xsd")));
    }


}
