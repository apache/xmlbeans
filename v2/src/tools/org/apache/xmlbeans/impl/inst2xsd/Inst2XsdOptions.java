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
package org.apache.xmlbeans.impl.inst2xsd;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 19, 2004

     Options:
       * Design
          o Russian Doll Design
          o Salami Slice Design
          o Venetian Blind Design
       * Simple content types (leafs)
          o smart (default) - try to find out the right simple shema type
          o always xsd:string
       * Use enumeration - when there are multiple valid values in a list
          o never
          o only if not more than ( 20 ) - number option
 */
public class Inst2XsdOptions
{
    // design
    public static final int DESIGN_RUSSIAN_DOLL   = 1;
    public static final int DESIGN_SALAMI_SLICE   = 2;
    public static final int DESIGN_VENETIAN_BLIND = 3;

    private int _design = DESIGN_RUSSIAN_DOLL;

    // schema type for simple content values
    public static final int SIMPLE_CONTENT_TYPES_SMART  = 1;
    public static final int SIMPLE_CONTENT_TYPES_STRING = 2;

    private int _simpleContentTypes = SIMPLE_CONTENT_TYPES_SMART;

    // use enumeration
    public static final int ENUMERATION_NEVER = 1;
    public static final int ENUMERATION_NOT_MORE_THAN_DEFAULT = 10;

    private int _enumerations = ENUMERATION_NOT_MORE_THAN_DEFAULT;


    public int getDesign()
    {
        return _design;
    }

    /**
     * Design
       o Russian Doll Design
       o Salami Slice Design
       o Venetian Blind Design
     * @param designType
     * @see #DESIGN_RUSSIAN_DOLL
     * @see #DESIGN_SALAMI_SLICE
     * @see #DESIGN_VENETIAN_BLIND
     */
    public void setDesign(int designType)
    {
        _design = designType;
    }

    public boolean isUseEnumerations()
    {
        return _enumerations>ENUMERATION_NEVER;
    }

    public int getUseEnumerations()
    {
        return _enumerations;
    }

    public void setUseEnumerations(int useEnumerations)
    {
        _enumerations = useEnumerations;
    }

    public int getSimpleContentTypes()
    {
        return _simpleContentTypes;
    }

    public void setSimpleContentTypes(int simpleContentTypes)
    {
        _simpleContentTypes = simpleContentTypes;
    }
}