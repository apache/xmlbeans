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

package org.apache.xmlbeans.impl.binding.joust;

/**
 * Creates instances of Expression to be used in conjunction with a
 * JavaOutputStream.  Instances of ExpressionFactory are retrieved via
 * JavaOutputStream.getExpressionFactory().
 *
 * We'll probably have to add more methods here to accommodate more
 * primitives, arrays and so forth.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface ExpressionFactory {

  /**
   * Returns an expression representing a constant boolean value.
   */
  public Expression createBoolean(boolean value);

  /**
   * Returns an expression representing a literal string.
   */
  public Expression createString(String value);

  /**
   * Returns an expression representing a constant int.
   */
  public Expression createInt(int value);

  /**
   * Returns an expression representing the 'null' token.
   */
  public Expression createNull();

  /**
   * Returns an expresion whose text representation is the given string.
   */
  public Expression createVerbatim(String value);

}