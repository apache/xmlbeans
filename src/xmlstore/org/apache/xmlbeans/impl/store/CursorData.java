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

import org.apache.xmlbeans.impl.store.Splay.CursorGoober;

import java.util.ArrayList;
import java.lang.ref.SoftReference;

public final class CursorData
{
    public static CursorData getOne ( Root r )
    {
        ArrayList dataCache = (ArrayList) tl_CachedCursorData.get();

        CursorData cd;

        SoftReference softRef;
        while (dataCache.size() != 0)
        {
            softRef = (SoftReference)(dataCache.remove(dataCache.size()-1));
            cd = (CursorData)softRef.get();
            if (cd==null)
                continue;
            else
            {
                cd._goober.set( r );
                return cd;
            }
        }

        cd = new CursorData( r );

        return cd;
    }

    private CursorData ( Root r )
    {
        _goober = new CursorGoober( r );
    }




    private static ThreadLocal tl_CachedCursorData =
        new ThreadLocal() { protected Object initialValue() { return new ArrayList(); } };

    protected void release ( boolean cache )
    {
        if (_goober.getSplay() != null)
        {
            clearSelections();

            if (_stack != null)
                _stack.dispose();

            _goober.release();
            
            if (cache)
            {
                ArrayList dataCache = (ArrayList) tl_CachedCursorData.get();

                if (dataCache.size() < 128)
                    dataCache.add( new SoftReference (this) );
            }
        }
    }
    
    protected void finalize ( )
    {
        Splay s = _goober.getSplay();
        
        if (s != null)
        {
            synchronized ( _goober.getRoot() )
            {
                release(false);
            }
        }
    }

    protected  void clearSelections (  )
    {
        if (_selections != null)
            _selections.dispose();

        _currentSelection = -2;
    }
    
    public final CursorGoober _goober;

    protected Cursor.Selections  _stack;

    protected Cursor.Selections  _selections;
    protected int                _currentSelection;
}
