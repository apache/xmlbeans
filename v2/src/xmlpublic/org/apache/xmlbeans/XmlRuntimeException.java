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

package org.apache.xmlbeans;

import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;

/**
 * An unchecked XML exception.
 * May contain any number of {@link XmlError} objects.
 * 
 * @see XmlError
 * @see XmlException
 */ 
public class XmlRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs an XmlRuntimeException from a message.
     */ 
    public XmlRuntimeException ( String m              ) { super( m );    }
    
    /**
     * Constructs an XmlRuntimeException from a message and a cause.
     */ 
    public XmlRuntimeException ( String m, Throwable t ) { super( m, t ); }
    
    /**
     * Constructs an XmlRuntimeException from a cause.
     */ 
    public XmlRuntimeException ( Throwable t           ) { super( t );    }
    
    /**
     * Constructs an XmlRuntimeException from a message, a cause, and a collection of XmlErrors.
     */ 
    public XmlRuntimeException ( String m, Throwable t, Collection errors )
    {
        super( m, t );

        if (errors != null)
            _errors = Collections.unmodifiableList( new ArrayList(errors) );
    }

    /**
     * Constructs an XmlRuntimeException from an XmlError.
     */ 
    public XmlRuntimeException ( XmlError error )
    {
        this( error.toString(), null, error );
    }

    /**
     * Constructs an XmlRuntimeException from a message, a cause, and an XmlError.
     */ 
    public XmlRuntimeException ( String m, Throwable t, XmlError error )
    {
        this( m, t, Collections.singletonList( error ) );
    }
    
    /**
     * Constructs an XmlRuntimeException from an {@link XmlException}.
     */ 
    public XmlRuntimeException ( XmlException xmlException )
    {
        super( xmlException.getMessage(), xmlException.getCause() );

        Collection errors = xmlException.getErrors();

        if (errors != null)
            _errors = Collections.unmodifiableList( new ArrayList( errors ) );
    }
    
    /**
     * Returns the first {@link XmlError} that caused this exception, if any.
     */ 
    public XmlError getError ( )
    {
        if (_errors == null || _errors.size() == 0)
            return null;

        return (XmlError) _errors.get( 0 );
    }
    
    /**
     * Returns the collection of {@link XmlError XmlErrors} that caused this exception, if any.
     */ 
    public Collection getErrors ( )
    {
        return _errors;
    }

    private List _errors;
}