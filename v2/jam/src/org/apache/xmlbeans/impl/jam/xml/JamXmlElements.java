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
package org.apache.xmlbeans.impl.jam.xml;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JamXmlElements {

  public static final String JAMSERVICE = "jam-service";

  public static final String PACKAGE = "package";
  public static final String CLASS = "class";
  public static final String NAME = "name";

  public static final String CLASS_NAME = "name";
  public static final String PACKAGE_NAME = "package";

  public static final String ISINTERFACE = "is-interface";
  public static final String INTERFACE = "interface";
  public static final String SUPERCLASS = "superclass";
  public static final String MODIFIERS = "modifiers";
  public static final String PARAMETER = "parameter";
  public static final String TYPE = "parameter";
  public static final String CONSTRUCTOR = "constructor";
  public static final String METHOD = "method";
  public static final String FIELD = "field";
  public static final String RETURNTYPE = "return-type";
  public static final String COMMENT = "comment";
  public static final String SOURCEPOSITION = "source-position";
  public static final String LINE = "line";
  public static final String COLUMN = "column";
  public static final String SOURCEURI = "source-uri";
  public static final String VALUE = "value";
  public static final String ANNOTATION = "annotation";
  public static final String ANNOTATIONVALUE = "annotation-value";
}
