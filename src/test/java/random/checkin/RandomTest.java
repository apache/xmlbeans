
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
package random.checkin;

import org.apache.xmlbeans.impl.tool.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import random.common.Random;

import java.util.Arrays;


public class RandomTest {

    long seed;
    int iterations;
    int threads;
    int docs;

    @BeforeEach
    public void setUp() {
        seed = System.currentTimeMillis();
        iterations = Integer.MAX_VALUE;
        threads = 1;
        docs = 10;
    }

    @Test
    void testNoQuery() {
        String[] args = new String[]{"-seed", "0", "-i", "20", "-noquery"};
        CommandLine cl = new CommandLine(args,
                Arrays.asList("?", "help", "readonly", "noquery", "nosave"),
                Arrays.asList("seed", "i", "t", "docs", "?", "help"));
         Random.runTest(cl);
    }
}
