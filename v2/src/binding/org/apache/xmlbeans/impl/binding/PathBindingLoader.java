/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 2, 2003
 */
package org.apache.xmlbeans.impl.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.Collection;

public class PathBindingLoader extends BindingLoader
{
    private final Collection loaderPath;
    public static final PathBindingLoader EMPTY_LOADER = new PathBindingLoader(Collections.EMPTY_LIST);
    
    public static BindingLoader forPath(BindingLoader[] pathArray)
    {
        IdentityHashMap seen = new IdentityHashMap();
        
        List path = new ArrayList(pathArray.length);
        for (int i = 0; i < pathArray.length; i++)
            addToPath(path, seen, pathArray[i]);
        
        if (path.size() == 0)
            return EMPTY_LOADER;
        
        if (path.size() == 1)
            return (BindingLoader)path.get(0);
        
        return new PathBindingLoader(path);
    }
    
    private static void addToPath(List path, IdentityHashMap seen, BindingLoader loader)
    {
        if (seen.containsKey(loader))
            return;
        
        if (loader instanceof PathBindingLoader)
            for (Iterator j = ((PathBindingLoader)path).loaderPath.iterator(); j.hasNext(); )
                addToPath(path, seen, (BindingLoader)j.next());
        else
            path.add(loader);
    }
    
    private PathBindingLoader(List path)
    {
        loaderPath = Collections.unmodifiableList(path);
    }
    
    public BindingType getBindingType(JavaName jName, XmlName xName)
    {
        BindingType result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).getBindingType(jName, xName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingType getBindingTypeForXmlPojo(XmlName xName)
    {
        BindingType result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).getBindingTypeForXmlPojo(xName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingType getBindingTypeForXmlObj(XmlName xName)
    {
        BindingType result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).getBindingTypeForXmlObj(xName);
            if (result != null)
                return result;
        }
        return null;
    }

    public BindingType getBindingTypeForJava(JavaName jName)
    {
        BindingType result = null;
        for (Iterator i = loaderPath.iterator(); i.hasNext(); )
        {
            result = ((BindingLoader)i.next()).getBindingTypeForJava(jName);
            if (result != null)
                return result;
        }
        return null;
    }
}
