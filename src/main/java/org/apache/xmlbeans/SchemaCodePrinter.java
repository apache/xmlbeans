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

import org.apache.xmlbeans.impl.repackage.Repackager;

import java.io.IOException;
import java.io.Writer;

/**
 * This class is used to provide alternate implementations of the
 * schema Java code generation.
 */

public interface SchemaCodePrinter {
    // implement a method of each pair ... otherwise a stackoverflow is inevitable ...
    @Deprecated
    default void printTypeImpl(Writer writer, SchemaType sType) throws IOException {
        printTypeImpl(writer, sType, null);
    }

    default void printTypeImpl(Writer writer, SchemaType sType, XmlOptions opt) throws IOException {
        printTypeImpl(writer, sType);
    }

    @Deprecated
    default void printType(Writer writer, SchemaType sType) throws IOException {
        printType(writer, sType, null);
    }

    default void printType(Writer writer, SchemaType sType, XmlOptions opt) throws IOException {
        printType(writer, sType);
    }

    void printHolder(Writer writer, SchemaTypeSystem system, XmlOptions opt, Repackager repackager) throws IOException;
}

