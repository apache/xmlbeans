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
import junit.framework.Assert;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import misc.common.JiraTestBase;
import common.Common;

/**
 * This class contains tests that need to invoke the SchemaCompiler class which is
 * equivalent to using scomp from the command line and capture errors
 *
 * The tests need to be run with cmd line param that points to the test case root directory and xmlbeans root
 * ex: -Dcases.location=D:\svnnew\xmlbeans\trunk\test\cases -Dxbean.rootdir=D:\svnnew\xmlbeans\trunk
 */
public class SchemaCompilerTests  extends Common
{
    public static String scompTestFilesRoot = XBEAN_CASE_ROOT + P + "compile" + P + "scomp" + P + "schemacompiler" + P;
    public static String schemaCompOutputDirPath = OUTPUTROOT+ P + "compile" + P + "scomp" + P;

    public  SchemaCompilerTests(String name){
        super(name);
    }

    public void testUnionRedefine()
    {
        List errors = new ArrayList();

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "union_initial.xsd"), new File(scompTestFilesRoot + "union_redefine.xsd")});

        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + "unionred" + P + "src" + P));
        params.setClassesDir(new File(schemaCompOutputDirPath + "unionred" + P +"classes" + P));

        // throws NPE
        try {
            SchemaCompiler.compile(params);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            fail("NPE when executing scomp");
        }

        if (JiraTestBase.printOptionErrMsgs(errors)) {
            Assert.fail("testUnionRedefine() : failures when executing scomp");
        }

    }


}
