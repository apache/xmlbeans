package com.mytest;

/**
 *  @xsdgen:complexType.rootElement load
 */
public class MyClass {


    private YourClass myelt;
    private String myatt = "DEFAULT:"+YourClass.RND.nextInt();


    public YourClass getMyelt()
    {
        return myelt;
    }

    public void setMyelt(YourClass myelt)
    {
        this.myelt = myelt;
    }


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
