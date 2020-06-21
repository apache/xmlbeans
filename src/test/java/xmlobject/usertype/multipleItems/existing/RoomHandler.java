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

package xmlobject.usertype.multipleItems.existing;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

public class RoomHandler
{

    public static void encodeRoom(Room obj, SimpleValue target)
    {
        String digits;
        if (obj.getDigits() < 10)
            digits = "00" + Integer.toString(obj.getDigits());
        else if (obj.getDigits() < 100)
            digits = "0" + Integer.toString(obj.getDigits());
        else
            digits = Integer.toString(obj.getDigits());
        target.setStringValue(digits + "-" + obj.getLetters());
    }


    public static Room decodeRoom(SimpleValue obj) throws XmlValueOutOfRangeException
    {
        String encoded = obj.getStringValue();
        if (encoded.length() != 6)
            throw new XmlValueOutOfRangeException("Invalid Room format: " + encoded);

        Room sku = new Room();
        try
        {
            sku.setDigits(Integer.parseInt(encoded.substring(0,3)));
        } catch (NumberFormatException e) {
            throw new XmlValueOutOfRangeException("Invalid Room format: " + encoded);
        } catch (IllegalArgumentException e) {
            throw new XmlValueOutOfRangeException("Invalid Room format: " + encoded);
        }

        sku.setLetters(encoded.substring(4,6));
        return sku;
    }
}
