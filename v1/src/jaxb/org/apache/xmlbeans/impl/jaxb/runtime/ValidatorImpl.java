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

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.Validator;
import javax.xml.bind.ValidationException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.PropertyException;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.bind.helpers.ValidationEventImpl;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlError;

public class ValidatorImpl implements Validator 
{
    ValidationEventHandler _handler = new DefaultValidationEventHandler();

    public void setEventHandler(ValidationEventHandler handler) 
    {
        _handler = handler;
    }

    public ValidationEventHandler getEventHandler() 
    {
        return _handler;
    }

    public Object getProperty(String property) throws PropertyException
    {
        throw new PropertyException(property);
    }

    public void setProperty(String property, Object value) throws PropertyException
    {
        throw new PropertyException(property,  value);
    }

    public boolean validate(Object o) throws ValidationException
    {
        if (o == null)
            throw new IllegalArgumentException();

        if (! ( o instanceof XmlObject))
            throw new ValidationException("Cannot validate tree rooted at subrootObj.");

        return validateImpl((XmlObject)o, _handler);
    }

    public boolean validateRoot(Object o) throws ValidationException
    {
        return validate(o);
    }

    static boolean validateImpl(XmlObject o, ValidationEventHandler handler)
        throws ValidationException
    {

        assert o != null && handler != null;

        final List l = new ArrayList();
        final XmlOptions opts = new XmlOptions().setErrorListener(l);

        final boolean valid = o.validate(opts);

        // convert to ValidationEvent and dispatch
        for (int i = 0 ; i < l.size() ; i++)
        {
            XmlError xe = (XmlError)l.get(i);

            int severity = xe.getSeverity() == XmlError.SEVERITY_ERROR ? 
                ValidationEvent.ERROR :
                ValidationEvent.WARNING;

            ValidationEvent ve = new ValidationEventImpl(severity, xe.getMessage(), 
                ValidationLocatorEventImpl.create(xe));

            try 
            {
                if (! handler.handleEvent(ve))
                    throw new ValidationException(ve.getMessage());
            }
            catch (RuntimeException e)
            {
                throw new ValidationException(e);
            }
        }

        return valid;
    }

}
