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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.XmlID;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.impl.common.XmlErrorContext;
import org.apache.xmlbeans.impl.common.XBeanDebug;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.math.BigInteger;

public class StscChecker
{
    public static void checkAll()
    {
        // walk the tree of types
        StscState state = StscState.get();

        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(state.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(state.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(state.redefinedGlobalTypes()));
        allSeenTypes.addAll(Arrays.asList(state.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            if (!state.noPvr() &&  // option to turn off particle restriction checking
                !gType.isDocumentType()) // Don't check doc types for restriction. 
            {
                checkRestriction((SchemaTypeImpl)gType);
            }
            checkFields((SchemaTypeImpl)gType);
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }

        checkSubstitutionGroups(state.globalElements());
    }
    
    /**
     * The following code checks rule #5 of http://www.w3.org/TR/xmlschema-1/#coss-ct
     * as well as attribute + element default/fixed validity.
     */
    public static void checkFields(SchemaTypeImpl sType)
    {
        if (sType.isSimpleType())
            return;
        
        XmlObject location = sType.getParseObject();
        
        SchemaAttributeModel sAttrModel = sType.getAttributeModel();
        if (sAttrModel != null)
        {
            SchemaLocalAttribute[] sAttrs  = sAttrModel.getAttributes();
            QName idAttr = null;
            for (int i = 0; i < sAttrs.length; i++)
            {
                if (XmlID.type.isAssignableFrom(sAttrs[i].getType()))
                {
                    if (idAttr == null)
                        idAttr = sAttrs[i].getName();
                    else
                        StscState.get().error("Both " + QNameHelper.pretty(idAttr) + " and " + sAttrs[i].getName() + " are xs:ID attributes; only one ID attribute is allowed on a type.", XmlErrorContext.GENERIC_ERROR, location);
                    if (sAttrs[i].getDefaultText() != null)
                        StscState.get().error("An attribute of type xs:ID is not allowed to have a default or fixed constraint.", XmlErrorContext.GENERIC_ERROR, location);
                }
                else
                {
                    String valueConstraint = sAttrs[i].getDefaultText();
                    if (valueConstraint != null)
                    {
                        try
                        {
                            XmlAnySimpleType val = sAttrs[i].getDefaultValue();
                            if (!val.validate())
                                throw new Exception();
                            
                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)sType.getAttributeProperty(sAttrs[i].getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(val));
                            }
                        }
                        catch (Exception e)
                        {
                            if (sAttrs[i].isFixed())
                                StscState.get().error("The " + QNameHelper.pretty(sAttrs[i].getName()) + " element fixed value '" + valueConstraint + "' is not a valid value for " + QNameHelper.readable(sAttrs[i].getType()), XmlErrorContext.GENERIC_ERROR, location);
                            else
                                StscState.get().error("The " + QNameHelper.pretty(sAttrs[i].getName()) + " element default value '" + valueConstraint + "' is not a valid value for " + QNameHelper.readable(sAttrs[i].getType()), XmlErrorContext.GENERIC_ERROR, location);
                        }
                    }
                }
            }
        }
        
        checkElementDefaults(sType.getContentModel(), location, sType);
    }
    
    private static void checkElementDefaults(SchemaParticle model, XmlObject location, SchemaType parentType)
    {
        if (model == null)
            return;
        switch (model.getParticleType())
        {
            case SchemaParticle.SEQUENCE:
            case SchemaParticle.CHOICE:
            case SchemaParticle.ALL:
                SchemaParticle[] children = model.getParticleChildren();
                for (int i = 0; i < children.length; i++)
                {
                    checkElementDefaults(children[i], location, parentType);
                }
                break;
           case SchemaParticle.ELEMENT:
                String valueConstraint = model.getDefaultText();
                if (valueConstraint != null)
                {
                    if (model.getType().isSimpleType() || model.getType().getContentType() == SchemaType.SIMPLE_CONTENT)
                    {
                        try
                        {
                            XmlAnySimpleType val = model.getDefaultValue();
                            if (!val.validate())
                                throw new Exception();
                            
                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)parentType.getElementProperty(model.getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(val));
                            }
                        }
                        catch (Exception e)
                        {
                            if (model.isFixed())
                                StscState.get().error("The " + QNameHelper.pretty(model.getName()) + " element fixed value '" + valueConstraint + "' is not a valid value for " + QNameHelper.readable(model.getType()), XmlErrorContext.GENERIC_ERROR, location);
                            else
                                StscState.get().error("The " + QNameHelper.pretty(model.getName()) + " element default value '" + valueConstraint + "' is not a valid value for " + QNameHelper.readable(model.getType()), XmlErrorContext.GENERIC_ERROR, location);
                        }
                    }
                    else if (model.getType().getContentType() == SchemaType.MIXED_CONTENT)
                    {
                        if (!model.getType().getContentModel().isSkippable())
                        {
                            String constraintName = (model.isFixed() ? "fixed" : "default");

                            StscState.get().error("The " + QNameHelper.pretty(model.getName()) + " element cannot have a " +
                                    constraintName + " value '" + valueConstraint + "' because it's content is mixed " +
                                    "but not emptiable.", XmlErrorContext.GENERIC_ERROR, location);
                        }
                        else
                        {
                            // Element Default Valid (Immediate): cos-valid-default.2.2.2
                            // no need to validate the value; type is a xs:string
                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)parentType.getElementProperty(model.getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(XmlString.type.newValue(valueConstraint)));
                            }
                        }
                    }
                    else if (model.getType().getContentType() == SchemaType.ELEMENT_CONTENT)
                    {
                        StscState.get().error("The " + QNameHelper.pretty(model.getName()) + " element cannot have a default value '" + valueConstraint + "' because its type has element content only.", XmlErrorContext.GENERIC_ERROR, location);
                    }
                    else if (model.getType().getContentType() == SchemaType.EMPTY_CONTENT)
                    {
                        StscState.get().error("The " + QNameHelper.pretty(model.getName()) + " element cannot have a default value '" + valueConstraint + "' because its type has empty content only.", XmlErrorContext.GENERIC_ERROR, location);
                    }
                }
                break;
                
           default:
                // nothing to do.
                break;
        }
    }
    
    /**
     * The following code only checks rule #5 of http://www.w3.org/TR/xmlschema-1/#derivation-ok-restriction
     *  (Everything else can and should be done in StscResolver, because we can give more detailed line # info there
     */
    public static boolean checkRestriction(SchemaTypeImpl sType)
    {
        if (sType.getDerivationType() == SchemaType.DT_RESTRICTION && !sType.isSimpleType())
        {
            StscState state = StscState.get();
            
            // we don't remember very precise line number information, but it's better than nothin.
            XmlObject location = sType.getParseObject();
        
            SchemaType baseType = sType.getBaseType();
            if (baseType.isSimpleType())
            {
                state.error("The base type of a complex type restriction must be a complex type.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                return false;
            }
            
            // 5 The appropriate case among the following must be true:
            switch (sType.getContentType())
            {
                case SchemaType.SIMPLE_CONTENT:
                    // 5.1 If the {content type} of the complex type definition is a simple type definition, then one of the following must be true:
                    switch (baseType.getContentType())
                    {
                        case SchemaType.SIMPLE_CONTENT:
                            // 5.1.1 The {content type} of the {base type definition} must be a simple type definition of which the {content type} is a ·valid restriction· as defined in Derivation Valid (Restriction, Simple) (§3.14.6).
                            // todo: we don't allow the content type to be an "on the side" simple type, so nothing fancy to check here
                            break;
                            
                        case SchemaType.MIXED_CONTENT:
                            // 5.1.2 The {base type definition} must be mixed and have a particle which is ·emptiable· as defined in Particle Emptiable (§3.9.6).
                            if (baseType.getContentModel() != null && !baseType.getContentModel().isSkippable())
                            {
                                state.error("A type with a simple content model can only restrict a mixed content model that has skippable elements.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                                return false;
                            }
                            break;
                            
                        default:
                            state.error("A type with a simple content model can only restrict a simple or mixed content model.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                            return false;
                    }
                    break;
                    
                case SchemaType.EMPTY_CONTENT:
                    // 5.2 If the {content type} of the complex type itself is empty , then one of the following must be true:
                    switch (baseType.getContentType())
                    {
                        case SchemaType.EMPTY_CONTENT:
                            // 5.2.1 The {content type} of the {base type definition} must also be empty.
                            break;
                        case SchemaType.MIXED_CONTENT:
                        case SchemaType.ELEMENT_CONTENT:
                            // 5.2.2 The {content type} of the {base type definition} must be elementOnly or mixed and have a particle which is ·emptiable· as defined in Particle Emptiable (§3.9.6).
                            if (baseType.getContentModel() != null && !baseType.getContentModel().isSkippable())
                            {
                                state.error("A type with an empty content model can only restrict a content model that has skippable elements.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                                return false;
                            }
                            break;
                        default:                            
                            state.error("A type with an empty content model cannot restrict a type with a simple content model.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                            return false;
                    }
                    break;
                    
                case SchemaType.MIXED_CONTENT:
                    // 5.3 If the {content type} of the {base type definition} is mixed...
                    if (baseType.getContentType() != SchemaType.MIXED_CONTENT)
                    {
                        state.error("A type with a mixed content model can only restrict another type with a mixed content model.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                        return false;
                    }
                    
                    // FALLTHROUGH
                case SchemaType.ELEMENT_CONTENT:
                    // 5.3 ... or the {content type} of the complex type definition itself is element-only,...
                    if (baseType.getContentType() == SchemaType.EMPTY_CONTENT)
                    {
                        state.error("A type with element or mixed content cannot restrict an empty type.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                        return false;
                    }
                    if (baseType.getContentType() == SchemaType.SIMPLE_CONTENT)
                    {
                        state.error("A type with element or mixed content cannot restrict a simple type.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                        return false;
                    }
                    
                    // 5.3 ... then the particle of the complex type definition itself must be a ·valid restriction· of the particle of the {content type} of the {base type definition}
                    SchemaParticle baseModel = baseType.getContentModel();
                    SchemaParticle derivedModel = sType.getContentModel();
                    assert(baseModel != null && derivedModel != null);
                    if (baseModel == null || derivedModel == null)
                    {
                        XBeanDebug.logStackTrace("Null models that weren't caught by EMPTY_CONTENT: " + baseType + " (" + baseModel + "), " + sType + " (" + derivedModel + ")");
                        state.error("Illegal restriction.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                        return false;
                    }
                    
                    // 5.3 ...  as defined in Particle Valid (Restriction) (§3.9.6).
                    List errors = new ArrayList();
                    boolean isValid = isParticleValidRestriction(baseModel, derivedModel, errors, location);
                    if (!isValid)
                    {
                        // we only add the last error, because isParticleValidRestriction may add errors
                        // to the collection that it later changes its mind about, or it may (inadvertently)
                        // forget to describe an error into the collection....
                        if (errors.size() == 0)
                            state.error("Invalid restriction.", XmlErrorContext.ILLEGAL_RESTRICTION, location);
                        else
                            state.getErrorListener().add(errors.get(errors.size() - 1));
                    }
            }
        }
        return true;
    }
    
    /**
     * This function takes in two schema particle types, a baseModel, and a derived model and returns true if the
     * derivedModel can be egitimately be used for restriction.  Errors are put into the errors collections.
     * @param baseModel - The base schema particle
     * @param derivedModel - The derived (restricted) schema particle
     * @param errors - Invalid restriction errors are put into this collection
     * @param context
     * @return boolean, true if valid restruction, false if invalid restriction
     * @
     */
    public static boolean isParticleValidRestriction(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        boolean restrictionValid = false;
        // 1 They are the same particle.
        if (baseModel.equals(derivedModel)) {
            restrictionValid = true;
        } else {
            // Implement table defined in schema spec on restrictions at:
            //   http://www.w3.org/TR/xmlschema-1/#cos-particle-restrict
            switch (baseModel.getParticleType()) {
                case SchemaParticle.ELEMENT:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = nameAndTypeOK((SchemaLocalElement) baseModel, (SchemaLocalElement) derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.ALL:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.CHOICE:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.SEQUENCE:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.WILDCARD:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = nsCompat(baseModel, (SchemaLocalElement) derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            restrictionValid = nsSubset(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.ALL:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.CHOICE:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.ALL:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.ALL:
                            restrictionValid = recurse(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.CHOICE:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = recurseUnordered(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.CHOICE:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.ALL:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.CHOICE:
                            restrictionValid = recurseLax(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = mapAndSum(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.SEQUENCE:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.ALL:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.CHOICE:
                            errors.add(XmlError.forObject(formatInvalidCombinationError(baseModel, derivedModel), context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = recurse(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                default:
                    assert false : XBeanDebug.logStackTrace("Unknown schema type for Base Type");

            }
        }

        return restrictionValid;
    }

    private static boolean mapAndSum(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        // mapAndSum is call if base: CHOICE, derived: SEQUENCE
        assert baseModel.getParticleType() == SchemaParticle.CHOICE;
        assert derivedModel.getParticleType() == SchemaParticle.SEQUENCE;
        boolean mapAndSumValid = true;
        // Schema Component Constraint: Particle Derivation OK (Sequence:Choice -- MapAndSum)
        // For a sequence group particle to be a ·valid restriction· of a choice group particle all of the following
        // must be true:
        // 1 There is a complete functional mapping from the particles in the {particles} of R to the particles in the
        // {particles} of B such that each particle in the {particles} of R is a ·valid restriction· of the particle in
        // the {particles} of B it maps to as defined by Particle Valid (Restriction) (§3.9.6).
        // interpretation:  each particle child in derived should have a match in base.
        // 2 The pair consisting of the product of the {min occurs} of R and the length of its {particles} and unbounded
        // if {max occurs} is unbounded otherwise the product of the {max occurs} of R and the length of its {particles}
        // is a valid restriction of B's occurrence range as defined by Occurrence Range OK (§3.9.6).
        // NOTE: This clause is in principle more restrictive than absolutely necessary, but in practice will cover
        // all the likely cases, and is much easier to specify than the fully general version.
        // NOTE: This case allows the "unfolding" of iterated disjunctions into sequences. It may be particularly useful
        // when the disjunction is an implicit one arising from the use of substitution groups.

        // Map step - for each member of the derived model's particle children search base model's particle children
        //  for match
        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        for (int i = 0; i < derivedParticleArray.length; i++) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            boolean foundMatch = false;
            for (int j = 0; j < baseParticleArray.length; j++) {
                SchemaParticle baseParticle = baseParticleArray[j];
                // recurse to check if there is a match
                if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                    // if there is a match then no need to check base particles anymore
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                mapAndSumValid = false;
                errors.add(XmlError.forObject(formatMappingError(), context));
                break;
            }
        }

        // Sum step
        BigInteger derivedRangeMin = derivedModel.getMinOccurs().multiply(BigInteger.valueOf(derivedModel.getParticleChildren().length));
        BigInteger derivedRangeMax = null;
        BigInteger UNBOUNDED = null;
        if (derivedModel.getMaxOccurs() == UNBOUNDED) {
            derivedRangeMax = null;
        } else {
            derivedRangeMax = derivedModel.getMaxOccurs().multiply(BigInteger.valueOf(derivedModel.getParticleChildren().length));
        }

        // Now check derivedRange (derivedRangeMin and derivedRangeMax) against base model occurrence range
        // Schema Component Constraint: Occurrence Range OK
        // For a particle's occurrence range to be a valid restriction of another's occurrence range all of the following must be true:
        // 1 Its {min occurs} is greater than or equal to the other's {min occurs}.
        // 2 one of the following must be true:
        //   2.1 The other's {max occurs} is unbounded.
        //   2.2 Both {max occurs} are numbers, and the particle's is less than or equal to the other's.

        if (derivedRangeMin.compareTo(baseModel.getMinOccurs()) < 0) {
            mapAndSumValid = false;
            errors.add(XmlError.forObject(formatOccurenceRangeMinErrorChoiceSequence(derivedRangeMin, baseModel), context));
        } else if (baseModel.getMaxOccurs() == UNBOUNDED || derivedRangeMax != UNBOUNDED && derivedRangeMax.compareTo(baseModel.getMaxOccurs()) > 0) {
            mapAndSumValid = false;
            errors.add(XmlError.forObject(formatOccurenceRangeMaxErrorChoiceSequence(derivedRangeMax, baseModel), context));
        }

        return mapAndSumValid;
    }

    private static String formatOccurenceRangeMinErrorChoiceSequence(BigInteger derivedRangeMin, SchemaParticle baseModel) {
        return "Invalid Restriction.  The total minOccurs for the derived <sequence>'s elements: "
                + derivedRangeMin.toString()
                + "must not be less than the base <choice>'s minOccurs ("
                + baseModel.getMinOccurs().toString() + ")";
    }

    private static String formatOccurenceRangeMaxErrorChoiceSequence(BigInteger derivedRangeMax, SchemaParticle baseModel) {
        return "Invalid Restriction.  The total maxOccurs for the derived <sequence>'s elements ("
                + derivedRangeMax.toString()
                + ") must not be greater than the base <choice>'s maxOccurs ("
                + printMaxOccurs(baseModel.getMaxOccurs()) + ")";
       
    }

    private static String formatMappingError() {
        return "Invalid Restriction.  At least one restricted type does not match.";
    }

    private static boolean recurseAsIfGroup(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurseAsIfGroup is called if:
        // base: ALL, derived: ELEMENT
        // base: CHOICE, derived: ELEMENT
        // base: SEQUENCE, derived: ELEMENT
        assert (baseModel.getParticleType() == SchemaParticle.ALL && derivedModel.getParticleType() == SchemaParticle.ELEMENT)
                || (baseModel.getParticleType() == SchemaParticle.CHOICE && derivedModel.getParticleType() == SchemaParticle.ELEMENT)
                || (baseModel.getParticleType() == SchemaParticle.SEQUENCE && derivedModel.getParticleType() == SchemaParticle.ELEMENT);
        // Schema Component Constraint: Particle Derivation OK (Elt:All/Choice/Sequence -- RecurseAsIfGroup)

        // For an element declaration particle to be a ·valid restriction· of a group particle
        // (all, choice or sequence) a group particle of the variety corresponding to B's, with {min occurs} and
        // {max occurs} of 1 and with {particles} consisting of a single particle the same as the element declaration
        // must be a ·valid restriction· of the group as defined by Particle Derivation OK
        // (All:All,Sequence:Sequence -- Recurse) (§3.9.6), Particle Derivation OK (Choice:Choice -- RecurseLax)
        // (§3.9.6) or Particle Derivation OK (All:All,Sequence:Sequence -- Recurse) (§3.9.6), depending on whether
        // the group is all, choice or sequence

        // interpretation:  make a fake group of the right type, with min occurs and max occurs of 1
        SchemaParticleImpl asIfPart = new SchemaParticleImpl();
        asIfPart.setParticleType(baseModel.getParticleType());
        asIfPart.setMinOccurs(BigInteger.ONE);
        asIfPart.setMaxOccurs(BigInteger.ONE);
        asIfPart.setParticleChildren(new SchemaParticle[] { derivedModel });
        
        // the recurse
        return isParticleValidRestriction(baseModel, asIfPart, errors, context); 
    }

    private static boolean recurseLax(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        // recurseLax is called if base: CHOICE, derived: CHOICE
        assert baseModel.getParticleType() == SchemaParticle.CHOICE && derivedModel.getParticleType() == SchemaParticle.CHOICE;
        boolean recurseLaxValid = true;
        //Schema Component Constraint: Particle Derivation OK (Choice:Choice -- RecurseLax)
        // For a choice group particle to be a ·valid restriction· of another choice group particle all of the
        // following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence
        // Range OK (§3.9.6);
        // 2 There is a complete ·order-preserving· functional mapping from the particles in the {particles} of R
        // to the particles in the {particles} of B such that each particle in the {particles} of R is a
        // ·valid restriction· of the particle in the {particles} of B it maps to as defined by
        // Particle Valid (Restriction) (§3.9.6).
        // NOTE: Although the ·validation· semantics of a choice group does not depend on the order of its particles,
        // derived choice groups are required to match the order of their base in order to simplify
        // checking that the derivation is OK.
        // interpretation:  check derived choices for match in order, must get an in order match on a base particle,
        //                  don't need to check if base particles are skippable.  a lot like recurse

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            return false;
        }
        // cycle thru both derived particle children and base particle children looking for matches
        //  if the derived particle does not match the base particle then base particle can be skipped

        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        int i = 0, j = 0;
        for (; i < derivedParticleArray.length && j < baseParticleArray.length;) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            SchemaParticle baseParticle = baseParticleArray[j];
            // try to match the two particles by recursing
            if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                // cool found a match, increment both indexes
                i++;
                j++;
            } else {
                // did not match, increment the base particle array index only
                // Ok, let's skip this base particle, increment base particle array index only
                j++;
            }
        }

        // ok, got to the end of one of the arrays
        // if at end of base particle array and not at the end of derived particle array then remaining derived
        //  particles must not match
        if (i < derivedParticleArray.length) {
            recurseLaxValid = false;
            String message = "Found derived particles that are not matched in the base content model.";
            errors.add(XmlError.forObject(formatDerivedMappingError(message, baseModel, derivedModel), context));
        }


        return recurseLaxValid;
    }

    private static boolean recurseUnordered(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurseUnorder is called when base: ALL and derived: SEQ
        assert baseModel.getParticleType() == SchemaParticle.ALL && derivedModel.getParticleType() == SchemaParticle.SEQUENCE;
        boolean recurseUnorderedValid = true;
        // Schema Component Constraint: Particle Derivation OK (Sequence:All -- RecurseUnordered)
        // For a sequence group particle to be a ·valid restriction· of an all group particle all of the
        // following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by
        // Occurrence Range OK (§3.9.6).
        // 2 There is a complete functional mapping from the particles in the {particles} of R to the particles
        // in the {particles} of B such that all of the following must be true:
        // 2.1 No particle in the {particles} of B is mapped to by more than one of the particles in
        // the {particles} of R;
        // 2.2 Each particle in the {particles} of R is a ·valid restriction· of the particle in the {particles} of B
        // it maps to as defined by Particle Valid (Restriction) (§3.9.6);
        // 2.3 All particles in the {particles} of B which are not mapped to by any particle in the {particles}
        // of R are ·emptiable· as defined by Particle Emptiable (§3.9.6).
        // NOTE: Although this clause allows reordering, because of the limits on the contents of all groups the
        // checking process can still be deterministic.
        // 1, 2.2, and 2.3 are the same as recurse, so do 2.1 and then call recurse

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            return false;
        }

        // read baseParticle array QNames into hashmap
        SchemaParticle[] baseParticles = baseModel.getParticleChildren();
        HashMap baseParticleMap = new HashMap(10);
        Object MAPPED = new Object();
        // Initialize the hashmap
        for (int i = 0; i < baseParticles.length; i++)
            baseParticleMap.put(baseParticles[i].getName(), baseParticles[i]);
        
        // go thru the sequence (derived model's children) and check off from base particle map
        SchemaParticle[] derivedParticles = derivedModel.getParticleChildren();
        for (int i = 0; i < derivedParticles.length; i++) {
            SchemaParticle matchedBaseParticle = (SchemaParticle) baseParticleMap.get(derivedParticles[i].getName());
            if (matchedBaseParticle == null) {
                recurseUnorderedValid = false;
                errors.add(XmlError.forObject(formatInvalidAllSeqMappingError(), context));
                break;
            } else {
                // got a match
                if (matchedBaseParticle == MAPPED) {
                    // whoa, this base particle has already been matched (see 2.1 above)
                    recurseUnorderedValid = false;
                    errors.add(XmlError.forObject(formatMappedMoreThanOnceError(baseModel, derivedModel), context));
                    break;
                } else {
                    if (derivedParticles[i].getMaxOccurs() == null ||
                            derivedParticles[i].getMaxOccurs().compareTo(BigInteger.ONE) > 0) {
                        // no derived particles can have a max occurs greater than 1
                        recurseUnorderedValid = false;
                        errors.add(XmlError.forObject(formatAllSeqMaxOccursGreaterThan1Error(derivedParticles[i]), context));
                        break;
                    }
                    if (!isParticleValidRestriction(matchedBaseParticle, derivedParticles[i], errors, context))
                    {
                        // already have an error
                        recurseUnorderedValid = false;
                        break;
                    }
                    // everything is cool, got a match, update to MAPPED
                    baseParticleMap.put(derivedParticles[i].getName(), MAPPED);
                }
            }
        }

        // if everything is cool so far then check to see if any base particles are not matched
        if (recurseUnorderedValid) {
            // get all the hashmap keys and loop thru looking for NOT_MAPPED
            Set baseParticleCollection = baseParticleMap.keySet();
            for (Iterator iterator = baseParticleCollection.iterator(); iterator.hasNext();) {
                QName baseParticleQName = (QName) iterator.next();
                if (baseParticleMap.get(baseParticleQName) != MAPPED && !((SchemaParticle)baseParticleMap.get(baseParticleQName)).isSkippable()) {
                    // this base particle was not mapped and is not "particle emptiable" (skippable)
                    recurseUnorderedValid = false;
                    errors.add(XmlError.forObject(formatGroupMappingError(baseModel, derivedModel), context));
                }
            }
        }

        return recurseUnorderedValid;
    }

    private static String formatAllSeqMaxOccursGreaterThan1Error(SchemaParticle derivedModel) {
        return "Invalid Restriction.  The "
                + printParticle(derivedModel)
                + " has a maxOccurs great than 1 ("
                + printMaxOccurs(derivedModel.getMaxOccurs())
                + ").  When restricting an <all> with a <sequence>, no <element> can have a maxOccurs > 1";
    }

    private static String formatInvalidAllSeqMappingError() {
        return "Invalid Restriction.  Each element in the derived content model must match an element in the base "
                + "content model.";
    }

    private static String formatGroupMappingError(SchemaParticle baseModel, SchemaParticle derivedModel) {
        return "Invalid Restriction.  The members of the derived content model must match the members of the base "
                + " content model unless the member of the base content model is optional (particle emptiable).";
    }

    private static String formatMappedMoreThanOnceError(SchemaParticle baseModel, SchemaParticle derivedModel) {
        return "Invalid Restriction.  When using a <sequence> to restrict an <all> the elements in the <all> can be mapped only once. "
                + " Found element in the <sequence> that maps an element in the <all> more than once.";
    }

    private static boolean recurse(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurse is called when base: ALL derived: ALL or base: SEQUENCE derived: SEQUENCE
        boolean recurseValid = true;
        // For an all or sequence group particle to be a ·valid restriction· of another group particle with the same
        // {compositor} all of the following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by
        // Occurrence Range OK (§3.9.6).
        // 2 There is a complete ·order-preserving· functional mapping from the particles in the {particles} of R to
        // the particles in the {particles} of B such that all of the following must be true:
        //   2.1 Each particle in the {particles} of R is a ·valid restriction· of the particle in the {particles}
        //   of B it maps to as defined by Particle Valid (Restriction) (§3.9.6).
        //   2.2 All particles in the {particles} of B which are not mapped to by any particle in the {particles}
        //   of R are ·emptiable· as defined by Particle Emptiable (§3.9.6).
        // NOTE: Although the ·validation· semantics of an all group does not depend on the order of its particles,
        // derived all groups are required to match the order of their base in order to simplify checking that
        // the derivation is OK.
        // [Definition:]  A complete functional mapping is order-preserving if each particle r in the domain R maps
        // to a particle b in the range B which follows (not necessarily immediately) the particle in the range B
        // mapped to by the predecessor of r, if any, where "predecessor" and "follows" are defined with respect to
        // the order of the lists which constitute R and B.

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            // error message is formatted in occurrencRangeOK ...
            return false;
        }
        // cycle thru both derived particle children and base particle children looking for matches
        //  if the derived particle does not match the base particle then base particle can be skipped if it is
        //  skippable (same as "particle emptiable") otherwise is an invalid restriction.
        // after the derived particles have been cycled if there are any base particles left over then they
        //  must be skippable or invalid restriction

        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        int i = 0, j = 0;
        for (; i < derivedParticleArray.length && j < baseParticleArray.length;) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            SchemaParticle baseParticle = baseParticleArray[j];
            // try to match the two particles by recursing
            if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                // cool found a match, increment both indexes
                i++;
                j++;
            } else {
                // did not match, increment the base particle array index only
                //  that's ok if the base particle is skippable
                if (baseParticle.isSkippable()) {
                    // Ok, let's skip this base particle, increment base particle array index only
                    j++;
                } else {
                    // whoa, particles are not valid restrictions and base is not skippable - ERROR
                    recurseValid = false;
                    String message = "Found non-optional particle that is not mapped in base content model.";
                    errors.add(XmlError.forObject(formatDerivedMappingError(message, baseModel, derivedModel), context));
                    break;
                }
            }
        }

        // ok, got to the end of one of the arrays
        // if at end of base particle array and not at the end of derived particle array then remaining derived
        //  particles must not match
        if (i < derivedParticleArray.length) {
            recurseValid = false;
            String message = "Found derived particles that are not matched in the base content model.";
            errors.add(XmlError.forObject(formatDerivedMappingError(message, baseModel, derivedModel), context));
        } else {
            // if at end of derived particle array and not at end of base particle array then chck remaining
            //  base particles to assure they are skippable
            if (j < baseParticleArray.length) {
                for (int k = j; k < baseParticleArray.length; k++) {
                    if (!baseParticleArray[k].isSkippable()) {
                        recurseValid = false;
                        String message = "Found trailing base particles that are not optional and are not " +
                                "represented in the derived content model.";
                        errors.add(XmlError.forObject(formatDerivedMappingError(message, baseModel, derivedModel), context));
                    }

                }
            }
        }

        return recurseValid;
    }

    private static String formatDerivedMappingError(String message, SchemaParticle baseModel, SchemaParticle derivedModel) {
        return "Invalid Restriction.  The derived (restricted) content model must match the base content model unless "
                + "the part of the base content model that does not match is optional.  A mismatch found between a base " +
                printParticle(baseModel) + " and a derived " + printParticle(derivedModel) + ".  " + message;
    }

    private static boolean nsRecurseCheckCardinality(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // nsRecurseCheckCardinality is called when:
        // base: ANY, derived: ALL
        // base: ANY, derived: CHOICE
        // base: ANY, derived: SEQUENCE
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        assert (derivedModel.getParticleType() == SchemaParticle.ALL)
                || (derivedModel.getParticleType() == SchemaParticle.CHOICE)
                || (derivedModel.getParticleType() == SchemaParticle.SEQUENCE);
        boolean nsRecurseCheckCardinality = true;
        // For a group particle to be a ·valid restriction· of a wildcard particle all of the following must be true:
        // 1 Every member of the {particles} of the group is a ·valid restriction· of the wildcard as defined by Particle Valid (Restriction) (§3.9.6).
        //  Note:  not positive what this means.  Interpreting to mean that every particle of the group must adhere to wildcard derivation rules
        //         in a recursive manner
        //  Loop thru the children particles of the group and invoke the appropriate function to check for wildcard restriction validity
        
        // BAU - an errata should be submitted on this clause of the spec, because the
        // spec makes no sense, as the xstc particlesR013.xsd test exemplifies.
        // what we _should_ so is an "as if" on the wildcard, allowing it minOccurs="0" maxOccurs="unbounded"
        // before recursing
        SchemaParticleImpl asIfPart = new SchemaParticleImpl();
        asIfPart.setParticleType(baseModel.getParticleType());
        asIfPart.setWildcardProcess(baseModel.getWildcardProcess());
        asIfPart.setWildcardSet(baseModel.getWildcardSet());
        asIfPart.setMinOccurs(BigInteger.ZERO);
        asIfPart.setMaxOccurs(null);
        asIfPart.setTransitionRules(baseModel.getWildcardSet(), true);
        asIfPart.setTransitionNotes(baseModel.getWildcardSet(), true);

        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.ELEMENT:
                    // Check for valid Wildcard/Element derivation
                    nsRecurseCheckCardinality = nsCompat(asIfPart, (SchemaLocalElement) particle, errors, context);
                    break;
                case SchemaParticle.WILDCARD:
                    // Check for valid Wildcard/Wildcard derivation
                    nsRecurseCheckCardinality = nsSubset(asIfPart, particle, errors, context);
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.CHOICE:
                case SchemaParticle.SEQUENCE:
                    // Check for valid Wildcard/Group derivation
                    nsRecurseCheckCardinality = nsRecurseCheckCardinality(asIfPart, derivedModel, errors, context);
                    break;
            }
            // If any particle is invalid then break the loop
            if (!nsRecurseCheckCardinality) {
                break;
            }
        }

        // 2 The effective total range of the group, as defined by Effective Total Range (all and sequence) (§3.8.6)
        // (if the group is all or sequence) or Effective Total Range (choice) (§3.8.6) (if it is choice) is a valid
        // restriction of B's occurrence range as defined by Occurrence Range OK (§3.9.6).

        if (nsRecurseCheckCardinality) {
            nsRecurseCheckCardinality = checkGroupOccurrenceOK(baseModel, derivedModel, errors, context);
        }

        return nsRecurseCheckCardinality;
    }

    private static boolean checkGroupOccurrenceOK(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        boolean groupOccurrenceOK = true;
        BigInteger minRange = BigInteger.ZERO;
        BigInteger maxRange = BigInteger.ZERO;
        switch (derivedModel.getParticleType()) {
            case SchemaParticle.ALL:
            case SchemaParticle.SEQUENCE:
                minRange = getEffectiveMinRangeAllSeq(derivedModel);
                maxRange = getEffectiveMaxRangeAllSeq(derivedModel);
                break;
            case SchemaParticle.CHOICE:
                minRange = getEffectiveMinRangeChoice(derivedModel);
                maxRange = getEffectiveMaxRangeChoice(derivedModel);
                break;
        }

        // Check min occurs for validity
        // derived min occurs is valid if its {min occurs} is greater than or equal to the other's {min occurs}.
        if (minRange.compareTo(baseModel.getMinOccurs()) < 0) {
            groupOccurrenceOK = false;
            errors.add(XmlError.forObject(formatGroupMinOccursError(derivedModel, baseModel), context));
        }
        // Check max occurs for validity
        // one of the following must be true:
        // The base model's {max occurs} is unbounded.
        // or both {max occurs} are numbers, and the particle's is less than or equal to the other's
        BigInteger UNBOUNDED = null;
        if (baseModel.getMaxOccurs() != UNBOUNDED) {
            if (maxRange == UNBOUNDED) {
                groupOccurrenceOK = false;
                errors.add(XmlError.forObject(formatGroupMaxOccursError(derivedModel, baseModel), context));
            } else {
                if (maxRange.compareTo(baseModel.getMaxOccurs()) > 0) {
                    groupOccurrenceOK = false;
                    errors.add(XmlError.forObject(formatGroupMaxOccursError(derivedModel, baseModel), context));
                }
            }
        }
        return groupOccurrenceOK;
    }

    private static String formatGroupMaxOccursError(SchemaParticle derivedModel, SchemaParticle baseModel) {
        return "Invalid Restriction.  The maxOccurs for the derived group "
                + printParticle(derivedModel)
                + " is great than the base maxOccurs of "
                + printMaxOccurs(baseModel.getMaxOccurs());
    }

    private static String formatGroupMinOccursError(SchemaParticle derivedModel, SchemaParticle baseModel) {
        return "Invalid Restriction.  The minOccurs for the derived group "
                + printParticle(derivedModel)
                + " is less than the base minOccurs of "
                + baseModel.getMinOccurs();
    }

    private static BigInteger getEffectiveMaxRangeChoice(SchemaParticle derivedModel) {
        BigInteger maxRange = BigInteger.ZERO;
        BigInteger UNBOUNDED = null;
        // Schema Component Constraint: Effective Total Range (choice)
        // The effective total range of a particle whose {term} is a group whose {compositor} is choice
        // is a pair of minimum and maximum, as follows:
        // MAXIMUM
        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded (note: seems to be the same as Max Range All or Sequence),
        // or 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded,
        // otherwise 3) the product of the particle's {max occurs} and the maximum of the {max occurs} of every
        // wildcard or element declaration particle in the group's {particles} and the *maximum* (note: this is the difference
        // between MaxRange Choice ans MaxRange All or Sequence) part of the
        // effective total range of each of the group particles in the group's {particles}
        // (or 0 if there are no {particles}).

        boolean nonZeroParticleChildFound = false;
        BigInteger maxOccursInWildCardOrElement = BigInteger.ZERO;
        BigInteger maxOccursInGroup = BigInteger.ZERO;
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    // if unbounded then maxoccurs will be null
                    if (particle.getMaxOccurs() == UNBOUNDED) {
                        maxRange = UNBOUNDED;
                    } else {
                        if (particle.getIntMaxOccurs() > 0) {
                            // show tht at least one non-zero particle is found for later test
                            nonZeroParticleChildFound = true;
                            if (particle.getMaxOccurs().compareTo(maxOccursInWildCardOrElement) > 0) {
                                maxOccursInWildCardOrElement = particle.getMaxOccurs();
                            }
                        }
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    maxRange = getEffectiveMaxRangeAllSeq(derivedModel);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
                case SchemaParticle.CHOICE:
                    maxRange = getEffectiveMaxRangeChoice(derivedModel);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
            }
            // if an unbounded has been found then we are done
            if (maxRange == UNBOUNDED) {
                break;
            }
        }

        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded
        if (maxRange != UNBOUNDED) {
            // 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded
            if (nonZeroParticleChildFound && derivedModel.getMaxOccurs() == UNBOUNDED) {
                maxRange = UNBOUNDED;
            } else {
                // 3) the product of the particle's {max occurs} and the maximum of the {max occurs} of every
                // wildcard or element declaration particle in the group's {particles} and the *maximum*
                // part of the effective total range of each of the group particles in the group's {particles}
                maxRange = derivedModel.getMaxOccurs().multiply(maxOccursInWildCardOrElement.add(maxOccursInGroup));
            }
        }

        return maxRange;
    }

    private static BigInteger getEffectiveMaxRangeAllSeq(SchemaParticle derivedModel) {
        BigInteger maxRange = BigInteger.ZERO;
        BigInteger UNBOUNDED = null;
        // Schema Component Constraint: Effective Total Range (all and sequence)
        // The effective total range of a particle whose {term} is a group whose {compositor} is all or sequence is a
        // pair of minimum and maximum, as follows:
        // MAXIMUM
        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded, or 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded, otherwise
        // 3) the product of the particle's {max occurs} and the *sum* of the {max occurs} of every wildcard or element
        // declaration particle in the group's {particles} and the maximum part of the effective total range of each of
        // the group particles in the group's {particles} (or 0 if there are no {particles}).

        boolean nonZeroParticleChildFound = false;
        BigInteger maxOccursTotal = BigInteger.ZERO;
        BigInteger maxOccursInGroup = BigInteger.ZERO;
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    // if unbounded then maxoccurs will be null
                    if (particle.getMaxOccurs() == UNBOUNDED) {
                        maxRange = UNBOUNDED;
                    } else {
                        if (particle.getIntMaxOccurs() > 0) {
                            // show tht at least one non-zero particle is found for later test
                            nonZeroParticleChildFound = true;
                            maxOccursTotal = maxOccursTotal.add(particle.getMaxOccurs());
                        }
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    maxRange = getEffectiveMaxRangeAllSeq(derivedModel);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
                case SchemaParticle.CHOICE:
                    maxRange = getEffectiveMaxRangeChoice(derivedModel);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
            }
            // if an unbounded has been found then we are done
            if (maxRange == UNBOUNDED) {
                break;
            }
        }

        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded
        if (maxRange != UNBOUNDED) {
            // 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded
            if (nonZeroParticleChildFound && derivedModel.getMaxOccurs() == UNBOUNDED) {
                maxRange = UNBOUNDED;
            } else {
                // 3) the product of the particle's {max occurs} and the sum of the {max occurs} of every wildcard or element
                // declaration particle in the group's {particles} and the maximum part of the effective total range of each of
                // the group particles in the group's {particles}
                maxRange = derivedModel.getMaxOccurs().multiply(maxOccursTotal.add(maxOccursInGroup));
            }
        }

        return maxRange;

    }

    private static BigInteger getEffectiveMinRangeChoice(SchemaParticle derivedModel) {
        // Schema Component Constraint: Effective Total Range (choice)
        // The effective total range of a particle whose {term} is a group whose {compositor} is choice is a pair of
        // minimum and maximum, as follows:
        // MINIMUM
        // The product of the particle's {min occurs}
        // and the *minimum* of the {min occurs} of every wildcard or element
        // declaration particle in the group's {particles} and the minimum part of the effective total range of each of
        // the group particles in the group's {particles} (or 0 if there are no {particles}).
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        if (particleChildren.length == 0)
            return BigInteger.ZERO;
        BigInteger minRange = null;
        // get the minimum of every wildcard or element
        // total up the effective total range for each group
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    if (minRange == null || minRange.compareTo(particle.getMinOccurs()) > 0) {
                        minRange = particle.getMinOccurs();
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    BigInteger mrs = getEffectiveMinRangeAllSeq(derivedModel);
                    if (minRange == null || minRange.compareTo(mrs) > 0) {
                        minRange = mrs;
                    }
                    break;
                case SchemaParticle.CHOICE:
                    BigInteger mrc = getEffectiveMinRangeChoice(derivedModel);
                    if (minRange == null || minRange.compareTo(mrc) > 0) {
                        minRange = mrc;
                    }
                    break;
            }
        }
        if (minRange == null)
            minRange = BigInteger.ZERO;

        // calculate the total
        minRange = derivedModel.getMinOccurs().multiply(minRange);
        return minRange;
    }

    private static BigInteger getEffectiveMinRangeAllSeq(SchemaParticle derivedModel) {
        BigInteger minRange = BigInteger.ZERO;
        // Schema Component Constraint: Effective Total Range (all and sequence)
        // The effective total range of a particle whose {term} is a group whose {compositor} is all or sequence is a
        // pair of minimum and maximum, as follows:
        // MINIMUM
        // The product of the particle's {min occurs}
        // and the *sum* of the {min occurs} of every wildcard or element
        // declaration particle in the group's {particles}
        // and the minimum part of the effective total range of each
        // of the group particles in the group's {particles} (or 0 if there are no {particles}).
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        BigInteger particleTotalMinOccurs = BigInteger.ZERO;
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(particle.getMinOccurs());
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(getEffectiveMinRangeAllSeq(derivedModel));
                    break;
                case SchemaParticle.CHOICE:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(getEffectiveMinRangeChoice(derivedModel));
                    break;
            }
        }

        minRange = derivedModel.getMinOccurs().multiply(particleTotalMinOccurs);

        return minRange;
    }

    private static boolean nsSubset(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // nsSubset is called when base: ANY, derived: ANY
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        assert derivedModel.getParticleType() == SchemaParticle.WILDCARD;
        boolean nsSubset = false;
        // For a wildcard particle to be a ·valid restriction· of another wildcard particle all of the following must be true:
        // 1 R's occurrence range must be a valid restriction of B's occurrence range as defined by Occurrence Range OK (§3.9.6).
        if (occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            // 2 R's {namespace constraint} must be an intensional subset of B's {namespace constraint} as defined
            // by Wildcard Subset (§3.10.6).
            if (baseModel.getWildcardSet().inverse().isDisjoint(derivedModel.getWildcardSet())) {
                nsSubset = true;
            } else {
                nsSubset = false;
                errors.add(XmlError.forObject(formatNSIsNotSubsetError(baseModel, derivedModel), context));
            }
        } else {
            nsSubset = false;
            errors.add(XmlError.forObject(formatNSIsNotSubsetError(baseModel, derivedModel), context));
        }


        return nsSubset;
    }

    private static boolean nsCompat(SchemaParticle baseModel, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // nsCompat is called when base: ANY, derived: ELEMENT
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        boolean nsCompat = false;
        // For an element declaration particle to be a ·valid restriction· of a wildcard particle all of the following must be true:
        // 1 The element declaration's {target namespace} is ·valid· with respect to the wildcard's {namespace constraint}
        // as defined by Wildcard allows Namespace Name (§3.10.4).
        if (baseModel.getWildcardSet().contains(derivedElement.getName())) {
            // 2 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence Range OK (§3.9.6).
            if (occurrenceRangeOK(baseModel, (SchemaParticle) derivedElement, errors, context)) {
                nsCompat = true;
            } else {
                errors.add(XmlError.forObject(formatOccurenceRangeMinError(baseModel, (SchemaParticle) derivedElement), context));
            }
        } else {
            nsCompat = false;
            errors.add(XmlError.forObject(formatNSIsNotSubsetError(baseModel, (SchemaParticle) derivedElement), context));
        }


        return nsCompat;
    }

    private static String formatNSIsNotSubsetError(SchemaParticle baseParticle, SchemaParticle derivedParticle) {
        return "Invalid Restriction. The namespace(s) of the derived field: " + printParticle(derivedParticle)
                + " are not a subset of the namespace(s) of the base field: " + printParticle(baseParticle);
    }

    private static String formatInvalidCombinationError(SchemaParticle baseModel, SchemaParticle derivedModel) {
        return "Invalid Restriction.  The derived content model "
                + printParticle(derivedModel)
                + " cannot restrict base content model "
                + printParticle(baseModel);
    }

    private static boolean nameAndTypeOK(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // nameAndTypeOK called when base: ELEMENT and derived: ELEMENT
        boolean nameAndTypeOK = false;
        // Schema Component Constraint: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)
        // 1 The declarations' {name}s and {target namespace}s are the same.
        if (((SchemaParticle)baseElement).canStartWithElement(derivedElement.getName())) {
            // 2 Either B's {nillable} is true or R's {nillable} is false.
            if (baseElement.isNillable() || !derivedElement.isNillable()) {
                // 3 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence Range OK (§3.9.6).
                if (occurrenceRangeOK((SchemaParticle) baseElement, (SchemaParticle) derivedElement, errors, context)) {
                    // 4 either B's declaration's {value constraint} is absent, or is not fixed,
                    // or R's declaration's {value constraint} is fixed with the same value.
                    nameAndTypeOK = checkFixed(baseElement, derivedElement, errors, context);
                    if (nameAndTypeOK) {
                        // 5 R's declaration's {identity-constraint definitions} is a subset of B's declaration's {identity-constraint definitions}, if any.
                        nameAndTypeOK = checkIdentityConstraints(baseElement, derivedElement, errors, context);
                        if (nameAndTypeOK) {
                            // 7 R's {type definition} is validly derived given {extension, list, union} from B's {type definition} as
                            // defined by Type Derivation OK (Complex) (§3.4.6) or Type Derivation OK (Simple) (§3.14.6), as appropriate.
                            nameAndTypeOK = typeDerivationOK(baseElement.getType(), derivedElement.getType(), errors, context);
                            if (nameAndTypeOK) {
                                // 6 R's declaration's {disallowed substitutions} is a superset of B's declaration's {disallowed substitutions}.
                                nameAndTypeOK = blockSetOK(baseElement, derivedElement, errors, context);
                            }
                        }
                    }
                }
            }
        }
        return nameAndTypeOK;
    }
    
    private static boolean blockSetOK(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context)
    {
        if (baseElement.blockRestriction() && !derivedElement.blockRestriction())
        {
            errors.add(XmlError.forObject("Restriction Invalid.  Derived " + printParticle((SchemaParticle)derivedElement) + " does not block restriction as does the base " + printParticle((SchemaParticle)baseElement), context));
            return false;
        }
        if (baseElement.blockExtension() && !derivedElement.blockExtension())
        {
            errors.add(XmlError.forObject("Restriction Invalid.  Derived " + printParticle((SchemaParticle)derivedElement) + " does not block extension as does the base " + printParticle((SchemaParticle)baseElement), context));
            return false;
        }
        if (baseElement.blockSubstitution() && !derivedElement.blockSubstitution())
        {
            errors.add(XmlError.forObject("Restriction Invalid.  Derived " + printParticle((SchemaParticle)derivedElement) + " does not block substitution as does the base " + printParticle((SchemaParticle)baseElement), context));
            return false;
        }
        return true;    
    }

    private static boolean typeDerivationOK(SchemaType baseType, SchemaType derivedType, Collection errors, XmlObject context) {
        boolean typeDerivationOK = false;
        // 1 If B and D are not the same type definition, then the {derivation method} of D must not be in the subset.
        // 2 One of the following must be true:
        // 2.1 B and D must be the same type definition.
        // 2.2 B must be D's {base type definition}.
        // 2.3 All of the following must be true:
        // 2.3.1 D's {base type definition} must not be the ·ur-type definition·.
        // 2.3.2 The appropriate case among the following must be true:
        // 2.3.2.1 If D's {base type definition} is complex, then it must be validly derived from B given the subset as defined by this constraint.
        // 2.3.2.2 If D's {base type definition} is simple, then it must be validly derived from B given the subset as defined in Type Derivation OK (Simple) (§3.14.6).
        //   This line will check if derivedType is a subType of baseType (should handle all of the 2.xx checks above)
        if (baseType.isAssignableFrom(derivedType)) {
            // Ok derived type is subtype but need to make sure that all of the derivations between the two types are by
            // Restriction.
            typeDerivationOK = checkAllDerivationsForRestriction(baseType, derivedType, errors, context);
        } else {
            // derived type is not a sub-type of base type
            typeDerivationOK = false;
            errors.add(XmlError.forObject(formatDerivedTypeNotSubTypeError(baseType, derivedType), context));
        }

        return typeDerivationOK;
    }

    private static String formatDerivedTypeNotSubTypeError(SchemaType baseType, SchemaType derivedType) {
        return "Restriction Invalid.  Derived Type: " + printType(derivedType) + " is not a sub-type of Base Type: " + printType(baseType);
    }

    private static boolean checkAllDerivationsForRestriction(SchemaType baseType, SchemaType derivedType, Collection errors, XmlObject context) {

        boolean allDerivationsAreRestrictions = true;
        SchemaType currentType = derivedType;
        // run up the types hierarchy from derived Type to base Type and make sure that all are derived by
        //   restriction.  If any are not then this is not a valid restriction.
        while (!baseType.equals(currentType)) {
            if (currentType.getDerivationType() == SchemaType.DT_RESTRICTION) {
                currentType = currentType.getBaseType();
            } else {
                allDerivationsAreRestrictions = false;
                errors.add(XmlError.forObject(formatAllDerivationsAreNotRestrictionsError(baseType, derivedType, currentType), context));
                break;
            }
        }
        return allDerivationsAreRestrictions;
    }

    private static String formatAllDerivationsAreNotRestrictionsError(SchemaType baseType, SchemaType derivedType, SchemaType currentType) {
        return "Invalid Restriction.  The type " + printType(derivedType) + " derived from base type "
                + printType(baseType) + " has an intermediary type " + printType(currentType)
                + "that is not derived by restriction.";
    }

    private static boolean checkIdentityConstraints(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // 5 R's declaration's {identity-constraint definitions} is a subset of B's declaration's {identity-constraint definitions}, if any.
        boolean identityConstraintsOK = true;

        SchemaIdentityConstraint[] baseConstraints = baseElement.getIdentityConstraints();
        SchemaIdentityConstraint[] derivedConstraints = derivedElement.getIdentityConstraints();
        // cycle thru derived's identity constraints and check each to assure they in the array of base constraints
        for (int i = 0; i < derivedConstraints.length; i++) {
            SchemaIdentityConstraint derivedConstraint = derivedConstraints[i];
            if (checkForIdentityConstraintExistence(baseConstraints, derivedConstraint)) {
                identityConstraintsOK = false;
                errors.add(XmlError.forObject(formatIdentityConstraintsNotSubsetError(baseElement, derivedElement), context));
                break;
            }
        }
        return identityConstraintsOK;
    }

    private static String formatIdentityConstraintsNotSubsetError(SchemaLocalElement baseElement, SchemaLocalElement derivedElement) {
        return "Restriction Invalid.  The identity constraints for " + printParticle((SchemaParticle)derivedElement)
                + " are not a subset of the identity constraints for " + printParticle((SchemaParticle)baseElement);
    }

    private static boolean checkForIdentityConstraintExistence(SchemaIdentityConstraint[] baseConstraints, SchemaIdentityConstraint derivedConstraint) {
        // spin thru the base identity constraints check to see if derived constraint exists
        boolean identityConstraintExists = false;
        for (int i = 0; i < baseConstraints.length; i++) {
            SchemaIdentityConstraint baseConstraint = baseConstraints[i];
            if (baseConstraint.getName().equals(derivedConstraint.getName())) {
                identityConstraintExists = true;
                break;
            }
        }
        return identityConstraintExists;
    }


    private static boolean checkFixed(SchemaLocalElement baseModel, SchemaLocalElement derivedModel, Collection errors, XmlObject context) {
        // 4 either B's declaration's {value constraint} is absent, or is not fixed,
        // or R's declaration's {value constraint} is fixed with the same value.
        boolean checkFixed = false;
        if (baseModel.isFixed()) {
            if (baseModel.getDefaultText().equals(derivedModel.getDefaultText())) {
                //  R's declaration's {value constraint} is fixed with the same value.
                checkFixed = true;
            } else {
                // The derived element has a fixed value that is different than the base element
                errors.add(XmlError.forObject(formatInvalidFixedError(baseModel, derivedModel), context));
                checkFixed = false;
            }
        } else {
            //  B's declaration's {value constraint} is absent, or is not fixed,
            checkFixed = true;
        }
        return checkFixed;
    }

    private static String formatInvalidFixedError(SchemaLocalElement baseModel, SchemaLocalElement derivedModel) {
        return "The Derived Content Model on Element: '"
                + printParticle((SchemaParticle)derivedModel)
                + "' has a fixed value of: '"
                + derivedModel.getDefaultText()
                + "' which does not match the Base Content Model: '"
                + printParticle((SchemaParticle)baseModel)
                + "' what has a fixed value of: '"
                + baseModel.getDefaultText() + "'";
    }

    private static boolean occurrenceRangeOK(SchemaParticle baseParticle, SchemaParticle derivedParticle, Collection errors, XmlObject context) {
        boolean occurrenceRangeOK = false;
        // Note: in the following comments (from the schema spec) other is the baseModel
        // 1 Its {min occurs} is greater than or equal to the other's {min occurs}.
        if (derivedParticle.getMinOccurs().compareTo(baseParticle.getMinOccurs()) >= 0) {
            // 2 one of the following must be true:
            // 2.1 The other's {max occurs} is unbounded.
            if (baseParticle.getMaxOccurs() == null) {
                occurrenceRangeOK = true;
            } else {
                // 2.2 Both {max occurs} are numbers, and the particle's is less than or equal to the other's.
                if (derivedParticle.getMaxOccurs() != null && baseParticle.getMaxOccurs() != null &&
                        derivedParticle.getMaxOccurs().compareTo(baseParticle.getMaxOccurs()) <= 0) {
                    occurrenceRangeOK = true;
                } else {
                    occurrenceRangeOK = false;
                    if (baseParticle.getName() == null || derivedParticle.getName() == null) {
                        errors.add(XmlError.forObject(formatOccurenceRangeMaxErrorGroup(baseParticle, derivedParticle), context));
                    } else {
                        errors.add(XmlError.forObject(formatOccurenceRangeMaxError(baseParticle, derivedParticle), context));
                    }
                }
            }
        } else {
            occurrenceRangeOK = false;
            if (baseParticle.getName() == null || derivedParticle.getName() == null) {
                errors.add(XmlError.forObject(formatOccurenceRangeMinErrorGroup(baseParticle, derivedParticle), context));
            } else {
                errors.add(XmlError.forObject(formatOccurenceRangeMinError(baseParticle, derivedParticle), context));
            }
        }
        return occurrenceRangeOK;
    }

    private static String formatOccurenceRangeMaxErrorGroup(SchemaParticle baseParticle, SchemaParticle derivedParticle) {
        return "Invalid Restriction.  The maxOccurs for the "
                + printParticle(derivedParticle)
                + " (" + printMaxOccurs(derivedParticle.getMaxOccurs())
                + ") is greater than than the maxOccurs for the base "
                + printParticle(baseParticle)
                + " (" + printMaxOccurs(baseParticle.getMaxOccurs()) + ")";
    }

    private static String formatOccurenceRangeMinErrorGroup(SchemaParticle baseParticle, SchemaParticle derivedParticle) {
        return "Invalid Restriction.  The minOccurs for the "
                + printParticle(derivedParticle)
                + " (" + derivedParticle.getMinOccurs().toString()
                + ") is less than than the minOccurs for the base "
                + printParticle(baseParticle)
                + " (" + baseParticle.getMinOccurs().toString() + ")";
    }

    private static String formatOccurenceRangeMinError(SchemaParticle baseField, SchemaParticle derivedField) {
        return "Invalid Restriction.  The minOccurs for the element: '"
                + printParticle(derivedField)
                + "' (" + derivedField.getMinOccurs().toString()
                + ") is less than than the minOccurs for the base element: '"
                + printParticle(baseField)
                + "' (" + baseField.getMinOccurs().toString() + ")";
    }

    private static String formatOccurenceRangeMaxError(SchemaParticle baseField, SchemaParticle derivedField) {
        return "Invalid Restriction.  The maxOccurs for the element: '"
                + printParticle(derivedField)
                + "' (" + printMaxOccurs(derivedField.getMaxOccurs())
                + ") is greater than than the maxOccurs for the base element: '"
                + printParticle(baseField) + "' (" + printMaxOccurs(baseField.getMaxOccurs()) + ")";
    }
    
    private static String printParticle(SchemaParticle part)
    {
        switch (part.getParticleType()) {
            case SchemaParticle.ALL:
                return "<all>";
            case SchemaParticle.CHOICE:
                return "<choice>";
            case SchemaParticle.ELEMENT:
                return "<element name=\"" + QNameHelper.pretty(part.getName()) + "\">";
            case SchemaParticle.SEQUENCE:
                return "<sequence>";
            case SchemaParticle.WILDCARD:
                return "<any>";
            default :
                return "??";
        }
    }
    
    private static String printMaxOccurs(BigInteger bi)
    {
        if (bi == null)
            return "unbounded";
        return bi.toString();
    }
    
    private static String printType(SchemaType type)
    {
        if (type.getName() != null)
            return QNameHelper.pretty(type.getName());
        return type.toString();
    }

    private static void checkSubstitutionGroups(SchemaGlobalElement[] elts)
    {
        StscState state = StscState.get();

        for (int i = 0 ; i < elts.length ; i++)
        {
            SchemaGlobalElement elt = elts[i];
            SchemaGlobalElement head = elt.substitutionGroup();

            if (head != null)
            {
                SchemaType headType = head.getType();
                SchemaType tailType = elt.getType();
                XmlObject parseTree = ((SchemaGlobalElementImpl)elt)._parseObject;

                if (! headType.isAssignableFrom(tailType))
                {
                    state.error("Element " + QNameHelper.pretty(elt.getName()) +
                        " must have a type that is derived from the type of its substitution group.",
                        XmlErrorContext.INCONSISTENT_TYPE, parseTree);
                    
                }
                else if (head.finalExtension() && head.finalRestriction())
                {
                    state.error("Element " + QNameHelper.pretty(elt.getName()) + 
                        " cannot be substituted for element with final='#all'",
                        XmlErrorContext.CANNOT_DERIVE_FINAL, parseTree);
                }
                else if (! headType.equals(tailType))
                {
                    if (head.finalExtension() && 
                             tailType.getDerivationType() == SchemaType.DT_EXTENSION)
                    {
                        state.error("Element " + QNameHelper.pretty(elt.getName()) + 
                            " cannot be substituted for element with final='extension'",
                            XmlErrorContext.CANNOT_DERIVE_FINAL, parseTree);
                    }
                    else if (head.finalRestriction() &&
                             tailType.getDerivationType() == SchemaType.DT_RESTRICTION)
                    {
                        state.error("Element " + QNameHelper.pretty(elt.getName()) + 
                            " cannot be substituted for element with final='restriction'",
                            XmlErrorContext.CANNOT_DERIVE_FINAL, parseTree);
                    }
                }
            }

        }
    }
}
