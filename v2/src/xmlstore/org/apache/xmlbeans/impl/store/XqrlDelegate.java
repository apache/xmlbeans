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

package org.apache.xmlbeans.impl.store;

import java.lang.reflect.Method;

public final class XqrlDelegate 
{
    private XqrlDelegate ( )
    {
    }

    static void check ( Object o )
    {
        if (o == null)
        {
            throw
                new UnsupportedOperationException(
                    "This operation requires xqrl.jar" );
        }
    }

    static Path.Query compilePath ( String path, String currentNodeVar )
    {
        return
            (Path.Query)
                invoke( _compilePath, new Object[] { path, currentNodeVar } );
    }
    
    static Path.Query compileQuery ( String queryExpr, String currentNodeVar )
    {
        return
            (Path.Query)
                invoke(
                    _compileQuery, new Object[] { queryExpr, currentNodeVar, new Boolean(true) } );
    }
    
    private static void throwRuntimeException ( Throwable e )
    {
        if (e instanceof RuntimeException)
            throw (RuntimeException) e;
        
        Throwable cause = e.getCause();
        
        RuntimeException rte = new RuntimeException( cause.getMessage() );
        
        rte.initCause( cause );

        throw rte;
    }

    private static Object invoke ( Method method, Object[] params )
    {
        if (method == null)
        {
            throw
                new UnsupportedOperationException(
                    "This operation requires xqrl.jar" );
        }

        try
        {
            return method.invoke( null, params );
        }
        catch ( Throwable e )
        {
            throwRuntimeException( e );
            
            return null; // Unreachable
        }
    }

    // Loose coupling functionality with xqrl.jar

    private static Method _compilePath;
    private static Method _compileQuery;

    static
    {
        boolean hasXqrl;
        
        try
        {
            Class.forName( "org.apache.xmlbeans.impl.store.XqrlImpl" );
            
            hasXqrl = true;
        }
        catch ( ClassNotFoundException e )
        {
            hasXqrl = false;
        }

        if (hasXqrl)
        {
            try
            {
                Class xqrlImpl =
                    Class.forName( "org.apache.xmlbeans.impl.store.XqrlImpl" );

                _compilePath =
                    xqrlImpl.getDeclaredMethod(
                        "compilePath",
                        new Class[] { String.class, String.class } );

                _compileQuery =
                    xqrlImpl.getDeclaredMethod(
                        "compileQuery",
                        new Class[] { String.class, String.class, Boolean.class } );
                
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }
}
