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

import javax.xml.stream.Location;

final class EmptyLocation
    implements Location
{
    private static final Location INSTANCE = new EmptyLocation();

    private EmptyLocation()
    {
    }

    public static Location getInstance()
    {
        return INSTANCE;
    }

    public int getLineNumber()
    {
        return -1;
    }

    public int getColumnNumber()
    {
        return -1;
    }

    public int getCharacterOffset()
    {
        return -1;
    }

    public String getLocationURI()
    {
        return null;
    }

    public String getPublicId()
    {
        return null;
    }

    public String getSystemId()
    {
        return null;
    }
}
