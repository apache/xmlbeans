package scomp.redefine.detailed;

import scomp.common.BaseCase;
import xbean.scomp.redefine.attrGroupRedefine.AttrGroupEltDocument;


import java.lang.reflect.Method;

public class AttrGroupRedefine extends BaseCase{

    /**
     * test that fields from the old type def are not
     * visible anymore
     */
    public void testCodeGeneration(){
       AttrGroupEltDocument doc=AttrGroupEltDocument.Factory.newInstance();
        AttrGroupEltDocument.AttrGroupElt elt=doc.addNewAttrGroupElt();

       try{
           elt.getClass().getDeclaredField("attr2");
           fail("field should be redefined");
       }catch(NoSuchFieldException e){}


        try{
             elt.getClass().getDeclaredMethod("getAttr1",null);
             elt.getClass().getDeclaredMethod("getAttr2A",null);

           Method m=elt.getClass().getDeclaredMethod("getAttr3A",null);
            assertEquals(m.getReturnType(),Class.forName("java.lang.Integer.TYPE") );
            }catch(NoSuchMethodException e){
            fail("Fields not redefined");
        }   catch (ClassNotFoundException e1){}
    }
}
