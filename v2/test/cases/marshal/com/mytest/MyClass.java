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

/**
 *  @xsdgen:complexType.rootElement load
 */
public class MyClass
{


    private YourClass myelt;
    private String myatt = "DEFAULT:" + YourClass.RND.nextInt();


    //generic factory
    public Object createYourClass()
    {
        return new YourClass();
    }


    public YourClass getMyelt()
    {
        return myelt;
    }

    public void setMyelt(YourClass myelt)
    {
        this.myelt = myelt;
    }

    /**
     *  @xsdgen:element.nillable true
     */
    public String getMyatt()
    {
        return myatt;
    }

    public void setMyatt(String myatt)
    {
        this.myatt = myatt;
    }


    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MyClass)) return false;

        final MyClass myClass = (MyClass)o;

        if (myatt != null ? !myatt.equals(myClass.myatt) : myClass.myatt != null) return false;
        if (myelt != null ? !myelt.equals(myClass.myelt) : myClass.myelt != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (myelt != null ? myelt.hashCode() : 0);
        result = 29 * result + (myatt != null ? myatt.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "com.mytest.MyClass{" +
            "myelt=" + myelt +
            ", myatt='" + myatt + "'" +
            "}";
    }


}
