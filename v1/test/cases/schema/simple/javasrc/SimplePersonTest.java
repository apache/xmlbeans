/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

import org.openuri.mytest.Person;
import org.openuri.mytest.CustomerDocument;

import java.util.Date;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import drtcases.TestEnv;
import junit.framework.Assert;

import org.apache.xmlbeans.impl.store.Root;

public class SimplePersonTest
{
    public static void main(String args[]) throws Exception
    {
        test();
    }

    public static void test() throws Exception
    {
        CustomerDocument doc =
            CustomerDocument.Factory.parse(
                TestEnv.xbeanCase("schema/simple/person.xml"), null);

        // Move from the root to the root customer element
        Person person = doc.getCustomer();
        Assert.assertEquals("Howdy", person.getFirstname());
        Assert.assertEquals(4,   person.sizeOfNumberArray());
        Assert.assertEquals(436, person.getNumberArray(0));
        Assert.assertEquals(123, person.getNumberArray(1));
        Assert.assertEquals(44,  person.getNumberArray(2));
        Assert.assertEquals(933, person.getNumberArray(3));
        Assert.assertEquals(2,   person.sizeOfBirthdayArray());
        Assert.assertEquals(new Date("Tue Aug 25 17:00:00 PDT 1998"), person.getBirthdayArray(0));

        Person.Gender.Enum g = person.getGender();
        Assert.assertEquals(Person.Gender.MALE, g);

        Assert.assertEquals("EGIQTWYZJ", new String(person.getHex()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64()));

        Assert.assertEquals("GGIQTWYGG", new String(person.getHexAtt()));
        Assert.assertEquals("This string is base64Binary encoded!",
                            new String(person.getBase64Att()));

        person.setFirstname("George");
        Assert.assertEquals("George", person.getFirstname());

        person.setHex("hex encoding".getBytes());
        Assert.assertEquals("hex encoding", new String(person.getHex()));

        person.setBase64("base64 encoded".getBytes());
        Assert.assertEquals("base64 encoded",
                            new String(person.getBase64()));

        //person.setHexAtt("hex encoding in attributes".getBytes());
        //Assert.assertEquals("hex encoding in attributes",
        //                    new String(person.getHexAtt()));

        //person.setBase64Att("base64 encoded in attributes".getBytes());
        //Assert.assertEquals("base64 encoded in attributes",
        //                    new String(person.getBase64Att()));
//
//        XmlCursor cp = person.newXmlCursor();
//        Root.dump( cp );

//        XmlCursor c = person.xgetBirthdayArray(0).newXmlCursor();

//        Root.dump( c );

//        person.setBirthday(0,new Date("Tue Aug 25 16:00:00 PDT 2001"));

//        Root.dump( c );

//        c.toNextToken();

//        System.out.println( "---" + c.getText() + "---" );

//        Root.dump( c );

//        Assert.assertEquals(person.getBirthdayArray(0), new Date("Tue Aug 25 16:00:00 PDT 2002"));
//
//        person.setFirstname("George");
//        Assert.assertEquals(person.getFirstname(), "George");
//
//        person.addNumber( (short) 69 );
//        Assert.assertEquals(person.countNumber(), 5);
//        Assert.assertEquals(person.getNumberArray(4), 69);
//
//
//        while ( c.hasNextToken() )
//            c.toNextToken();
    }
}
