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

package com.bea.test;

import weblogic.xml.schema.model.parser.XSDParserFactory;
import weblogic.xml.schema.model.parser.XSDParser;
import weblogic.xml.schema.model.SchemaDocument;
import weblogic.xml.schema.model.XSDSchema;
import weblogic.xml.schema.model.XSDException;
import org.apache.xmlbeans.xml.stream.XMLInputStreamFactory;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.apache.xmlbeans.xml.stream.events.Name;

import java.io.Reader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.File;

import xml.util.SchemaType;
import xml.util.SchemaTypeSystem;
import org.apache.xmlbeans.schema.SchemaTypeSystemBuilder;
import org.apache.xmlbeans.schema.SchemaTypeCodePrinter;
import org.apache.xmlbeans.tool.CommandLine;
import org.openuri.mytest.Simplicity;
import org.openuri.mytest.Person;

public class Test
{
    public static void main(String[] args)
    {
        /*
        CommandLine cl = new CommandLine(args, null);
        XSDParser xsdparser = XSDParserFactory.newInstance().createXSDParser();
        XMLInputStreamFactory xmlparser = XMLInputStreamFactory.newInstance();
        boolean simpletest = false;

        SchemaTypeSystemBuilder builder = new SchemaTypeSystemBuilder("test");

        args = cl.args();
        for (int i = 0; i < args.length; i++)
        {
            try
            {
                Reader input = new FileReader(args[i]);
                SchemaDocument sdoc = xsdparser.parseSchema(xmlparser.newInputStream(input));
                XSDSchema s = sdoc.getSchema();
                builder.addAllComponents(s);
            }
            catch (FileNotFoundException e)
            {
                System.out.println("Error opening file " + args[i] + ":\n" + e);
            }
            catch (XMLStreamException e)
            {
                System.out.println("Error parsing file " + args[i] + ":\n" + e);
            }
            catch (XSDException e)
            {
                System.out.println("Error parsing schema file " + args[i] + ":\n" + e);
            }
        }

        builder.resolveAllTypes();
        builder.javaizeAllTypes();

        // OK, here we go

        SchemaType[] gtypes = builder.globalTypes();
        for (int i = 0; i < gtypes.length; i++)
        {
            SchemaType type = gtypes[i];

            // print only types in this type system
            if (!type.getSchemaTypeSystem().equals(builder))
                continue;

            try
            {
                System.err.println("Printing type");
                SchemaTypeCodePrinter.printType(new OutputStreamWriter(System.out), type);
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
            }
        }
        */

        boolean simpletest = false;

        if (simpletest)
        {
            TestStore store = new TestStore(org.openuri.mytest.SimpleDocument.type);
            TestStore.Cursor cursor = store.newCursor();

            System.err.println("Got store, filling it in...");
            cursor.addChild(new Name("http://openuri.org/mytest", "simple"));
            cursor.toFirstChild();
            cursor.setAttribute(new Name(null, "note"), "a smallish number");
            cursor.setText("143");
            cursor.toRoot();
            cursor.toFirstChild();
            Simplicity simp = (Simplicity)cursor.obj();
            System.err.println("The value of simplicity: " + simp.intValue());
            System.err.println("And a note: " + simp.getNote());
        }
        else
        {
            TestStore store = new TestStore(org.openuri.mytest.CustomerDocument.type);
            TestStore.Cursor cursor = store.newCursor();
            System.err.println("Got store, filling it in...");
            cursor.addChild(new Name("http://openuri.org/mytest", "customer"));
            cursor.toFirstChild();
            cursor.addChild(new Name("http://openuri.org/mytest", "firstname"));
            cursor.addChild(new Name("http://openuri.org/mytest", "number"));
            cursor.addChild(new Name("http://openuri.org/mytest", "number"));
            cursor.addChild(new Name("http://openuri.org/mytest", "birthday"));
            cursor.addChild(new Name("http://openuri.org/mytest", "number"));
            cursor.addChild(new Name("http://openuri.org/mytest", "number"));
            cursor.addChild(new Name("http://openuri.org/mytest", "birthday"));
            cursor.toFirstChild();
            cursor.setText("Howdy");
            cursor.toNext();
            cursor.setText("436");
            cursor.toNext();
            cursor.setText("123");
            cursor.toNext();
            cursor.setText("1998-08-26Z");
            cursor.toNext();
            cursor.setText("44");
            cursor.toNext();
            cursor.setText("933");
            cursor.toNext();
            cursor.setText("2000-08-06-08:00");
            cursor.toRoot();
            cursor.toFirstChild();
            Person person = (Person)cursor.obj();
            System.err.println("Person.getFirstname() = " + person.getFirstname());
            int sum = 0;
            for (int i = 0; i < person.countOfNumber(); i++)
            {
                System.err.println("Person.getNumber(" + i +") = " + person.getNumber(i));
                sum += person.getNumber(i);
            }
            System.err.println("sum of numbers = " + sum);
            for (int i = 0; i < person.countOfBirthday(); i++)
            {
                System.err.println("Person.getBirthday(" + i +") = " + person.getBirthday(i));
                System.err.println("Person.xgetBirthday(" + i +") = " + person.xgetBirthday(i));
                System.err.println("Person.xgetBirthday(" + i +").gDateValue().getDay() = " + person.xgetBirthday(i).gDateValue().getDay());
                sum += person.getNumber(i);
            }
            System.err.println("Setting firstname to George");
            person.setFirstname("George");
            System.err.println("Person.getFirstname() = " + person.getFirstname());
            System.err.flush();
        }

    }
}
