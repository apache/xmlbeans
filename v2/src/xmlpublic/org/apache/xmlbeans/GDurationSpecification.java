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

import java.math.BigDecimal;

/**
 * Represents an XML Schema-compatible duration.
 * <p>
 * Both the immutable GDuration and the mutable GDurationBuilder are
 * GDurationSpecifications.  Use this interface where you want to
 * allow callers to pass any implementation of a GDuration.
 * 
 * @see GDuration
 */
public interface GDurationSpecification
{
    /**
     * True if this instance is immutable.
     */
    boolean isImmutable();

    /**
     * Returns the sign of the duration: +1 is forwards
     * and -1 is backwards in time.
     */
    int getSign();

    /**
     * Gets the year component.
     */
    int getYear();

    /**
     * Gets the month-of-year component.
     */
    int getMonth();

    /**
     * Gets the day-of-month component.
     */
    int getDay();

    /**
     * Gets the hour-of-day component.
     */
    int getHour();

    /**
     * Gets the minute-of-hour component.
     */
    int getMinute();

    /**
     * Gets the second-of-minute component.
     */
    int getSecond();

    /**
     * Gets the fraction-of-second. Range from 0 (inclusive) to 1 (exclusive).
     */
    BigDecimal getFraction();

    /**
     * Returns true if all of the individual components
     * of the duration are nonnegative.
     */
    boolean isValid();

    /**
     * Comparison to another GDuration.
     * <ul>
     * <li>Returns -1 if this < date. (less-than)
     * <li>Returns 0 if this == date. (equal)
     * <li>Returns 1 if this > date. (greater-than)
     * <li>Returns 2 if this <> date. (incomparable)
     * </ul>
     * Two instances are incomparable if they have different amounts
     * of information.
     */
    int compareToGDuration(GDurationSpecification duration);
}
