package org.apache.xmlbeans.impl.binding.bts;

import java.util.*;
import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: pcal
 * Date: Oct 28, 2003
 * Time: 10:44:54 AM
 * To change this template use Options | File Templates.
 */
public class SimpleDocumentBinding extends BindingType
{
  private String mElementName = "";

  public SimpleDocumentBinding(BindingTypeName btname, String elementName) {
    super(btname);
    System.out.println("++"+elementName);
    mElementName = elementName;
  }

  public SimpleDocumentBinding(org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleDocumentBinding node)
  {
    super(node);
    mElementName = node.getElementName();
  }

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
  {
    org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleDocumentBinding sdbNode =
            (org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleDocumentBinding)super.write(node);
    sdbNode.setElementName(mElementName);
    return sdbNode;
  }

  public String getElementName() { return mElementName; }

  public void setElementName(String name) { mElementName = name; }
}
