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

package org.apache.xmlbeans.impl.marshal.util;

import java.lang.reflect.Array;

public class ArrayUtils
{

    public static String arrayToString(Object array)
    {
        if (array == null) return "null";
        if (!array.getClass().isArray()) return array.toString();

        StringBuffer buf = new StringBuffer();
        buf.append("[");

        final int lim = -1 + Array.getLength(array);
        for (int i = 0; i <= lim; i++) {
            Object o = Array.get(array, i);
            buf.append((o == array) ? "(this Array)" : arrayToString(o));
            if (i < lim)
                buf.append(", ");
        }

        buf.append("]");
        return buf.toString();
    }


    public static boolean arrayEquals(Object aa, Object bb)
    {
        if (aa == bb) return true;

        if (aa == null)
            return (bb == null);

        if (!aa.getClass().isArray())
            return aa.equals(bb);

        if (bb == null)
            return (aa == null);

        if (!bb.getClass().isArray())
            return bb.equals(aa);

        final int lena = Array.getLength(aa);
        final int lenb = Array.getLength(bb);

        if (lena != lenb) return false;


        for (int i = 0; i < lena; i++) {
            final Object oa = Array.get(aa, i);
            final Object ob = Array.get(bb, i);
            if (!arrayEquals(oa, ob)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args)
    {
        Object a = new int[]{3,4,5,34423,45};
        Object b = new int[]{3,4,5,34423,45};

        if (a == b) throw new AssertionError("bad objs");
        if (!arrayEquals(a, b)) throw new AssertionError("bad equals");

        a = new int[][]{{1,2,3},{3,4,5}};
        b = new int[][]{{1,2,3},{3,4,5}};

        if (a == b) throw new AssertionError("bad objs");
        if (!arrayEquals(a, b)) throw new AssertionError("bad equals");

        System.out.println("ok");

    }

}
