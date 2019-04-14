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

import org.apache.xmlbeans.impl.jam.provider.JamLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class DirectoryScanner {

  // ========================================================================
  // Variables

  private boolean mCaseSensitive = true;
  private File mRoot;
  private JamLogger mLogger;
  private List mIncludeList = null;
  private List mExcludeList = null;
  private String[] mIncludes;
  private String[] mExcludes;
  private Vector mFilesIncluded;
  private Vector mDirsIncluded;
  private boolean mIsDirty = false;
  private String[] mIncludedFilesCache = null;


  // ========================================================================
  // Constructors

  public DirectoryScanner(File dirToScan, JamLogger logger) {
    if (logger == null) throw new IllegalArgumentException("null logger");
    mLogger = logger;
    mRoot = dirToScan;
  }

  // ========================================================================
  // Public methods

  public void include(String pattern) {
    if (mIncludeList == null) mIncludeList = new ArrayList();
    mIncludeList.add(pattern);
    mIsDirty = true;
  }

  public void exclude(String pattern) {
    if (mExcludeList == null) mExcludeList = new ArrayList();
    mExcludeList.add(pattern);
    mIsDirty = true;
  }

  /**
   * Scans the root directory with the patterns that have been included
   * and excluded and returns the names of the resulting file set
   * relative to the root dir.
   */
  public String[] getIncludedFiles() throws IOException {
    if (!mIsDirty && mIncludedFilesCache != null) {
      return mIncludedFilesCache;
    }
    if (mIncludeList != null) {
      String[] inc = new String[mIncludeList.size()];
      mIncludeList.toArray(inc);
      setIncludes(inc);
    } else {
      setIncludes(null);
    }
    if (mExcludeList != null) {
      String[] exc = new String[mExcludeList.size()];
      mExcludeList.toArray(exc);
      setExcludes(exc);
    } else {
      setExcludes(null);
    }
    scan();
    mIncludedFilesCache = new String[mFilesIncluded.size()];
    mFilesIncluded.copyInto(mIncludedFilesCache);
    return mIncludedFilesCache;
  }

  public void setDirty() {
    mIsDirty = true;
  }

  public File getRoot() {
    return mRoot;
  }

  // ========================================================================
  // Private methods

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
   *                 list is given, all elements2 must be
   * non-<code>null</code>.
   */
  private void setIncludes(String[] includes) {
    if (includes == null) {
      this.mIncludes = null;
    } else {
      this.mIncludes = new String[includes.length];
      for (int i = 0; i < includes.length; i++) {
        String pattern;
        pattern = includes[i].replace('/', File.separatorChar).
                replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
          pattern += "**";
        }
        this.mIncludes[i] = pattern;
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
   *                 given, all elements2 must be non-<code>null</code>.
   */
  private void setExcludes(String[] excludes) {
    if (excludes == null) {
      this.mExcludes = null;
    } else {
      this.mExcludes = new String[excludes.length];
      for (int i = 0; i < excludes.length; i++) {
        String pattern;
        pattern = excludes[i].replace('/', File.separatorChar).
                replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
          pattern += "**";
        }
        this.mExcludes[i] = pattern;
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
    if (mIncludes == null) {
      // No mIncludes supplied, so set it to 'matches all'
      mIncludes = new String[1];
      mIncludes[0] = "**";
    }
    if (mExcludes == null) {
      mExcludes = new String[0];
    }
    mFilesIncluded = new Vector();
    mDirsIncluded = new Vector();
    if (isIncluded("")) {
      if (!isExcluded("")) {
        mDirsIncluded.addElement("");
      }
    }
    scandir(mRoot, "", true);
  }

  /**
   * Scans the given directory for files and directories. Found files and
   * directories are placed in their respective collections, based on the
   * matching of mIncludes, mExcludes, and the selectors.  When a directory
   * is found, it is scanned recursively.
   *
   * @param dir   The directory to scan. Must not be <code>null</code>.
   * @param vpath The path relative to the base directory (needed to
   *              prevent problems with an absolute path when using
   *              dir). Must not be <code>null</code>.
   * @param fast  Whether or not this call is part of a fast scan.
   *
   * @see #mFilesIncluded
   * @see #mDirsIncluded
   */
  private void scandir(File dir, String vpath, boolean fast)
          throws IOException {
    if (mLogger.isVerbose(this)) {
      mLogger.verbose("[DirectoryScanner] scanning dir "+dir+" for '"+vpath+"'");
    }
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
          mDirsIncluded.addElement(name);
          if (mLogger.isVerbose(this)) mLogger.verbose("...including dir "+name);
          scandir(file, name + File.separator, fast);
        } else {
          if (couldHoldIncluded(name)) {
            scandir(file, name + File.separator, fast);
          }
        }
      } else if (file.isFile()) {
        if (isIncluded(name)) {
          if (!isExcluded(name)) {
            mFilesIncluded.addElement(name);
            if (mLogger.isVerbose(this)) {
              mLogger.verbose("...including "+name+" under '"+dir);
            }
          } else {
            if (mLogger.isVerbose(this)) {
              mLogger.verbose("...EXCLUDING "+name+" under '"+dir);
            }
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
    for (int i = 0; i < mIncludes.length; i++) {
      if (matchPath(mIncludes[i], name, mCaseSensitive)) {
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
    for (int i = 0; i < mIncludes.length; i++) {
      if (matchPatternStart(mIncludes[i], name, mCaseSensitive)) {
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
    for (int i = 0; i < mExcludes.length; i++) {
      if (matchPath(mExcludes[i], name, mCaseSensitive)) {
        return true;
      }
    }
    return false;
  }



  /**
   * Returns the names of the directories which matched at least one
   * of the include patterns and none of the exclude patterns.  The
   * names are relative to the base directory.
   *
   * @return the names of the directories which matched at least one of the
   * include patterns and none of the exclude patterns.

  private String[] getIncludedDirectories() {
    String[] directories = new String[mDirsIncluded.size()];
    mDirsIncluded.copyInto(directories);
    return directories;
  }
   */

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

  private static boolean matchPatternStart(String pattern, String str) {
    return matchPatternStart(pattern, str, true);
  }
   */

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
  private static boolean matchPatternStart(String pattern, String str,
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

  private static boolean matchPath(String pattern, String str) {
    return matchPath(pattern, str, true);
  }
   */
  
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
  private static boolean matchPath(String pattern, String str,
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
   * Breaks a path up into a Vector of path elements2, tokenizing on
   * <code>File.separator</code>.
   *
   * @param path Path to tokenize. Must not be <code>null</code>.
   *
   * @return a Vector of path elements2 from the tokenized path
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