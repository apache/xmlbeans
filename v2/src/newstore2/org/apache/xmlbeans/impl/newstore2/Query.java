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

package org.apache.xmlbeans.impl.newstore2;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public abstract class Query
{
    abstract XmlObject[] execute ( Cur c, XmlOptions options );
    
    //
    // Xqrl store specific implementation of compiled path/query
    //

    static XmlObject[] execQuery ( Cur c, String queryExpr, XmlOptions options )
    {
        return getCompiledQuery( queryExpr, options ).execute( c, options );
    }

    public static synchronized Query getCompiledQuery ( String queryExpr, XmlOptions options )
    {
        return getCompiledQuery( queryExpr, Path.getCurrentNodeVar( options ) );
    }
    
    static synchronized Query getCompiledQuery ( String queryExpr, String currentVar )
    {
        assert queryExpr != null;
        
        Query query = (Query) _xqrlQueryCache.get( queryExpr );

        if (query != null)
            return query;

        // TODO - may want to add other query engines here: xqrl v2, saxon?
        
        query = createXqrlCompiledQuery( queryExpr, currentVar );

        if (query == null)
            throw new RuntimeException( "No query engine found" );

        _xqrlQueryCache.put( queryExpr, query );

        return query;
    }
    
    private static Query createXqrlCompiledQuery ( String queryExpr, String currentVar )
    {
        if (_xqrlCompileQuery == null)
        {
            try
            {
                Class xqrlImpl = Class.forName( "org.apache.xmlbeans.impl.newstore2.XqrlImpl" );

                _xqrlCompileQuery =
                    xqrlImpl.getDeclaredMethod(
                        "compileQuery", new Class[] { String.class, String.class, Boolean.class } );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        Object[] args = new Object[] { queryExpr, currentVar, new Boolean( true ) };

        try
        {
            return (Query) _xqrlCompileQuery.invoke( null, args );
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    //
    //
    //

    private static HashMap _xqrlQueryCache = new HashMap();
    
    private static Method _xqrlCompileQuery;
}
