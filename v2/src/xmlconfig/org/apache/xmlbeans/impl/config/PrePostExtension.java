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
/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Apr 12, 2004
 */
package org.apache.xmlbeans.impl.config;

import org.apache.xmlbeans.XmlObject;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.Extensionconfig;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

public class PrePostExtension
{
    public static int OPERATION_SET = 1;
    public static int OPERATION_INSERT = 2;
    public static int OPERATION_REMOVE = 3;

    private static final Class[] PARAMTYPES_PREPOST = new Class[]{int.class, XmlObject.class, QName.class, boolean.class, int.class};
    private static final String SIGNATURE = "(int, org.apache.xmlbeans.XmlObject, javax.xml.namespace.QName, boolean, int)";

    private NameSet _xbeanSet;
    private Class _delegateToClass;
    private String _delegateToClassName;
    private Method _preSet;
    private Method _postSet;

    static PrePostExtension newInstance(NameSet xbeanSet, Extensionconfig.PrePostSet prePostXO)
    {
        if (prePostXO==null)
            return null;

        PrePostExtension result = new PrePostExtension();

        result._xbeanSet = xbeanSet;
        result._delegateToClassName = prePostXO.getStaticHandler();
        result._delegateToClass = InterfaceExtension.validateClass(result._delegateToClassName, prePostXO);

        if ( result._delegateToClass==null ) // no HandlerClass
        {
            SchemaConfig.warning("Handler class '" + prePostXO.getStaticHandler() + "' not found on classpath, skip validation.", prePostXO);
            return result;
        }

        if (!result.lookAfterPreAndPost(prePostXO))
            return null;

        return result;
    }

    private boolean lookAfterPreAndPost(XmlObject loc)
    {
        assert _delegateToClass!=null : "Delegate to class handler expected.";
        boolean valid = true;

        try
        {
            _preSet = _delegateToClass.getMethod("preSet", PARAMTYPES_PREPOST);

            if (!_preSet.getReturnType().equals(boolean.class))
            {
		// just emit an warning and don't remember as a preSet
                SchemaConfig.warning("Method '" + _delegateToClass.getName() +
                    ".preSet" + SIGNATURE + "' " +
                    "should return boolean to be considered for a preSet handler.", loc);
                _preSet = null;
            }
        }
        catch (NoSuchMethodException e)
        {} // not available is ok, _preSet will be null
        catch (SecurityException e)
        {
            SchemaConfig.error("Security violation for class '" + _delegateToClass.getName() +
                "' accesing method preSet" + SIGNATURE , loc);
            valid = false;
        }

        try
        {
            _postSet = _delegateToClass.getMethod("postSet", PARAMTYPES_PREPOST);
        }
        catch (NoSuchMethodException e)
        {} // not available is ok, _postSet will be null
        catch (SecurityException e)
        {
            SchemaConfig.error("Security violation for class '" + _delegateToClass.getName() +
                "' accesing method postSet" + SIGNATURE, loc);
            valid = false;
        }

        if (_preSet==null && _postSet==null)
        {
            SchemaConfig.error("prePostSet handler specified '" + _delegateToClass.getName() +
                "' but no preSet" + SIGNATURE + " or " +
                "postSet" + SIGNATURE + " methods found.", loc);
            valid = false;
        }

        return valid;
    }

    // public methods
    public boolean contains(String fullJavaName)
    {
        return _xbeanSet.contains(fullJavaName);
    }

    public boolean hasPreCall()
    {
        return _preSet!=null;
    }

    public boolean hasPostCall()
    {
        return _postSet!=null;
    }

    /**
     * Returns the name of the handler in a form that can be put in a java source.
     */
    public String getHandlerNameForJavaSource()
    {
        // used only in validation
        if (_delegateToClass==null)
            return null;

        return InterfaceExtension.emitType(_delegateToClass);
    }

    /**
     * Returns the gened code for makeing the preSet call
     * @param identifier
     * @param isAttr
     * @param index usualy is 'i', or can be -1 for non array properties
     * @return gened code
     */
    public String getPreCall(int opType, String identifier, boolean isAttr, String index)
    {
        return _delegateToClassName + ".preSet(" + opType + ", this, " + identifier + ", " + isAttr + ", " + index + ")";
    }

    /**
     * Returns the gened code for makeing the preSet call
     * @param identifier
     * @param isAttr
     * @param index usualy is 'i', or can be -1 for non array properties
     * @return gened code
     */
    public String getPostCall(int opType, String identifier, boolean isAttr, String index)
    {
        return _delegateToClassName + ".postSet(" + opType + ", this, " + identifier + ", " + isAttr + ", " + index + ");";
    }

    public boolean hasNameSetIntersection(PrePostExtension ext)
    {
        return !NameSet.EMPTY.equals(_xbeanSet.intersect(ext._xbeanSet));
    }
}
