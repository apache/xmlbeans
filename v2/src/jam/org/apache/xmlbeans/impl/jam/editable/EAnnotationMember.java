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

package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JAnnotationMember;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JAnnotation;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EAnnotationMember extends JAnnotationMember, EElement {

  public void setValue(Object o);

  public void setValue(String value);

  public void setValue(JAnnotation value);

  public void setValue(boolean value);

  public void setValue(int value);

  public void setValue(short value);

  public void setValue(long value);

  public void setValue(float value);

  public void setValue(double value);

  public void setValue(JClass clazz);

  public void setValue(String[] value);

  public void setValue(JAnnotation[] value);

  public void setValue(boolean[] value);

  public void setValue(int[] value);

  public void setValue(short[] value);

  public void setValue(long[] value);

  public void setValue(float[] value);

  public void setValue(double[] value);

  public void setValue(JClass[] clazz);

//  public void setValue(Object o);
  //FIXME enums?
}