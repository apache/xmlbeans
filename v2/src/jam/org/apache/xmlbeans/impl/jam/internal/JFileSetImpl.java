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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.xmlbeans.impl.jam.JFileSet;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JFileSetImpl implements JFileSet {

  // ========================================================================
  // Variables

  private List mIncludes = new ArrayList();
  private List mExcludes = new ArrayList();
  private String mClasspath = null;
  private boolean mCaseSensitive = true;
  private File mBasedir;
  protected String[] includes;
  protected String[] excludes;
  protected Vector filesIncluded;
  protected Vector dirsIncluded;

  // ========================================================================
  // Constructors

  public JFileSetImpl(File root) {
    if (root == null) throw new IllegalArgumentException("null root");
    if (!root.exists()) {
      throw new IllegalArgumentException(root + " does not exist");
    }
    if (!root.isDirectory()) {
      throw new IllegalArgumentException(root + " is not a directory");
    }
    mBasedir = root;
  }

  // ========================================================================
  // Public methods

  public File[] getFiles() throws IOException {
    String[] includes = new String[mIncludes.size()];
    mIncludes.toArray(includes);
    String[] excludes = new String[mExcludes.size()];
    mExcludes.toArray(excludes);
    setIncludes(includes);
    setExcludes(excludes);
    scan();
    String[] names = getIncludedFiles();
    File[] out = new File[names.length];
    for (int i = 0; i < names.length; i++) {
      out[i] = new File(mBasedir, names[i]);
    }
    return out;
  }

  // ========================================================================
  // main() method

  public static void main(String[] args) {
    try {
      JFileSetImpl fs = new JFileSetImpl(new File(args[0]));
      for (int i = 1; i < args.length; i++) {
        fs.include(args[i]);
      }
      File[] files = fs.getFiles();
      for (int i = 0; i < files.length; i++) {
        System.out.println(files[i].toString());
      }
      System.out.flush();
    } catch (Throwable t) {
      t.printStackTrace();
      System.out.flush();
      System.exit(-1);
    }
  }

  // ========================================================================
  // JFileSet implementation

  public void include(String pattern) {
    mIncludes.add(pattern);
  }

  public void exclude(String pattern) {
    mExcludes.add(pattern);
  }

  public void setCaseSensitive(boolean b) {
    mCaseSensitive = b;
  }

  public void setClasspath(String classpath) {
    mClasspath = classpath;
  }

    // =========================================================================
  // Public methods

  public String getClasspath() { return mClasspath; }

  // ========================================================================
  // Directory scanner stuff

  /**
   * Sets the list of include patterns to use. All '/' and '\' characters
   * are replaced by <code>File.separatorChar</code>, so the separator used
   * need not match <code>File.separatorChar</code>.
   * <p>
   * When a pattern ends with a '/' or '\', "**" is appended.
   *
   * @param includes A list of include patterns.
   *                 May be <code>null</code>, indicating that all files
   *                 should be included. If a non-<code>null</code>
   *                 list is given, all elements must be
   * non-<code>null</code>.
   */
  private void setIncludes(String[] includes) {
    if (includes == null) {
      this.includes = null;
    } else {
      this.includes = new String[includes.length];
      for (int i = 0; i < includes.length; i++) {
        String pattern;
        pattern = includes[i].replace('/', File.separatorChar).
                replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
          pattern += "**";
        }
        this.includes[i] = pattern;
      }
    }
  }


  /**
   * Sets the list of exclude patterns to use. All '/' and '\' characters
   * are replaced by <code>File.separatorChar</code>, so the separator used
   * need not match <code>File.separatorChar</code>.
   * <p>
   * When a pattern ends with a '/' or '\', "**" is appended.
   *
   * @param excludes A list of exclude patterns.
   *                 May be <code>null</code>, indicating that no files
   *                 should be excluded. If a non-<code>null</code> list is
   *                 given, all elements must be non-<code>null</code>.
   */
  private void setExcludes(String[] excludes) {
    if (excludes == null) {
      this.excludes = null;
    } else {
      this.excludes = new String[excludes.length];
      for (int i = 0; i < excludes.length; i++) {
        String pattern;
        pattern = excludes[i].replace('/', File.separatorChar).
                replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
          pattern += "**";
        }
        this.excludes[i] = pattern;
      }
    }
  }


  /**
   * Scans the base directory for files which match at least one
   * include pattern and don't match any exclude patterns. If there
   * are selectors then the files must pass muster there, as well.
   *
   * @exception IllegalStateException if the base directory was set
   *            incorrectly (i.e. if it is <code>null</code>, doesn't exist,
   *            or isn't a directory).
   */
  private void scan() throws IllegalStateException, IOException {
    if (includes == null) {
      // No includes supplied, so set it to 'matches all'
      includes = new String[1];
      includes[0] = "**";
    }
    if (excludes == null) {
      excludes = new String[0];
    }
    filesIncluded = new Vector();
    dirsIncluded = new Vector();
    if (isIncluded("")) {
      if (!isExcluded("")) {
        dirsIncluded.addElement("");
      }
    }
    scandir(mBasedir, "", true);
  }

  /**
   * Scans the given directory for files and directories. Found files and
   * directories are placed in their respective collections, based on the
   * matching of includes, excludes, and the selectors.  When a directory
   * is found, it is scanned recursively.
   *
   * @param dir   The directory to scan. Must not be <code>null</code>.
   * @param vpath The path relative to the base directory (needed to
   *              prevent problems with an absolute path when using
   *              dir). Must not be <code>null</code>.
   * @param fast  Whether or not this call is part of a fast scan.
   *
   * @see #filesIncluded
   * @see #dirsIncluded
   */
  private void scandir(File dir, String vpath, boolean fast)
          throws IOException {
    String[] newfiles = dir.list();
    if (newfiles == null) {
      /*
       * two reasons are mentioned in the API docs for File.list
       * (1) dir is not a directory. This is impossible as
       *     we wouldn't get here in this case.
       * (2) an IO error occurred (why doesn't it throw an exception
       *     then???)
       */
      throw new IOException("IO error scanning directory "
              + dir.getAbsolutePath());
    }
    /*
      if (!followSymlinks) {
      Vector noLinks = new Vector();
      for (int i = 0; i < newfiles.length; i++) {
      try {
      if (fileUtils.isSymbolicLink(dir, newfiles[i])) {
      String name = vpath + newfiles[i];
      File   file = new File(dir, newfiles[i]);
      if (file.isDirectory()) {
      dirsExcluded.addElement(name);
      } else {
      filesExcluded.addElement(name);
      }
      } else {
      noLinks.addElement(newfiles[i]);
      }
      } catch (IOException ioe) {
      String msg = "IOException caught while checking "
      + "for links, couldn't get cannonical path!";
      // will be caught and redirected to Ant's logging system
      System.err.println(msg);
      noLinks.addElement(newfiles[i]);
      }
      }
      newfiles = new String[noLinks.size()];
      noLinks.copyInto(newfiles);
      }*/
    for (int i = 0; i < newfiles.length; i++) {
      String name = vpath + newfiles[i];
      File file = new File(dir, newfiles[i]);
      if (file.isDirectory()) {
        if (isIncluded(name) && !isExcluded(name)) {
          dirsIncluded.addElement(name);
          scandir(file, name + File.separator, fast);
        } else {
          if (couldHoldIncluded(name)) {
            scandir(file, name + File.separator, fast);
          }
        }
      } else if (file.isFile()) {
        if (isIncluded(name)) {
          if (!isExcluded(name)) {
            filesIncluded.addElement(name);
          }
        }
      }
    }
  }

  /**
   * Tests whether or not a name matches against at least one include
   * pattern.
   *
   * @param name The name to match. Must not be <code>null</code>.
   * @return <code>true</code> when the name matches against at least one
   *         include pattern, or <code>false</code> otherwise.
   */
  private boolean isIncluded(String name) {
    for (int i = 0; i < includes.length; i++) {
      if (matchPath(includes[i], name, mCaseSensitive)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether or not a name matches the start of at least one include
   * pattern.
   *
   * @param name The name to match. Must not be <code>null</code>.
   * @return <code>true</code> when the name matches against the start of at
   *         least one include pattern, or <code>false</code> otherwise.
   */
  private boolean couldHoldIncluded(String name) {
    for (int i = 0; i < includes.length; i++) {
      if (matchPatternStart(includes[i], name, mCaseSensitive)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether or not a name matches against at least one exclude
   * pattern.
   *
   * @param name The name to match. Must not be <code>null</code>.
   * @return <code>true</code> when the name matches against at least one
   *         exclude pattern, or <code>false</code> otherwise.
   */
  private boolean isExcluded(String name) {
    for (int i = 0; i < excludes.length; i++) {
      if (matchPath(excludes[i], name, mCaseSensitive)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Returns the names of the files which matched at least one of the
   * include patterns and none of the exclude patterns.  The names are
   * relative to the base directory.
   *
   * @return the names of the files which matched at least one of the
   *         include patterns and none of the exclude patterns.
   */
  private String[] getIncludedFiles() {
    String[] files = new String[filesIncluded.size()];
    filesIncluded.copyInto(files);
    return files;
  }

  /**
   * Returns the names of the directories which matched at least one
   * of the include patterns and none of the exclude patterns.  The
   * names are relative to the base directory.
   *
   * @return the names of the directories which matched at least one of the
   * include patterns and none of the exclude patterns.
   */
  private String[] getIncludedDirectories() {
    String[] directories = new String[dirsIncluded.size()];
    dirsIncluded.copyInto(directories);
    return directories;
  }


  // ========================================================================
  // SelectorUtils stuff

  /**
   * Tests whether or not a given path matches the start of a given
   * pattern up to the first "**".
   * <p>
   * This is not a general purpose test and should only be used if you
   * can live with false positives. For example, <code>pattern=**\a</code>
   * and <code>str=b</code> will yield <code>true</code>.
   *
   * @param pattern The pattern to match against. Must not be
   *                <code>null</code>.
   * @param str     The path to match, as a String. Must not be
   *                <code>null</code>.
   *
   * @return whether or not a given path matches the start of a given
   * pattern up to the first "**".
   */
  public static boolean matchPatternStart(String pattern, String str) {
    return matchPatternStart(pattern, str, true);
  }

  /**
   * Tests whether or not a given path matches the start of a given
   * pattern up to the first "**".
   * <p>
   * This is not a general purpose test and should only be used if you
   * can live with false positives. For example, <code>pattern=**\a</code>
   * and <code>str=b</code> will yield <code>true</code>.
   *
   * @param pattern The pattern to match against. Must not be
   *                <code>null</code>.
   * @param str     The path to match, as a String. Must not be
   *                <code>null</code>.
   * @param mCaseSensitive Whether or not matching should be performed
   *                        case sensitively.
   *
   * @return whether or not a given path matches the start of a given
   * pattern up to the first "**".
   */
  public static boolean matchPatternStart(String pattern, String str,
                                          boolean mCaseSensitive) {
    // When str starts with a File.separator, pattern has to start with a
    // File.separator.
    // When pattern starts with a File.separator, str has to start with a
    // File.separator.
    if (str.startsWith(File.separator) !=
            pattern.startsWith(File.separator)) {
      return false;
    }
    Vector patDirs = tokenizePath(pattern);
    Vector strDirs = tokenizePath(str);
    int patIdxStart = 0;
    int patIdxEnd = patDirs.size() - 1;
    int strIdxStart = 0;
    int strIdxEnd = strDirs.size() - 1;
    // up to first '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      String patDir = (String) patDirs.elementAt(patIdxStart);
      if (patDir.equals("**")) {
        break;
      }
      if (!match(patDir, (String) strDirs.elementAt(strIdxStart),
              mCaseSensitive)) {
        return false;
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      return true;
    } else if (patIdxStart > patIdxEnd) {
      // String not exhausted, but pattern is. Failure.
      return false;
    } else {
      // pattern now holds ** while string is not exhausted
      // this will generate false positives but we can live with that.
      return true;
    }
  }

  /**
   * Tests whether or not a given path matches a given pattern.
   *
   * @param pattern The pattern to match against. Must not be
   *                <code>null</code>.
   * @param str     The path to match, as a String. Must not be
   *                <code>null</code>.
   *
   * @return <code>true</code> if the pattern matches against the string,
   *         or <code>false</code> otherwise.
   */
  public static boolean matchPath(String pattern, String str) {
    return matchPath(pattern, str, true);
  }

  /**
   * Tests whether or not a given path matches a given pattern.
   *
   * @param pattern The pattern to match against. Must not be
   *                <code>null</code>.
   * @param str     The path to match, as a String. Must not be
   *                <code>null</code>.
   * @param mCaseSensitive Whether or not matching should be performed
   *                        case sensitively.
   *
   * @return <code>true</code> if the pattern matches against the string,
   *         or <code>false</code> otherwise.
   */
  public static boolean matchPath(String pattern, String str,
                                  boolean mCaseSensitive) {
    // When str starts with a File.separator, pattern has to start with a
    // File.separator.
    // When pattern starts with a File.separator, str has to start with a
    // File.separator.
    if (str.startsWith(File.separator) !=
            pattern.startsWith(File.separator)) {
      return false;
    }
    Vector patDirs = tokenizePath(pattern);
    Vector strDirs = tokenizePath(str);
    int patIdxStart = 0;
    int patIdxEnd = patDirs.size() - 1;
    int strIdxStart = 0;
    int strIdxEnd = strDirs.size() - 1;
    // up to first '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      String patDir = (String) patDirs.elementAt(patIdxStart);
      if (patDir.equals("**")) {
        break;
      }
      if (!match(patDir, (String) strDirs.elementAt(strIdxStart),
              mCaseSensitive)) {
        return false;
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!patDirs.elementAt(i).equals("**")) {
          return false;
        }
      }
      return true;
    } else {
      if (patIdxStart > patIdxEnd) {
        // String not exhausted, but pattern is. Failure.
        return false;
      }
    }
    // up to last '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      String patDir = (String) patDirs.elementAt(patIdxEnd);
      if (patDir.equals("**")) {
        break;
      }
      if (!match(patDir, (String) strDirs.elementAt(strIdxEnd),
              mCaseSensitive)) {
        return false;
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!patDirs.elementAt(i).equals("**")) {
          return false;
        }
      }
      return true;
    }
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patDirs.elementAt(i).equals("**")) {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // '**/**' situation, so skip one
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
        for (int i = 0; i <= strLength - patLength; i++) {
          for (int j = 0; j < patLength; j++) {
            String subPat = (String) patDirs.elementAt(patIdxStart + j + 1);
            String subStr = (String) strDirs.elementAt(strIdxStart + i + j);
            if (!match(subPat, subStr, mCaseSensitive)) {
              continue strLoop;
            }
          }
          foundIdx = strIdxStart + i;
          break;
        }
      if (foundIdx == -1) {
        return false;
      }
      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (!patDirs.elementAt(i).equals("**")) {
        return false;
      }
    }
    return true;
  }


  /**
   * Tests whether or not a string matches against a pattern.  The
   * pattern may contain two special characters:<br> '*' means zero or
   * more characters<br> '?' means one and only one character
   *
   * @param pattern The pattern to match against.
   *                Must not be <code>null</code>.
   * @param str     The string which must be matched against the pattern.
   *                Must not be <code>null</code>.
   * @param mCaseSensitive Whether or not matching should be performed
   *                        case sensitively.
   *
   *
   * @return <code>true</code> if the string matches against the pattern,
   *         or <code>false</code> otherwise.
   */
  private static boolean match(String pattern, String str,
                               boolean mCaseSensitive) {
    char[] patArr = pattern.toCharArray();
    char[] strArr = str.toCharArray();
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;
    char ch;
    boolean containsStar = false;
    for (int i = 0; i < patArr.length; i++) {
      if (patArr[i] == '*') {
        containsStar = true;
        break;
      }
    }
    if (!containsStar) {
      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }
      for (int i = 0; i <= patIdxEnd; i++) {
        ch = patArr[i];
        if (ch != '?') {
          if (mCaseSensitive && ch != strArr[i]) {
            return false;// Character mismatch
          }
          if (!mCaseSensitive && Character.toUpperCase(ch) !=
                  Character.toUpperCase(strArr[i])) {
            return false; // Character mismatch
          }
        }
      }
      return true; // String matches against pattern
    }
    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }
    // Process characters before first star
    while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?') {
        if (mCaseSensitive && ch != strArr[strIdxStart]) {
          return false;// Character mismatch
        }
        if (!mCaseSensitive && Character.toUpperCase(ch) !=
                Character.toUpperCase(strArr[strIdxStart])) {
          return false;// Character mismatch
        }
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }
    // Process characters after last star
    while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?') {
        if (mCaseSensitive && ch != strArr[strIdxEnd]) {
          return false;// Character mismatch
        }
        if (!mCaseSensitive && Character.toUpperCase(ch) !=
                Character.toUpperCase(strArr[strIdxEnd])) {
          return false;// Character mismatch
        }
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }
    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // Two stars next to each other, skip the first one.
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
        for (int i = 0; i <= strLength - patLength; i++) {
          for (int j = 0; j < patLength; j++) {
            ch = patArr[patIdxStart + j + 1];
            if (ch != '?') {
              if (mCaseSensitive && ch != strArr[strIdxStart + i + j]) {
                continue strLoop;
              }
              if (!mCaseSensitive && Character.toUpperCase(ch) !=
                      Character.toUpperCase(strArr[strIdxStart + i + j])) {
                continue strLoop;
              }
            }
          }
          foundIdx = strIdxStart + i;
          break;
        }
      if (foundIdx == -1) {
        return false;
      }
      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }
    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (patArr[i] != '*') {
        return false;
      }
    }
    return true;
  }

  /**
   * Breaks a path up into a Vector of path elements, tokenizing on
   * <code>File.separator</code>.
   *
   * @param path Path to tokenize. Must not be <code>null</code>.
   *
   * @return a Vector of path elements from the tokenized path
   */
  private static Vector tokenizePath(String path) {
    Vector ret = new Vector();
    StringTokenizer st = new StringTokenizer(path, File.separator);
    while (st.hasMoreTokens()) {
      ret.addElement(st.nextToken());
    }
    return ret;
  }
}