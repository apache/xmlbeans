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

package org.apache.xmlbeans.impl.marshal.util;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.ChangeStamp;
import org.apache.xmlbeans.impl.newstore2.Public2;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class AttrCache
{
    public AttrCache(Node n, QName attrName)
    {
        QName[] names = new QName[1];
        names[0] = attrName;

        init(n, names);
    }

    public AttrCache(Node n, QName[] attrNames)
    {
        init(n, attrNames);
    }

    public Node lookup(String key)
    {
        ensureCache();

        return (Node)_map.get(key);
    }

    private void init(Node n, QName[] attrNames)
    {
        _node = n;
        _map = new HashMap();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < attrNames.length; i++) {
            if (attrNames[i].getNamespaceURI().length() > 0) {
                sb.append("declare namespace ns");
                sb.append(i);
                sb.append("='");
                sb.append(attrNames[i].getNamespaceURI());
                sb.append("' ");
            }
        }

        for (int i = 0; i < attrNames.length; i++) {
            if (i > 0)
                sb.append("|");

            sb.append(".//@");

            if (attrNames[i].getNamespaceURI().length() > 0) {
                sb.append("ns");
                sb.append(i);
                sb.append(":");
            }

            sb.append(attrNames[i].getLocalPart());
        }

        _path = Public2.compilePath(sb.toString(), null);
    }

    private void ensureCache()
    {
        if (_stamp != null && !_stamp.hasChanged())
            return;

        XmlCursor c = Public2.getCursor(_node);

        _stamp = c.getDocChangeStamp();

        _map.clear();

        c.selectPath(_path);

        while (c.toNextSelection()) {
            final String attr_val = c.getTextValue();

            if (c.toParent()) {
                _map.put(attr_val, c.getDomNode());
            } else {
                //not sure why this will ever happen.
                assert false : " failed to move to parent: " + c;
            }
        }

        c.dispose();
    }

    private Node _node;
    private String _path;
    private ChangeStamp _stamp;
    private HashMap _map;
}
