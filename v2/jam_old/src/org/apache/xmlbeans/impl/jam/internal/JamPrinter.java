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

package org.apache.xmlbeans.impl.jam.internal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;

/**
 * Utility class for printing out a JAM.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JamPrinter {

  // ========================================================================
  // Factory

  public static JamPrinter newInstance() {
    return new JamPrinter();
  }

  private JamPrinter() {
  }

  private static final String INDENT = "  ";

  // ========================================================================
  // Public methods

  public void print(JElement root, PrintWriter out) {
    print(root, 0, out);
  }

  public void print(JClassIterator iter, PrintWriter out) {
    while(iter.hasNext()) {
      JClass clazz = iter.nextClass();
      out.println("------------------------------");
      out.println(clazz.getQualifiedName());
      out.println("------------------------------");
      print(clazz,out);
      out.println();
    }
  }

  // ========================================================================
  // Private methods

  private void print(JElement a, int indent, PrintWriter out) {
    indent(indent, out);
    out.print("[");
    out.print(getTypeKey(a));
    out.print("] ");
    if (a instanceof JMethod) {
      out.print(((JMethod) a).getReturnType().getFieldDescriptor());
      out.print(" ");
      out.println(a.getSimpleName());
    } else {
      out.println(a.getSimpleName());
    }
    indent++;
    // print out the annotations
    JAnnotation[] atts = a.getAnnotations();
    if (atts != null) print(atts, indent, out);
    // now recursively print out the children
    JElement[] children = getChildrenFor(a);
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
        if (children[i] != null) print(children[i], indent, out);
      }
    }
  }

  private void print(JAnnotation[] atts, int indent, PrintWriter out) {
    for (int i = 0; i < atts.length; i++) {
      indent(indent, out);
      out.print("<");
      out.print(getTypeKey(atts[i]));
      out.print("> ");
      out.print(atts[i].getName());
      JAnnotation[] subs = atts[i].getAnnotations();
      if (subs.length > 0) {
        out.println();
        print(subs, indent + 1, out);
      } else {
        out.print("=");
        out.println(atts[i].getStringValue());
      }
    }
  }

  private void indent(int indent, PrintWriter out) {
    for (int i = 0; i < indent; i++) out.print(INDENT);
  }

  private String getTypeKey(Object o) {
    if (o == null) return "[?UNKNOWN!]";
    String type = o.getClass().getName();
    int lastDot = type.lastIndexOf(".");
    if (lastDot != -1 && lastDot + 1 < type.length()) {
      type = type.substring(lastDot + 1);
    }
    return type;
  }


  // this is quite gross, but we don't want to expose getChildren() to
  // the public any more
  private static JElement[] getChildrenFor(JElement parent) {
    Collection list = new ArrayList();
    if (parent instanceof JClass) {
      list.addAll(Arrays.asList(((JClass) parent).getDeclaredFields()));
      list.addAll(Arrays.asList(((JClass) parent).getDeclaredMethods()));
      list.addAll(Arrays.asList(((JClass) parent).getConstructors()));
      list.addAll(Arrays.asList(((JClass) parent).getClasses()));
    } else if (parent instanceof JConstructor) {
      list.addAll(Arrays.asList(((JConstructor) parent).getParameters()));
    } else if (parent instanceof JMethod) {
      list.addAll(Arrays.asList(((JMethod) parent).getParameters()));
    }
    JElement[] out = new JElement[list.size()];
    list.toArray(out);
    return out;
  }
}

