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

package org.apache.xmlbeans.impl.binding.tylar;


/**
 * These constants describe the physical structure of the tylar archive.
 * The values are subject to change at any time and should not be used
 * outside of this package.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface TylarConstants {

  // ========================================================================
  // Constants

  public static final char SEP = '/';

  public static final String SRC_ROOT     = "META-INF"+SEP+"src";
  public static final String BINDING_FILE = "META-INF"+SEP+"binding-file.xml";
  public static final String BINDING_SER  = "META-INF"+SEP+"binding-file.ser";
  public static final String SCHEMA_DIR   = "META-INF"+SEP+"schemas";

  public static final String STS_PACKAGE  = "schema.system";


  public static final boolean SHOW_XSB_ERRORS = false;

}
