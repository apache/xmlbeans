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

package org.apache.xmlbeans.impl.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Provides utility services for jarring and unjarring files and directories.
 * Note that a given instance of JarHelper is not threadsafe with respect to
 * multiple jar operations.
 */
public class JarHelper {
    // ========================================================================
    // Constants

    private static final int BUFFER_SIZE = 2156;

    // ========================================================================
    // Variables

    private final byte[] mBuffer = new byte[BUFFER_SIZE];
    private boolean mVerbose = false;
    private String mDestJarName = "";

    // ========================================================================
    // Constructor

    /**
     * Instantiates a new JarHelper.
     */
    public JarHelper() {
    }

    // ========================================================================
    // Public methods

    /**
     * Jars a given directory or single file into a JarOutputStream.
     */
    public void jarDir(File dirOrFile2Jar, File destJar)
            throws IOException {

        if (dirOrFile2Jar == null || destJar == null) {
            throw new IllegalArgumentException();
        }

        mDestJarName = destJar.getCanonicalPath();
        try (OutputStream fout = Files.newOutputStream(destJar.toPath());
             JarOutputStream jout = new JarOutputStream(fout)) {
            //jout.setLevel(0);
            jarDir(dirOrFile2Jar, jout, null);
        }
    }

    public void setVerbose(boolean b) {
        mVerbose = b;
    }

    // ========================================================================
    // Private methods

    private static final char SEP = '/';

    /**
     * Recursively jars up the given path under the given directory.
     */
    private void jarDir(File dirOrFile2jar, JarOutputStream jos, String path)
            throws IOException {
        if (mVerbose) {
            System.out.println("checking " + dirOrFile2jar);
        }
        if (dirOrFile2jar.isDirectory()) {
            String[] dirList = dirOrFile2jar.list();
            String subPath = (path == null) ? "" : (path + dirOrFile2jar.getName() + SEP);
            if (path != null) {
                JarEntry je = new JarEntry(subPath);
                je.setTime(dirOrFile2jar.lastModified());
                jos.putNextEntry(je);
                jos.flush();
                jos.closeEntry();
            }
            if (dirList != null) {
                for (String s : dirList) {
                    File f = new File(dirOrFile2jar, s);
                    jarDir(f, jos, subPath);
                }
            }
        } else {
            if (dirOrFile2jar.getCanonicalPath().equals(mDestJarName)) {
                if (mVerbose) {
                    System.out.println("skipping " + dirOrFile2jar.getPath());
                }
                return;
            }

            if (mVerbose) {
                System.out.println("adding " + dirOrFile2jar.getPath());
            }
            try (InputStream fis = Files.newInputStream(dirOrFile2jar.toPath())) {
                JarEntry entry = new JarEntry(path + dirOrFile2jar.getName());
                entry.setTime(dirOrFile2jar.lastModified());
                jos.putNextEntry(entry);
                int mByteCount;
                while ((mByteCount = fis.read(mBuffer)) != -1) {
                    jos.write(mBuffer, 0, mByteCount);
                    if (mVerbose) {
                        System.out.println("wrote " + mByteCount + " bytes");
                    }
                }
                jos.flush();
                jos.closeEntry();
            }
        }
    }
}
