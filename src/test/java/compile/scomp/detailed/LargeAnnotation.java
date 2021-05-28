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

import compile.scomp.common.CompileCommon;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.util.LongUTFDataInputStream;
import org.apache.xmlbeans.impl.util.LongUTFDataOutputStream;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class LargeAnnotation {
    @Test
    public void longUTFInOutput() throws IOException {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 0x01FF_FFFF;
        int targetStringLength = 0x0001_FFFF;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(targetStringLength * 2)) {
            Random random = new Random();

            String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(Character::isValidCodePoint)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

            try (LongUTFDataOutputStream ldos = new LongUTFDataOutputStream(bos)) {
                ldos.writeLongUTF(generatedString);

                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                     LongUTFDataInputStream ldis = new LongUTFDataInputStream(bis)) {
                    String str = ldis.readLongUTF();
                    assertEquals(generatedString, str);
                }
            }
        }

        targetStringLength = 0xFFFF;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(targetStringLength + 3)) {
            try (LongUTFDataOutputStream ldos = new LongUTFDataOutputStream(bos)) {
                String exp;
                {
                    char[] chs = new char[0xFFFF];
                    Arrays.fill(chs, 'a');
                    exp = new String(chs);
                }

                ldos.writeUTF(exp);

                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                     LongUTFDataInputStream ldis = new LongUTFDataInputStream(bis)) {
                    String str = ldis.readLongUTF();
                    assertEquals(exp, str);
                }
            }

            String exp;
            {
                char[] chs = new char[0xFFFF];
                Arrays.fill(chs, 'a');
                chs[0xFFFD] = '\u1234';
                chs[0xFFFE] = '\u5678';
                exp = new String(chs);
            }

            bos.reset();
            try (LongUTFDataOutputStream ldos = new LongUTFDataOutputStream(bos)) {
                assertThrows(UTFDataFormatException.class, () -> ldos.writeUTF(exp));
            }

            bos.reset();
            try (LongUTFDataOutputStream ldos = new LongUTFDataOutputStream(bos)) {
                ldos.writeLongUTF(exp);

                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                     LongUTFDataInputStream ldis = new LongUTFDataInputStream(bis)) {
                    String str = ldis.readLongUTF();
                    assertEquals(exp, str);
                }
            }
        }
    }


    @Test
    public void bug235() throws XmlException, IOException {
        ArrayList<XmlError> err = new ArrayList<>();
        XmlOptions xm_opt = new XmlOptions().setErrorListener(err);
        xm_opt.setSavePrettyPrint();

        File dir = new File(CompileCommon.fileLocation + "/largeAnnotation");
        File[] files = dir.listFiles((x) -> x.getName().endsWith(".xsd"));
        assertNotNull(files);
        XmlObject[] schemas = new XmlObject[files.length];
        for (int i=0; i<files.length; i++) {
            schemas[i] = XmlObject.Factory.parse(files[i]);
        }

        SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null,
            schemas, null, XmlBeans.getBuiltinTypeSystem(), null, xm_opt);

    }
}
