/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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

