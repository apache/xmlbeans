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

package org.apache.xmlbeans.impl.store;

// BUGBUG: ***TLL*** Why isn't this exposed in org.apache.xmlbeans????

import org.apache.xmlbeans.XmlObject;
import java.util.Iterator;


/**
 * Interface to represent lists of XmlObject instances returned
 * from a query. Used also as a convenient typed way of collecting
 * XmlObject instances.
 */
public interface XmlObjectList {

    /**
     * Adds a XmlObject to the end of the list.
     *
     * @param xml the XmlObject instance to add to the list
     */
    void add(XmlObject xml);

    /**
     * Appends the contents of another list to this list.
     *
     * @param xmlList the list which is to be appended to this list.
     */
    void addAll(XmlObjectList xmlList);

    /**
     * Appends an array of XmlObject instances to this list.
     *
     * @param xmlArray the array which is to be appended to this list.
     */
    void addAll(XmlObject[] xmlArray);

    /**
     * Clears out the contents of this list. After this operation,
     * {@link #isEmpty() isEmpty()} will return <code>true</code>.
     */
    void clear();

    /**
     * Gets the XmlObject at a specified position in the list.
     *
     * @param index the specified position
     * @return the XmlObject at the position in this list.
     *
     * @throws IndexOutOfBoundsException if
     *         <code>(index < 0) || (index >= size())</code>
     */
    XmlObject get(int index);

    /**
     * Tests if this XmlList is empty. This is equivalent to
     * <code>{@link #size() size()} == 0</code>.
     *
     * @return boolean indicating if this list is empty.
     */
    boolean isEmpty();

    /**
     * Returns an Iterator over all the XmlObject instances contained in this
     * list. This iterator will throw an UnsupportedOperationException in
     * response to its remove method.
     *
     * @return Iterator over the contents of this list
     */
    Iterator iterator();

    /**
     * Returns the number of XmlObject instances contained in this list.
     *
     * @return int size of the list.
     */
    int size();

    /**
     * Returns a copy of the contents of the XmlList in a newly created array of
     * XmlObjects.
     *
     * @return array of XmlObject instances which is a copy of the contents of
     *         the list.
     */
    XmlObject[] toArray();



    /**
     * Static factory class for creating new instances of XmlObjectList
     */
    public static final class Factory
    {
        // the class to instantiate
        private static final Class CLASS;
        static {
            try {
                CLASS = Class.forName("com.bea.wli.variables.XmlObjectListImpl");
            } catch (Throwable t) {
                IllegalStateException ise = new IllegalStateException("Cannot load XmlObjectListImpl.");
                ise.initCause(t);
                throw ise;
            }
        }


        /**
         * creates a new empty XmlObjectList instance.
         *
         * @return a new empty XmlObjectList instance
         */
        public static XmlObjectList newInstance() {
            try {
                return (XmlObjectList)CLASS.newInstance();
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }


        /**
         * creates a new XmlObjectList instance, filled with the elements
         * of the given list
         *
         * @return a new XmlObjectList instance.
         */
        public static XmlObjectList newInstance(XmlObjectList list) {
            try {
                XmlObjectList newList = (XmlObjectList)CLASS.newInstance();
                newList.addAll(list);
                return newList;
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }


        /**
         * creates a new XmlObjectList instance given an array of XmlObjects.
         *
         * @return a new XmlObjectList instance.
         */
        public static XmlObjectList newInstance(XmlObject[] xmlObjects) {
            try {
                XmlObjectList newList = (XmlObjectList) CLASS.newInstance();
                newList.addAll(xmlObjects);
                return newList;
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }
    }
}
