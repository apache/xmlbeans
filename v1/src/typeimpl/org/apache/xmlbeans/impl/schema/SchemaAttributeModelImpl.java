/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.SchemaLocalAttribute;
import javax.xml.namespace.QName;

import java.util.Map;
import java.util.LinkedHashMap;

public class SchemaAttributeModelImpl implements SchemaAttributeModel
{
    private Map attrMap;
    private QNameSet wcSet;
    private int wcProcess;

    public SchemaAttributeModelImpl()
    {
        attrMap = new LinkedHashMap();
        wcSet = null;
        wcProcess = NONE;
    }

    public SchemaAttributeModelImpl(SchemaAttributeModel sam)
    {
        attrMap = new LinkedHashMap();
        if (sam == null)
        {
            wcSet = null;
            wcProcess = NONE;
        }
        else
        {
            SchemaLocalAttribute[] attrs = sam.getAttributes();
            for (int i = 0; i < attrs.length; i++)
            {
                attrMap.put(attrs[i].getName(), attrs[i]);
            }

            if (sam.getWildcardProcess() != SchemaAttributeModel.NONE)
            {
                wcSet = sam.getWildcardSet();
                wcProcess = sam.getWildcardProcess();
            }
        }
    }
    
    private static final SchemaLocalAttribute[] EMPTY_SLA_ARRAY = new SchemaLocalAttribute[0];

    public SchemaLocalAttribute[] getAttributes()
    {
        return (SchemaLocalAttribute[])attrMap.values().toArray(EMPTY_SLA_ARRAY);
    }

    public SchemaLocalAttribute getAttribute(QName name)
    {
        return (SchemaLocalAttribute)attrMap.get(name);
    }

    public void addAttribute(SchemaLocalAttribute attruse)
    {
        attrMap.put(attruse.getName(), attruse);
    }
    
    public void removeProhibitedAttribute(QName name)
    {
        attrMap.remove(name);
    }

    public QNameSet getWildcardSet()
    {
        return wcSet == null ? QNameSet.EMPTY : wcSet;
    }

    public void setWildcardSet(QNameSet set)
    {
        wcSet = set;
    }

    public int getWildcardProcess()
    {
        return wcProcess;
    }

    public void setWildcardProcess(int proc)
    {
        wcProcess = proc;
    }
}
