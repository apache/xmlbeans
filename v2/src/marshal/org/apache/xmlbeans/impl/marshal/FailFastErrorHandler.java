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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.marshal.util.collections.EmptyIterator;

import java.util.AbstractCollection;
import java.util.Iterator;

public class FailFastErrorHandler extends AbstractCollection
{
    private static final FailFastErrorHandler INSTANCE =
        new FailFastErrorHandler();

    public static FailFastErrorHandler getInstance()
    {
        return INSTANCE;
    }

    private FailFastErrorHandler()
    {
    }

    //TODO: this is pretty ugly in that we are throwing a Runtime Exception
    //we really need to revisit the entire error propogation strategy.
    public boolean add(Object obj)
    {
        if (obj instanceof XmlError) {
            XmlError err = (XmlError)obj;
            throw new XmlRuntimeException(err);
        }
        throw new XmlRuntimeException("unknown error: " + obj);
    }

    public Iterator iterator()
    {
        return EmptyIterator.getInstance();
    }

    public int size()
    {
        return 0;
    }
}
