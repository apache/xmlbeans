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
