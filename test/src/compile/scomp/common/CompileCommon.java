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
package compile.scomp.common;

import org.apache.xmlbeans.XmlError;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * @author jacobd
 * Date: Aug 4, 2004
 */
public class CompileCommon {

    public static final String P = File.separator;

    public static String fwroot = getRootFile();
    public static String caseroot = fwroot +P+"test" + P + "cases";
    //location of files under "cases folder"
    public static String fileLocation = caseroot+P + "xbean" + P + "compile" + P + "scomp" + P;
    public static File outputroot = new File(fwroot, "build" + P + "test" + P + "output");


    /**
     * If System.property for 'xbean.rootdir' == null
     * use '.' as basePath
     * '.' should be where the build.xml file lives
     * @return
     * @throws IllegalStateException
     */
    public static String getRootFile() throws IllegalStateException {
            String baseDir = System.getProperty("xbean.rootdir");
            if(baseDir == null)
                return new File(".").getAbsolutePath();
            else
                return new File(baseDir).getAbsolutePath();
    }

    public static File xbeanCase(String str) {
        return (new File(caseroot + fileLocation, str));
    }

    public static File xbeanOutput(String str) {
        File result = (new File(outputroot, str));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    public static void deltree(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    deltree(new File(dir, list[i]));
            }
            if (!dir.delete())
                System.out.println("Could not delete " + dir);
            //throw new IllegalStateException("Could not delete " + dir);
        }
    }

    public static void listErrors(List errors) {
        for (int i = 0; i < errors.size(); i++) {
            XmlError error = (XmlError) errors.get(i);
            if (error.getSeverity() == XmlError.SEVERITY_ERROR)
                System.out.println(error.toString());
        }
    }

    public static boolean isJDK14() {
        return System.getProperty("java.version").startsWith("1.4");
    }

    /** compare contents of two vectors */
    public static void comparefNameVectors(Vector act, Vector exp) throws Exception
    {
        if (exp == null)
            throw new Exception("Exp was null");
        if (act == null)
            throw new Exception("Act was null");

        if (exp.size() != act.size())
            throw new Exception("Size was not the same");

        //use Vector.equals to compare
        if (!act.equals(exp))
            throw new Exception("Expected FNames did Not Match");

        //check sequence is as expected (not sure if vector.equals does this
        for (int i = 0; i < exp.size(); i++) {
            if (!exp.get(i).equals(act.get(i)))
                throw new Exception("Item[" + i + "]-was not as expected" +
                        "ACT[" + i + "]-" + act.get(i) + " != EXP[" + i + "]-" + exp.get(i));
        }
    }

}
