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

package repackage;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;

public class Repackager
{
    public Repackager ( String repackageSpecs )
    {
        _fromPackages = new ArrayList();
        _toPackages = new ArrayList();
        
        List repackages = splitPath( repackageSpecs, ';' );

        // Sort the repackage spec so that longer from's are first to match
        // longest package first
        
        for ( ; ; )
        {
            boolean swapped = false;

            for ( int i = 1 ; i < repackages.size() ; i++ )
            {
                String spec1 = (String) repackages.get( i - 1 );
                String spec2 = (String) repackages.get( i );
                
                if (spec1.indexOf( ':' ) < spec2.indexOf( ':' ))
                {
                    repackages.set( i - 1, spec2 );
                    repackages.set( i, spec1 );
                    
                    swapped = true;
                }
            }

            if (!swapped)
                break;
        }

        for ( int i = 0 ; i < repackages.size() ; i++ )
        {
            String spec = (String) repackages.get( i );
            
            int j = spec.indexOf( ':' );

            if (j < 0 || spec.indexOf( ':', j + 1 ) >= 0)
                throw new RuntimeException( "Illegal repackage specification: " + spec );

            String from = spec.substring( 0, j );
            String to = spec.substring( j + 1 );

            _fromPackages.add( Repackager.splitPath( from, '.' ) );
            _toPackages.add( Repackager.splitPath( to, '.' ) );
        }

        _fromMatchers = new Matcher [ _fromPackages.size() * 2 ];
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

            _fromMatchers[ off + i ] = Pattern.compile( pattern ).matcher( null );
            _toPackageNames[ off + i ] = toPackage;
        }
    }

    public void repackage ( StringBuffer sb )
    {
        for ( int i = 0 ; i < _fromMatchers.length ; i++ )
        {
            Matcher matcher = (Matcher) _fromMatchers[ i ];

            matcher.reset( sb );

            while ( matcher.find() )
                sb.replace( matcher.start(), matcher.end(), _toPackageNames[ i ] );
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
    
    public static ArrayList splitPath ( String path, char separator )
    {
        ArrayList components = new ArrayList();
        
        for ( ; ; )
        {
            int i = path.indexOf( separator );

            if (i < 0)
                break;

            components.add( path.substring( 0, i ) );
            
            path = path.substring( i + 1 );
        }

        if (path.length() > 0)
            components.add( path );

        return components;
    }
    
    public static String dirForPath ( String path )
    {
        return new File(path).getParent();
    }

    private List _fromPackages;
    private List _toPackages;
    
    private Matcher[] _fromMatchers;
    private String[]  _toPackageNames;
}