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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.QNameSet;

import java.math.BigInteger;

import java.util.*;

import org.w3.x2001.xmlSchema.TopLevelElement;
import org.w3.x2001.xmlSchema.TopLevelAttribute;
import org.w3.x2001.xmlSchema.SimpleType;
import org.w3.x2001.xmlSchema.Attribute;
import org.w3.x2001.xmlSchema.Element;
import org.w3.x2001.xmlSchema.KeyrefDocument.Keyref;
import org.apache.xmlbeans.impl.common.XmlErrorContext;
import javax.xml.namespace.QName;

public class StscResolver
{
    /**
     * Does a topo walk of all the types to resolve them.
     */
    public static void resolveAll()
    {
        // resolve tree of types
        StscState state = StscState.get();

        SchemaType[] documentTypes = state.documentTypes();
        for (int i = 0 ; i < documentTypes.length ; i++)
            resolveSubstitutionGroup((SchemaTypeImpl)documentTypes[i]);

        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(state.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(state.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(state.redefinedGlobalTypes()));
        allSeenTypes.addAll(Arrays.asList(state.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            resolveType((SchemaTypeImpl)gType);
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }

        // Resolve all keyref refs
        resolveIdentityConstraints();
    }

    public static boolean resolveType(SchemaTypeImpl sImpl)
    {
        if (sImpl.isResolved())
            return true;
        if (sImpl.isResolving())
        {
            StscState.get().error("Cyclic dependency error", XmlErrorContext.CYCLIC_DEPENDENCY, sImpl.getParseObject());
            return false; // cyclic dependency error
        }
        // System.out.println("Resolving " + sImpl);

        sImpl.startResolving();

        if (sImpl.isDocumentType())
            resolveDocumentType(sImpl);
        else if (sImpl.isAttributeType())
            resolveAttributeType(sImpl);
        else if (sImpl.isSimpleType())
            StscSimpleTypeResolver.resolveSimpleType(sImpl);
        else
            StscComplexTypeResolver.resolveComplexType(sImpl);

        sImpl.finishResolving();
        // System.out.println("Finished resolving " + sImpl);
        return true;
    }

    public static boolean resolveSubstitutionGroup(SchemaTypeImpl sImpl)
    {
        assert sImpl.isDocumentType();

        if (sImpl.isSGResolved())
            return true;
        if (sImpl.isSGResolving())
        {
            StscState.get().error("Cyclic dependency error", XmlErrorContext.CYCLIC_DEPENDENCY, sImpl.getParseObject());
            return false; // cyclic dependency error
        }

        sImpl.startResolvingSGs();

        // Resolve substitution group

        TopLevelElement elt = (TopLevelElement)sImpl.getParseObject();
        SchemaTypeImpl substitutionGroup = null;
        QName eltName = new QName(sImpl.getTargetNamespace(), elt.getName());

        // BUG: How do I tell if the type is in this compilation unit?
        if (elt.isSetSubstitutionGroup())
        {
            substitutionGroup = StscState.get().findDocumentType(elt.getSubstitutionGroup(), 
                sImpl.getChameleonNamespace());

            if (substitutionGroup == null)
                StscState.get().notFoundError(elt.getSubstitutionGroup(), XmlErrorContext.ELEMENT_REF_NOT_FOUND, elt.xgetSubstitutionGroup());
                // recovery - ignore substitution group
            else if (! resolveSubstitutionGroup(substitutionGroup) )
                substitutionGroup = null;
            else
                sImpl.setSubstitutionGroup(elt.getSubstitutionGroup());
        }

        // Walk up the chain of subtitution groups adding this schematype to each head's
        // member list
        while (substitutionGroup != null)
        {

            substitutionGroup.addSubstitutionGroupMember(eltName);

            if (substitutionGroup.getSubstitutionGroup() == null)
                break;

            substitutionGroup = StscState.get().findDocumentType(
                substitutionGroup.getSubstitutionGroup(), substitutionGroup.getChameleonNamespace());

            assert substitutionGroup != null : "Could not find document type for: " + substitutionGroup.getSubstitutionGroup();

            if (! resolveSubstitutionGroup(substitutionGroup) )
                substitutionGroup = null; // cyclic dependency - no subst group

        }

        sImpl.finishResolvingSGs();
        return true;

    }

    public static void resolveDocumentType ( SchemaTypeImpl sImpl )
    {
        assert sImpl.isResolving();
        
        assert sImpl.isDocumentType();
        

        // translate the global element associated with this document type
        // and construct a content model which allows just that element
        
        List anonTypes = new ArrayList();

        SchemaGlobalElementImpl element =
            (SchemaGlobalElementImpl)
                StscTranslator.translateElement(
                    (Element) sImpl.getParseObject(),
                    sImpl.getTargetNamespace(), sImpl.isChameleon(),
                    anonTypes, sImpl );

        SchemaLocalElementImpl contentModel = null;

        if (element != null)
        {
            StscState.get().addGlobalElement( element );
                    
            contentModel = new SchemaLocalElementImpl();
        
            contentModel.setParticleType( SchemaParticle.ELEMENT );
            StscTranslator.copyGlobalElementToLocalElement( element, contentModel );
            contentModel.setMinOccurs( BigInteger.ONE );
            contentModel.setMaxOccurs( BigInteger.ONE );

            contentModel.setTransitionNotes(QNameSet.EMPTY, true);
        }

        Map elementPropertyModel =
            StscComplexTypeResolver.buildContentPropertyModelByQName(
                contentModel, sImpl );

        SchemaTypeImpl baseType = sImpl.getSubstitutionGroup() == null ?
            BuiltinSchemaTypeSystem.ST_ANY_TYPE :
            StscState.get().findDocumentType(sImpl.getSubstitutionGroup(), 
                 sImpl.isChameleon() ? sImpl.getTargetNamespace() : null)
            ;

        sImpl.setBaseTypeRef( baseType.getRef() );
        sImpl.setBaseDepth( baseType.getBaseDepth() + 1 );
        sImpl.setDerivationType( SchemaType.DT_RESTRICTION );
        sImpl.setComplexTypeVariety( SchemaType.ELEMENT_CONTENT );

        sImpl.setContentModel(
            contentModel, new SchemaAttributeModelImpl(),
            elementPropertyModel, Collections.EMPTY_MAP, false );
        
        sImpl.setWildcardSummary(
            QNameSet.EMPTY, false, QNameSet.EMPTY, false );

        sImpl.setAnonymousTypeRefs( makeRefArray( anonTypes ) );



    }
    
    public static void resolveAttributeType ( SchemaTypeImpl sImpl )
    {
        assert sImpl.isResolving();

        assert sImpl.isAttributeType();
        
        List anonTypes = new ArrayList();

        SchemaGlobalAttributeImpl attribute =
            (SchemaGlobalAttributeImpl) StscTranslator.translateAttribute(
                (Attribute) sImpl.getParseObject(), sImpl.getTargetNamespace(),
                sImpl.isChameleon(), anonTypes, sImpl, null, false );

        SchemaAttributeModelImpl attributeModel = new SchemaAttributeModelImpl();

        if (attribute != null)
        {
            StscState.get().addGlobalAttribute( attribute );
            
            SchemaLocalAttributeImpl attributeCopy = new SchemaLocalAttributeImpl();
            StscTranslator.copyGlobalAttributeToLocalAttribute( attribute, attributeCopy );
            attributeModel.addAttribute( attributeCopy );
        }

        sImpl.setBaseTypeRef( BuiltinSchemaTypeSystem.ST_ANY_TYPE.getRef() );
        sImpl.setBaseDepth( sImpl.getBaseDepth() + 1 );
        sImpl.setDerivationType( SchemaType.DT_RESTRICTION );
        sImpl.setComplexTypeVariety( SchemaType.EMPTY_CONTENT );
        
        Map attributePropertyModel =
            StscComplexTypeResolver.buildAttributePropertyModelByQName(
                attributeModel, sImpl );

        sImpl.setContentModel(
            null, attributeModel, Collections.EMPTY_MAP, attributePropertyModel, false );

        sImpl.setWildcardSummary(
            QNameSet.EMPTY, false, QNameSet.EMPTY, false );
        
        sImpl.setAnonymousTypeRefs( makeRefArray( anonTypes ) );
    }
    
    private static SchemaType.Ref[] makeRefArray(Collection typeList)
    {
        SchemaType.Ref[] result = new SchemaType.Ref[typeList.size()];
        int j = 0;
        for (Iterator i = typeList.iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType)i.next()).getRef();
        return result;
    }


    public static void resolveIdentityConstraints()
    {
        StscState state = StscState.get();
        SchemaIdentityConstraintImpl[] idcs = state.idConstraints();

        for (int i = 0 ; i < idcs.length ; i++)
        {
            if (!idcs[i].isResolved())
            {
                Keyref xsdkr = (Keyref)idcs[i].getParseObject();
                QName keyName = xsdkr.getRefer();
                SchemaIdentityConstraintImpl key = null;

                key = state.findIdConstraint(keyName, idcs[i].getChameleonNamespace());
                if (key == null)
                {
                    state.notFoundError(keyName, XmlErrorContext.IDC_NOT_FOUND, xsdkr);
                }
                else 
                {
                    if (key.getConstraintCategory() == SchemaIdentityConstraintImpl.CC_KEYREF)
                        state.error("Keyref cannot refer to another keyref.", 
                            XmlErrorContext.IDC_NOT_FOUND, idcs[i].getParseObject());

                    if (key.getFields().length != idcs[i].getFields().length)
                        state.error("Keyref does not have same number of fields as key",
                            XmlErrorContext.IDC_NOT_FOUND, idcs[i].getParseObject());

                    idcs[i].setReferencedKey(key.getRef());
                }
            }
        }
    }

}
