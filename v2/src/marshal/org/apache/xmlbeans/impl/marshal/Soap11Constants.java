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

package org.apache.xmlbeans.impl.marshal;

import javax.xml.namespace.QName;

final class Soap11Constants
{
    static final QName ID_NAME = new QName("id");
    static final QName REF_NAME = new QName("href");

    private static final char REF_PREFIX_CHAR = '#';
    private static final String ID_PREFIX = "i";
    private static final String REF_PREFIX = REF_PREFIX_CHAR + ID_PREFIX;

    private Soap11Constants()
    {
    }

    static String constructRefValueFromId(int id)
    {
        return REF_PREFIX + Integer.toString(id);
    }

    static String constructIdValueFromId(int id)
    {
        return ID_PREFIX + Integer.toString(id);
    }


    //returns null on error
    static String extractIdFromRef(String attval)
    {
        char firstchar = attval.charAt(0);
        if (REF_PREFIX_CHAR == firstchar) {
            return attval.substring(1);
        } else {
            return null;
        }
    }


}
