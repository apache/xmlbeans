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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;

public class SchemaLocalElementImpl extends SchemaParticleImpl
        implements SchemaLocalElement, SchemaWSDLArrayType
{
    private boolean _blockExt;
    private boolean _blockRest;
    private boolean _blockSubst;
    protected boolean _abs;
    private SchemaAnnotation _annotation;
    private SOAPArrayType _wsdlArrayType;
    private SchemaIdentityConstraint.Ref[] _constraints = new SchemaIdentityConstraint.Ref[0];


    public SchemaLocalElementImpl()
    {
        setParticleType(SchemaParticle.ELEMENT);
    }

    public boolean blockExtension()
    {
        return _blockExt;
    }

    public boolean blockRestriction()
    {
        return _blockRest;
    }

    public boolean blockSubstitution()
    {
        return _blockSubst;
    }

    public boolean isAbstract()
    {
        return _abs;
    }

    public void setAbstract(boolean abs)
    {
        _abs = abs;
    }

    public void setBlock(boolean extension, boolean restriction, boolean substitution)
    {
        mutate();
        _blockExt = extension;
        _blockRest = restriction;
        _blockSubst = substitution;
    }

    public void setAnnotation(SchemaAnnotation ann)
    {
        _annotation = ann;
    }

    public void setWsdlArrayType(SOAPArrayType arrayType)
    {
        _wsdlArrayType = arrayType;
    }

    public SchemaAnnotation getAnnotation()
    {
        return _annotation;
    }

    public SOAPArrayType getWSDLArrayType()
    {
        return _wsdlArrayType;
    }

    public void setIdentityConstraints(SchemaIdentityConstraint.Ref[] constraints) {
        mutate();
        _constraints = constraints;
    }

    public SchemaIdentityConstraint[] getIdentityConstraints() {
        SchemaIdentityConstraint[] result = new SchemaIdentityConstraint[_constraints.length];
        for (int i = 0 ; i < result.length ; i++)
            result[i] = _constraints[i].get();
        return result;
    }

    public SchemaIdentityConstraint.Ref[] getIdentityConstraintRefs() {
        SchemaIdentityConstraint.Ref[] result = new SchemaIdentityConstraint.Ref[_constraints.length];
        System.arraycopy(_constraints, 0, result, 0, result.length);
        return result;
    }

}
