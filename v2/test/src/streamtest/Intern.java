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

package streamtest;

import java.util.Date;
import java.util.HashMap;

public class Intern
{
    static final int MAX_ITER = 1000;
    static final int MAX_CREATE = 100;
    static final int MAX_COMP = 100;

    public static void main(String[] args)
    {
        char[] ca = { 'a', 'a', 'a'};

        System.out.println("aaa" == "aaa");
        System.out.println("aaa" == new String("aaa"));
        System.out.println("aaa" == new String(ca));
        System.out.println("aaa" == new String(ca).intern());
        System.out.println("aaa" == String.valueOf(ca));
        System.out.println("aaa" == String.copyValueOf(ca));
        System.out.println("aaa" == "aaa");

        long start;
        double d1 = 0;
        double d2 = 0;
        double d3 = 0;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            System.out.println(qn1.toString());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1 == qn2;
                }
            }
        }
        d1 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalIntern(qn2);
                }
            }
        }
        d2 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create1(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalExtern(qn2);
                }
            }
        }
        d3 = new Date().getTime() - start;

        System.out.println("create1:\t" + d1 + "\t" + d2 + "\t" + d3);

        //create2
        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1 == qn2;
                }
            }
        }
        d1 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalIntern(qn2);
                }
            }
        }
        d2 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create2(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalExtern(qn2);
                }
            }
        }
        d3 = new Date().getTime() - start;

        System.out.println("create2:\t" + d1 + "\t" + d2 + "\t" + d3);

        //create3
        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1 == qn2;
                }
            }
        }
        d1 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)).intern(),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)).
                    intern());
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalIntern(qn2);
                }
            }
        }
        d2 = new Date().getTime() - start;

        start = new Date().getTime();
        for( int i = 0; i<MAX_ITER; i++ )
        {
            Cache cache = new Cache();
            Cache.QName qn1 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
            for( int j=0; j<MAX_CREATE; j++ )
            {
                Cache.QName qn2 = cache.create3(String.valueOf( Math.
                    round(Math.random()*MAX_CREATE/10)),
                    String.valueOf(Math.round(Math.random()*MAX_CREATE/10)));
                for( int k=0; k<MAX_COMP; k++ )
                {
                    boolean b = qn1.equalExtern(qn2);
                }
            }
        }
        d3 = new Date().getTime() - start;

        System.out.println("create3:\t" + d1 + "\t" + d2 + "\t" + d3);
    }
}

class Cache
{
    HashMap h1 = new HashMap();

    QName create1(String l, String u)
    {
        return new QName(l ,u);
    }

    QName create2(String l, String u)
    {
        int hcode = l.hashCode() + 33*u.hashCode();
        Integer key = new Integer(hcode);

        if( h1.containsKey( key ) )
            return (QName)h1.get(key);

        QName qn = new QName(l, u);
        h1.put(key, qn);
        return qn;
    }

    synchronized QName create3(String l, String u)
    {
        return create2(l, u);
    }

    static class QName
    {
        private String local;
        private String uri;

        private QName()
        {}
        private QName(String l, String u)
        {
            local = l;
            uri = u;
        }

        boolean equalIntern( QName qn )
        {
            return local==qn.local && uri==qn.uri;
        }

        boolean equalExtern( QName qn )
        {
            return local.equals(qn.local) && uri.equals(qn.uri);
        }

        public String toString()
        {
            return local + "," + uri;
        }
    }
}

