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

package repackage;

import org.apache.xmlbeans.impl.common.StringUtils;
import org.apache.xmlbeans.impl.regex.Match;
import org.apache.xmlbeans.impl.regex.RegularExpression;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class Repackager
{
    public Repackager ( String repackageSpecs )
    {
        _fromPackages = new ArrayList();
        _toPackages = new ArrayList();

        String[] repackages = StringUtils.split( repackageSpecs, ';' );

        // Sort the repackage spec so that longer from's are first to match
        // longest package first

        for ( ; ; )
        {
            boolean swapped = false;

            for ( int i = 1 ; i < repackages.length ; i++ )
            {
                String spec1 = repackages[ i - 1 ];
                String spec2 = repackages[ i ];

                if (spec1.indexOf( ':' ) < spec2.indexOf( ':' ))
                {
                    repackages[ i - 1 ] = spec2;
                    repackages[ i ] = spec1;

                    swapped = true;
                }
            }

            if (!swapped)
                break;
        }

        for ( int i = 0 ; i < repackages.length ; i++ )
        {
            String spec = repackages[ i ];

            int j = spec.indexOf( ':' );

            if (j < 0 || spec.indexOf( ':', j + 1 ) >= 0)
                throw new RuntimeException( "Illegal repackage specification: " + spec );

            String from = spec.substring( 0, j );
            String to = spec.substring( j + 1 );

            _fromPackages.add( StringUtils.split( from, '.' ) );
            _toPackages.add( StringUtils.split( to, '.' ) );
        }

        _fromPatterns = new RegularExpression [ _fromPackages.size() * 2 ];
        _toPackageNames = new String [ _fromPackages.size() * 2 ];

        addPatterns( '.', 0 );
        addPatterns( '/', _fromPackages.size() );
    }

    void addPatterns ( char sep, int off )
    {
        for ( int i = 0 ; i < _fromPackages.size() ; i++ )
        {
            List from = (List) _fromPackages.get( i );
            List to = (List) _toPackages.get( i );

            String pattern = "";

            for ( int j = 0 ; j < from.size() ; j++ )
            {
                if (j > 0)
                    pattern += "\\" + sep;

                pattern += from.get( j );
            }

            String toPackage = "";

            for ( int j = 0 ; j < to.size() ; j++ )
            {
                if (j > 0)
                    toPackage += sep;

                toPackage += to.get( j );
            }

            _fromPatterns[ off + i ] = new RegularExpression( pattern );
            _toPackageNames[ off + i ] = toPackage;
        }
    }

    public void repackage ( StringBuffer sb )
    {
        for ( int i = 0 ; i < _fromPatterns.length ; i++ )
        {
            Match matcher = new Match();
            boolean matches = _fromPatterns[ i ].matches(sb.toString());

            if (matches)
                StringUtils.replaceAll(matcher, sb, _toPackageNames[ i ]);
        }
    }

    public List getFromPackages ( )
    {
        return _fromPackages;
    }

    public List getToPackages ( )
    {
        return _toPackages;
    }

    public static String dirForPath ( String path )
    {
        return new File(path).getParent();
    }

    private List _fromPackages;
    private List _toPackages;

    private RegularExpression[] _fromPatterns;
    private String[]  _toPackageNames;
}
