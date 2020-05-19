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

package org.apache.xmlbeans.impl.config;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;


public class PrePostExtensionImpl implements PrePostExtension {

    private static final String[] PARAMTYPES_STRING = {
        "int", "org.apache.xmlbeans.XmlObject", "javax.xml.namespace.QName", "boolean", "int"
    };
    private static final String SIGNATURE = "(" + String.join(", ", PARAMTYPES_STRING) + ")";

    private NameSet _xbeanSet;
    private ClassOrInterfaceDeclaration _delegateToClass;
    private String _delegateToClassName;
    private MethodDeclaration _preSet;
    private MethodDeclaration _postSet;

    static PrePostExtensionImpl newInstance(Parser loader, NameSet xbeanSet, Extensionconfig.PrePostSet prePostXO)
    {
        if (prePostXO==null)
            return null;

        PrePostExtensionImpl result = new PrePostExtensionImpl();

        result._xbeanSet = xbeanSet;
        result._delegateToClassName = prePostXO.getStaticHandler();
        result._delegateToClass = InterfaceExtensionImpl.validateClass(loader, result._delegateToClassName, prePostXO);

        if ( result._delegateToClass==null ) // no HandlerClass
        {
            BindingConfigImpl.warning("Handler class '" + prePostXO.getStaticHandler() + "' not found on classpath, skip validation.", prePostXO);
            return result;
        }

        if (!result.lookAfterPreAndPost(loader, prePostXO))
            return null;

        return result;
    }

    private boolean lookAfterPreAndPost(Parser loader, XmlObject loc) {
        assert (_delegateToClass!=null) : "Delegate to class handler expected.";
        boolean valid = true;

        _preSet = InterfaceExtensionImpl.getMethod(_delegateToClass, "preSet", PARAMTYPES_STRING);
        // _preSet==null is ok

        if (_preSet!=null && !_preSet.getType().equals(PrimitiveType.booleanType())) {
            // just emit an warning and don't remember as a preSet
            BindingConfigImpl.warning("Method '" + _delegateToClass.getNameAsString() +
                ".preSet" + SIGNATURE + "' " +
                "should return boolean to be considered for a preSet handler.", loc);
            _preSet = null;
        }

        _postSet = InterfaceExtensionImpl.getMethod(_delegateToClass, "postSet", PARAMTYPES_STRING);
        // _postSet==null is ok

        if (_preSet==null && _postSet==null)
        {
            BindingConfigImpl.error("prePostSet handler specified '" + _delegateToClass.getNameAsString() +
                "' but no preSet" + SIGNATURE + " or postSet" + SIGNATURE + " methods found.", loc);
            valid = false;
        }

        return valid;
    }

    // public methods
    public NameSet getNameSet()
    {
        return _xbeanSet;
    }

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

    public String getStaticHandler()
    {
        return _delegateToClassName;
    }

    /**
     * Returns the name of the handler in a form that can be put in a java source.
     */
    public String getHandlerNameForJavaSource() {
        return (_delegateToClass == null) ? null : _delegateToClass.getNameAsString();
    }

    boolean hasNameSetIntersection(PrePostExtensionImpl ext)
    {
        return !NameSet.EMPTY.equals(_xbeanSet.intersect(ext._xbeanSet));
    }

}
