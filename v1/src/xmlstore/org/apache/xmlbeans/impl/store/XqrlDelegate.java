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

package org.apache.xmlbeans.impl.store;

import java.lang.reflect.Method;
import org.apache.xmlbeans.XmlOptions;

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
                    "This query is too complex to be processed." );
        }
    }

    static Path.Query compilePath ( String path, XmlOptions options )
    {
        return
            (Path.Query)
                invoke( _compilePath, new Object[] { path, options } );
    }
    
    static Path.Query compileQuery ( String queryExpr, XmlOptions options )
    {
        return
            (Path.Query)
                invoke(
                    _compileQuery, new Object[] { queryExpr, options, new Boolean(true) } );
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
                    "This query is too complex to be processed." );
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
                        new Class[] { String.class, XmlOptions.class } );

                _compileQuery =
                    xqrlImpl.getDeclaredMethod(
                        "compileQuery",
                        new Class[] { String.class, XmlOptions.class, Boolean.class } );
                
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }
}
