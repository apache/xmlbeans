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

package misc.checkin;

import org.apache.xmlbeans.impl.store.CharUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CharUtilTests {
    private static class Triple {

        Triple(Object src, int off, int cch) {
            _src = src;
            _off = off;
            _cch = cch;
        }

        final Object _src;
        final int _off;
        final int _cch;
    }

    private char randomChar() {
        int n = rnd(27);
        return n == 0 ? ' ' : (char) ('a' + n - 1);
    }

    private String randomString() {
        StringBuilder sb = new StringBuilder();

        for (int i = rnd(128); i >= 0; i--) {
            sb.append(randomChar());
        }

        return sb.toString();
    }

    private interface CharUtilTest {
        void newText(String s);

        int numTexts();

        String getText(int i);

        int length(int i);

        void insert(int i, int j, int off);

        void remove(int i, int off, int cch);
    }

    private static class RealCharUtil implements CharUtilTest {
        final List<Triple> _triples = new ArrayList<>();
        final CharUtil _cu = new CharUtil(1024);

        public void newText(String s) {
            _triples.add(new Triple(s, 0, s.length()));
        }

        public int numTexts() {
            return _triples.size();
        }

        public String getText(int i) {
            Triple t = _triples.get(i);

            return CharUtil.getString(t._src, t._off, t._cch);
        }

        public int length(int i) {
            return _triples.get(i)._cch;
        }

        public void insert(int i, int j, int off) {
            Triple ti = _triples.get(i);
            Triple tj = _triples.get(j);

            Object src =
                _cu.insertChars(off, ti._src, ti._off, ti._cch, tj._src, tj._off, tj._cch);

            _triples.set(i, new Triple(src, _cu._offSrc, _cu._cchSrc));
        }

        public void remove(int i, int off, int cch) {
            Triple ti = _triples.get(i);

            Object src = _cu.removeChars(off, cch, ti._src, ti._off, ti._cch);

            _triples.set(i, new Triple(src, _cu._offSrc, _cu._cchSrc));
        }
    }

    private static class FakeCharUtil implements CharUtilTest {
        final List<String> _strings = new ArrayList<>();

        public void newText(String s) {
            _strings.add(s);
        }

        public int numTexts() {
            return _strings.size();
        }

        public String getText(int i) {
            return _strings.get(i);
        }

        public int length(int i) {
            return _strings.get(i).length();
        }

        public void insert(int i, int j, int off) {
            String si = _strings.get(i);
            String sj = _strings.get(j);

            _strings.set(i, si.substring(0, off) + sj + si.substring(off));
        }

        public void remove(int i, int off, int cch) {
            String si = _strings.get(i);

            _strings.set(i, si.substring(0, off) + si.substring(off + cch));
        }
    }

    @Test
    void testCharUtil() throws Exception {
        RealCharUtil real = new RealCharUtil();
        FakeCharUtil fake = new FakeCharUtil();

        for (int iter = 0; iter < 5000; iter++) {
            switch (rnd(4)) {
                case 0: {
                    String s = randomString();

                    real.newText(s);
                    fake.newText(s);

                    break;
                }

                case 1: {
                    assertEquals(real.numTexts(), fake.numTexts());

                    if (real.numTexts() > 0) {
                        int j = rnd(real.numTexts());

                        assertEquals(real.getText(j), fake.getText(j));
                    }
                }

                case 2: {
                    if (real.numTexts() > 1) {
                        int i = rnd(real.numTexts());
                        int j = rnd(real.numTexts());
                        int off = rnd(real.length(i) + 1);

                        real.insert(i, j, off);
                        fake.insert(i, j, off);

                        assertEquals(real.getText(i), fake.getText(i));
                        assertEquals(real.getText(j), fake.getText(j));
                    }
                }
                case 3: {
                    int i = rnd(real.numTexts());
                    int l = real.length(i);
                    int off = rnd(l + 1);
                    int cch = rnd(l - off + 1);

                    real.remove(i, off, cch);
                    fake.remove(i, off, cch);

                    assertEquals(real.getText(i), fake.getText(i));
                }
            }
        }
    }

    private int rnd(int n) {
        return n == 1 ? 0 : _rnd.nextInt(n - 1);
    }

    private final Random _rnd = new Random(0);

    @Test
    void testThreadLocal() {
        assertNotNull(CharUtil.getThreadLocalCharUtil(), "Should always get a CharUtil from ThreadLocals");
        CharUtil.clearThreadLocals();
        assertNotNull(CharUtil.getThreadLocalCharUtil(), "Should always get a CharUtil from ThreadLocals");
    }
}