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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;

/**
 * This class is the hook which causes the SchemaTypeSystem to be loaded when
 * a generated class is used.  It isn't used by the runtime directly, instead
 * this class is used by the schema compiler as a template class.  By using a
 * template class, the SchemaTypeSystemImpl can create all the binary files
 * required without needing to rely on javac.  The generated source still
 * requires a java compiler.
 *
 * @see SchemaTypeSystemImpl#save(org.apache.xmlbeans.Filer)
 */
//
// !!! It's important that there never NEVER be any references to this class because
// !!! the static initializer will fail.  This class must only be used as a class file.
// !!! If this scares you, turn back now !!!
//
// !!! If you modify this class, you will have to run bootstrap.
// !!! If this scares you, turn back now !!!
//
public final class TypeSystemHolder extends SchemaTypeSystemImpl {
    // TODO: provide parameter-less parent constructor
    private TypeSystemHolder() { super(TypeSystemHolder.class); }

    // the type system
    public static final TypeSystemHolder typeSystem = new TypeSystemHolder();

    // Commenting out this line has the effect of not loading all components in a
    // typesystem upfront, but just as they are needed, which may improve
    // performance significantly
    //static { typeSystem.resolve(); }
}
