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
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.CompositeBindingLoader;
import org.apache.xmlbeans.impl.binding.tylar.DefaultTylarLoader;
import org.apache.xmlbeans.impl.binding.tylar.Tylar;
import org.apache.xmlbeans.impl.binding.tylar.TylarLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.jar.JarInputStream;

/**
 * creates BindingContext objects from various inputs.
 */
public final class BindingContextFactoryImpl extends BindingContextFactory
{

    public BindingContext createBindingContext(URI tylarUri)
        throws IOException, XmlException
    {
        return createBindingContext(new URI[]{tylarUri});
    }

    public BindingContext createBindingContext(URI[] tylarUris)
        throws IOException, XmlException
    {
        if (tylarUris == null) throw new IllegalArgumentException("null uris");
        //FIXME loader class needs to be pluggable
        TylarLoader loader = DefaultTylarLoader.getInstance();
        if (loader == null) throw new IllegalStateException("null loader");
        return createBindingContext(loader.load(tylarUris));
    }

    public BindingContext createBindingContext(JarInputStream jar)
        throws IOException, XmlException
    {
        if (jar == null) throw new IllegalArgumentException("null InputStream");
        //FIXME loader class needs to be pluggable
        TylarLoader loader = DefaultTylarLoader.getInstance();
        if (loader == null) throw new IllegalStateException("null TylarLoader");
        return createBindingContext(loader.load(jar));
    }

    // REVIEW It's unfortunate that we can't expose this method to the public
    // at the moment.  It's easy to imagine cases where one has already built
    // up the tylar and doesn't want to pay the cost of re-parsing it.
    // Of course, exposing it means we expose Tylar to the public as well,
    // and this should be done with caution.
    public BindingContext createBindingContext(Tylar tylar)
    {
        // build the loader chain - this is the binding files plus
        // the builtin loader
        BindingLoader loader = tylar.getBindingLoader();
        // finally, glue it all together
        return new BindingContextImpl(loader);
    }

    public BindingContext createBindingContext()
    {
        BindingFile empty = new BindingFile();
        return createBindingContext(empty);
    }

    // ========================================================================
    // Private methods

    private static BindingContextImpl createBindingContext(BindingFile bf)
    {
        BindingLoader bindingLoader = buildBindingLoader(bf);
        return new BindingContextImpl(bindingLoader);
    }

    private static BindingLoader buildBindingLoader(BindingFile bf)
    {
        BindingLoader builtins = BuiltinBindingLoader.getInstance();
        return CompositeBindingLoader.forPath(new BindingLoader[]{builtins, bf});
    }

    /**
     * @deprecated We no longer support naked config files.  This is currently
     * only used by MarshalTests
     */
    public BindingContext createBindingContextFromConfig(File bindingConfig)
        throws IOException, XmlException
    {
        BindingConfigDocument doc =
            BindingConfigDocument.Factory.parse(bindingConfig);
        BindingFile bf = BindingFile.forDoc(doc);
        return createBindingContext(bf);
    }


}
