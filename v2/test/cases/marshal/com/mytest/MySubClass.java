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

package com.mytest;

import java.math.BigInteger;

public class MySubClass
    extends MyClass
{

    private BigInteger bigInt;
    private boolean issetBigInt;

    public BigInteger getBigInt()
    {
        return bigInt;
    }

    public void setBigInt(BigInteger bigInt)
    {
        this.bigInt = bigInt;
        setIsSetBigInt(bigInt != null);
    }

    public boolean isSetBigInt() {
        return issetBigInt;
    }

    public void setIsSetBigInt(boolean val) {
        issetBigInt = val;
    }

    public String toString()
    {
        return "com.mytest.MySubClass{" +
            "super=" + super.toString() +
            " bigInt=" + bigInt +
            "}";
    }


}
