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
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.common;

import java.io.*;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Provides utility services for jarring and unjarring files and directories.
 * Note that a given instance of JarHelper is not threadsafe with respect to
 * multiple jar operations.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JarHelper
{
  // ========================================================================
  // Constants

  private static final int BUFFER_SIZE = 2156;

  // ========================================================================
  // Variables

  private byte[] mBuffer = new byte[BUFFER_SIZE];
  private int mByteCount = 0;
  private boolean mVerbose = false;

  // ========================================================================
  // Constructor

  /**
   * Instantiates a new JarHelper.
   */
  public JarHelper() {}

  // ========================================================================
  // Public methods

  /**
   * Jars a given directory or single file into a JarOutputStream.
   */
  public void jarDir(File dirOrFile2Jar, File destJar)
          throws IOException {
    FileOutputStream fout = new FileOutputStream(destJar);
    JarOutputStream jout = new JarOutputStream(fout);
    //jout.setLevel(0);
    try {
      jarDir(dirOrFile2Jar, jout, null);
    } catch(IOException ioe) {
      throw ioe;
    } finally {
      jout.close();
      fout.close();
    }
  }

  /**
   * Unjars a given jar file into a given directory.
   */
  public void unjarDir(File jarFile, File destDir) throws IOException {
    BufferedOutputStream dest = null;
    FileInputStream fis = new FileInputStream(jarFile);
    unjar(fis,destDir);
  }

  /**
   * Given an InputStream on a jar file, unjars the contents into the given
   * directory.
   */
  public void unjar(InputStream in, File destDir) throws IOException {
    BufferedOutputStream dest = null;
    JarInputStream jis = new JarInputStream(in);
    JarEntry entry;
    while ((entry = jis.getNextJarEntry()) != null) {
      if (entry.isDirectory()) {
        File dir = new File(destDir,entry.getName());
        dir.mkdir();
        if (entry.getTime() != -1) dir.setLastModified(entry.getTime());
        continue;
      }
      int count;
      byte data[] = new byte[BUFFER_SIZE];
      File destFile = new File(destDir, entry.getName());
      if (mVerbose)
        System.out.println("unjarring " + destFile +
                           " from " + entry.getName());
      FileOutputStream fos = new FileOutputStream(destFile);
      dest = new BufferedOutputStream(fos, BUFFER_SIZE);
      while ((count = jis.read(data, 0, BUFFER_SIZE)) != -1) {
        dest.write(data, 0, count);
      }
      dest.flush();
      dest.close();
      if (entry.getTime() != -1) destFile.setLastModified(entry.getTime());
    }
    jis.close();
  }

  public void setVerbose(boolean b) {
    mVerbose = b;
  }

  // ========================================================================
  // Private methods

  /**
   * Recursively jars up the given path under the given directory.
   */
  private void jarDir(File dirOrFile2jar, JarOutputStream jos, String path)
          throws IOException {
    if (mVerbose) System.out.println("checking " + dirOrFile2jar);
    if (dirOrFile2jar.isDirectory()) {
      String[] dirList = dirOrFile2jar.list();
      String subPath = (path == null)? File.separator :
              (path+dirOrFile2jar.getName()+File.separator);
      JarEntry je = new JarEntry(subPath);
      je.setTime(dirOrFile2jar.lastModified());
      jos.putNextEntry(je);
      jos.flush();
      jos.closeEntry();
      for (int i = 0; i < dirList.length; i++) {
        File f = new File(dirOrFile2jar, dirList[i]);
        jarDir(f,jos,subPath);
      }
    } else {
      if (mVerbose) System.out.println("adding " + dirOrFile2jar);
      FileInputStream fis = new FileInputStream(dirOrFile2jar);
      try {
        JarEntry entry = new JarEntry(path+dirOrFile2jar.getName());
        entry.setTime(dirOrFile2jar.lastModified());
        jos.putNextEntry(entry);
        while ((mByteCount = fis.read(mBuffer)) != -1) {
          jos.write(mBuffer, 0, mByteCount);
          if (mVerbose) System.out.println("wrote " + mByteCount + " bytes");
        }
        jos.flush();
        jos.closeEntry();
      } catch (IOException ioe) {
        throw ioe;
      } finally {
        fis.close();
      }
    }
  }
}