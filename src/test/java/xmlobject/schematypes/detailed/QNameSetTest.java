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

package xmlobject.schematypes.detailed;

import org.apache.xmlbeans.QNameSetBuilder;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.Random;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QNameSetTest {

    private static String format(int iter, String p, QNameSetBuilder set) {
        return "case# " + iter + " " + p + " " + set.toString();
    }

    @Test
    public void testQNameSets() {
        int iterations = 10000;
        int seed = 0;

        Random rnd = new Random(seed);
        String[] localname = {"a", "b", "c", "d", "e"};
        String[] namespace = {"n1", "n2", "n3", "n4", "n5"};
        int width = localname.length;

        QName[] name = new QName[width * namespace.length];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < namespace.length; j++) {
                name[i + width * j] = new QName(namespace[j], localname[i]);
            }
        }

        Stack<QNameSetBuilder> teststack = new Stack<QNameSetBuilder>(); // stack of sets
        Stack<boolean[]> trackstack = new Stack<boolean[]>(); // stack of boolean arrays

        QNameSetBuilder current = new QNameSetBuilder();
        boolean[] contents = new boolean[width * namespace.length];
        boolean[] temp;
        int i = 0;
        int j = 0;

        for (int l = 0; l < iterations; l++) {
            // apply a random operation

            if (rnd.nextInt(3) != 0) {
                i = rnd.nextInt(width - 1); // don't do the last one for isAll test
                j = rnd.nextInt(namespace.length - 1); // don't do the last one for isAll test
            }

            switch (teststack.size() < 1 ? 24 : rnd.nextInt(iterations - l > teststack.size() ? 24 : 5)) {
                default:
                    // new
                    teststack.push(current);
                    trackstack.push(contents);
                    current = new QNameSetBuilder();
                    contents = new boolean[width * namespace.length];
                    break;

                case 19:
                case 20:
                case 22:
                    // random
                    teststack.push(current);
                    trackstack.push(contents);
                    current = new QNameSetBuilder();
                    contents = new boolean[width * namespace.length];

                    if (rnd.nextInt(2) == 0) {
                        current.invert();
                        for (int k = 0; k < width; k++) {
                            contents[k + width * (namespace.length - 1)] = true;
                        }
                    }

                    for (int h = 0; h < namespace.length - 1; h++) {
                        if (rnd.nextInt(2) == 0)
                            current.removeNamespace(namespace[h]);
                        else {
                            current.addNamespace(namespace[h]);
                            contents[width - 1 + width * h] = true;
                        }
                        for (int k = 0; k < width - 1; k++) {
                            if (rnd.nextInt(2) == 0)
                                current.remove(name[k + width * h]);
                            else {
                                current.add(name[k + width * h]);
                                contents[k + width * h] = true;
                            }
                        }
                    }
                    break;

                case 0:
                    // add set
                    current.addAll(teststack.pop());
                    temp = trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = true;
                    break;

                case 1:
                    // remove set
                    current.removeAll(teststack.pop());
                    temp = trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = false;
                    break;

                case 2:
                    // restrict set
                    current.restrict(teststack.pop());
                    temp = trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (!temp[k])
                            contents[k] = false;
                    break;

                case 3:
                    // union
                    current = new QNameSetBuilder(current.union(teststack.pop()));
                    temp = trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = true;
                    break;

                case 4:
                    // intersect
                    current = new QNameSetBuilder(current.intersect(teststack.pop()));
                    temp = trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (!temp[k])
                            contents[k] = false;
                    break;

                case 5:
                    // copy
                    current = new QNameSetBuilder(current);
                    break;

                case 6:
                case 7:
                case 8:
                    // add one + name[i + width * j];
                    current.add(name[i + width * j]);
                    contents[i + width * j] = true;
                    break;

                case 9:
                case 10:
                case 11:
                    // remove one + name[i + width * j];
                    current.remove(name[i + width * j]);
                    contents[i + width * j] = false;
                    break;

                case 12:
                case 13:
                    // add namespace + namespace[j];
                    current.addNamespace(namespace[j]);
                    for (int k = 0; k < width; k++)
                        contents[k + width * j] = true;
                    break;

                case 14:
                case 15:
                    // remove namespace + namespace[j];
                    current.removeNamespace(namespace[j]);
                    for (int k = 0; k < width; k++)
                        contents[k + width * j] = false;
                    break;

                case 16:
                case 17:
                    // invert
                    current.invert();
                    for (int k = 0; k < width * namespace.length; k++)
                        contents[k] = !contents[k];
                    break;

                case 18:
                    // inverse
                    current = new QNameSetBuilder(current.inverse());
                    for (int k = 0; k < width * namespace.length; k++)
                        contents[k] = !contents[k];
                    break;

            }

            // System.out.println(format(teststack.size(), l, label, current));

            // then, verify current matches contents
            int count = 0;
            for (int k = 0; k < width * namespace.length; k++) {
                assertTrue(format(l, "Content mismatch " + name[k], current), (current.contains(name[k]) == contents[k]));
                if (contents[k])
                    count++;
            }

            assertTrue(format(l, "ERROR: isEmpty is wrong", current), ((count == 0) == current.isEmpty()));

            assertEquals(format(l, "ERROR: isAll is wrong", current), (count == width * namespace.length), current.isAll());

            // test isDisjoint and containsAll
            if (teststack.size() >= 1) {
                boolean disjoint = true;
                temp = trackstack.peek();
                for (int k = 0; k < width * namespace.length; k++) {
                    if (temp[k] && contents[k]) {
                        disjoint = false;
                        break;
                    }
                }
                assertEquals(format(l, "ERROR: disjoint is wrong", current), disjoint, current.isDisjoint(teststack.peek()));

                boolean containsAll = true;
                for (int k = 0; k < width * namespace.length; k++) {
                    if (temp[k] && !contents[k]) {
                        containsAll = false;
                        break;
                    }
                }
                assertEquals(format(l, "ERROR: containsAll is wrong", current), containsAll, current.containsAll(teststack.peek()));
            }
        }
    }
}
