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
package org.apache.xmlbeans.impl.inst2xsd;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 *
 * This class generates a set of XMLSchemas from an instance XML document.
 *
 * How it works:
 *  - first: pass through all the instances, building a TypeSystemHolder structure
 *  - second: serialize the TypeSystemHolder structure into   
 */
public class Inst2Xsd
{
    public static void main(String[] args)
        throws IOException, XmlException
    {
        if (args.length != 1)
        {
            System.out.println("Usage: Inst2xsd instance.xml");
            return;
        }

        File instFile = new File(args[0]);
        Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();
        inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
        SchemaDocument[] schemas = inst2xsd(new Reader[] {new FileReader(instFile)}, inst2XsdOptions);
    }

    private Inst2Xsd() {}

    public static SchemaDocument[] inst2xsd(Reader[] instReaders, Inst2XsdOptions options)
        throws IOException, XmlException
    {
        XmlObject[] instances = new XmlObject[ instReaders.length ];
        for (int i = 0; i < instReaders.length; i++)
        {
            instances[i] = XmlObject.Factory.parse(instReaders[i]);
        }
        return inst2xsd(instances, options);
    }

    public static SchemaDocument[] inst2xsd(XmlObject[] instances, Inst2XsdOptions options)
    {
        if (options==null)
            options = new Inst2XsdOptions();

        // create structure
        TypeSystemHolder typeSystemHolder = new TypeSystemHolder();

        XsdGenStrategy strategy;
        switch (options.getDesign())
        {
            case Inst2XsdOptions.DESIGN_RUSSIAN_DOLL:
                strategy = new RussianDollStrategy();
                break;

            case Inst2XsdOptions.DESIGN_SALAMI_SLICE:
                strategy = new SalamiSliceStrategy();
                break;

            case Inst2XsdOptions.DESIGN_VENETIAN_BLIND:
                strategy = new VenetianBlindStrategy();
                break;

            default:
                throw new IllegalArgumentException("Unknown design.");
        }
        // processDoc the instance
        strategy.processDoc(instances, options, typeSystemHolder);

        // debug only
        //System.out.println("typeSystemHolder.toString(): " + typeSystemHolder);
        SchemaDocument[] sDocs = typeSystemHolder.getSchemaDocuments();

        for (int i = 0; i < sDocs.length; i++)
        {
            System.out.println("--------------------\n\n" + sDocs[i] );
        }
        assert validateInstances(sDocs, instances);
        // end debug only

        return sDocs;
    }

    private static boolean validateInstances(SchemaDocument[] sDocs, XmlObject[] instances)
    {
        SchemaTypeLoader sLoader;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        try
        {
            sLoader = XmlBeans.loadXsd(sDocs, schemaOptions);
        }
        catch (Exception e)
        {
            if (compErrors.isEmpty() || !(e instanceof XmlException))
            {
                e.printStackTrace(System.out);
            }
            System.out.println("Schema invalid");
            for (Iterator errors = compErrors.iterator(); errors.hasNext(); )
                System.out.println(errors.next());
            return false;
        }

        boolean result = true;

        for (int i = 0; i < instances.length; i++)
        {
            String instance = instances[i].toString();

            XmlObject xobj;

            try
            {
                xobj = sLoader.parse( instance, null, new XmlOptions().setLoadLineNumbers() );
            }
            catch (XmlException e)
            {
                System.out.println("Error:\n" + instance + " not loadable: " + e);
                e.printStackTrace(System.out);
                result = false;
                continue;
            }

            Collection errors = new ArrayList();

            if (xobj.schemaType() == XmlObject.type)
            {
                System.out.println(instance + " NOT valid.  ");
                System.out.println("  Document type not found." );
                result = false;
            }
            else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
                System.out.println("Instance[" + i + "] valid.");
            else
            {
                System.out.println("Instance[" + i + "] NOT valid.");
                for (Iterator it = errors.iterator(); it.hasNext(); )
                {
                    System.out.println("    " + it.next());
                }
                result = false;
            }
        }

        return result;
    }

}