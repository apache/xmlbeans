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

package org.apache.xmlbeans.impl.tool;

import org.w3.x2001.xmlSchema.SchemaDocument.Schema;
import org.w3.x2001.xmlSchema.ImportDocument.Import;
import org.w3.x2001.xmlSchema.IncludeDocument.Include;

import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public abstract class SchemaImportResolver
{
    /**
     * Called when the ImportLoader wishes to resolve the
     * given import.  Should return a SchemaResource whose
     * "equals" relationship reveals when a SchemaResource is
     * duplicated and shouldn't be examined again.
     *
     * Returns null if the resource reference should be ignored.
     */
    public abstract SchemaResource lookupResource(String nsURI, String URL);

    /**
     * Called to notify that the expected namespace is different from the
     * actual namespace, or if no namespace is known, to report the
     * discovered namespace.
     */
    public abstract void reportActualNamespace(SchemaResource resource, String actualNamespace);

    /**
     * Used to supply a schema resource with an optional associated
     * expected-namespace-URI and original-location-URL.
     *
     * The equals (and hashCode) implementations of the SchemaResource
     * objects will be used to avoid examining the same resource twice;
     * these must be implemented according to the desired rules for
     * determining that two resources are the same.
     */
    public interface SchemaResource
    {
        /**
         * Returns a parsed schema object.
         */

        public Schema getSchema();

        public String getNamespace();
        public String getSchemaLocation();
    }

    protected final void resolveImports(SchemaResource[] resources)
    {
        LinkedList queueOfResources = new LinkedList(Arrays.asList(resources));
        LinkedList queueOfLocators = new LinkedList();
        Set seenResources = new HashSet();

        for (;;)
        {
            SchemaResource nextResource;

            // fetch next resource.
            if (!queueOfResources.isEmpty())
            {
                // either off the initial queue
                nextResource = (SchemaResource)queueOfResources.removeFirst();
            }
            else if (!queueOfLocators.isEmpty())
            {
                // or off the list of locators
                SchemaLocator locator = (SchemaLocator)queueOfLocators.removeFirst();
                nextResource = lookupResource(locator.namespace, locator.schemaLocation);
                if (nextResource == null)
                    continue;
            }
            else
            {
                // if no more, then terminate loop
                break;
            }

            // track and skip duplicates
            if (seenResources.contains(nextResource))
                continue;
            seenResources.add(nextResource);

            // get resource contents
            Schema schema = nextResource.getSchema();
            if (schema == null)
                continue;

            // check actual namespace
            String actualTargetNamespace = schema.getTargetNamespace();
            if (actualTargetNamespace == null)
                actualTargetNamespace = "";

            // report actual namespace
            String expectedTargetNamespace = nextResource.getNamespace();
            if (expectedTargetNamespace == null ||
                    !actualTargetNamespace.equals(expectedTargetNamespace))
            {
                reportActualNamespace(nextResource, actualTargetNamespace);
            }

            // now go through and record all the imports
            Import[] schemaImports = schema.getImportArray();
            for (int i = 0; i < schemaImports.length; i++)
            {
                queueOfLocators.add(new SchemaLocator(schemaImports[i].getNamespace() == null ? "" : schemaImports[i].getNamespace(), schemaImports[i].getSchemaLocation()));
            }
            
            // and record all the includes too
            Include[] schemaIncludes = schema.getIncludeArray();
            for (int i = 0; i < schemaIncludes.length; i++)
            {
                queueOfLocators.add(new SchemaLocator(null, schemaIncludes[i].getSchemaLocation()));
            }
        }
    }

    private static class SchemaLocator
    {
        public SchemaLocator(String namespace, String schemaLocation)
        {
            this.namespace = namespace;
            this.schemaLocation = schemaLocation;
        }

        public final String namespace;
        public final String schemaLocation;
    }
}
