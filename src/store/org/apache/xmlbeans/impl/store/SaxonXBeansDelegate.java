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

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.impl.common.XPath;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Constructor;


/**
 * Date: Dec 6,2004
 *
 * Help class to decouple from xbean_xpath.jar and saxon.jar (version Saxon-B 8.1.1)
 */
public final class SaxonXBeansDelegate
{
    protected static boolean _saxonAvailable = true; //first time assume Saxon is available
    private static Constructor _constructor;
    private static Constructor _xqConstructor;

    private SaxonXBeansDelegate()
    {}

    static void init()
    {
        Class saxonXPathImpl = null;
        Class saxonXQueryImpl = null;
        try
        {
            // from xbean_xpath.jar
            saxonXPathImpl = Class
                .forName("org.apache.xmlbeans.impl.xpath.saxon.XBeansXPath");
            saxonXQueryImpl = Class
                .forName("org.apache.xmlbeans.impl.xquery.saxon.XBeansXQuery");
        }
        catch (ClassNotFoundException e)
        {
            _saxonAvailable = false;
        }
        catch (NoClassDefFoundError e)
        {
            _saxonAvailable = false;
        }

        if (_saxonAvailable)
        {
            try
            {
                _constructor =
                    saxonXPathImpl.getConstructor(new Class[]{String.class,
                                                              String.class,
                                                              Map.class,
                                                              String.class});
                _xqConstructor =
                    saxonXQueryImpl.getConstructor(new Class[]{String.class,
                                                               String.class,
                                                               Integer.class});
            }
            catch (Exception e)
            {
                _saxonAvailable = false;
                throw new RuntimeException(e);
            }
        }
    }

    static SelectPathInterface createInstance(String xpath,
                                              String contextVar,
                                              Map namespaceMap)
    {
        if (_saxonAvailable && _constructor == null)
            init();

        if (_constructor == null)
            return null;

        try
        {
            Object defaultNS = namespaceMap.get(XPath._DEFAULT_ELT_NS);
            if ( defaultNS != null )
                namespaceMap.remove( XPath._DEFAULT_ELT_NS );
            return (SaxonXBeansDelegate.SelectPathInterface)
                    _constructor.newInstance(new Object[] {xpath,contextVar,namespaceMap,(String)defaultNS});
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

   static QueryInterface createQueryInstance(String query, String contextVar, int boundary)
   {
       if (_saxonAvailable && _xqConstructor == null)
           init();

        if (_xqConstructor == null)
            return null;

        try
        {
            return (SaxonXBeansDelegate.QueryInterface)
                    _xqConstructor.newInstance(new Object[] {query,contextVar,new Integer(boundary)});
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static interface SelectPathInterface
    {
        public List selectPath(Object node);
    }

     public static interface QueryInterface
    {
        public List execQuery(Object node, Map variableBindings);
    }
}
