/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.validator;

import org.apache.xmlbeans.impl.common.Chars;
import org.apache.xmlbeans.impl.common.IdentityConstraint;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.ValidatorListener.Event;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.schema.SchemaTypeVisitorImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.values.NamespaceContext;
import org.apache.xmlbeans.impl.values.JavaUriHolderEx;
import org.apache.xmlbeans.impl.values.JavaBase64HolderEx;
import org.apache.xmlbeans.impl.values.JavaBooleanHolderEx;
import org.apache.xmlbeans.impl.values.XmlDateImpl;
import org.apache.xmlbeans.impl.values.JavaDecimalHolderEx;
import org.apache.xmlbeans.impl.values.JavaDoubleHolderEx;
import org.apache.xmlbeans.impl.values.XmlDurationImpl;
import org.apache.xmlbeans.impl.values.JavaFloatHolderEx;
import org.apache.xmlbeans.impl.values.JavaHexBinaryHolderEx;
import org.apache.xmlbeans.impl.values.JavaBooleanHolder;
import org.apache.xmlbeans.impl.values.XmlQNameImpl;
import org.apache.xmlbeans.impl.values.JavaQNameHolderEx;
import org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.values.XmlListImpl;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SimpleValue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import javax.xml.namespace.QName;

public final class Validator
    implements ValidatorListener
{
    public Validator (
        SchemaType type, SchemaField field, SchemaTypeLoader globalLoader,
        XmlOptions options, Collection defaultErrorListener )
    {
        options = XmlOptions.maskNull(options);
        _errorListener = (Collection) options.get(XmlOptions.ERROR_LISTENER);

        if (_errorListener == null)
            _errorListener = defaultErrorListener;

        _constraintEngine = new IdentityConstraint(_errorListener, type.isDocumentType());

        _globalTypes = globalLoader;
        _chars = new Chars();
        _rootType = type;
        _rootField = field;

        _vc = new ValidatorVC();
    }

    private class ValidatorVC implements ValidationContext
    {
        public void invalid ( String message )
        {
            Validator.this.emitError( _event, message );
        }

        Event _event;
    }

    public boolean isValid ( )
    {
        return !_invalid && _constraintEngine.isValid();
    }

    private void emitError ( Event event, String msg )
    {
        emitError(event, msg, XmlError.SEVERITY_ERROR);
    }

    private void emitError ( Event event, String msg, int severity )
    {
        _errorState++;

        if (_suspendErrors == 0)
        {
            if (severity == XmlError.SEVERITY_ERROR)
                _invalid = true;

            if (_errorListener != null)
            {
                assert event != null;

                _errorListener.add(
                    XmlError.forCursor( msg, severity, event.getLocationAsCursor() ) );
            }
        }
    }

    private void emitFieldError ( Event event, String msg, QName name )
    {
        emitFieldError( event, msg + " " + QNameHelper.pretty( name ) );
    }

    private void emitFieldError ( Event event, String msg )
    {
        emitFieldError(event, msg, XmlError.SEVERITY_ERROR);
    }

    private void emitFieldError ( Event event, String msg, int severity )
    {
        if (_stateStack != null && _stateStack._field != null)
        {
            msg +=
                " in element " + QNameHelper.pretty( _stateStack._field.getName() );
        }

        Validator.this.emitError( event, msg, severity );
    }

    // For XmlEventListener.error

    public void error ( XmlError error )
    {
        _errorState++;

        if (_suspendErrors == 0)
        {
            _invalid = true;

            if (_errorListener != null)
                _errorListener.add( error );
        }
    }

    public void nextEvent ( int kind, Event event )
    {
        if (_eatContent > 0)
        {
            switch ( kind )
            {
            case END   : _eatContent--; break;
            case BEGIN : _eatContent++; break;
            }
        }
        else
        {
            assert
                kind == BEGIN || kind == ATTR ||
                    kind == END || kind == TEXT || kind == ENDATTRS;

            switch ( kind )
            {
            case BEGIN    : beginEvent(    event ); break;
            case ATTR     : attrEvent(     event ); break;
            case ENDATTRS : endAttrsEvent( event ); break;
            case TEXT     : textEvent(     event ); break;
            case END      : endEvent(      event ); break;
            }
        }
    }

    private void beginEvent ( Event event )
    {
        State state = topState();

        SchemaType  elementType  = null;
        SchemaField elementField = null;

        if (state == null)
        {
            elementType = _rootType;
            elementField = _rootField;
        }
        else
        {

            QName name = event.getName();

            assert name != null;

            state._isEmpty = false;

            if (state._isNil)
            {
                emitFieldError(event,  "Nil element cannot have element content");
                _eatContent = 1;
                return;
            }

            if (!state.visit( name ))
            {
                emitFieldError( event, "Element not allowed:", name );
                _eatContent = 1;
                return;
            }

            SchemaParticle currentParticle = state.currentParticle();

            if (currentParticle.getParticleType() == SchemaParticle.WILDCARD)
            {
                QNameSet elemWildcardSet = currentParticle.getWildcardSet();

                if (!elemWildcardSet.contains( name ))
                {
                    emitFieldError( event, "Element not allowed:", name );
                    _eatContent = 1;
                    return;
                }

                int wildcardProcess = currentParticle.getWildcardProcess();

                if (wildcardProcess == SchemaParticle.SKIP)
                {
                    _eatContent = 1;
                    return;
                }

                elementField = _globalTypes.findElement( name );

                if (elementField == null)
                {
                    if (wildcardProcess == SchemaParticle.STRICT)
                    {
                        emitFieldError(
                            event, "Element not allowed (strict wildcard, and no definition found):", name );
                    }

                    _eatContent = 1;

                    return;
                }
            }
            else
            {
                assert
                    currentParticle.getParticleType() == SchemaParticle.ELEMENT;

                // If the current element particle name does not match the name
                // of the event, then the current element is a substitute for
                // the current particle. Replace the field with the global
                // element for the replacement

                if (! currentParticle.getName().equals(name))
                {
                    if (((SchemaLocalElement)currentParticle).blockSubstitution())
                    {
                        emitFieldError(event, 
                            "Element substitution not allowed when group head has block='substitution'", name);
                        _eatContent = 1;
                        return;
                    }

                    SchemaGlobalElement newField = _globalTypes.findElement(name);

                    assert newField != null;

                    if (newField != null)
                        elementField = newField;
                }

                else
                {
                    elementField = (SchemaField) currentParticle;
                }
            }

            elementType = elementField.getType();
        }

        assert elementType != null;

        //
        // the no-type is always invalid (even if there is an xsi:type)
        //

        if (elementType.isNoType())
        {
            emitFieldError(event, "Invalid type.");
            _eatContent = 1;
        }

        //
        // See if the element has an xsi:type on it
        //

        SchemaType xsiType = null;

        if (event.getXsiType( _chars ))
        {
            String value = _chars.asString();

            // Turn off the listener so a public error message
            // does not get generated, but I can see if there was
            // an error through the error state

            int originalErrorState = _errorState;

            _suspendErrors++;

            try
            {
                _vc._event = null;

                xsiType =
                    _globalTypes.findType(
                        XmlQNameImpl.validateLexical( value, _vc, event ) );

            }
            catch ( Throwable t )
            {
                _errorState++;
            }
            finally
            {
                _suspendErrors--;
            }

            if (originalErrorState != _errorState)
            {
                emitFieldError(
                    event, "Invalid xsi:type qname: '" + value + "'" );

                _eatContent = 1;

                return;
            }
            else if (xsiType == null)
            {
                emitError(event,  "Could not find xsi:type: '" + value + "'");

                _eatContent = 1;

                return;
            }
        }

        if (xsiType != null && !xsiType.equals(elementType))
        {
            if (!elementType.isAssignableFrom(xsiType))
            {
                emitFieldError(
                    event,
                    "Type '" + xsiType +
                        "' is not derived from '" + elementType + "'" );

                _eatContent = 1;

                return;
            }

            if (elementType.blockExtension())
            {
                for ( SchemaType t = xsiType ; ! t.equals( elementType ) ;
                      t = t.getBaseType() )
                {
                    if (t.getDerivationType() == SchemaType.DT_EXTENSION)
                    {
                        emitFieldError(
                            event,
                            "Extension type: '" + xsiType +
                                "' may not be substituted for: '" +
                                    elementType + "'" );

                        _eatContent = 1;

                        return;
                    }
                }
            }

            if (elementType.blockRestriction())
            {
                for ( SchemaType t = xsiType ; ! t.equals( elementType ) ;
                      t = t.getBaseType() )
                {
                    if (t.getDerivationType() == SchemaType.DT_RESTRICTION)
                    {
                        emitFieldError(
                            event,
                            "Restriction type: '" + xsiType +
                                "' may not be substituted for: '" +
                                    elementType + "'" );

                        _eatContent = 1;

                        return;
                    }
                }
            }

            if (elementField instanceof SchemaLocalElement)
            {
                SchemaLocalElement sle  = (SchemaLocalElement)elementField;

                if (sle.blockExtension() || sle.blockRestriction())
                {
                    for ( SchemaType t = xsiType ; ! t.equals( elementType ) ;
                          t = t.getBaseType() )
                    {
                        if ((t.getDerivationType() == SchemaType.DT_RESTRICTION && sle.blockRestriction()) ||
                            (t.getDerivationType() == SchemaType.DT_EXTENSION && sle.blockExtension()))
                        {
                            emitError(
                                event,
                                "Derived type: '" + xsiType +
                                    "' may not be substituted for element '" +
                                    QNameHelper.pretty(sle.getName()) + "'" );

                            _eatContent = 1;

                            return;
                        }
                    }
                }

            }

            elementType = xsiType;
        }

        if (elementField instanceof SchemaLocalElement)
        {
            SchemaLocalElement sle = (SchemaLocalElement)elementField;

            if (sle.isAbstract())
            {
                emitError(event,  "Element '" + QNameHelper.pretty(sle.getName()) +
                    "' is abstract and cannot be used in an instance.");
                _eatContent = 1;
                return;
            }
        }

        if (elementType != null && elementType.isAbstract())
        {
            emitFieldError(
                event,
                "Abstract type: " + elementType +
                    " cannot be used in an instance" );

            _eatContent = 1;

            return;
        }

        boolean isNil = false;
        boolean hasNil = false;

        if (event.getXsiNil(_chars))
        {
            _vc._event = event;
            isNil = JavaBooleanHolder.validateLexical(_chars.asString(), _vc);
            hasNil = true;
        }
        
        // note in schema spec 3.3.4, you're not even allowed to say xsi:nil="false" if you're not nillable!
        if (hasNil && !elementField.isNillable())
        {
            emitFieldError(event,  "Element has xsi:nil attribute but is not nillable");

            _eatContent = 1;
            return;
        }

        newState( elementType, elementField, isNil );

        // Dispatch this element event to any identity constraints
        // As well as adding any new identity constraints that exist

        _constraintEngine.element(
            event,
            elementType,
            elementField instanceof SchemaLocalElement
                ? ((SchemaLocalElement) elementField).getIdentityConstraints()
                : null );
    }

    private void attrEvent ( Event event )
    {
        QName attrName = event.getName();

        State state = topState();

        if (state._attrs == null)
            state._attrs = new HashSet();

        if (state._attrs.contains( attrName ))
        {
            emitFieldError(
                event,
                "Duplicate attribute: " + QNameHelper.pretty( attrName ) );

            return;
        }

        state._attrs.add( attrName );

        if (!state._canHaveAttrs)
        {
            emitFieldError( event, "Can't have attributes" );
            return;
        }

        SchemaLocalAttribute attrSchema =
            state._attrModel == null
                ? null
                : state._attrModel.getAttribute( attrName );

        if (attrSchema != null)
        {
            if (attrSchema.getUse() == SchemaLocalAttribute.PROHIBITED)
            {
                emitFieldError(
                    event,
                    "Attribute is prohibited: "
                        + QNameHelper.pretty( attrName ) );

                return;
            }

            String value =
                validateSimpleType(
                    attrSchema.getType(), attrSchema, event, false, false );

            _constraintEngine.attr( event, attrName, attrSchema.getType(), value );

            return;
        }

        int wildcardProcess = state._attrModel.getWildcardProcess();

        if (wildcardProcess == SchemaAttributeModel.NONE)
        {
            emitFieldError(
                event,
                "Attribute not allowed (no wildcards allowed): "
                    + QNameHelper.pretty( attrName ) );

            return;
        }

        QNameSet attrWildcardSet = state._attrModel.getWildcardSet();

        if (!attrWildcardSet.contains( attrName ))
        {
            emitFieldError(
                event,
                "Attribute not allowed: " + QNameHelper.pretty( attrName ) );

            return;
        }

        if (wildcardProcess == SchemaAttributeModel.SKIP)
            return;

        attrSchema = _globalTypes.findAttribute( attrName );

        if (attrSchema == null)
        {
            if (wildcardProcess == SchemaAttributeModel.LAX)
                return;

            assert wildcardProcess == SchemaAttributeModel.STRICT;

            emitFieldError(
                event,
                "Attribute not allowed (strict wildcard, and no definition found): " + QNameHelper.pretty( attrName ) );

            return;
        }

        String value =
            validateSimpleType(
                attrSchema.getType(), attrSchema, event, false, false );

        _constraintEngine.attr( event, attrName, attrSchema.getType(), value );
    }

    private void endAttrsEvent ( Event event )
    {
        State state = topState();

        if (state._attrModel != null)
        {
            SchemaLocalAttribute[] attrs = state._attrModel.getAttributes();

            for ( int i = 0 ; i < attrs.length ; i++ )
            {
                SchemaLocalAttribute sla = attrs[ i ];

                if (state._attrs == null ||
                        !state._attrs.contains( sla.getName() ))
                {
                    if (sla.getUse() == SchemaLocalAttribute.REQUIRED)
                    {
                        emitFieldError(
                            event, "Expected attribute: ", sla.getName() );
                    }
                    else if (sla.isDefault() || sla.isFixed())
                    {
                        _constraintEngine.attr(event, sla.getName(), sla.getType(), sla.getDefaultText());

                        // We don't need to validate attribute defaults because this is done at compiletime.
                        /*
                        String value = sla.getDefaultText();
                        SchemaType type = sla.getType();

                        if (XmlQName.type.isAssignableFrom(type))
                        {
                            emitFieldError(
                                event, 
                                "Default QName values are unsupported for attribute: " + 
                                    QNameHelper.pretty(sla.getName()), 
                                XmlError.SEVERITY_INFO);
                        }

                        else 
                        {
                            validateSimpleType(
                                type, sla.getDefaultText(), event );

                            _constraintEngine.attr( event, type, value );
                        }
                        */
                    }
                }
            }
        }
    }

    private void endEvent ( Event event )
    {
        State state = topState();

        if (!state._isNil)
        {
            if (!state.end())
                emitFieldError( event, "Expected element(s)" );

            // This end event has no text, use this fact to pass no text to
            // handleText

            if (state._isEmpty)
                handleText( event, true, state._field );
        }

        popState( event );

        _constraintEngine.endElement( event );
    }

    private void textEvent ( Event event )
    {
        State state = topState();

        if (state._isNil)
            emitFieldError(event, "Nil element cannot have simple content");
        else
            handleText( event, false, state._field );

        state._isEmpty = false;
    }


    private void handleText (
        Event event, boolean emptyContent, SchemaField field )
    {
        State state = topState();

        if (!state._sawText)
        {
            if (state._hasSimpleContent)
            {
                String value =
                    validateSimpleType(
                        state._type, field, event, emptyContent, true );

                _constraintEngine.text( event, state._type, value, false );
            }
            else if (emptyContent)
            {
                _constraintEngine.text( event, state._type, null, true );
            }
            else
                _constraintEngine.text( event, state._type, "", false);
        }

        if (!emptyContent && !state._canHaveMixedContent && 
            !event.textIsWhitespace() & !state._hasSimpleContent)
        {
            if (field instanceof SchemaLocalElement) 
            {
                SchemaLocalElement e = (SchemaLocalElement)field;
                emitError(event, "Element: '" + QNameHelper.pretty(e.getName()) + "' cannot have mixed content.");
            }
            else
                emitError( event, "Can't have mixed content" );
        }

        if (!emptyContent)
            state._sawText = true;
    }

    private final class State
    {
        boolean visit ( QName name )
        {
            return _canHaveElements && _visitor.visit( name );
        }

        boolean end ( )
        {
            return !_canHaveElements || _visitor.visit( null );
        }

        SchemaParticle currentParticle ( )
        {
            assert _visitor != null;
            return _visitor.currentParticle();
        }

        SchemaType  _type;
        SchemaField _field;

        boolean _canHaveAttrs;
        boolean _canHaveMixedContent;
        boolean _hasSimpleContent;

        boolean _sawText;
        boolean _isEmpty;
        boolean _isNil;

        SchemaTypeVisitorImpl _visitor;
        boolean _canHaveElements;

        SchemaAttributeModel _attrModel;

        HashSet _attrs;

        State _next;
    }

    private void newState ( SchemaType type, SchemaField field, boolean isNil )
    {
        State state = new State();

        state._type = type;
        state._field = field;
        state._isEmpty = true;
        state._isNil = isNil;

        if (type.isSimpleType())
        {
            state._hasSimpleContent = true;
        }
        else
        {
            state._canHaveAttrs = true;
            state._attrModel = type.getAttributeModel();

            switch ( type.getContentType() )
            {
            case SchemaType.EMPTY_CONTENT :
                break;

            case SchemaType.SIMPLE_CONTENT :
                state._hasSimpleContent = true;
                break;

            case SchemaType.MIXED_CONTENT :
                state._canHaveMixedContent = true;
                // Fall through

            case SchemaType.ELEMENT_CONTENT :

                SchemaParticle particle = type.getContentModel();

                state._canHaveElements = particle != null;

                if (state._canHaveElements)
                    state._visitor = initVisitor( particle );

                break;

            default :
                throw new RuntimeException( "Unexpected content type" );
            }
        }

        pushState( state );
    }

    private void popState ( Event e )
    {
        if (_stateStack._visitor != null)
        {
            poolVisitor( _stateStack._visitor );
            _stateStack._visitor = null;
        }

        _stateStack = _stateStack._next;
    }

    private void pushState ( State state )
    {
        state._next = _stateStack;
        _stateStack = state;
    }

    private LinkedList _visitorPool = new LinkedList();

    private void poolVisitor( SchemaTypeVisitorImpl visitor )
    {
        _visitorPool.add( visitor );
    }

    private SchemaTypeVisitorImpl initVisitor( SchemaParticle particle )
    {
        if (_visitorPool.isEmpty())
            return new SchemaTypeVisitorImpl( particle );

        SchemaTypeVisitorImpl result =
            (SchemaTypeVisitorImpl) _visitorPool.removeLast();

        result.init( particle );

        return result;
    }

    private State topState ( )
    {
        return _stateStack;
    }

    //
    // Simple Type Validation
    //
    // emptyContent means that you can't use the event to get text: there is
    // not text, but you can use the event to do prefix resolution (in the case
    // where the default is a qname)
    //

    private String validateSimpleType (
        SchemaType type, SchemaField field, Event event,
        boolean emptyContent, boolean canApplyDefault )
    {
        if (!type.isSimpleType() &&
                type.getContentType() != SchemaType.SIMPLE_CONTENT)
        {
            assert false;
            // throw new RuntimeException( "Not a simple type" );
            return null; // should never happen
        }

        //
        // the no-type is always invalid
        //

        if (type.isNoType())
        {
            emitError(event, "Invalid type.");
            return null;
        }



        // Get the value as a string (as normalized by the white space rule
        // TODO - will want to optimize this later

        String value = "";

        if (!emptyContent)
        {
            event.getText( _chars, type.getWhiteSpaceRule() );
            value = _chars.asString();
        }

        // See if I can apply a default/fixed value

        if (value.length() == 0 && canApplyDefault && field != null &&
                (field.isDefault() || field.isFixed()))
        {
            if (XmlQName.type.isAssignableFrom(type))
            {
                emitError(
                    event,
                    "Default QName values are unsupported for " +
                        QNameHelper.readable(type) + " - ignoring.",
                    XmlError.SEVERITY_INFO);

                return null;
            }

            String defaultValue =
                XmlWhitespace.collapse(
                    field.getDefaultText(), type.getWhiteSpaceRule() );

// BUGBUG - should validate defaultValue at compile time
            return
                validateSimpleType( type, defaultValue, event )
                    ? defaultValue
                    : null;
        }

        if (!validateSimpleType( type, value, event ))
            return null;

        if (field != null && field.isFixed())
        {
// TODO - fixed value should have been cooked at compile time
            String fixedValue =
                XmlWhitespace.collapse(
                    field.getDefaultText(), type.getWhiteSpaceRule() );

            if (!validateSimpleType( type, fixedValue, event ))
                return null;

            XmlObject val = type.newValue( value );
            XmlObject def = type.newValue( fixedValue );

            if (!val.valueEquals( def ))
            {
                // BUGBUG - make this more verbose

                emitError(
                    event,
                    "Value not equal to fixed value. " + value );

                return null;
            }
        }

        return value;
    }

    private boolean validateSimpleType (
        SchemaType type, String value, Event event )
    {
        if (!type.isSimpleType() &&
                type.getContentType() != SchemaType.SIMPLE_CONTENT)
        {
            assert false;
            throw new RuntimeException( "Not a simple type" );
        }

        int retState = _errorState;

        switch ( type.getSimpleVariety() )
        {
        case SchemaType.ATOMIC : validateAtomicType( type, value, event );break;
        case SchemaType.UNION  : validateUnionType(  type, value, event );break;
        case SchemaType.LIST   : validateListType(   type, value, event );break;

        default : throw new RuntimeException( "Unexpected simple variety" );
        }

        return retState == _errorState;
    }

    private void validateAtomicType (
        SchemaType type, String value, Event event )
    {
        // Now we should have only an atomic type to validate

        assert type.getSimpleVariety() == SchemaType.ATOMIC;

        // Record the current error state to see if any new errors are made
        int errorState = _errorState;
        _vc._event = event;

        switch ( type.getPrimitiveType().getBuiltinTypeCode() )
        {
        case SchemaType.BTC_ANY_SIMPLE :
        {
            // Always valid!
            break;
        }
        case SchemaType.BTC_STRING :
        {
            JavaStringEnumerationHolderEx.validateLexical( value, type, _vc );
            break;
        }
        case SchemaType.BTC_DECIMAL :
        {
            JavaDecimalHolderEx.validateLexical( value, type, _vc );

            if (errorState == _errorState)
            {
                JavaDecimalHolderEx.validateValue(
                    new BigDecimal( value ), type, _vc );
            }

            break;
        }
        case SchemaType.BTC_BOOLEAN :
        {
            JavaBooleanHolderEx.validateLexical( value, type, _vc );
            break;
        }
        case SchemaType.BTC_FLOAT :
        {
            float f =
                JavaFloatHolderEx.validateLexical( value, type, _vc );

            if (errorState == _errorState)
                JavaFloatHolderEx.validateValue( f, type, _vc );

            break;
        }
        case SchemaType.BTC_DOUBLE :
        {
            double d =
                JavaDoubleHolderEx.validateLexical( value, type, _vc );

            if (errorState == _errorState)
                JavaDoubleHolderEx.validateValue( d, type, _vc );

            break;
        }
        case SchemaType.BTC_QNAME :
        {
            QName n =
                JavaQNameHolderEx.validateLexical(
                    value, type, _vc, event );

            if (errorState == _errorState)
                JavaQNameHolderEx.validateValue( n, type, _vc );

            break;
        }
        case SchemaType.BTC_ANY_URI :
        {
            JavaUriHolderEx.validateLexical( value, type, _vc );

            break;
        }
        case SchemaType.BTC_DATE_TIME :
        case SchemaType.BTC_TIME :
        case SchemaType.BTC_DATE :
        case SchemaType.BTC_G_YEAR_MONTH :
        case SchemaType.BTC_G_YEAR :
        case SchemaType.BTC_G_MONTH_DAY :
        case SchemaType.BTC_G_DAY :
        case SchemaType.BTC_G_MONTH :
        {
            GDate d = XmlDateImpl.validateLexical( value, type, _vc );

            if (d != null)
                XmlDateImpl.validateValue( d, type, _vc );

            break;
        }
        case SchemaType.BTC_DURATION :
        {
            GDuration d = XmlDurationImpl.validateLexical( value, type, _vc );

            if (d != null)
                XmlDurationImpl.validateValue( d, type, _vc );

            break;
        }
        case SchemaType.BTC_BASE_64_BINARY :
        {
            byte[] v =
                JavaBase64HolderEx.validateLexical( value, type, _vc );

            if (v != null)
                JavaBase64HolderEx.validateValue( v, type, _vc );

            break;
        }
        case SchemaType.BTC_HEX_BINARY :
        {
            byte[] v =
                JavaHexBinaryHolderEx.validateLexical( value, type, _vc );

            if (v != null)
                JavaHexBinaryHolderEx.validateValue( v, type, _vc );

            break;
        }
        case SchemaType.BTC_NOTATION :
            // Unimplemented. 
            break;

        default :
            throw new RuntimeException( "Unexpected primitive type code" );
        }
    }

    private void validateListType (
        SchemaType type, String value, Event event )
    {
        int errorState = _errorState;

        if (!type.matchPatternFacet( value ))
        {
            emitError(
                event,
                "List '" + value + "' does not match pattern for " + QNameHelper.readable(type) );
        }

        String[] items = XmlListImpl.split_list(value);

        
        int i;
        XmlObject o;

        if ((o = type.getFacet( SchemaType.FACET_LENGTH )) != null)
        {
            if ((i = ((SimpleValue)o).getIntValue()) != items.length)
            {
                emitError(
                    event,
                    "List (" + value + ") does not have " + i +
                        " items per length facet for " + QNameHelper.readable(type));
            }
        }

        if ((o = type.getFacet( SchemaType.FACET_MIN_LENGTH )) != null)
        {
            if ((i = ((SimpleValue)o).getIntValue()) > items.length)
            {
                emitError(
                    event,
                    "List (" + value + ") has only " + items.length +
                        " items, fewer than min length facet (" + i + ") for " + QNameHelper.readable(type) );
            }
        }
        
        if ((o = type.getFacet( SchemaType.FACET_MAX_LENGTH )) != null)
        {
            if ((i = ((SimpleValue)o).getIntValue()) < items.length)
            {
                emitError(
                    event,
                    "List (" + value + ") has " + items.length +
                        " items, more than max length facet (" + i + ") for " + QNameHelper.readable(type) );
            }
        }

        SchemaType itemType = type.getListItemType();

        for ( i = 0 ; i < items.length ; i++ )
        {
            validateSimpleType(
                itemType, items[i], event );
        }

        // If no errors up to this point, then I can create an
        // XmlList from this value and campare it again enums.

        if (errorState == _errorState)
        {
            if (type.getEnumerationValues() != null)
            {
                // Lists which contain QNames will need a resolver

                NamespaceContext.push(
                    new NamespaceContext( event ) );

                try
                {
                    XmlObject listValue = ( (SchemaTypeImpl) type).newValidatingValue( value );
                }
                catch (XmlValueOutOfRangeException e)
                {
                    emitError(
                        event,
                        "List value (" + value +
                            ") is not a valid enumeration value for " + QNameHelper.readable(type));
                }
                finally
                {
                    NamespaceContext.pop();
                }
            }
        }
    }

    private void validateUnionType (
        SchemaType type, String value, Event event )
    {
        // TODO - if xsi:type is specified on a union, it selects
        // that union member type

        if (!type.matchPatternFacet( value ))
        {
            emitError(
                event,
                "Union '" + value + "' does not match pattern for " + QNameHelper.readable(type));
        }

        int currentWsr = SchemaType.WS_PRESERVE;
        String currentValue = value;

        SchemaType[] types = type.getUnionMemberTypes();

        int originalState = _errorState;

        int i;
        for ( i = 0 ; i < types.length ; i++ )
        {
            int memberWsr = types[ i ].getWhiteSpaceRule();

            if (memberWsr == SchemaType.WS_UNSPECIFIED)
                memberWsr = SchemaType.WS_PRESERVE;

            if (memberWsr != currentWsr)
            {
                currentWsr = memberWsr;
                currentValue = XmlWhitespace.collapse( value, currentWsr );
            }

            int originalErrorState = _errorState;

            _suspendErrors++;

            try
            {
                validateSimpleType( types[ i ], currentValue, event );
            }
            finally
            {
                _suspendErrors--;
            }

            if (originalErrorState == _errorState)
                break;
        }

        _errorState = originalState;

        if (i >= types.length)
        {
            emitError(
                event,
                "Union '" + value + "' does not match any members of " + QNameHelper.readable(type) );
        }
        else
        {
            XmlObject[] unionEnumvals = type.getEnumerationValues();

            if (unionEnumvals != null)
            {
                // Unions which contain QNames will need a resolver

                NamespaceContext.push( new NamespaceContext( event ) );

                try
                {
                    XmlObject unionValue = type.newValue( value );

                    for ( i = 0 ; i < unionEnumvals.length ; i++ )
                    {
                        if (unionValue.valueEquals( unionEnumvals[ i ] ))
                            break;
                    }

                    if (i >= unionEnumvals.length)
                    {
                        emitError(
                            event,
                            "Union '" + value +
                                "' is not a valid enumeration value for " + QNameHelper.readable(type) );
                    }
                }
                catch (XmlValueOutOfRangeException e)
                {
                    // actually, the current union code always ends up here when invalid
                    emitError(
                        event,
                        "Union '" + value +
                            "' is not a valid enumeration value for " + QNameHelper.readable(type) );
                }
                finally
                {
                    NamespaceContext.pop();
                }
            }
        }
    }


    //
    // Members of the validator class
    //

    private boolean            _invalid;
    private SchemaType         _rootType;
    private SchemaField        _rootField;
    private SchemaTypeLoader   _globalTypes;
    private Chars              _chars;
    private State              _stateStack;
    private int                _errorState;
    private Collection         _errorListener;
    private ValidatorVC        _vc;
    private int                _suspendErrors;
    private IdentityConstraint _constraintEngine;
    private int                _eatContent;
}
