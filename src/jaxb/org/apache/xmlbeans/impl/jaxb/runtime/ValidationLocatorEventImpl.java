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

package org.apache.xmlbeans.impl.jaxb.runtime;

import javax.xml.bind.ValidationEventLocator;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;

public class ValidationLocatorEventImpl implements ValidationEventLocator 
{
    int _lineNumber;
    int _columnNumber;
    int _offset;
    URL _url;
    XmlObject _object;

    public static ValidationLocatorEventImpl create(XmlError error)
    {
        return new ValidationLocatorEventImpl(error.getLine(), error.getColumn(),
            error.getOffset(), error.getObjectLocation(), error.getSourceName());
    }

    private ValidationLocatorEventImpl(int lineNumber, int columnNumber,
        int offset, XmlObject object, String sourceName)
    {
        _lineNumber = lineNumber;
        _columnNumber = columnNumber;
        _offset = offset;
        _object = object;

        try { _url = new URL(sourceName); }
        catch (MalformedURLException e) {}

    }

    public java.net.URL getURL()
    {
        return _url;
    }
    public int getOffset()
    {
        return _offset;
    }
    public int getLineNumber()
    {
        return _lineNumber;
    }

    public int getColumnNumber()
    {
        return _columnNumber;
    }

    public org.w3c.dom.Node getNode()
    {
        return null;
    }
    public java.lang.Object getObject()
    {
        return _object;
    }

}
