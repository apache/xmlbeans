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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaTypeSystem;

public interface TypeMatcher
{

    /**
     * Called to initializes the TypeMatcher instance.  This is guaranteed
     * to be called prior to any of the match... methods.
     */
    public void init(TypeMatcherContext ctx);

    /**
     * Returns a collection of MatchedTypes, advising which
     * Java classes and which Schema types should be matched
     * with each other.  Not every class or type needs to be
     * matched by the matcher.  Any remaining ones will be
     * dealt with automatically if possible, and warnings
     * will be produced for any types that are not covered.
     */ 
    MatchedType[] matchTypes(JClass[] classes, SchemaTypeSystem sts);
    
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
