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

public class SimpleContentExample
{
    private String simpleContent;

    private float floatAttOne;


    public String getSimpleContent()
    {
        return simpleContent;
    }

    public void setSimpleContent(String simpleContent)
    {
        this.simpleContent = simpleContent;
    }

    public float getFloatAttOne()
    {
        return floatAttOne;
    }

    public void setFloatAttOne(float floatAttOne)
    {
        this.floatAttOne = floatAttOne;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SimpleContentExample)) return false;

        final SimpleContentExample simpleContentExample = (SimpleContentExample)o;

        if (floatAttOne != simpleContentExample.floatAttOne) return false;
        if (simpleContent != null ? !simpleContent.equals(simpleContentExample.simpleContent) :
            simpleContentExample.simpleContent != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (simpleContent != null ? simpleContent.hashCode() : 0);
        result = 29 * result + Float.floatToIntBits(floatAttOne);
        return result;
    }

    public String toString()
    {
        return "com.mytest.SimpleContentExample{" +
            "simpleContent=" + simpleContent +
            ", floatAttOne=" + floatAttOne +
            "}";
    }

}
