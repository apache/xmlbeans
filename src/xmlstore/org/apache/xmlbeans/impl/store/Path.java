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

package org.apache.xmlbeans.impl.store;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.xmlbeans.impl.store.Splay.CursorGoober;
import org.apache.xmlbeans.impl.store.Cursor.Selections;
import org.apache.xmlbeans.impl.store.Cursor.PathEngine;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.XPath;
import org.apache.xmlbeans.impl.values.TypeStore;

/**
 * Represents a precompiled path expression
 */

public abstract class Path
{
    static Selections newSelections ( )
    {
        return new Selections();
    }

    static PathEngine select (
        Root r, Splay s, int p, String pathExpr, XmlOptions options )
    {
        Path path = getPath( pathExpr, options );
        return (path == null) ? null : path.execute( r, s, p, options );
    }

    public static String _useXqrlForXpath = "use xqrl for xpath";

    public static String _useXbeanForXpath = "use xbean for xpath";

    public static Path getPath ( String pathExpr )
    {
        return getPath( pathExpr, false, null );
    }
    
    public static Path getPath ( String pathExpr, XmlOptions options )
    {
        return
            getPath(
                pathExpr,
                XmlOptions.maskNull( options ).hasOption( _useXqrlForXpath ),
                options );
    }

    public static Path getPath ( String pathExpr, boolean xqrl, XmlOptions options )
    {
        Object path = null;
        
        synchronized ( _xbeanPathCache )
        {
            if (!xqrl)
                path = _xbeanPathCache.get( pathExpr );

            if (path == null)
                path = _xqrlPathCache.get( pathExpr );

            if (path == null)
            {
                String pathStr = getCompiledPath( pathExpr, xqrl, options );
                
                path =
                    (pathStr == null)
                        ? null
                        : getPath( pathExpr, xqrl, options );
            }
        }

        return (Path) path;
    }
    
    public static String getCompiledPath ( String pathExpr, XmlOptions options )
    {
        return getCompiledPath( pathExpr, false, options );
    }

    private static String getCurrentNodeVar ( XmlOptions options )
    {
        String currentNodeVar = "this";

        options = XmlOptions.maskNull( options );
        
        if (options.hasOption( XmlOptions.XQUERY_CURRENT_NODE_VAR ))
        {
            currentNodeVar = (String) options.get( XmlOptions.XQUERY_CURRENT_NODE_VAR );

            if (currentNodeVar.startsWith( "$" ))
            {
                throw
                    new IllegalArgumentException(
                        "Omit the '$' prefix for the current node variable" );
            }
        }

        return currentNodeVar;
    }
    
    public static String getCompiledPath ( String pathExpr, boolean xqrl, XmlOptions options )
    {
        Path path = null;
        
        options = XmlOptions.maskNull( options );
        
        String currentNodeVar = getCurrentNodeVar( options );

        synchronized ( _xbeanPathCache )
        {
            assert (xqrl |= options.hasOption( _useXqrlForXpath )) || true;

            if (!xqrl || options.hasOption( _useXbeanForXpath ))
            {
                path = (Path) _xbeanPathCache.get( pathExpr );

                if (path == null)
                {
                    path = XbeanPathImpl.create( pathExpr, currentNodeVar );

                    if (path != null)
                        _xbeanPathCache.put( path.getPathExpr(), path );
                }
            }

            if (path == null)
            {
                assert ! options.hasOption( _useXbeanForXpath );

                path = (Path) _xqrlPathCache.get( pathExpr );

                if (path == null)
                {
                    path = XqrlPathImpl.create( pathExpr, currentNodeVar );

                    if (path != null)
                        _xqrlPathCache.put( path.getPathExpr(), path );
                }
            }
        }

        return path == null ? null : path.getPathExpr();
    }

    public interface Query
    {
        PathEngine  executePath  ( Root r, Splay s, int p, XmlOptions options );
        XmlCursor   executeQuery ( Cursor c, XmlOptions options );
        XmlObject[] executeQuery ( Type type, XmlOptions options );
        String      getQueryExpr ( );
    }

//    public static Query getQuery ( String queryExpr )
//    {
//        return getQuery( queryExpr, null );
//    }
    
    public static Query getQuery ( String queryExpr, XmlOptions options )
    {
        Object query = null;
        
        synchronized ( _xqrlQueryCache )
        {
            query = _xqrlQueryCache.get( queryExpr );

            if (query == null)
            {
                String queryStr = getCompiledQuery( queryExpr, options );
                
                query =
                    (queryStr == null) ? null : getQuery( queryExpr, options );
            }
        }

        return (Query) query;
    }
    
    public static String getCompiledQuery ( String queryExpr, XmlOptions options )
    {
        Query query = null;
        String currentNodeVar = getCurrentNodeVar( options );
        
        synchronized ( _xqrlQueryCache )
        {
            query = (Query) _xqrlQueryCache.get( queryExpr );

            if (query == null)
            {
                query = XqrlDelegate.compileQuery( queryExpr, currentNodeVar );

                if (query != null)
                    _xqrlQueryCache.put( query.getQueryExpr(), query );
            }
        }

        return query == null ? null : query.getQueryExpr();
    }

    public static XmlCursor query (
        Cursor c, String queryExpr, XmlOptions options )
    {
        Query query = getQuery( queryExpr, options );
        return (query == null) ? null : query.executeQuery( c, options );
    }

    public static XmlObject[] query (
        Type type, String queryExpr, XmlOptions options )
    {
        Query query = getQuery( queryExpr, options );
        return (query == null) ? null : query.executeQuery( type, options );
    }

    protected abstract PathEngine execute (
        Root r, Splay s, int p, XmlOptions options );

    protected abstract String getPathExpr ( );

    private static class XqrlPathImpl extends Path
    {
        private XqrlPathImpl ( String pathExpr, Query compiledPath )
        {
            _pathExpr = pathExpr;
            _compiledPath = compiledPath;
        }

        private String _pathExpr;
        private Query  _compiledPath;

        static Path create ( String pathExpr, String currentNodeVar )
        {
            return new XqrlPathImpl(
                pathExpr,
                XqrlDelegate.compilePath( pathExpr, currentNodeVar ) );
        }

        protected PathEngine execute (
            Root r, Splay s, int p, XmlOptions options )
        {
            return _compiledPath.executePath( r, s, p, options );
        }
        
        protected String getPathExpr ( ) { return _pathExpr; }
    }

    //
    //
    //

    private static final class XbeanPathImpl extends Path
    {
        private XbeanPathImpl (
            XPath xpath, String pathExpr, String currentNodeVar )
        {
            _pathExpr = pathExpr;
            _xpath = xpath;
            _currentNodeVar = currentNodeVar;
        }

//            try
//            {
//                String currentNodeVar = "this";
//                
//                if (XmlOptions.XQUERY_CURRENT_NODE_VAR.has( options ))
//                {
//                    currentNodeVar =
//                        "$" + XmlOptions.XQUERY_CURRENT_NODE_VAR.get( options );
//
//                    if (currentNodeVar.startsWith( "$$" ))
//                    {
//                        throw
//                            new IllegalArgumentException(
//                                "Omit the '$p' refix for the current " +
//                                    "node variable" );
//                    }
//                }

        static Path create ( String pathExpr, String currentNodeVar )
        {
            assert !currentNodeVar.startsWith( "$" );

            try
            {
                return
                    new XbeanPathImpl(
                        XPath.compileXPath( pathExpr, currentNodeVar ),
                        pathExpr, currentNodeVar );
            }
            catch ( XPath.XPathCompileException e )
            {
                return null;
            }
        }

        protected String getPathExpr ( ) { return _pathExpr; }
        
        protected PathEngine execute (
            Root r, Splay s, int p, XmlOptions options )
        {
            // The builtin XPath engine works only on containers.  Delegate to
            // xqrl otherwise.  Also, if the path had a //. at the end, the
            // simple xpath engine can't do the generate case, it only handles
            // attrs and elements.

            if (p != 0 || !s.isContainer() || _xpath.sawDeepDot())
            {
                // If the xbean path compiler could could handle the path, then
                // the xqrl compiler better be able to!

                try
                {
                    return
                        getPath( _pathExpr, true, null ).
                            execute( r, s, p, options );
                }
                catch ( Throwable e )
                {
                    throw new RuntimeException( "Can't compile path", e );
                }
            }

            return new XBeanPathEngine( _xpath, r, s );
        }

// TODO - because this xpath engine does not use a saver, any attributes in the
// path which refer to namesapce attributes will require us to run the XQRL
// path engine which is based on the saver.

        private static class XBeanPathEngine
            extends XPath.ExecutionContext implements PathEngine
        {
            XBeanPathEngine ( XPath xpath, Root r, Splay s )
            {
                assert s.isContainer();

                _root = r;
                _curr = _top = s;
                _version = r.getVersion();

                init( xpath );
            }

            public boolean next ( Selections selections )
            {
                int initialSize = selections.currentSize();

                for ( ; ; )
                {
                    if (_root.getVersion() != _version)
                        throw new IllegalStateException( "Document changed" );

                    if (_curr == null)
                        return false;

                    advance( selections );

                    if (initialSize < selections.currentSize())
                        return true;
                }
            }

            private Splay doAttrs ( int ret, Splay s, Selections selections )
            {
                if ((ret & ATTRS) == 0)
                    return null;

                for ( s = _curr.nextSplay() ; s.isAttr() ;
                      s = s.nextSplay() )
                {
                    if (s.isNormalAttr() && attr( s.getName() ))
                        selections.add( _root, s );
                }

                return s;
            }

            private void advance ( Selections selections )
            {
                if (_curr == _top)
                {
                    int ret = start();

                    if ((ret & HIT) != 0)
                        selections.add( _root, _curr );

                    Splay s = doAttrs( ret, _curr, selections );

                    if ((ret & DESCEND) == 0 || _curr.isLeaf())
                        _curr = null;
                    else
                        _curr = s == null ? _curr.nextNonAttrSplay() : s;

                    return;
                }

                for ( ; ; )
                {
                    if (_curr.isFinish())
                    {
                        if (_curr.getContainer() == _top)
                            _curr = null;
                        else
                        {
                            end();
                            _curr = _curr.nextSplay();
                        }

                        return;
                    }

                    if (_curr.isBegin())
                    {
                        int ret = element( _curr.getName() );

                        if ((ret & HIT) != 0)
                            selections.add( _root, _curr );

                        Splay s = doAttrs( ret, _curr, selections );

                        if (_curr.isLeaf())
                        {
                            end();
                            _curr = s == null ? _curr.nextNonAttrSplay() : s;
                        }
                        else if ((ret & DESCEND) == 0)
                            _curr = ((Splay.Container) _curr).getFinish();
                        else
                            _curr = s == null ? _curr.nextNonAttrSplay() : s;

                        return;
                    }

                    _curr = _curr.nextSplay();
                }
            }

            private Root  _root;
            private long  _version;
            private Splay _top;
            private Splay _curr;
        }

        private String _pathExpr;
        private XPath  _xpath;
        private String _currentNodeVar;
    }

    private static HashMap _xqrlPathCache = new HashMap();
    private static HashMap _xbeanPathCache = new HashMap();
    private static HashMap _xqrlQueryCache = new HashMap();
}
