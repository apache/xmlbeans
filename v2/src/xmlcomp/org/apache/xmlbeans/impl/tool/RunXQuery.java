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

package org.apache.xmlbeans.impl.tool;

import java.io.*;
import java.util.*;
import org.apache.xmlbeans.*;

public class RunXQuery
{
    public static void main ( String[] args ) throws Exception
    {
        CommandLine cl =
            new CommandLine(
                args,
                Arrays.asList( new String[] { "q", "qf" } ) );
        
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        args = cl.args();
        
        if (args.length == 0)
        {
            System.out.println("Run an XQuery against an XML instance");
            System.out.println("Usage:");
            System.out.println("xquery [-verbose] [-pretty] [-q <query> | -qf query.xq] [file.xml]*");
            System.out.println(" -q <query> to specify a query on the command-line");
            System.out.println(" -qf <query> to specify a file containing a query");
            System.out.println(" -pretty pretty-prints the results");
            System.out.println(" -license prints license information");
            System.out.println(" the query is run on each XML file specified");
            System.out.println("");
            System.exit(0);
            return;
        }

        boolean verbose = cl.getOpt( "verbose" ) != null;
        boolean pretty = cl.getOpt( "pretty" ) != null;

        //
        // Get and compile the query
        //
        
        String query = cl.getOpt( "q" );
        String queryfile = cl.getOpt( "qf" );

        if (query == null && queryfile == null)
        {
            System.err.println( "No query specified" );
            System.exit(0);
            return;
        }
        
        if (query != null && queryfile != null)
        {
            System.err.println( "Specify -qf or -q, not both." );
            System.exit(0);
            return;
        }
        
        try
        {
            if (queryfile != null)
            {
                File queryFile = new File( queryfile );
                FileInputStream is = new FileInputStream( queryFile );
                InputStreamReader r = new InputStreamReader( is );
                
                StringBuffer sb = new StringBuffer();

                for ( ; ; )
                {
                    int ch = r.read();

                    if (ch < 0)
                        break;

                    sb.append( (char) ch );
                }

                r.close();
                is.close();

                query = sb.toString();
            }
        }
        catch ( Throwable e )
        {
            System.err.println( "Cannot read query file: " + e.getMessage() );
            System.exit(1);
            return;
        }

        if (verbose)
        {
            System.out.println( "Compile Query:" );
            System.out.println( query );
            System.out.println();
        }
            
        try
        {
            query= XmlBeans.compileQuery( query );
        }
        catch ( Exception e )
        {
            System.err.println( "Error compiling query: " + e.getMessage() );
            System.exit(1);
            return;
        }

        //
        // Get the instance
        //
        
        File[] files = cl.getFiles();
        
        for (int i = 0; i < files.length; i++)
        {
            XmlObject x;
                
            try
            {
                if (verbose)
                {
                    InputStream is = new FileInputStream( files[i] );

                    for ( ; ; )
                    {
                        int ch = is.read();

                        if (ch < 0)
                            break;

                        System.out.write( ch );
                    }
                    
                    is.close();

                    System.out.println();
                }
                
                x = XmlObject.Factory.parse( files[i] );
            }
            catch ( Throwable e )
            {
                System.err.println( "Error parsing instance: " + e.getMessage() );
                System.exit(1);
                return;
            }
            
            if (verbose)
            {
                System.out.println( "Executing Query..." );
                System.err.println();
            }
    
            XmlObject[] result = null;
    
            try
            {
                result = x.execQuery( query );
            }
            catch ( Throwable e )
            {
                System.err.println( "Error executing query: " + e.getMessage() );
                System.exit(1);
                return;
            }
    
            if (verbose)
            {
                System.out.println( "Query Result:" );
            }
            
            XmlOptions opts = new XmlOptions();
            opts.setSaveOuter();
            if (pretty)
                opts.setSavePrettyPrint();
            
            for (int j = 0; j < result.length; j++)
            {
                result[j].save( System.out, opts );
                System.out.println();
            }
        }
    }
}