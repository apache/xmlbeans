/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

