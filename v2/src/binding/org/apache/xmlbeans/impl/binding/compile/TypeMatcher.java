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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;

public interface TypeMatcher
{
    /**
     * Returns a collection of MatchedTypes, advising which
     * Java classes and which Schema types should be matched
     * with each other.  Not every class or type needs to be
     * matched by the matcher.  Any remaining ones will be
     * dealt with automatically if possible, and warnings
     * will be produced for any types that are not covered.
     */ 
    MatchedType[] matchTypes(BothSourceSet bss);
    
    /**
     * Returns a collection of MatchedProperties, advising which
     * Java properties and Schema properties (elements or
     * attributes) should be matched with each other.
     * Any properties not returned here will not be bound.
     * It is acceptable to rebind properties that were already
     * bound in a base class. Conflicts will result in an error.
     */ 
    MatchedProperties[] matchProperties(JClass jClass, SchemaType sType);

    /**
     * Substitutes a class-to-bind for a declared-class seen.
     * This is used when, for example, an interface is used to
     * always stand in for a specific implementation class.  In
     * reality, it is the implementation class which is being
     * bound, but all the declared properties have types corresponding
     * to the interface.
     */
    JClass substituteClass(JClass declaredClass);
    
    public static class MatchedType
    {
        private JClass jClass;
        private SchemaType sType;

        public MatchedType(JClass jClass, SchemaType sType)
        {
            this.jClass = jClass;
            this.sType = sType;
        }

        public JClass getJClass()
        {
            return jClass;
        }

        public SchemaType getSType()
        {
            return sType;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof MatchedType)) return false;

            final MatchedType matchedType = (MatchedType) o;

            if (!jClass.equals(matchedType.jClass)) return false;
            if (!sType.equals(matchedType.sType)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = jClass.hashCode();
            result = 29 * result + sType.hashCode();
            return result;
        }
    }
    
    public static class MatchedProperties
    {
        private JProperty jProperty;
        private SchemaProperty sProperty;

        public MatchedProperties(JProperty jProperty, SchemaProperty sProperty)
        {
            this.jProperty = jProperty;
            this.sProperty = sProperty;
        }

        public JProperty getJProperty()
        {
            return jProperty;
        }

        public SchemaProperty getSProperty()
        {
            return sProperty;
        }
    }
    
    
    
}
