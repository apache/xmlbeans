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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.BindingContextFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.binding.tylar.Tylar;
import org.apache.xmlbeans.impl.binding.tylar.DefaultTylarLoader;
import org.apache.xmlbeans.impl.binding.tylar.TylarLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

/**
 * creates BindingContext objects from various inputs.
 */
public final class BindingContextFactoryImpl
    extends BindingContextFactory
{

  public BindingContext createBindingContext(URI[] tylarUris)
          throws IOException, XmlException {
    if (tylarUris == null) throw new IllegalArgumentException("null uris");
    //FIXME loader needs to be pluggable
    TylarLoader loader = DefaultTylarLoader.getInstance();
    if (loader == null) throw new IllegalArgumentException("null loader");
    Tylar[] tylars = new Tylar[tylarUris.length];
    for (int i = 0; i < tylars.length; i++) {
      tylars[i] = loader.load(tylarUris[i]);
    }
    return createBindingContext(tylars);
  }

  // REVIEW It's unfortunate that we can't expose this method to the public
    // at the moment.  It's easy to imagine cases where one has already built
    // up the tylar and doesn't want to pay the cost of re-parsing it.
    // Of course, exposing it means we expose Tylar to the public as well,
    // and this should be done with caution.
    public BindingContext createBindingContext(Tylar[] tylars)
    {
        // get the binding files
        BindingFile[] bfs = new BindingFile[tylars.length];
        for (int i = 0; i < tylars.length; i++) {
            bfs[i] = tylars[i].getBindingFile();
        }
        // also build the loader chain - this is the binding files plus
        // the builtin loader
        BindingLoader[] loaders = new BindingLoader[bfs.length + 1];
        System.arraycopy(bfs, 0, loaders, 0, bfs.length);
        loaders[loaders.length - 1] = BuiltinBindingLoader.getInstance();
        BindingLoader loader = PathBindingLoader.forPath(loaders);
        // finally, glue it all together
        RuntimeBindingTypeTable tbl = buildUnmarshallingTypeTable(bfs, loader);
        return new BindingContextImpl(loader, tbl);
    }

    public BindingContext createBindingContext()
    {
        BindingFile empty = new BindingFile();
        return createBindingContext(empty);
    }


    public BindingContext createBindingContext(InputStream bindingConfig)
        throws IOException, XmlException
    {
        BindingConfigDocument doc =
            BindingConfigDocument.Factory.parse(bindingConfig);
        return createBindingContext(doc);
    }

    public BindingContext createBindingContext(File bindingConfig)
        throws IOException, XmlException
    {
        BindingConfigDocument doc =
            BindingConfigDocument.Factory.parse(bindingConfig);
        return createBindingContext(doc);
    }

    public static BindingContextImpl createBindingContext(BindingConfigDocument doc)
    {
        BindingFile bf = BindingFile.forDoc(doc);
        return createBindingContext(bf);
    }


    private static BindingContextImpl createBindingContext(BindingFile bf)
    {
        BindingLoader bindingLoader = buildBindingLoader(bf);
        RuntimeBindingTypeTable tbl = buildUnmarshallingTypeTable(bf, bindingLoader);

        return new BindingContextImpl(bindingLoader, tbl);
    }

    private static BindingLoader buildBindingLoader(BindingFile bf)
    {
        BindingLoader builtins = BuiltinBindingLoader.getInstance();
        return PathBindingLoader.forPath(new BindingLoader[]{builtins, bf});
    }


    private static RuntimeBindingTypeTable buildUnmarshallingTypeTable(BindingFile bf,
                                                                       BindingLoader loader)
    {
        RuntimeBindingTypeTable tbl = RuntimeBindingTypeTable.createRuntimeBindingTypeTable();
        populateTable(bf, loader, tbl);
        return tbl;
    }

    private static RuntimeBindingTypeTable buildUnmarshallingTypeTable(BindingFile[] bfs,
                                                                       BindingLoader loader)
    {
        RuntimeBindingTypeTable tbl = RuntimeBindingTypeTable.createRuntimeBindingTypeTable();
        for (int i = 0; i < bfs.length; i++) populateTable(bfs[i], loader, tbl);
        return tbl;
    }

    private static RuntimeBindingTypeTable populateTable(BindingFile bf,
                                                         BindingLoader loader,
                                                         RuntimeBindingTypeTable tbl)
    {
        //TODO scott This may need some more thought; may want to iterate
        //through typenames instead of types and resolve them with the loader.
        //The loader currently isn't really being used here.
        for (Iterator itr = bf.bindingTypes().iterator(); itr.hasNext();) {
            BindingType type = (BindingType)itr.next();
            if (type instanceof SimpleDocumentBinding) continue;
            TypeUnmarshaller um = createTypeUnmarshaller(type, loader, tbl);
            tbl.putTypeUnmarshaller(type, um);
        }
        tbl.initUnmarshallers(loader);
        return tbl;
    }

    private static TypeUnmarshaller createTypeUnmarshaller(BindingType type,
                                                           BindingLoader loader,
                                                           RuntimeBindingTypeTable table)
    {
        //TODO: cleanup this nasty instanceof stuff (Visitor?)

        if (type instanceof SimpleBindingType) {
            //note this could return a static for builtin types
            return createSimpleTypeUnmarshaller((SimpleBindingType)type, loader, table);
        } else if (type instanceof ByNameBean) {
            return new ByNameUnmarshaller((ByNameBean)type);
        }

        throw new AssertionError("UNIMPLEMENTED TYPE: " + type);
    }

    private static TypeUnmarshaller createSimpleTypeUnmarshaller(SimpleBindingType stype,
                                                                 BindingLoader loader,
                                                                 RuntimeBindingTypeTable table)
    {
        TypeUnmarshaller um = table.getTypeUnmarshaller(stype);
        if (um != null) return um;

        //let's try using the as if type
        BindingType asif = loader.getBindingType(stype.getAsIfBindingTypeName());
        if (asif == null) {
            throw new AssertionError("unable to get asif type for " + stype);
        }
        um = table.getTypeUnmarshaller(asif);
        if (um != null) return um;

        String msg = "unable to get simple type unmarshaller for " + stype + " using asif=" + asif;
        throw new AssertionError(msg);
    }

}
