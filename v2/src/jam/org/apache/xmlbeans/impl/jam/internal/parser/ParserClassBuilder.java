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
package org.apache.xmlbeans.impl.jam.internal.parser;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.editable.EService;
import org.apache.xmlbeans.impl.jam.editable.EServiceFactory;
import org.apache.xmlbeans.impl.jam.editable.EServiceParams;
import org.apache.xmlbeans.impl.jam.provider.JStoreParams;
import org.apache.xmlbeans.impl.jam.provider.JPath;
import org.apache.xmlbeans.impl.jam.provider.EClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.BaseJClassLoader;
import org.apache.xmlbeans.impl.jam.internal.JamPrinter;
import org.apache.xmlbeans.impl.jam.internal.parser.generated.JavaLexer;
import org.apache.xmlbeans.impl.jam.internal.parser.generated.JavaParser;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ParserClassBuilder implements EClassBuilder {

  // ========================================================================
  // Constants

  private static final boolean VERBOSE = false;

  // ========================================================================
  // Variables

  private JPath mSourcePath;
  private boolean mVerbose = VERBOSE;
  private PrintWriter mOut;

  // ========================================================================
  // Constructors

  private ParserClassBuilder() {}

  public ParserClassBuilder(JStoreParams jsp) {
    mSourcePath = jsp.getInputSourcepath();
    mOut = jsp.getOut();
  }

  // ========================================================================
  // BaseJClassLoader implementation

  public EClass build(String pkg, String name, JClassLoader loader) {
    if (loader == null) throw new IllegalArgumentException("null loader");
    if (pkg == null) throw new IllegalArgumentException("null pkg");
    if (name == null) throw new IllegalArgumentException("null name");
    String filespec = pkg.replace('.',File.separatorChar)+
            File.separatorChar+name+".java";
    if (name.indexOf(".") != -1) {
      throw new IllegalArgumentException("inner classes are NYI at the moment");
    }
    InputStream in = mSourcePath.findInPath(filespec);
    if (in == null) {
      if (mVerbose) {
        mOut.println("[ParserClassBuilder] could not find "+filespec);
      }
      return null;
    } else {
      if (mVerbose) {
        mOut.println("[ParserClassBuilder] loading class "+pkg+"  "+name);
        mOut.println("[ParserClassBuilder] from file "+filespec);
      }
    }
    Reader rin = new InputStreamReader(in);
    try {
      EClass[] clazz = parse(rin,loader);
      if (clazz.length > 1) {
        System.out.println("WARNING: multiple classes per package are not "+
                           "handled correctly at the moment.  FIXME");
      }
      return clazz[0]; //FIXME deal properly with multiple classes
    } catch(Throwable t) {
      t.printStackTrace();
    }
    return null;
  }

  // ========================================================================
  // Private methods

  private static EClass[] parse(Reader in, JClassLoader loader) throws Exception {
    if (in == null) throw new IllegalArgumentException("null in");
    if (loader == null) throw new IllegalArgumentException("null loader");
    JavaLexer lexer = new JavaLexer(in);
    JavaParser parser = new JavaParser(lexer);
    parser.setClassLoader(loader);
    parser.start();
    return parser.getResults();
  }

  // ========================================================================
  // main method

  public static void main(String[] files) {
    new ParserClassBuilder.MainTool().process(files);
  }

  static class MainTool {
    private List mFailures = new ArrayList();
    private int mCount = 0;
    private PrintWriter mOut = new PrintWriter(System.out);
    private long mStartTime = System.currentTimeMillis();

    public void process(String[] files) {
      try {
        for(int i=0; i<files.length; i++) {
          File input = new File(files[i]);
          parse(new ParserClassBuilder(),input);
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
      mOut.println("\n\n\n");
      int fails = mFailures.size();
      if (fails != 0) {
        mOut.println("The following files failed to parse:");
        for(int i=0; i<fails; i++) {
          mOut.println(((File)mFailures.get(i)).getAbsolutePath());
        }
      }
      mOut.println((((mCount-fails)*100)/mCount)+
                   "% ("+(mCount-fails)+"/"+mCount+") "+
                   "of input java files successfully parsed.");
      mOut.println("Total time: "+
                   ((System.currentTimeMillis()-mStartTime)/1000)+
                   " seconds.");
      mOut.flush();
      System.out.flush();
      System.err.flush();
    }

    private void parse(ParserClassBuilder parser, File input)
            throws Exception
    {
      System.gc();
      if (input.isDirectory()) {
        if (VERBOSE) mOut.println("scanning in directory "+input);
        File[] files = input.listFiles();
        for(int i=0; i<files.length; i++) {
          parse(parser,files[i]);
        }
      } else {
        if (!input.getName().endsWith(".java")) return;
        if (VERBOSE) {
          mOut.println("-----------------------------------------");
          mOut.println("processing "+input);
          mOut.println("-----------------------------------------");
        }
        mCount++;
        EClass[] results = null;
        try {
          results = parser.parse(new FileReader(input),null);
          if (results == null) {
            mOut.println("[error, parser result is null]");
            addFailure(input);
          } else {
            if (VERBOSE) {
              JamPrinter jp = JamPrinter.newInstance();
              for(int i=0; i<results.length; i++) {
                jp.print(results[i],mOut);
              }
            }
          }
        } catch(Throwable e) {
          e.printStackTrace(mOut);
          addFailure(input);
        }
      }
    }

    private void addFailure(File file) {
      mFailures.add(file);
    }
  }
}