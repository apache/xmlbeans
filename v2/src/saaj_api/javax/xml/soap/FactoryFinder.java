/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package javax.xml.soap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Properties;

/**
 * This class is used to locate factory classes for javax.xml.soap.
 * It has package scope since it is not part of JAXM and should not
 * be accessed from other packages.
 */
class FactoryFinder {
    /**
     * instantiates an object go the given classname.
     *
     * @param factoryClassName
     * @return a factory object
     * @throws SOAPException
     */
    private static Object newInstance(String factoryClassName) throws SOAPException {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (Exception exception) {
            throw new SOAPException(exception.toString(), exception);
        }

        try {
            Class factory = null;
            if (classloader == null) {
                factory = Class.forName(factoryClassName);
            } else {
                try {
                    factory = classloader.loadClass(factoryClassName);
                } catch (ClassNotFoundException cnfe) {}
            }
            if (factory == null) {
                classloader = FactoryFinder.class.getClassLoader();
                factory = classloader.loadClass(factoryClassName);
            }
            return factory.newInstance();
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new SOAPException("Provider " + factoryClassName + " not found", classnotfoundexception);
        } catch (Exception exception) {
            throw new SOAPException("Provider " + factoryClassName + " could not be instantiated: " + exception, exception);
        }
    }

    /**
     * Instantiates a factory object given the factory's property name and the
     * default class name.
     *
     * @param factoryPropertyName
     * @param defaultFactoryClassName
     * @return a factory object
     * @throws SOAPException
     */
    static Object find(String factoryPropertyName, String defaultFactoryClassName) throws SOAPException {
        try {
            String factoryClassName = System.getProperty(factoryPropertyName);
            if (factoryClassName != null) {
                return newInstance(factoryClassName);
            }
        } catch (SecurityException securityexception) {}

        try {
            String propertiesFileName = System.getProperty("java.home")
                                        + File.separator + "lib"
                                        + File.separator + "jaxm.properties";
            File file = new File(propertiesFileName);
            if (file.exists()) {
                FileInputStream fileInput = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInput);
                fileInput.close();
                String factoryClassName = properties.getProperty(factoryPropertyName);
                return newInstance(factoryClassName);
            }
        } catch (Exception exception1) {}

        String factoryResource = "META-INF/services/" + factoryPropertyName;

        try {
            InputStream inputstream = getResource(factoryResource);
            if (inputstream != null) {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
                String factoryClassName = bufferedreader.readLine();
                bufferedreader.close();
                if ((factoryClassName != null) && !"".equals(factoryClassName)) {
                    return newInstance(factoryClassName);
                }
            }
        } catch (Exception exception2) {}

        if (defaultFactoryClassName == null) {
            throw new SOAPException("Provider for " + factoryPropertyName + " cannot be found", null);
        } else {
            return newInstance(defaultFactoryClassName);
        }
    }

    /**
     * Returns an input stream for the specified resource.
     *
     * <p>This method will firstly try
     * <code>ClassLoader.getSystemResourceAsStream()</code> then
     * the class loader of the current thread with
     * <code>getResourceAsStream()</code> and finally attempt
     * <code>getResourceAsStream()</code> on
     * <code>FactoryFinder.class.getClassLoader()</code>.
     *
     * @param factoryResource  the resource name
     * @return  an InputStream that can be used to read that resource, or
     *              <code>null</code> if the resource could not be resolved
     */
    private static InputStream getResource(String factoryResource) {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException securityexception) {}

        InputStream inputstream;
        if (classloader == null) {
            inputstream = ClassLoader.getSystemResourceAsStream(factoryResource);
        } else {
            inputstream = classloader.getResourceAsStream(factoryResource);
        }

        if (inputstream == null) {
            inputstream = FactoryFinder.class.getClassLoader().getResourceAsStream(factoryResource);
        }
        return inputstream;
    }
}
