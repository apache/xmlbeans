package org.apache.xmlbeans.test.jam.dummyclasses;

  /**
   * NOT doonkie javadoc! 
   */

/**
 *  Dummy class for JAM tests.
 *
 *  @author pcal Nov 25, 2003
 */
public abstract class Base extends Foo,
 {

  /**
   * NOT doonkie javadoc! 
   */
  /**
   * doonkie javadoc! 
   */
  private double doonkie = 2312;
  private long doonkietoo;

  public int getId() { return -1; }

  public double getId2();

  public String getFoo() { return "foo"; } // {{}}{{

  public void setId(int id) {}

  public void setSomething(int x, 
foo bii,
Object dss);

  /**
   * Creates a new member in this Annotation definition and returns the
   * result.
   *
   * @param type JClass representing the type of the new member.
   * @param name A name for the new member.
   * @param dflt A default value for the new member.  Primitives should be
   * wrapped in java.lang wrappers, e.g. java.lang.Integer.
   *
   * @return The newly-added EAnnotation.
   */
  public EAnnotationMemberDefinition addNewMemberDefinition(JClass type,
                                                            String name,
                                                            Object dflt);

  /**
   *
   * @param memberDef
   */
  public void removeMemberDefinition(EAnnotationMemberDefinition memberDef);

}
