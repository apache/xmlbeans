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
package org.apache.xmlbeans.impl.jam.annotation;

/**
 * <p>Provides a proxied view of some annotation artifact.  JAM calls the
 * public methods on this class to initialize the proxy with annotation
 * values; those methods should not be called by user-code.</p>
 *
 *

 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class AnnotationProxy {

  // ========================================================================
  // Constants

  /**
   * <p>Name of the member of annotations which have only a single member.
   * As specified in JSR175, that name is "value", but you should use
   * this constant to prevent typos.</p>
   */
  public static final String SINGLE_MEMBER_NAME = "value";

  // ========================================================================
  // Public abstract methods

  /**
   * <p>Called by JAM to initialize a named member on this annotation proxy.
   * </p>
   *
   *
   * <p>
   * (DOCME: provide details on widening and conversion algorithms).
   * </p>
   *
   * @param name
   * @param value
   */
  public abstract void setMemberValue(String name, Object value);

  public abstract ValueMap getValueMap();

  // ========================================================================
  // Public methods

  /**
   * <p>Called by JAM to initialize this proxy's properties using a
   * JSR175 annotation instnce.  The value is guaranteed to be an instance
   * of the 1.5-specific <code>java.lang.annotation.Annotation</code>
   * marker interface.  (It's typed as <code>Object</code> in order to
   * preserve pre-1.5 compatibility).</p>
   *
   * <p>The implementation of this method introspects the given object
   * for JSR175 annotation member methods, invokes them, and then calls
   * <code>setMemberValue</code> using the method's name and invocation
   * result as the name and value.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */
  public void initFromAnnotationInstance(Object jst175annotationObject) {

  }

  /**
   * <p>Called by JAM to initialize this proxy's properties using a
   * javadoc tag.  The parameter will contain the raw contents of the tag,
   * excluding the name declaration (i.e. everything after the '@mytag').</p>
   *
   * <p>The implementation of this method parses the tagContents
   * for 'name = value' pairs delimited by line breaks.  If one or more such
   * pairs is found, <code>setMemberValue</code> is called for each.  If no
   * such pairs are found, <code>setMemberValue()</code> is called using
   * SINGLE_MEMBER_NAME as the name and the tag contents as the value.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */
  public void initFromJavadocTag(String tagContents) {

  }

}