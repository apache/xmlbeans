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

package org.apache.xmlbeans;

import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import weblogic.xml.stream.XMLInputStream;
import weblogic.xml.stream.XMLStreamException;

/**
 * A hook for the XML Bean Factory mechanism.
 * Provided for advanced users who wish to provide their own
 * implementation of the Factory.parse methods. This is used, for example,
 * to defer reading XML streams until needed.
 * <p>
 * To use the hook, call XmlFactoryHook.ThreadContext.setHook(), passing
 * your own XmlFactoryHook implementation.  Then every call to a Factory
 * method will be delgated to your hook.
 *
 * <pre>
 * MyHook hook = new MyHook();
 * XmlFactoryHook.ThreadContext.setHook(hook);
 * // this results in a call to hook.parse(...)
 * XmlObject.Factory.parse(new File("test.xml"));
 * </pre>
 * 
 * If the hook needs to turn around and invoke the built-in parsers, then
 * it should do so by calling the appropriate method on the passed
 * SchemaTypeLoader.  Since SchemaTypeLoader.parse() methods delegate
 * to the registered hook, a hook that wishes to actually invoke the
 * default parser without having itself called back again should
 * unregister itself before calling loader.parse(), and then re-register
 * itself again after the call.
 * <pre>
 * void parse(SchemaTypeLoader loader, ...)
 * {
 *     XmlFactoryHook remember = XmlFactoryHook.ThreadContext.getHook();
 *     XmlFactoryHook.ThreadContext.setHook(null);
 *     loader.parse(...); // isn't hooked.
 *     XmlFactoryHook.ThreadContext.setHook(remember);
 * }
 * </pre>
 */
public interface XmlFactoryHook
{
    /** Hooks Factory.newInstance calls */
    public XmlObject newInstance ( SchemaTypeLoader loader, SchemaType type, XmlOptions options );
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, String xmlText, SchemaType type, XmlOptions options ) throws XmlException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, InputStream jiois, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, Reader jior, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, Node node, SchemaType type, XmlOptions options ) throws XmlException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException;
    /** Hooks Factory.newXmlSaxHandler calls */
    public XmlSaxHandler newXmlSaxHandler ( SchemaTypeLoader loader, SchemaType type, XmlOptions options );

    /**
     * Used to manage the XmlFactoryHook for the current thread.
     */ 
    public final static class ThreadContext
    {
        private static ThreadLocal threadHook = new ThreadLocal();
        
        /**
         * Returns the current thread's hook, or null if none.
         */ 
        public static XmlFactoryHook getHook()
        {
            return (XmlFactoryHook)threadHook.get();
        }

        /**
         * Sets the hook for the current thread.
         */ 
        public static void setHook(XmlFactoryHook hook)
        {
            threadHook.set(hook);
        }

        // provided to prevent unwanted construction
        private ThreadContext()
        {
        }
    }
}
