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

public class Room
{
    private int digits;
    private String letters;


    public Room()
    {
    }

    public Room(int digits, String letters)
    {
        setDigits(digits);
        setLetters(letters);
    }

    public int getDigits()
    {
        return digits;
    }

    public void setDigits(int digits)
    {
        if (digits > 999 || digits < 0)
            throw new IllegalArgumentException("bad digits");
        this.digits = digits;
    }

    public String getLetters()
    {
        return letters;
    }

    public void setLetters(String letters)
    {
        if (letters == null || letters.length() != 2)
            throw new IllegalArgumentException("bad letters");
        this.letters = letters;
    }
}
