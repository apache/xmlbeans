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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A binding of a simple user-defined type that operates by
 * delegating to another well-known (e.g., builtin) binding.
 */
public class SoapArrayType extends BindingType
{

    // ========================================================================
    // Variables

    private QName itemName;
    private BindingTypeName itemType;
    private boolean itemNillable;
    private int[] ranks;

    // ========================================================================
    // Constructors

    public SoapArrayType(BindingTypeName btName)
    {
        super(btName);
    }

    public SoapArrayType(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        this((org.apache.xml.xmlbeans.bindingConfig.SoapArray)node);
    }

    public SoapArrayType(org.apache.xml.xmlbeans.bindingConfig.SoapArray node)
    {
        super(node);
        this.itemName = node.getItemName();

        final org.apache.xml.xmlbeans.bindingConfig.Mapping itype =
            node.getItemType();
        final JavaTypeName jName = JavaTypeName.forString(itype.getJavatype());
        final XmlTypeName xName = XmlTypeName.forString(itype.getXmlcomponent());
        this.itemType = BindingTypeName.forPair(jName, xName);

        itemNillable = node.getItemNillable();


        if (node.isSetRanks()) {
            final java.util.List ranks_list = node.getRanks();
            if (!ranks_list.isEmpty()) {
                final int len = ranks_list.size();
                int[] new_ranks = new int[len];
                for (int i = 0; i < len; i++) {
                    final int r = ((Integer)ranks_list.get(i)).intValue();
                    if (r < 0) {
                        String msg = "illegal negative array rank: " + ranks_list;
                        throw new IllegalArgumentException(msg);
                    }
                    new_ranks[i] = r;
                }
                ranks = new_ranks;
            }
        }
    }


    protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        final org.apache.xml.xmlbeans.bindingConfig.SoapArray wa =
            (org.apache.xml.xmlbeans.bindingConfig.SoapArray)super.write(node);

        wa.setItemName(itemName);

        final org.apache.xml.xmlbeans.bindingConfig.Mapping mapping =
            wa.addNewItemType();
        mapping.setJavatype(itemType.getJavaName().toString());
        mapping.setXmlcomponent(itemType.getXmlName().toString());

        wa.setItemNillable(itemNillable);

        if (ranks != null) {
            List rl = new ArrayList(ranks.length);
            for (int i = 0, alen = ranks.length; i < alen; i++) {
                final int rank = ranks[i];
                if (rank < 0) {
                    throw new IllegalStateException("negative rank at index " +
                                                    i + ": " + rank);
                }
                rl.add(new Integer(rank));
            }
            wa.setRanks(rl);
        }

        return wa;
    }

    public void accept(BindingTypeVisitor visitor) throws XmlException
    {
        visitor.visit(this);
    }


    // ========================================================================
    // Public methods
    public QName getItemName()
    {
        return itemName;
    }

    public void setItemName(QName itemName)
    {
        this.itemName = itemName;
    }

    public BindingTypeName getItemType()
    {
        return itemType;
    }

    public void setItemType(BindingTypeName itemType)
    {
        this.itemType = itemType;
    }

    public boolean isItemNillable()
    {
        return itemNillable;
    }

    public void setItemNillable(boolean nillable)
    {
        this.itemNillable = nillable;
    }

    public int[] getRanks()
    {
        return ranks;
    }

    public void setRanks(int[] ranks)
    {
        this.ranks = ranks;
    }

}
