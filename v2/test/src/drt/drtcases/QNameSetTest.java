/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package drtcases;

import javax.xml.namespace.QName;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Stack;

import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.QNameSetSpecification;
import junit.framework.TestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class QNameSetTest extends TestCase
{
    public QNameSetTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(QNameSetTest.class); }

    public static String format(int indent, int iter, String p, QNameSetBuilder set)
    {
        /*
        System.err.print(new DecimalFormat("00000 ").format(iter));
        while (indent-- > 0)
            System.err.print(' ');
        System.err.print(p);
        System.err.print(" = ");
        System.err.println(set.dump());
        */

        return "case# " + iter + " " + p + " " + set.toString();

    }


    public void testQNameSets()
    {
        int iterations = 10000;
        int seed = 0;
        int stopat = -1;

        Random rnd = new Random(seed);
        String[] localname = {"a", "b", "c", "d", "e"};
        String[] namespace = {"n1", "n2", "n3", "n4", "n5"};
        int width = localname.length;

        QName[] name = new QName[width * namespace.length];
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < namespace.length; j++)
            {
                name[i + width * j] = new QName(namespace[j], localname[i]);
            }
        }

        Stack teststack = new Stack(); // stack of sets
        Stack trackstack = new Stack(); // stack of boolean arrays

        QNameSetBuilder current = new QNameSetBuilder();
        boolean[] contents = new boolean[width * namespace.length];
        boolean[] temp;
        int i = 0;
        int j = 0;

        for (int l = 0; l < iterations; l++)
        {
            // for debugging

            if (l == stopat)
                System.err.println("We're here");

            // apply a random operation

            if (rnd.nextInt(3) != 0)
            {
                i = rnd.nextInt(width - 1); // don't do the last one for isAll test
                j = rnd.nextInt(namespace.length - 1); // don't do the last one for isAll test
            }

            String label;

            switch (teststack.size() < 1 ? 24 : rnd.nextInt(iterations - l > teststack.size() ? 24 : 5))
            {
                default:
                    teststack.push(current);
                    trackstack.push(contents);
                    current = new QNameSetBuilder();
                    contents = new boolean[width * namespace.length];
                    label = "new";
                    break;

                case 19:
                case 20:
                case 22:
                    teststack.push(current);
                    trackstack.push(contents);
                    current = new QNameSetBuilder();
                    contents = new boolean[width * namespace.length];

                    if (rnd.nextInt(2) == 0)
                    {
                        current.invert();
                        for (int k = 0; k < width; k++)
                        {
                            contents[k + width * (namespace.length - 1)] = true;
                        }
                    }

                    for (int h = 0; h < namespace.length - 1; h++)
                    {
                        if (rnd.nextInt(2) == 0)
                            current.removeNamespace(namespace[h]);
                        else
                        {
                            current.addNamespace(namespace[h]);
                            contents[width - 1 + width * h] = true;
                        }
                        for (int k = 0; k < width - 1; k++)
                        {
                            if (rnd.nextInt(2) == 0)
                                current.remove(name[k + width * h]);
                            else
                            {
                                current.add(name[k + width * h]);
                                contents[k + width * h] = true;
                            }
                        }
                    }
                    label = "random";
                    break;

                case 0:
                    current.addAll((QNameSetSpecification)teststack.pop());
                    temp = (boolean[])trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = true;
                    label = "add set";
                    break;

                case 1:
                    current.removeAll((QNameSetSpecification)teststack.pop());
                    temp = (boolean[])trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = false;
                    label = "remove set";
                    break;

                case 2:
                    current.restrict((QNameSetSpecification)teststack.pop());
                    temp = (boolean[])trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (!temp[k])
                            contents[k] = false;
                    label = "restrict set";
                    break;

                case 3:
                    label = "union";
                    current = new QNameSetBuilder(current.union((QNameSetSpecification)teststack.pop()));
                    temp = (boolean[])trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (temp[k])
                            contents[k] = true;
                    label = "union";
                    break;

                case 4:
                    label = "intersect";
                    current = new QNameSetBuilder(current.intersect((QNameSetSpecification)teststack.pop()));
                    temp = (boolean[])trackstack.pop();
                    for (int k = 0; k < width * namespace.length; k++)
                        if (!temp[k])
                            contents[k] = false;
                    label = "intersect";
                    break;

                case 5:
                    current = new QNameSetBuilder(current);
                    label = "copy";
                    break;

                case 6:
                case 7:
                case 8:
                    current.add(name[i + width * j]);
                    contents[i + width * j] = true;
                    label = "add one " + name[i + width * j];
                    break;

                case 9:
                case 10:
                case 11:
                    current.remove(name[i + width * j]);
                    contents[i + width * j] = false;
                    label = "remove one " + name[i + width * j];
                    break;

                case 12:
                case 13:
                    current.addNamespace(namespace[j]);
                    for (int k = 0; k < width; k++)
                        contents[k + width * j] = true;
                    label = "add namespace " + namespace[j];
                    break;

                case 14:
                case 15:
                    current.removeNamespace(namespace[j]);
                    for (int k = 0; k < width; k++)
                        contents[k + width * j] = false;
                    label = "remove namespace " + namespace[j];
                    break;

                case 16:
                case 17:
                    current.invert();
                    for (int k = 0; k < width * namespace.length; k++)
                        contents[k] = !contents[k];
                    label = "invert";
                    break;

                case 18:
                    current = new QNameSetBuilder(current.inverse());
                    for (int k = 0; k < width * namespace.length; k++)
                        contents[k] = !contents[k];
                    label = "inverse";
                    break;

            }

            // System.out.println(format(teststack.size(), l, label, current));

            // then, verify current matches contents
            int count = 0;
            for (int k = 0; k < width * namespace.length; k++)
            {
                Assert.assertTrue(format(0, l, "Content mismatch " + name[k], current), (current.contains(name[k]) == contents[k]));
                {
                    // testprint(0, l, "ERROR ON " + name[k], current);
                    // testprint(0, l, "expected " + contents[k] + ", got " + !contents[k], current);
                    // System.exit(1);
                }
                if (contents[k])
                    count++;
            }

            Assert.assertTrue(format(0, l, "ERROR: isEmpty is wrong", current), ((count == 0) == current.isEmpty()));
            {
                // testprint(0, l, "ERROR: isEmpty is wrong", current);
                // testprint(0, l, "expected " + (count == 0) + ", got " + !(count == 0), current);
                // System.exit(1);
            }

            Assert.assertTrue(format(0, l, "ERROR: isAll is wrong", current), (count == width * namespace.length) == current.isAll());
            {
                // testprint(0, l, "ERROR: isAll is wrong", current);
                // testprint(0, l, "expected " + (count == width * namespace.length) + ", got " + !(count == width * namespace.length), current);
                // System.exit(1);
            }

            // test isDisjoint and containsAll
            if (teststack.size() >= 1)
            {
                boolean disjoint = true;
                temp = (boolean[])trackstack.peek();
                for (int k = 0; k < width * namespace.length; k++)
                {
                    if (temp[k] && contents[k])
                    {
                        disjoint = false;
                        break;
                    }
                }
                Assert.assertTrue(format(0, l, "ERROR: disjoint is wrong", current), disjoint == current.isDisjoint((QNameSetSpecification)teststack.peek()));
                {
                    // testprint(0, l, "ERROR: disjoint is wrong", current);
                    // testprint(0, l, "expected " + disjoint + ", got " + !disjoint, (QNameSetBuilder)teststack.peek());
                    // System.exit(1);
                }
                
                
                boolean containsAll = true;
                for (int k = 0; k < width * namespace.length; k++)
                {
                    if (temp[k] && !contents[k])
                    {
                        containsAll = false;
                        break;
                    }
                }
                Assert.assertTrue(format(0, l, "ERROR: containsAll is wrong", current), containsAll == current.containsAll((QNameSetSpecification)teststack.peek()));
            }
            
        }
    }
}
