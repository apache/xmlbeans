package org.apache.xmlbeans.impl.common;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 24, 2003
 */
public class InvalidLexicalValueException
    extends RuntimeException
{
    public InvalidLexicalValueException()
    {
        super();
    }

    public InvalidLexicalValueException(String msg)
    {
        super(msg);
    }

    public InvalidLexicalValueException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public InvalidLexicalValueException(Throwable cause)
    {
        super(cause);
    }
}
