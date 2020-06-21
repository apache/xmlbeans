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

package org.apache.xmlbeans;

import javax.xml.namespace.QName;

/**
 * The UserType class represents a mapping between an XML Schema QName and
 * a custom Java class type. It is used during code generation to determine
 * how to convert user-defined simple types to user defined Java classes.
 */
public interface UserType
{
    /**
     * The QName of the simple value that will be converted to a Java class.
     */
    QName getName();

    /**
     * The class name the simple value will be converted to.
     */
    String getJavaName();

    /**
     * A class which provides public static methods to convert {@link SimpleValue}
     * objects to and from the Java type specified by {@link #getJavaName()}.
     */
    String getStaticHandler();
}
