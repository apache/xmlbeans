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

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Repackage
{
    public static void main ( String[] args ) throws Exception
    {
        new Repackage().repackage( args );
    }
    
    public void repackage ( String[] args ) throws Exception
    {
        if (args.length != 2 || !args[0].equals( "-repackage" ))
            throw new RuntimeException( "Usage: repackage -repackage [spec]" );
        
        _repackager = new Repackager( args[ 1 ] );

        _fromPackages = _repackager.getFromPackages();
        _toPackages = _repackager.getToPackages();
        
        _packagePattern =
            Pattern.compile( "^\\s*package\\s+((?:\\w|\\.)*)\\s*;", Pattern.MULTILINE );
        
        _moveAlongFiles = new ArrayList();
        _movedDirs = new HashMap();
        
        File currentDir   = new File( "." );
        File buildDir     = new File( currentDir, "build" );
        File repackageDir = new File( buildDir, "repackage" );

        System.out.println( "Deleting repackage dir ..." );
        
        recursiveDelete( repackageDir );

        repackageDir.mkdirs();
        
        ArrayList files = new ArrayList();

        fillFiles( files, currentDir );
        
        System.out.println( "Repackaging " + files.size() + " files ..." );

        int prefixLength = currentDir.getCanonicalPath().length();

        _sourceBase = currentDir;
        _targetBase = repackageDir;

        for ( int i = 0 ; i < files.size() ; i++ )
        {
            File from = (File) files.get( i );

            String name = from.getCanonicalPath().substring( prefixLength + 1 );

            repackageFile( name );
        }
        
        finishMovingFiles();
    }

    public void repackageFile ( String name )
        throws IOException
    {
        if (name.endsWith( ".java" ))
            repackageJavaFile( name );
        else if (name.endsWith( ".xsdconfig" ))
            repackageNonJavaFile( name );
        else if (name.startsWith( "bin" + File.separatorChar ))
            repackageNonJavaFile( name );
        else
            moveAlongWithJavaFiles( name );
    }

    public void moveAlongWithJavaFiles( String name )
    {
        _moveAlongFiles.add(name);
    }
    
    public void finishMovingFiles ( )
        throws IOException
    {
        for ( Iterator i = _moveAlongFiles.iterator(); i.hasNext(); )
        {
            String name = (String) i.next();
            String toName = name;
            
            String srcDir = Repackager.dirForPath( name );
            String toDir = (String) _movedDirs.get( srcDir );
            
            if (toDir != null)
                toName = new File( toDir, new File( name ).getName() ).toString(); 

            if (name.endsWith( ".html" ))
                repackageNonJavaFile( name, toName );
            else
                copyFile( new File( _sourceBase, name ), new File( _targetBase, toName ) );
        }
    }

    public void repackageNonJavaFile ( String name )
        throws IOException
    {
        StringBuffer sb = readFile( new File( _sourceBase, name ) );

        _repackager.repackage( sb );
        
        writeFile( new File( _targetBase, name ), sb );
    }
    
    public void repackageNonJavaFile ( String sourceName, String targetName )
        throws IOException
    {
        StringBuffer sb = readFile( new File( _sourceBase, sourceName ) );

        _repackager.repackage( sb );
        
        writeFile( new File( _targetBase, targetName ), sb );
    }
    
    public void repackageJavaFile ( String name )
        throws IOException
    {
        StringBuffer sb = readFile( new File( _sourceBase, name ) );

        Matcher packageMatcher = _packagePattern.matcher( sb );

        if (packageMatcher.find())
        {
            String pkg = packageMatcher.group( 1 );
            int pkgStart = packageMatcher.start( 1 );
            int pkgEnd = packageMatcher.end( 1 );
            
            if (packageMatcher.find())
                throw new RuntimeException( "Two package specifications found: " + name );
            
            List filePath = Repackager.splitPath( name, File.separatorChar );
            String srcDir = Repackager.dirForPath( name );
            
            // Sort the repackage spec so that longer from's are first to match
            // longest package first

            for ( ; ; )
            {
                boolean swapped = false;

                for ( int i = 1 ; i < filePath.size() ; i++ )
                {
                    String spec1 = (String) filePath.get( i - 1 );
                    String spec2 = (String) filePath.get( i );

                    if (spec1.indexOf( ':' ) < spec2.indexOf( ':' ))
                    {
                        filePath.set( i - 1, spec2 );
                        filePath.set( i, spec1 );

                        swapped = true;
                    }
                }

                if (!swapped)
                    break;
            }

            List pkgPath = Repackager.splitPath( pkg, '.' );

            int f = filePath.size() - 2;

            for ( int i = pkgPath.size() - 1 ; i >= 0 ; i-- )
            {
                if (!pkgPath.get( i ).equals( filePath.get( f-- ) ))
                    throw new RuntimeException( "Package spec differs from file path: " + name );
            }

            List changeTo = null;
            List changeFrom = null;
            
            from:
            for ( int i = 0 ; i < _fromPackages.size() ; i ++ )
            {
                List from = (List) _fromPackages.get( i );

                if (from.size() <= pkgPath.size())
                {
                    for ( int j = 0 ; j < from.size() ; j++ )
                        if (!from.get( j ).equals( pkgPath.get( j )))
                            continue from;

                    changeFrom = from;
                    changeTo = (List) _toPackages.get( i );

                    break;
                }
            }

            if (changeTo != null)
            {
                String newPkg = "";
                String newName = "";

                for ( int i = 0 ; i < changeTo.size() ; i++ )
                {
                    if (i > 0)
                    {
                        newPkg += ".";
                        newName += File.separatorChar;
                    }
                    
                    newPkg += changeTo.get( i );
                    newName += changeTo.get( i );
                }
                
                for ( int i = filePath.size() - pkgPath.size() - 2 ; i >= 0 ; i-- )
                    newName = (String) filePath.get( i ) + File.separatorChar + newName;

                for ( int i = changeFrom.size() ; i < pkgPath.size() ; i++ )
                {
                    newName += File.separatorChar + (String) pkgPath.get( i );
                    newPkg += '.' + (String) pkgPath.get( i );
                }

                newName += File.separatorChar + (String) filePath.get( filePath.size() - 1 );

                sb.replace( pkgStart, pkgEnd, newPkg );

                name = newName;
                String newDir = Repackager.dirForPath( name );
                
                if (!srcDir.equals(newDir))
                {
                    _movedDirs.put(srcDir, newDir);
                }
            }
        }

        _repackager.repackage( sb );
        
        writeFile( new File( _targetBase, name ), sb );
    }

    void writeFile ( File f, StringBuffer chars )
        throws IOException
    {
        f.getParentFile().mkdirs();
        
        OutputStream out = new FileOutputStream( f );
        Writer w = new OutputStreamWriter( out );
        Reader r = new StringReader( chars.toString() );

        copy( r, w );

        r.close();
        w.close();
        out.close();
    }
    
    StringBuffer readFile ( File f )
        throws IOException
    {
        InputStream in = new FileInputStream( f );
        Reader r = new InputStreamReader( in );
        StringWriter w = new StringWriter();

        copy( r, w );

        w.close();
        r.close();
        in.close();

        return w.getBuffer();
    }
    
    public void copyFile ( File from, File to ) throws IOException
    {
        to.getParentFile().mkdirs();
        
        FileInputStream in = new FileInputStream( from );
        FileOutputStream out = new FileOutputStream( to );

        copy( in, out );
        
        out.close();
        in.close();
    }
    
    public void copy ( InputStream in, OutputStream out ) throws IOException
    {
        byte[] buffer = new byte [ 1024 * 16 ];

        for ( ; ; )
        {
            int n = in.read( buffer, 0, buffer.length );

            if (n < 0)
                break;

            out.write( buffer, 0, n );
        }
    }
    
    public void copy ( Reader r, Writer w ) throws IOException
    {
        char[] buffer = new char [ 1024 * 16 ];

        for ( ; ; )
        {
            int n = r.read( buffer, 0, buffer.length );

            if (n < 0)
                break;

            w.write( buffer, 0, n );
        }
    }
    
    public void fillFiles ( ArrayList files, File file ) throws IOException
    {
        if (!file.isDirectory())
        {
            files.add( file );
            return;
        }

        // Exclude the build directory

        if (file.getName().equals( "build" ))
            return;
        
        // Exclude CVS directories
        if (file.getName().equals( "CVS" ))
            return;

        String[] entries = file.list();

        for ( int i = 0 ; i < entries.length ; i++ )
            fillFiles( files, new File( file, entries[ i ] ) );
    }

    public void recursiveDelete ( File file ) throws IOException
    {
        if (!file.exists())
            return;

        if (file.isDirectory())
        {
            String[] entries = file.list();

            for ( int i = 0 ; i < entries.length ; i++ )
                recursiveDelete( new File( file, entries[ i ] ) );
        }

        file.delete();
    }

    private File _sourceBase;
    private File _targetBase;

    private List _fromPackages;
    private List _toPackages;
    
    private Pattern _packagePattern;

    private Repackager _repackager;
    
    private Map _movedDirs;
    private List _moveAlongFiles;
}
