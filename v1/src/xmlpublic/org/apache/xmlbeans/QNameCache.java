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

package org.apache.xmlbeans;

import javax.xml.namespace.QName;

/**
 * A cache that can be used to pool QName instances.  Each thread has one.
 */ 
public final class QNameCache
{
    private static final float DEFAULT_LOAD = 0.70f;
    private final float loadFactor;
    private int numEntries = 0;
    private int threshold;
    private int hashmask;
    private QName[] table;

    /**
     * Creates a QNameCache with the given initialCapacity and loadFactor.
     * 
     * @param initialCapacity the number of entries to initially make space for
     * @param loadFactor a number to control the density of the hashtable
     */ 
    public QNameCache(int initialCapacity, float loadFactor)
    {
        assert initialCapacity > 0;
        assert loadFactor > 0 && loadFactor < 1;

        // Find a power of 2 >= initialCapacity
        int capacity = 16;
        while (capacity < initialCapacity) 
            capacity <<= 1;
    
        this.loadFactor = loadFactor;
        this.hashmask = capacity - 1;
        threshold = (int)(capacity * loadFactor);
        table = new QName[capacity];
    }

    /**
     * Creates a QNameCache with the given initialCapacity.
     * 
     * @param initialCapacity the number of entries to initially make space for
     */ 
    public QNameCache(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD);
    }

    /**
     * Fetches a QName with the given namespace and localname.
     * Creates one if one is not found in the cache.
     * 
     * @param uri the namespace
     * @param localName the localname
     * @return the cached QName
     */ 
    public QName getName(String uri, String localName)
    {
        /*
        return new QName(uri, localName);
        */
        assert localName != null;
        
        if (uri == null) uri = "";

        int index = hash(uri, localName) & hashmask;
        while (true) {
            QName q = table[index];
            if (q == null)
            {
                numEntries++;
                if (numEntries >= threshold)
                    rehash();

                return table[index] = new QName(uri, localName);
            }
            else if (equals(q, uri, localName))
                return q;
            else 
                index = (index-1) & hashmask;
        }
    }

    private void rehash()
    {
        int newLength = table.length * 2;
        QName[] newTable = new QName[newLength];
        int newHashmask = newLength - 1;

        for (int i = 0 ; i < table.length ; i++)
        {
            QName q = table[i];
            if (q != null)
            {
                int newIndex = hash(q.getNamespaceURI(), q.getLocalPart()) & newHashmask;
                while (newTable[newIndex] != null)
                    newIndex = (newIndex - 1) & newHashmask;
                newTable[newIndex] = q;
            }
        }

        table = newTable;
        hashmask = newHashmask;
        threshold = (int)(newLength * loadFactor);
    }
    private static int hash(String uri, String localName)
    {
        return (uri.hashCode() << 5) + localName.hashCode();
    }

    private static boolean equals(QName q, String uri, String localName)
    {
        return q.getLocalPart().equals(localName) && q.getNamespaceURI().equals(uri);
    }
}
