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

package org.apache.xmlbeans.impl.jam.annogen.internal.joust;

/**
 * Interface for building up metadata declarations in the generated source.
 * Typically, an instance of this class will be written out as javadoc tags
 * or JSR175 annotations.  However, not that this interface is indifferent
 * about that question - JavaOutputStream implementation decides how to
 * actually write out an Annotation.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface Annotation {

  public void setValue(String name, Annotation ann);

  public void setValue(String name, boolean value);

  public void setValue(String name, String value);

  public void setValue(String name, int value);

  public void setValue(String name, long value);

  public void setValue(String name, char value);



}
