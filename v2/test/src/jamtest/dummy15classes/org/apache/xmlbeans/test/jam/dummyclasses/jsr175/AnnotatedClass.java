package org.apache.xmlbeans.test.jam.dummyclasses.jsr175;

public @interface RequestForEnhancement {
    int    id();        // Unique ID number associated with RFE
    String synopsis();  // Synopsis of RFE
    String engineer()  default "[unassigned]";
    String date()      default "[unimplemented]";
}

@RequestForEnhancement(
    id       = 4561414,
    synopsis = "Balance the federal budget"
)
public abstract class AnnotatedClass {

  public abstract void setFoo(int value);

  public abstract int getFoo();
}
