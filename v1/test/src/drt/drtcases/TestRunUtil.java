/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package drtcases;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class TestRunUtil
{
    /**
     * Runs method that must be declared "static void test()" on the given
     * class, with the given additional jars or directories on the classpath.
     */
    public static void run(String classname, File[] classpath) throws Throwable
    {
        // System.err.println("Running " + classname + " with classpath:");
        for (int i = 0; i < classpath.length; i++)
        {
            // System.err.println(classpath[i]);
            if (!classpath[i].exists())
                throw new IllegalArgumentException("Classpath component " + classpath + " cannot be found!");
        }

        URL[] extracp = new URL[classpath.length];
        for (int i = 0; i < classpath.length; i++)
        {
            try
            {
                extracp[i] = classpath[i].toURL();
            }
            catch (MalformedURLException e)
            {
                throw new IllegalArgumentException("Malformed classpath filename");
            }
        }

        ClassLoader curcl = Thread.currentThread().getContextClassLoader();

        try
        {
            ClassLoader childcl = new URLClassLoader(extracp);
            Class javaClass = childcl.loadClass(classname);
            Class testClass = childcl.loadClass("org.openuri.mytest.CustomerDocument");
            if (testClass == null)
                throw new IllegalStateException();
            Method meth = javaClass.getMethod("test", new Class[0]); // should be static
            Thread.currentThread().setContextClassLoader(childcl);
            meth.invoke(null, new Object[0]);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("class not found");
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("no test() method found");
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException("could not invoke static public test method");
        }
        catch (InvocationTargetException e)
        {
            throw e.getCause();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(curcl);
        }
    }
}
