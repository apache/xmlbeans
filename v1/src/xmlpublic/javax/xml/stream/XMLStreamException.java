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

package javax.xml.stream;

/**
 * The base exception for unexpected processing errors.  This Exception
 * class is used to report well-formedness errors as well as unexpected
 * processing conditions.
 * @version 0.6
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 */
public class XMLStreamException extends Exception {

  protected Throwable nested;
  protected Location location;
  
  /**
   * Default constructor
   */
  public XMLStreamException(){ 
    super();
  }

  /**
   * Construct an exception with the assocated message.
   *
   * @param msg the message to report
   */
  public XMLStreamException(String msg) {
    super(msg);
  }

  /**
   * Construct an exception with the assocated exception
   *
   * @param th a nested exception
   */
  public XMLStreamException(Throwable th) {
    nested = th;
  }

  /**
   * Construct an exception with the assocated message and exception
   *
   * @param th a nested exception
   * @param msg the message to report
   */
  public XMLStreamException(String msg, Throwable th) {
    super(msg);
    nested = th;
  }

  /**
   * Construct an exception with the assocated message, exception and location.
   *
   * @param th a nested exception
   * @param msg the message to report
   * @param location the location of the error 
   */
  public XMLStreamException(String msg, Location location, Throwable th) {
    super("ParseError at [row,col]:["+location.getLineNumber()+","+
          location.getColumnNumber()+"]\n"+
          "Message: "+msg);
    nested = th;
    this.location = location;
  }

  /**
   * Construct an exception with the assocated message, exception and location.
   *
   * @param msg the message to report
   * @param location the location of the error 
   */
  public XMLStreamException(String msg, 
                            Location location) {
    super("ParseError at [row,col]:["+location.getLineNumber()+","+
          location.getColumnNumber()+"]\n"+
          "Message: "+msg);
    this.location = location;
  }
  

  /**
   * Gets the nested exception.
   *
   * @return Nested exception
   */
  public Throwable getNestedException() {
    return nested;
  }

  /**
   * Gets the location of the exception
   *
   * @return the location of the exception, may be null if none is available
   */
  public Location getLocation() {
    return location;
  }

}






