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

import org.apache.xmlbeans.impl.common.XPath;
import org.apache.xmlbeans.impl.common.XPath.XPathCompileException;
import org.apache.xmlbeans.impl.common.XPath.ExecutionContext;

import org.apache.xmlbeans.XmlOptions;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

// TODO - This class handled query *and* path ... rename it?

public abstract class Path
{
    Path ( String key )
    {
        _pathKey = key;
    }

    public static String _useXqrlForXpath = "use xqrl for xpath";

    public static String _useXbeanForXpath = "use xbean for xpath";

    static interface PathEngine
    {
        void release ( );
        boolean next ( Cur c );
    }

    abstract PathEngine execute ( Cur c );

    static Path getCompiledPath ( String pathExpr, XmlOptions options )
    {
        options = XmlOptions.maskNull( options );

        int force = 
            options.hasOption( _useXqrlForXpath )
                ? FORCE_XQRL
                : options.hasOption( _useXbeanForXpath )
                   ? FORCE_XBEAN
                   : FORCE_NEITHER;

        return getCompiledPath( pathExpr, force, getCurrentNodeVar( options ) );
    }

    private static final int FORCE_XQRL    = 0;
    private static final int FORCE_XBEAN   = 1;
    private static final int FORCE_NEITHER = 2;


    // TODO - This is a global lock ... make it not be a global lock?
    static synchronized Path getCompiledPath ( String pathExpr, int force, String currentVar )
    {
        Path path = null;

        // TODO - remove this when integrate xqrl
        if (force == FORCE_XQRL)
            throw new RuntimeException( "Not XQRL support yet" );

        // TODO - when integrate xqrl, add this back
//        if (force != FORCE_XQRL)
            path = (Path) _xbeanPathCache.get( pathExpr );

        // Check for other engine caches here .. make sure to check  force

        // Could not find the path in the caches, try to compile it
            
        if (path == null && force != FORCE_XQRL)
        {
            path = XbeanPath.create( pathExpr, currentVar );

            if (path != null)
                _xbeanPathCache.put( pathExpr, path );
        }

        // TODO - for xqrl integ, check for null and try to compile
        
        if (path == null)
            throw new RuntimeException( "XQRL no integrated yet, path too complex or invalid" );

        return path;
   }
    
    public static synchronized String compilePath ( String pathExpr, XmlOptions options )
    {
        return getCompiledPath( pathExpr, options )._pathKey;
    }

    //
    // Xbean store specific implementation of compiled path
    //

    private static final class XbeanPath extends Path
    {
        static Path create ( String pathExpr, String currentVar )
        {
            try
            {
                return
                    new XbeanPath(
                        pathExpr, currentVar, XPath.compileXPath( pathExpr, currentVar ) );
            }
            catch ( XPathCompileException e )
            {
                return null;
            }
        }
        
        private XbeanPath ( String pathExpr, String currentVar, XPath xpath )
        {
            super( pathExpr );

            _currentVar = currentVar;
            _compiledPath = xpath;
        }
        
        PathEngine execute ( Cur c )
        {
            // The builtin XPath engine works only on containers.  Delegate to
            // xqrl otherwise.  Also, if the path had a //. at the end, the
            // simple xpath engine can't do the generate case, it only handles
            // attrs and elements.

            if (!c.isContainer() || _compiledPath.sawDeepDot())
                return getCompiledPath( _pathKey, FORCE_XQRL, _currentVar ).execute( c );

            return new XbeanPathEngine( _compiledPath, c );
        }

        private final String _currentVar;
        private final XPath  _compiledPath;
    }

    private static final class XbeanPathEngine extends ExecutionContext implements PathEngine
    {
        XbeanPathEngine ( XPath xpath, Cur c )
        {
            assert c.isContainer();
            
            _version = c._locale.version();
            _cur = c.weakCur( this );

            _cur.push();
            
            init( xpath );

            int ret = start();

            if ((ret & HIT) != 0)
                c.addToSelection();

            doAttrs( ret, c );

            if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement( _cur ))
                release();
        }

        private void advance ( Cur c )
        {
            assert _cur != null;

            if (_cur.isFinish())
            {
                if (_cur.isAtEndOfLastPush())
                    release();
                else
                {
                    end();
                    _cur.next();
                }
            }
            else if (_cur.isElem())
            {
                int ret = element( _cur.getName() );
                
                if ((ret & HIT) != 0)
                    c.addToSelection( _cur );

                doAttrs( ret, c );
                
                if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement( _cur ))
                    _cur.skip();
            }
            else
                _cur.next();
        }
        
        private void doAttrs ( int ret, Cur c )
        {
            assert _cur.isContainer();
            
            if ((ret & ATTRS) != 0)
            {
                if (_cur.toFirstAttr())
                {
                    do
                    {
                        if (attr( _cur.getName() ))
                            c.addToSelection( _cur );
                    }
                    while ( _cur.toNextAttr() );

                    _cur.toParent();
                }
            }
        }

        public boolean next ( Cur c )
        {
            if (_version != _cur._locale.version())
                throw new ConcurrentModificationException( "Document changed during select" );
            
            int startCount = c.selectionCount();

            while ( _cur != null )
            {
                advance( c );

                if (startCount != c.selectionCount())
                    return true;
            }

            return false;
        }

        public void release( )
        {
            if (_cur != null)
            {
                _cur.release();
                _cur = null;
            }
        }
        
        private final long _version;
        private       Cur  _cur;
    }

    //
    //
    //

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

    //
    //
    //
    
    protected final String _pathKey;
    
    private static HashMap _xbeanPathCache = new HashMap();
} 