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

public class MySubSubClass
    extends MySubClass
{
    private String subsubname;

    public String getSubsubname()
    {
        return subsubname;
    }

    public void setSubsubname(String subsubname)
    {
        this.subsubname = subsubname;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MySubSubClass)) return false;
        if (!super.equals(o)) return false;

        final MySubSubClass mySubSubClass = (MySubSubClass)o;

        if (subsubname != null ? !subsubname.equals(mySubSubClass.subsubname) :
            mySubSubClass.subsubname != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (subsubname != null ? subsubname.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "com.mytest.MySubSubClass{" +
            "super=" + super.toString() + 
            "subsubname='" + subsubname + "'" +
            "}";
    }


}
