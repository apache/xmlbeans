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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.SchemaType;

import java.util.Map;
import java.util.HashMap;


/* package protected */

/**
 * Note that this is NOT a generic binding registry for users' binding; this is
 * a registry that is used internally just to manage binding between
 * XBeans for BindingTypes and BindingType wrapper classes.  The reason
 * for this mechanism is that XMLBeans binding is NOT YET powerful enough
 * to bind the various kinds of BindingTypes directly to schema yet.
 * 
 * However, in the future, we hope to make it powerful enough to do so,
 * so that most of the hand-coded binding betwen Java and XML can go away.
 * This class, and all the mechanisms that use it, should go away too.
 */
class KindRegistry
{
    private Map registryClassFromType = new HashMap();
    private Map registryTypeFromClass = new HashMap();
    
    synchronized void registerClassAndType(Class bindingTypeClass, SchemaType bindingTypeSchemaType)
    {
        registryClassFromType.put(bindingTypeSchemaType, bindingTypeClass);
        registryTypeFromClass.put(bindingTypeClass, bindingTypeSchemaType);
    }
    
    synchronized Class classForType(SchemaType type)
    {
        return (Class)registryClassFromType.get(type);
    }
    
    synchronized SchemaType typeForClass(Class clazz)
    {
        return (SchemaType)registryTypeFromClass.get(clazz);
    }
}
