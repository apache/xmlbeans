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
package org.apache.xmlbeans.impl.binding.compile;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * This class addresses the impedence mismatch between Java2Schema's
 * desire to push it's results out and the public API's desire to
 * present them as a generator.  When asked, we just push the results in here
 * and return ('generate') them on demand.
 */
/*package*/ class JavaToSchemaResultImpl implements SchemaGenerator,
        BindingFileGenerator
 {
  // =========================================================================
  // Constants

  private static final int XML_INDENT = 2;

  // ========================================================================
  // Variables

  private Map mTNS2Schema = new HashMap();
  private List mBindingFiles = new ArrayList();

  // ========================================================================
  // Package methods - called by Java2Schema

  /*package*/ void addSchema(SchemaDocument sd) {
    //REVIEW should we throw if they try to add to schemas for same tns?
    if (sd == null) throw new IllegalArgumentException("null SchemaDocument");
    mTNS2Schema.put(sd.getSchema().getTargetNamespace(),sd);
  }

  // REVIEW is there really any reason to allow more than one?
  /*package*/ void addBindingFile(BindingFile bf) { mBindingFiles.add(bf); }

  // ========================================================================
  // SchemaGenerator implementation

  public String[] getTargetNamespaces() {
    String[] out = new String[mTNS2Schema.keySet().size()];
    int n=0;
    for(Iterator i = mTNS2Schema.keySet().iterator(); i.hasNext(); n++) {
      out[n] = (String)i.next();
    }
    return out;
  }

  public void printSchema(String targetNamespace, OutputStream out)
          throws IOException
  {
    SchemaDocument sd = getSchemaFor(targetNamespace);
    if (sd == null) {
      //REVIEW what is the right thing to do?
    } else {
      sd.save(out,
              new XmlOptions().setSavePrettyPrint().
              setSavePrettyPrintIndent(XML_INDENT));
    }
  }

  // REVIEW do we really think that SchemaGenerator clients are never going to
  // want a method like this? i.e. is forcing them to dump stream right?
  private SchemaDocument getSchemaFor(String targetNamespace) {
    return (SchemaDocument)mTNS2Schema.get(targetNamespace);
  }

  // ========================================================================
  // BindingFileGenerator implementation

  public void printBindingFile(OutputStream out) throws IOException {
    if (mBindingFiles.size() > 0) {
      BindingFile bf = (BindingFile)mBindingFiles.get(0);
      BindingConfigDocument doc = bf.write();
      doc.save(out,
               new XmlOptions().setSavePrettyPrint().
               setSavePrettyPrintIndent(XML_INDENT));
    }
  }
}