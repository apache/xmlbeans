/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Jul 26, 2004
 */
package org.apache.xmlbeans.impl.inst2xsd;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;

interface XsdGenStrategy
{
    void processDoc(XmlObject[] instances, Inst2XsdOptions options, TypeSystemHolder typeSystemHolder);
}
