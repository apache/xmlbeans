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

import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlCursor.XmlMark;
import org.apache.xmlbeans.XmlCursor;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.xml.namespace.QName;

public abstract class Splay extends Goobers
{
    //
    // Can't change the kind of a base token
    //

    Splay ( int kind, boolean is )
    {
        _bits = kind;

        if (is)
            _bits += 0x8;
    }

    //
    // Splay kinds
    //

    static final int DOC      =  0; // DOC must be 0
    static final int BEGIN    =  1; // BEGIN must be 1
    static final int ATTR     =  2;
    static final int COMMENT  =  3;
    static final int PROCINST =  4;
    static final int ROOT     =  5; // ROOT must be second to last
    static final int END      =  6; // END must be last

    //
    //
    //

    final int getKind ( ) { return _bits & 0x7; }

    final boolean isDoc       ( ) { return getKind() == DOC;      }
    final boolean isRoot      ( ) { return getKind() == ROOT;     }
    final boolean isBegin     ( ) { return getKind() == BEGIN;    }
    final boolean isEnd       ( ) { return getKind() == END;      }
    final boolean isAttr      ( ) { return getKind() == ATTR;     }
    final boolean isComment   ( ) { return getKind() == COMMENT;  }
    final boolean isProcinst  ( ) { return getKind() == PROCINST; }
    final boolean isContainer ( ) { return getKind() <= BEGIN;    }
    final boolean isFinish    ( ) { return getKind() >= ROOT;     }

    final int getCch           ( ) { return _cch;              }

    final int getCdocBegin     ( ) { return 1;                 }
    final int getCchLeft       ( ) { return _cchLeft;          }
    final int getCdocBeginLeft ( ) { return _bits >> 5;        }
    final int getCchAfter      ( ) { return _cchAfter;         }
    final int getCchValue      ( ) { return _cch - _cchAfter;  }

    QName  getName  ( ) { throw new IllegalStateException(); }
    String getUri   ( ) { throw new IllegalStateException(); }
    String getLocal ( ) { throw new IllegalStateException(); }

    // Fourth bit in _bits has several meanings, depending on splay kind

    final boolean isNormalAttr ( ) { return (_bits & 0xF) == (0x0 + ATTR); }
    final boolean isXmlns      ( ) { return (_bits & 0xF) == (0x8 + ATTR); }
    final boolean isLeaf       ( ) { return (_bits & 0xF) == (0x8 + BEGIN); }
    final boolean isFragment   ( ) { return (_bits & 0xF) == (0x8 + COMMENT); }
    final boolean isXsiAttr    ( ) { return isNormalAttr() && getUri().equals( _xsi ); }
    final boolean isXsiNil     ( ) { return isNormalAttr() && getName().equals(_xsiNil ); }
    final boolean isXsiType    ( ) { return isNormalAttr() && getName().equals( _xsiType ); }
    final boolean isTypeable   ( ) { return isContainer() || isNormalAttr(); }

    final void toggleIsLeaf ( )
    {
        assert isBegin();
        _bits ^= 0x8;
    }

    final boolean isValid ( )
    {
        assert ((_bits & 0x10) == 0) || isTypeable();
        return (_bits & 0x10) == 0;
    }

    final boolean isInvalid ( )
    {
        assert ((_bits & 0x10) == 0) || isTypeable();
        return (_bits & 0x10) != 0;
    }

    final void toggleIsInvalid ( )
    {
        assert isTypeable();
        _bits ^= 0x10;
    }

    final void adjustCch ( int delta )
    {
        _cch += delta;
        assert _cch >= 0;
    }

    final void adjustCchAfter ( int delta )
    {
        _cchAfter += delta;
        assert _cchAfter >= 0;
    }

    final void adjustCchLeft ( int delta )
    {
        _cchLeft += delta;
        assert _cchLeft >= 0;
    }

    final void adjustCdocBeginLeft ( int d )
    {
        _bits += d * 32;
        assert getCdocBeginLeft() >= 0;
    }

    final Splay getFinishSplay ( )
    {
        assert isContainer();
        return isLeaf() ? this : ((Container) this).getFinish();
    }

    final int getPosAfter ( )
    {
        return getEndPos() - getCchAfter();
    }

    final int getPosLeafEnd ( )
    {
        assert isLeaf();
        return 1 + getCchValue();
    }

    final int getCpForPos ( Root r, int p )
    {
        int cp = r.getCp( this );

        if (p == 0)
            return cp;

        return
            isLeaf()
                ? p - (p <= getPosLeafEnd() ? 1 : 2) + cp
                : p - 1 + getCchValue() + cp;
    }

    final int getPostCch ( int p )
    {
        assert p >= 0 && p <= getEndPos();

        return
            isLeaf() && p <= getPosLeafEnd()
                ? getPosLeafEnd() - p
                : getEndPos() - p;
    }

    static final String _xsi =
        "http://www.w3.org/2001/XMLSchema-instance";

    static final String _schema =
        "http://www.w3.org/2001/XMLSchema";

    static final String _openFragUri =
        "http://www.openuri.org/fragment";

    static final String _xml1998Uri =
        "http://www.w3.org/XML/1998/namespace";

    static final String _xmlnsUri =
        "http://www.w3.org/2000/xmlns/";

    static final QName _xsiNil  = new QName( _xsi, "nil" );
    static final QName _xsiType = new QName( _xsi, "type" );
    static final QName _openuriFragment = new QName( _openFragUri, "fragment" );
    static final QName _xmlFragment = new QName( "", "xml-fragment" );

    static final boolean isXmlFragment ( QName name )
    {
        return _openuriFragment.equals( name ) || _xmlFragment.equals( name );
    }

    static boolean beginsWithXml ( String name )
    {
        if (name.length() < 3)
            return false;

        char ch;

        if (((ch = name.charAt( 0 )) == 'x' || ch == 'X') &&
                ((ch = name.charAt( 1 )) == 'm' || ch == 'M') &&
                ((ch = name.charAt( 2 )) == 'l' || ch == 'L'))
        {
            return true;
        }

        return false;
    }

    /**
     * This returns the pos of the very end of the splay.  This is not
     * generally a valid position to exist because in these cases, I
     * force the position to be 0 on the next splay.
     */

    final int getEndPos ( )
    {
        return getMaxPos() + 1;
    }

    /**
     * This returns the largest pos possible without actually being at
     * the end of the splay.
     */

    final int getMaxPos ( )
    {
        switch ( getKind() )
        {
        case ATTR     :
        case ROOT     : return 0;
        case BEGIN    : return (isLeaf() ? 1 : 0) + getCch();
        case DOC      :
        case COMMENT  :
        case PROCINST :
        case END      : return getCchAfter();

        default :
            assert false: "Unexpected splay kind " +getKind();
            return 0;
        }
    }

    static final boolean isWhiteSpace ( char ch )
    {
        switch ( ch )
        {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }

    static final boolean isWhiteSpace ( String s )
    {
        int l = s.length();

        while ( l-- > 0)
            if (!isWhiteSpace( s.charAt( l )))
                  return false;

        return true;
    }

    static final boolean isWhiteSpace ( StringBuffer sb )
    {
        int l = sb.length();

        while ( l-- > 0)
            if (!isWhiteSpace( sb.charAt( l )))
                  return false;

        return true;
    }

    final boolean isAfterWhiteSpace ( Root r )
    {
        int cchAfter = getCchAfter();

        if (cchAfter == 0)
            return true;

        int off =
            r._text.unObscure(
                r.getCp( this ) + getCch() - cchAfter, cchAfter );

        while ( cchAfter-- > 0 )
            if (!isWhiteSpace( r._text._buf[ off + cchAfter ] ))
                return false;

        return true;
    }

    final void setName ( Root r, QName newName )
    {
        assert isBegin() || isAttr() || isProcinst();

        // BUGBUG - deal with xsi:nil here too

        QName oldName = getName();

        if (!newName.equals( oldName ))
        {
            r.startChange();

            ((QNameSplay) this).changeName( newName );

            if (!isProcinst() && !isXmlns())
            {
                assert isAttr() || isBegin();

                if (isBegin())
                    disconnectTypes( r );

                if (_xsiType.equals( oldName ) || _xsiType.equals( newName ))
                    getContainer().disconnectTypes( r );
                else if (_xsiNil.equals( oldName ) || _xsiNil.equals( newName ))
                    getContainer().invalidateNil();
                else
                    disconnectTypes( r );
            }

            r.invalidateVersion();
        }
    }

    /**
     * If this is an invalid leaf, then refresh it's value and return the
     * number of characters added.
     */

    final int ensureContentValid ( )
    {
        assert isContainer();

        if (isValid())
            return 0;

        Type t = peekType();

        return t.validateContent( this, t );
    }

    /**
      * Ensure that the value (ATTR, COMMENT and PROCINST) is valid.  Really
      * only does anything for attrs.
      */

    final void ensureValueValid ( )
    {
        assert isNormalAttr();

        if (isInvalid())
        {
            Type t = peekType();
            t.validateValue( this, t );
        }
    }

    /**
     * Given a position in this splay, return the TokenType associated with
     * the content to the left of the position.
     */

    final TokenType getTokenType ( int p )
    {
        assert p >= 0 && p <= getMaxPos();

        // No need to revalidate text value here because there can be no pos
        // which could exists when this splay is invalid which could be on
        // text.

        // First, see if we're in/at the text after the splay

        if (p >= getPosAfter())
            return TokenType.TEXT;

        switch ( getKind() )
        {
        case ATTR :
            return isXmlns() ? TokenType.NAMESPACE : TokenType.ATTR;

        case BEGIN :
            if (p == 0)
                return TokenType.START;

            assert isLeaf();

            return p == getPosLeafEnd() ? TokenType.END : TokenType.TEXT;

        case ROOT     : return TokenType.ENDDOC;
        case DOC      : return TokenType.STARTDOC;
        case COMMENT  : return TokenType.COMMENT;
        case PROCINST : return TokenType.PROCINST;
        case END      : return TokenType.END;

        default :
            assert false: "Unexpected splay kind " + getKind();
            return null;
        }
    }

    /**
     * Cause this splay to become a leaf.  This does not logically
     * alter the document, but does alter the physical splay tree which
     * demands that I issue a change notice.
     */

    final void foliate ( Root r )
    {
        assert isBegin();
        assert !isLeaf();

        Begin b = (Begin) this;
        Splay e = b.getFinish();

        assert b.nextNonAttrSplay() == e;
        assert b._end == e;
        assert e.isEnd();
        assert !b.isLeaf();

        // Caller must initiate change
        assert r.validateChangeStarted();

        int cchBefore = getCch();

        e.saveTextAfter( r );

        b._end = null;
        b.toggleIsLeaf();

        b.adjustCchAfter( - cchBefore );

        // Move the goobers from the end to the begin

        Goober nextGoober;
        for ( Goober g = e.firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = e.nextGoober( g );

            assert g.getPos() == 0;
            g.set( this, cchBefore + 1 );
        }

        for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = nextGoober( g );

            int gp = g.getPos();

            if (gp > cchBefore + 1)
                g.set( this, gp + 1 );
        }

        e.removeSplay( r );
    }

    final void defoliate ( Root r )
    {
        assert isLeaf();

        // Caller must initiate change
        assert r.validateChangeStarted();

        // Caller must ensure leaf is valid
        assert isValid();

        Begin b = (Begin) this;

        int posLeafEnd = getPosLeafEnd();
        int cchAfter = getCchAfter();
        int cchValue = getCchValue();

        Splay a = this;

        int cchAttr = 0;

        for ( Splay t = nextSplay() ; t.isAttr() ; t = t.nextSplay() )
        {
            cchAttr += t.getCch();
            a = t;
        }

        r.insertSplay( b._end = new End( (Begin) this ), a );

        b.toggleIsLeaf();

        b.adjustCchAfter( cchValue - cchAfter );
        b._end.adjustCchAfter( cchAfter );

        r.updateCch( b, - cchAfter );
        r.updateCch( b._end, cchAfter );

        if (cchAttr > 0)
        {
            int cp = r.getCp( b );

            r._text.move(
                cp + cchValue + cchAfter + cchAttr,
                r._text, cp + cchValue, cchAfter );
        }

        Goober nextGoober;
        for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = nextGoober( g );

            int gp = g.getPos();

            if (gp >= posLeafEnd)
                g.set( b._end, gp - posLeafEnd );
        }

        assert validate();
    }

    /**
     * Remove the text after *this* splay.
     * No validation is done, caller must do that
     */

    private final void removeTextAfter ( Root r )
    {
        assert r != null;
        assert Root.dv > 0 || getRootSlow() == r;
        assert !isRoot() && !isAttr();

        // Caller must initiate change
        assert r.validateChangeStarted();

        int cchTextAfter = getCchAfter();

        if (cchTextAfter == 0)
            return;

        int cpTextAfter = r.getCp( this ) + getCch() - cchTextAfter;
        int posTextAfter = getPosAfter();

        adjustCchAfter( - cchTextAfter );

        r.updateCch( this, - cchTextAfter );

        r._text.remove( cpTextAfter, cchTextAfter );

        Goober nextGoober;
        for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = nextGoober( g );

            int k = g.getKind();

            if (g.getPos() >= posTextAfter)
            {
                if (k == ANNOTATION)
                    g.disconnect( r );
                else if (k == CURSOR)
                    g.set( nextSplay(), 0 );
            }
        }

        assert validate();
    }

    /**
     * Transfers the text after this "node" to be after the "node" before it.
     * No type invalidation is done here.  No checking for isInvalid is needed
     * either.  No validation is done, caller must do that.
     */

    private final int saveTextAfter ( Root r )
    {
        assert r != null;
        assert Root.dv > 0 || getRootSlow() == r;

        // Caller must initiate change
        assert r.validateChangeStarted();

        Splay endText = this;

        if (isBegin() && !isLeaf())
            endText = getFinishSplay();

        int cchEndText = endText.getCchAfter();

        if (cchEndText == 0)
            return 0;

        assert !isRoot() && !isDoc() && !isAttr();

        Splay getsText = prevNonAttrSplay();
        int getsTextLastPos = getsText.getEndPos();

        int cpGetsText = r.getCp( getsText ) + getsText.getCch();
        int cpEndText = r.getCp( endText ) + endText.getCch() - cchEndText;

        getsText.adjustCchAfter( cchEndText );
        endText.adjustCchAfter( - cchEndText );

        r.updateCch( getsText, cchEndText );
        r.updateCch( endText, - cchEndText );

        assert cpGetsText <= cpEndText;

        if (cpGetsText != cpEndText)
        {
            r._text.insert( cpGetsText, r._text, cpEndText, cchEndText );
            r._text.remove( cpEndText + cchEndText, cchEndText );
        }

        int posEndText = endText.getPosAfter();

        Goober nextGoober;
        for ( Goober g = endText.firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = endText.nextGoober( g );

            int gp = g.getPos();

            if (gp >= posEndText)
                g.set( getsText, getsTextLastPos + gp - posEndText );
        }

        assert validate();

        return cchEndText;
    }

    /**
     * Moves this splay, all of its content and attributes.  If no destination
     * is provided, then only a removal is performed.
     */

    final void move (
        Root r, Root rDst, Splay sDst, int pDst, boolean invalidate )
    {
        assert r != null;
        assert Root.dv > 0 || getRootSlow() == r;
        assert !isDoc() && !isFinish();

        assert rDst == null || (r != rDst || !sDst.between( r, pDst, this ) );
        assert rDst == null || sDst != null;
        assert rDst == null || Root.dv > 0 || sDst.getRootSlow() == rDst;
        assert rDst == null || pDst >= 0 && pDst < sDst.getEndPos();

        assert
            rDst == null ||
                (true || checkInsertionValidity( 0, sDst, pDst, false ));

        r.startChange();

        if (rDst != null)
            rDst.startChange();

        // The text after the logical entity to move/remove is not part of
        // this operation.  Transfer it to a previous splay.  No need to
        // fret about invalidity here, because it's text *after*.

        int cchSaved = saveTextAfter( r );

        // If any text was saved, I need to move the cursors at the beginning
        // of this splay to where the text was saved.  I do this because this
        // content is going away and cursors need to collapse to the right
        // location which, in this case, is before the after text.
        //
        // No need to deal with goobers other than cursor because annotations
        // and types will be dealt with appropriately later.
        //
        // Also, update the target if it was in the moved text!
        //
        // TODO - if moving xsi:type or xsi:nil must do stuff

        if (cchSaved > 0)
        {
            Splay gotText = prevNonAttrSplay();

            Goober nextGoober;
            for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
            {
                nextGoober = nextGoober( g );

                if (g.getKind() == CURSOR)
                    g.set( gotText, gotText.getEndPos() - cchSaved );
            }

            if (rDst != null)
            {
                Splay hadText =
                    isBegin() && !isLeaf() ? getFinishSplay() : this;

                if (sDst == hadText && pDst >= hadText.getEndPos())
                {
                    sDst = gotText;

                    pDst =
                        gotText.getEndPos() -
                            (cchSaved - (pDst - hadText.getEndPos()));
                }
            }
        }

        assert sDst != this;

        // Compute the splay, up to which, I will remove

        Splay last =
            isBegin() ? getFinishSplay().nextNonAttrSplay() : nextSplay();

        //
        //
        //

        Container container = getContainer();

        int cp = r.getCp( this );
        int cch = 0;

        for ( Splay s = this ; s != last ; s = s.nextSplay() )
        {
            // I'm going to blow away the Types here, make sure I have
            // all the text.

            if (s.isInvalid())
            {
                assert s.isLeaf() || s.isNormalAttr();

                if (s.isNormalAttr())
                    s.ensureValueValid();
                else if (s.isLeaf())
                    s.ensureContentValid();
            }

            cch += s.getCch();

            Goober nextGoober;
            for ( Goober g = s.firstGoober() ; g != null ; g = nextGoober )
            {
                nextGoober = s.nextGoober( g );

                switch ( g.getKind() )
                {
                case TYPE :
                    g.disconnect( r );
                    break;

                case ANNOTATION :
                    if (rDst == null)
                        g.disconnect( r );
                    else
                        g.set( rDst ); // Potential move to a new tree
                    break;

                case CURSOR :
                    g.set( last, 0 );
                    break;
                }
            }
        }

        // Remove the splays and the text.  Unfortunately, I need to remove
        // the text here.  I can't think of a way to keep the tree valid and
        // have the text moved in one copy .... Can you?

        Splay sRemoved = r.removeSplays( this, last );

        Splay first =
            sRemoved._leftSplay == null ? sRemoved : sRemoved._leftSplay;

        if (first.isBegin())
            ((Begin) first)._container = null;

        char[] textRemoved = rDst == null || cch == 0 ? null : new char [ cch ];

        r._text.remove( cp, cch, textRemoved, 0 );

        r.invalidateVersion();

        if (rDst != null)
        {
            sDst.insert(
                rDst, pDst, sRemoved, textRemoved, 0, cch, invalidate );
        }

        if (invalidate)
        {
            Type parentType = container.peekType();

            if (parentType != null)
            {
                if (isBegin())
                    parentType.invalidateElement( container, last );
                else if (isAttr() && isXsiNil())
                    parentType.invalidateNil();
            }
        }

        assert r.validate();
        assert rDst == null || rDst.validate();
    }

    /**
     * Removes this splay, all of its content and attributes.
     */

    final void remove ( Root r, boolean invalidate )
    {
        move( r, null, null, 0, invalidate );
    }

    private final void complain ( String msg )
    {
        throw new IllegalArgumentException( msg );
    }

    /**
     * Check to if the insertion of a "node" into the destination is allowed.
     * This assumes that the source is a single piece of contents (like a node).
     * Fragments are strings of content, not contained, and must not be
     * validated by this function.
     *
     * Returns true if the src to copy is characters (potentially 0 chars too)
     */

    final boolean checkInsertionValidity (
        int p, Splay sDst, int pDst, boolean endOk )
    {
        assert p >= 0;
        assert p < getEndPos();
        assert pDst >= 0;
        assert pDst < sDst.getEndPos();

        boolean srcChars = false;

        if (p > 0)
        {
            if (!endOk && isLeaf() && p == getPosLeafEnd())
                complain( "Can't move/copy/insert an end token." );

            srcChars = true;

            // valid text target will be tested for further down
        }
        else
        {
            if (isDoc())
                complain( "Can't move/copy/insert a whole document." );

            if (isFinish())
            {
                if (!endOk)
                    complain( "Can't move/copy/insert an end token." );

                srcChars = true;
            }
            else if (isFragment())
                srcChars = true;
        }

        if (pDst == 0 && sDst.isDoc())
            complain( "Can't insert before the start of the document." );

        if (p > 0 && sDst.isAttr())
            complain( "Can't insert text before an attribute." );

        if (isAttr() && pDst > 0 && (!sDst.isContainer() || pDst != 1))
            complain( "Can't insert an attribute in text." );

        if (p <= 0 && pDst <= 0)
        {
            if (isAttr())
            {
                boolean isOk = sDst.isAttr();

                if (!isOk)
                {
                    Splay t = sDst.prevNonAttrSplay();

                    if (t.isContainer() && t.getCchAfter() == 0)
                        isOk = true;
                }

                if (!isOk)
                {
                    complain(
                        "Can only move/copy/insert attribute after another " +
                          "attribute or start token." );
                }
            }
            else if (sDst.isAttr())
            {
                complain(
                    "Can't move/copy/insert a non-attribute before " +
                        "an attribute." );
            }
        }

        return srcChars;
    }

    /**
     * Insert in this splay at position p whose root is r the splays rootInsert
     * with text txt at offset cpTxt and length cchTxt
     */

    final void insert (
        Root r, int p, Splay rootInsert,
        Object txt, int offTxt, int cchTxt,
        boolean invalidate )
    {
// BUGBUG - what is a new xsitype attr is inserted? -- must disconnect types

        assert p < getEndPos();
        assert cchTxt >= 0;
        assert rootInsert != null;
        assert rootInsert._parentSplay == null;
        assert rootInsert.getCchLeft() + rootInsert.getCch() == cchTxt;

        // We are inserting a tree here.  Get the first splay in the tree
        // It must either be at the root or just to the left of the root.
        // This is the "classic" dog leg pattern I use to pass splay trees.

        assert
            rootInsert._leftSplay == null ||
                rootInsert._leftSplay._leftSplay == null;

        r.startChange();

        Splay sInsert =
            rootInsert._leftSplay == null ? rootInsert : rootInsert._leftSplay;

        assert !sInsert.isDoc() && !sInsert.isFinish();
        assert Root.dv > 0 || getRootSlow() == r;

        // When this member is called, we better be able to do the insert.

        assert true || sInsert.checkInsertionValidity( 0, this, p, false );

        // If there is a fragment, there better be text in it

        assert !sInsert.isFragment() || sInsert.getCch() > 0;

        //

        Splay s = this;

        assert Root.dv > 0 || sInsert.getRootSlow() == null;

        Splay sOrig = null;
        int   pOrig = 0;
        int   cchBypassed = 0;

        // Prepare for inserting into the middle of a splay

        if (p > 0)
        {
            int ple;

            if (s.isLeaf() && p <= (ple = s.getPosLeafEnd()))
            {
                if (s.isInvalid())
                {
                    assert ple == 1;
                    assert p == 1;

                    int cchValid = s.ensureContentValid();

                    p += cchValid;
                    ple += cchValid;
                }

                assert s.getFinishSplay() == s;

                defoliate( r );

                assert s.getFinishSplay() != s;

                if (p == ple)
                {
                    s = getFinishSplay();
                    p = 0;
                }

                s.insert( r, p, rootInsert, txt, offTxt, cchTxt, invalidate );

                return;
            }

            // You can copy an attr to a dst where it is the first attr, or
            // after all pre-exisitng attrs.  This these cases, p == 1 on
            // the begin.  In this case, there is no need to move text later.

            assert !sInsert.isAttr() || (p == 1 && s.isContainer());

            sOrig = s;
            pOrig = p;
            cchBypassed = getEndPos() - p;

            s = s.nextNonAttrSplay();
            p = 0;
        }

        assert p == 0;
        assert !s.isDoc();

        // Get the container receiving all of this

        Container c = s.getContainer();

        // Run over the top level content, parenting any BEGINS.  Also,
        // characterize the content being inserted for future invalidation.
        // Only fragments will have multiple top level begins.

        boolean insertingElements = false;
        boolean insertingText = false;
        boolean insertingNilAttr = false;
        Splay   insertingFragment = null;

        for ( Splay t = sInsert ; t != null ; t = t.nextSplay() )
        {
            if (t.isBegin())
            {
                insertingElements = true;

                Begin b = (Begin) t;

                assert b._container == null;

                b._container = c;

                t = t.getFinishSplay();
            }
            else if (t.isFragment())
            {
                assert insertingFragment == null;
                insertingFragment = t;
            }

            if (t.isXsiNil())
                insertingNilAttr = true;

            if (t.getCchAfter() > 0)
                insertingText = true;
        }

        // Now, insert the text (if any)

        r._text.insert( r.getCp( s ), txt, offTxt, cchTxt );

        // Insert the tree

        r.insertSplay( rootInsert, s.prevSplay() );

        // If the first splay to isnert was a fragment, remove it, leaving
        // the text it carried in the tree.  Don't perform invalidation.

        if (insertingFragment != null)
            insertingFragment.remove( r, false );

        // Invalidate the parent if requested

        if (invalidate)
        {
            Type cType = c.peekType();

            if (cType != null)
            {
                if (insertingElements)
                {
                    cType.invalidateElement( c, s );
                    insertingText = true;
                }

                if (insertingText)
                    cType.invalidateText();

                if (insertingNilAttr)
                    cType.invalidateNil();
            }
        }

        // If there is post text to move, move it to the end of the stuff
        // inserted.  Don't do this is if an attr was inserted because in this
        // case, the attr is logically after the text to be moved anyways.

        if (sOrig != null && !sInsert.isAttr())
        {
            int cchMoved = sOrig.moveChars( r, pOrig, -1, r, s, 0, true );
            assert cchMoved > 0;
            assert cchMoved == cchBypassed;
        }

        // build a leaf if possible

        if (s.isEnd() && s.getContainer().nextNonAttrSplay() == s)
            s.getContainer().foliate( r );

        r.invalidateVersion();

        assert r.validate();
    }

    /**
     * Replaces the contents of this (attributes as well) with a copy of the
     * contents of sSrc.  If preserveType is true, then the xsi:type attr
     * of the source is not copied, and the xsi:type of the target (this)
     * is not altered.
     */

    final void replaceContents (
        Root r, Splay sSrc, Root rSrc,
        boolean preserveType, boolean preserveNamespaces )
    {
        assert !isFinish() && !sSrc.isFinish();
        assert Root.dv > 0 || getRootSlow() == r;
        assert Root.dv > 0 || sSrc.getRootSlow() == rSrc;

        // If the src and dst are the same splay, then there is nothing to do

        if (this == sSrc)
            return;

        // If we are to preserve namespace mappings, make a copy of the
        // namspaces in scope here

        Map sourceNamespaces =
            preserveNamespaces ? sSrc.lookupAllPrefixMappings() : null;

        // If the destination is a simple container of text, then the content
        // model is limited to text.  Handle this specially.

        if (isAttr() || isComment() || isProcinst())
        {
            // TODO - Do this without creating a string?
            String str = sSrc.getText( rSrc );
            setText( r, str, 0, str.length() );
        }
        else
        {
            // No need to startChange() here, fcns I call will do it for me

            assert isContainer();

            // Make copy of source

            CopyContext copyContext = sSrc.copySplayContents( rSrc );

            // If we need to preserve the xsi:type attribute, then fetch it.
            // If there is more than one, the first one wins.

            String targetXsiType = null;

            if (preserveType)
            {
                for ( Splay s = nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                {
                    if (s.isXsiType())
                    {
                        targetXsiType = s.getText( r );
                        break;
                    }
                }
            }

            // Because I have removed the entire contents of the target, I can
            // insert fragment or node.

            removeContent( r, true );

            assert isLeaf() || getFinishSplay() == nextSplay();
            assert getCchValue() == 0;

            Splay copy = copyContext.getTree();

            if (copy != null)
            {
                // Need to compute a normalized splay/pos to insert at.  Only
                // two cases, either we're inserting in a leaf or a
                // container/finish pair.

                Splay insertSplay = this;
                int insertPos = 1;

                if (!isLeaf())
                {
                    insertSplay = nextSplay();
                    insertPos = 0;
                }

                char[] textCopy = copyContext.getText();

                insertSplay.insert(
                    r, insertPos, copy,
                    textCopy, 0, textCopy == null ? 0 : textCopy.length, true );

                // Now, go thorugh all the attrs, removing types and place the
                // original back in.

                if (preserveType)
                {
                    Splay next = nextSplay();

                    for ( Splay s = next ; s.isAttr() ; s = next )
                    {
                        next = s.nextSplay();

                        if (s.isXsiType())
                            s.remove( r, true );
                    }

                    if (targetXsiType != null)
                    {
                        Attr a = new Attr( _xsiType );

                        int cchType = targetXsiType.length();

                        a.adjustCch( cchType );

                        if (getEndPos() > 1)
                            insert( r, 1, a, targetXsiType, 0, cchType, true );
                        else
                        {
                            nextNonAttrSplay().insert(
                                r, 0, a, targetXsiType, 0, cchType, true );
                        }
                    }
                }
            }
        }

        if (sourceNamespaces != null)
            applyNamespaces( r, sourceNamespaces );
    }

    static final class CopyContext
    {
        void copyText ( char[] text )
        {
            _text = text;
        }

        void copySplay ( Splay s, boolean copyTextAfter )
        {
            assert !s.isDoc() && !s.isRoot();

            Splay t;

            switch ( s.getKind() )
            {
            case BEGIN :
            {
                s.ensureContentValid();

                t = _frontier = new Begin( s.getName(), _frontier );

                if (s.isLeaf())
                {
                    t.toggleIsLeaf();
                    _frontier = _frontier.getContainer();
                }

                break;
            }
            case ATTR :
            {
                if (s.isXmlns())
                    t = new Xmlns( s.getName() );
                else
                {
                    s.ensureValueValid();
                    t = new Attr( s.getName() );
                }
                break;
            }
            case COMMENT :
            {
                t = new Comment();
                break;
            }
            case PROCINST :
            {
                t = new Procinst( s.getName() );
                break;
            }
            case END :
            {
                Begin b = (Begin) _frontier;
                t = b._end = new End( b );
                _frontier = _frontier.getContainer();
                break;
            }
            default :
                throw new IllegalStateException();
            }

            assert s.isValid();

            int cch = s.getCchValue();

            if (copyTextAfter)
            {
                int cchAfter = s.getCchAfter();
                cch += cchAfter;
                t.adjustCchAfter( cchAfter );
            }

            if (cch > 0)
                t.adjustCch( cch );

            copy( t );
        }

        /**
         * May be called once before any other copy
         */

        void copyFragment ( int cch )
        {
            assert cch > 0;

            Splay s = new Fragment();

            s.adjustCchAfter( cch );
            s.adjustCch( cch );

            copy( s );
        }

        Splay getTree ( )
        {
            assert _frontier == null;

            if (_last == null)
                return null;

            if (_first != null)
            {
                _last.adjustCchLeft( _first.getCchLeft() + _first.getCch() );

                _last.adjustCdocBeginLeft(
                    _first.getCdocBeginLeft() + _first.getCdocBegin() );

                _last._leftSplay = _first;
                _first._parentSplay = _last;

                if (_tail != null)
                {
                    _last.adjustCchLeft( _tail.getCchLeft() + _tail.getCch() );

                    _last.adjustCdocBeginLeft(
                        _tail.getCdocBeginLeft() + _tail.getCdocBegin() );

                    _first._rightSplay = _tail;
                    _tail._parentSplay = _first;
                }
            }

            return _last;
        }

        char[] getText ( )
        {
            return _text;
        }

        private void copy ( Splay s )
        {
            //
            // Here I make sure that the structure of the splay tree returned
            // by the copy is such that the last splay copied is at the root,
            // the first splay copied is the left child of the last copied,
            // the second to last splay copied is the right child of the first,
            // and the third through the second to last are a left only list
            // off of the left child of the second!
            //
            // You may be asking your self, "Self, why all this madness?"
            //
            // Several reasons.  First, by making the first hang off the
            // left child of the last and have the right child of last be null,
            // the root will have the total splay statistics for the whole
            // tree.  Second, you have quick access to the first splay copied.
            // Third, building the third through the second to last do
            // not require walking to update statictics.
            //
            // Ultimately, I do this to be consistent with the format of the
            // tree when a range of splays are removed from a tree.  To avoid
            // an additional splay, the removal of a sub tree is shaped like
            // this.
            //

            if (_last != null)
            {
                if (_first == null)
                    _first = _last;
                else if (_tail == null)
                    _tail = _last;
                else
                {
                    _last.adjustCchLeft( _tail.getCchLeft() + _tail.getCch() );

                    _last.adjustCdocBeginLeft(
                        _tail.getCdocBeginLeft() + _tail.getCdocBegin() );

                    _last._leftSplay = _tail;
                    _tail._parentSplay = _last;
                    _tail = _last;
                }
            }

            _last = s;
        }

        private char[]    _text;
        private Splay     _first;
        private Splay     _tail;
        private Splay     _last;
        private Container _frontier;
    }

    /**
     * Copies the contents of this splay (deeply).
     * This will update any invalid content.
     *
     * Because this can return only text, if there is text at the beginning
     * of the contents, I will create a FRAG as the first splay (after any
     * attributes).
     */

    final CopyContext copySplayContents ( Root r )
    {
        assert !isFinish() && !isXmlns();

        if (isContainer())
            ensureContentValid();
        else if (isNormalAttr())
            ensureValueValid();

        CopyContext copyContext = new CopyContext();

        if (isContainer())
        {
            int cchAttrs = 0;

            Splay t = nextSplay(), s = t;

            for ( ; s.isAttr() ; s = s.nextSplay() )
            {
                copyContext.copySplay( s, false );
                cchAttrs += s.getCch();
            }

            int cchValue = isLeaf() ? getCchValue() : getCchAfter();

            if (cchValue > 0)
                copyContext.copyFragment( cchValue );

            int cchContents = 0;

            if (!isLeaf())
            {
                Splay finish = getFinishSplay();

                for ( Splay u = s ; u != finish ; u = u.nextSplay() )
                {
                    copyContext.copySplay( u, true );
                    cchContents += u.getCch();
                }
            }

            int cchAll = cchAttrs + cchValue + cchContents;

            if (cchAll > 0)
            {
                char[] text = new char [ cchAll ];

                int cp = r.getCp( this );
                int cch = getCch();

                if (cchAttrs > 0)
                    r._text.fetch( text, 0, cp + cch, cchAttrs );

                if (cchValue > 0)
                    r._text.fetch( text, cchAttrs, cp, cchValue );

                if (cchContents > 0)
                {
                    r._text.fetch(
                        text, cchAttrs + cchValue,
                        cp + cch + cchAttrs, cchContents );
                }

                copyContext.copyText( text );
            }
        }
        else
        {
            assert isNormalAttr() || isComment() || isProcinst();

            int cchValue = getCchValue();

            if (cchValue > 0)
            {
                copyContext.copyFragment( cchValue );

                char[] text = new char [ cchValue ];

                r._text.fetch( text, 0, r.getCp( this ), cchValue );

                copyContext.copyText( text );
            }
        }

        return copyContext;
    }

    /**
     * Copies this splay (deeply).  Also copies attributes after a begin
     * This will update any invalid content.
     */

    final Splay copySplay ( )
    {
        assert !isDoc() && !isFinish();

        // Compute splay to stop at (does not get copied).  Also compute
        // a splay, to be copied, which should not have it's text after copied.

        Splay stopHere, noTextAfter;

        if (isBegin())
        {
            noTextAfter = getFinishSplay();
            stopHere = noTextAfter.nextNonAttrSplay();
        }
        else
        {
            noTextAfter = null;
            stopHere = nextSplay();
        }

        // Make the copy

        CopyContext copyContext = new CopyContext();

        for ( Splay next, s = this ; s != stopHere ; s = next )
        {
            next = s.nextSplay();

            copyContext.copySplay( s, s != noTextAfter );
        }

        return copyContext.getTree();
    }

    final void removeAttributes ( Root r )
    {
        assert isContainer();

        for ( ; ; )
        {
            Splay s = nextSplay();

            if (!s.isAttr())
                break;

            s.remove( r, true );
        }
    }

    /**
     * Removes all content from splay.
     * Begin will be a leaf upon exit.
     */

    final void removeContent ( Root r, boolean removeAttrs )
    {
        assert Root.dv > 0 || getRootSlow() == r;

        r.startChange();

        if (isInvalid())
        {
            toggleIsInvalid();

            r.invalidateVersion();

            if (isContainer() && removeAttrs)
                removeAttributes( r );

            return;
        }

        switch ( getKind() )
        {
        case DOC :
        {
            if (getCchAfter() > 0)
            {
                removeChars(r, 1, getCchValue());
                removeTextAfter(r);
            }
            break;
        }

        case BEGIN :
        {
            if (isLeaf())
            {
                removeChars( r, 1, getCchValue() );

                if (removeAttrs)
                    removeAttributes( r );

                return;
            }

            break;
        }

        case ATTR :
        {
            if (isXmlns())
            {
                assert false: "Unexpected kind for removeContent";
            }

            // Fall through
        }

        case COMMENT  :
        case PROCINST :
        {
            int cchValue = getCchValue();

            if (cchValue == 0)
                return;

            r._text.remove( r.getCp( this ), cchValue );

            r.updateCch( this, - cchValue );

            if (getKind() == ATTR)
            {
                invalidateText();

                if (isXsiType())
                    getContainer().disconnectTypes( r );
            }

            r.invalidateVersion();

            return;
        }

        default :
            assert false: "Unexpected kind for removeContent";
        }

        assert isDoc() || (isBegin() && !isLeaf());

        Splay s = nextNonAttrSplay();

        if (s.isRoot())
        {
            assert isDoc();
        }
        else
        {
            //
            // Remove all the splays.  Inhibit invalidation on each remove to
            // prevent a flood of invalidations to the parent type (if there).
            //

            Splay next;
            for ( s = nextNonAttrSplay() ; !s.isFinish() ; s = next )
            {
                assert !s.isAttr();

                if (s.isLeaf())
                    next = s.nextNonAttrSplay();
                else if (s.isBegin())
                    next = s.getFinishSplay().nextSplay();
                else
                    next = s.nextSplay();

                s.remove( r, false );
            }

            //
            // Have to remove the text after this here because the above
            // removes could have suuffled text to after this.
            //

            assert !isLeaf();

            if (getCchAfter() > 0)
                removeTextAfter( r );

            //
            // Tricky code.  Merge the END and the BEGIN to form a leaf.
            //

            if (isBegin())
                foliate( r );

            invalidateText();

            r.invalidateVersion();
        }

// TODO - if removing text from xsi:type or nil must do stuff
// TODO - if removing text from xsi:type or nil must do stuff
// TODO - if removing text from xsi:type or nil must do stuff
// TODO - if removing text from xsi:type or nil must do stuff
// TODO - if removing text from xsi:type or nil must do stuff


// TODO - if removing attribute xsi:type or xsi:nil, must do stuiff
// TODO - if removing attribute xsi:type or xsi:nil, must do stuiff
// TODO - if removing attribute xsi:type or xsi:nil, must do stuiff
// TODO - if removing attribute xsi:type or xsi:nil, must do stuiff
// TODO - if removing attribute xsi:type or xsi:nil, must do stuiff

        if (removeAttrs)
            removeAttributes( r );

        // If the tree became empty, reset _leftOnly

        if (r._leftSplay == r._doc && r._doc._rightSplay == null)
            r._leftOnly = true;

        assert validate();
    }

    final int copyChars (
        Root r, int p, int cch, Root rDst, Splay sDst, int pDst )
    {
        assert pDst > 0 && pDst <= sDst.getEndPos();

        int postCch = getPostCch( p );

        if (p == 0 || cch == 0 || postCch == 0)
            return 0;

        if (cch < 0 || cch > postCch)
            cch = postCch;

        assert cch > 0;

// TODO - uses string to avoid problems with source and dest from same buffer
//        rewrite to not create string object

        char[] chars = new char [ cch ];

        r._text.fetch( chars, 0, getCpForPos( r, p ), cch );

        sDst.insertChars( pDst, rDst, chars, 0, cch );

        return cch;
    }

    /**
     * Move cch chars from pos p on this splay to pos pDst on sDst.
     * Source and destination can be different documents.  Normally cursors
     * stay put when their char is moved, but moveCursors can cause cursors
     * tomove with the chars (used internally).
     */

    final int moveChars (
        Root r, int p, int cch, Root rDst, Splay sDst, int pDst,
        boolean moveCursors )
    {
        if (p == 0)
            return 0;

        int postCch = getPostCch( p );

        if (cch == 0 || postCch == 0)
            return 0;

        if (cch < 0 || cch > postCch)
            cch = postCch;

        r.startChange();
        rDst.startChange();

        assert cch > 0;

        // I don't have to check the invalidity of the source because the
        // only invalid text I would have to deal with is that in a leaf, and
        // If the source is in a leaf here, then it must be positioned at the
        // end (1) which is not sensitive to the invalidity.
        //
        // However, I much check the destination for validity because I may
        // be appending text to an existing value.

        if (sDst.isContainer() && pDst == 1)
        {
            int cchValid = sDst.ensureContentValid();

            pDst += cchValid;

            if (sDst == this && p == 1)
                p += cchValid;
        }

        //

        if (pDst == 0)
        {
            sDst = sDst.prevNonAttrSplay();
            pDst = sDst.getEndPos();
        }

        // If the destination if in the range of the source, then this is
        // effectively a no-op.

// TODO - Probably should disconnect Types in the range

        if (sDst == this && pDst >= p && pDst <= p + cch)
            return cch;

        assert pDst > 0;

        Container cDst = sDst.getContainer( pDst );
        Container c = getContainer( p );

        // Move the text

        rDst._text.move(
            sDst.getCpForPos( rDst, pDst ), r._text, getCpForPos( r, p ), cch );

        // Perform the "insertion" first

        if (!sDst.isLeaf() || pDst > sDst.getPosLeafEnd())
            sDst.adjustCchAfter( cch );

        rDst.updateCch( sDst, cch );

        for ( Goober g = sDst.firstGoober() ; g != null ;
              g = sDst.nextGoober( g ) )
        {
            int gp = g.getPos();

            if (gp >= pDst)
                g.set( gp + cch );
        }

        assert sDst != this || pDst < p || pDst > p + cch;

        if (sDst == this)
        {
            int pDstSave = pDst;

            if (pDst > p + cch)
                pDst -= cch;

            if (p >= pDstSave)
                p += cch;
        }

        // Then perform the "removal"

        if (!isLeaf() || p > getPosLeafEnd())
            adjustCchAfter( - cch );

        r.updateCch( this, - cch );

        Goober nextGoober;
        for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = nextGoober( g );

            int gp = g.getPos();

            if (gp >= p + cch)
            {
                g.set( gp - cch );
            }
            else if (gp >= p)
            {
                if (!moveCursors && g.getKind() == CURSOR)
                {
                    if (p == getEndPos())
                        g.set( nextSplay(), 0 );
                    else
                        g.set( p );
                }
                else
                    g.set( rDst, sDst, pDst + gp - p );
            }
        }

        cDst.invalidateText();
        c.invalidateText();

        r.invalidateVersion();
        rDst.invalidateVersion();

        return cch;
    }

    /**
     * Remove cch chars starting at position p on this splay.
     */

    final int removeChars ( Root r, int p, int cch )
    {
        int maxPos = getMaxPos();

        assert p > 0 && p <= maxPos;

        if (p == 0)
            return 0;

        r.startChange();

// TODO - merge the two following chunks of code??????

        if (isLeaf())
        {
            int ple = getPosLeafEnd();

            if (p <= ple)
            {
                if (cch < 0 || ple - p < cch)
                    cch = ple - p;

                if (cch == 0)
                    return 0;

                r._text.remove( r.getCp( this ) + p - 1, cch );

                r.updateCch( this, - cch );

                Goober nextGoober;

                for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
                {
                    nextGoober = nextGoober( g );

                    int k = g.getKind();
                    int gp = g.getPos();

                    if (gp >= p + cch)
                        g.set( gp - cch );
                    else if (gp >= p)
                    {
                        if (k == CURSOR)
                            g.set( p );
                        else
                        {
                            assert k == ANNOTATION;
                            g.disconnect( r );
                        }
                    }
                }

                invalidateText();

                r.invalidateVersion();

                return cch;
            }
        }

        int posAfter = getPosAfter();

        assert p >= posAfter;

        int maxCch = maxPos - p + 1;

        assert maxCch >= 0;

        if (cch < 0 || cch > maxCch)
            cch = maxCch;

        if (cch <= 0)
            return 0;

        Container c = getContainer( p );

        r._text.remove( getCpForPos( r, p ), cch );

        adjustCchAfter( - cch );

        r.updateCch( this, - cch );

        Goober nextGoober;
        for ( Goober g = firstGoober() ; g != null ; g = nextGoober )
        {
            nextGoober = nextGoober( g );

            int k = g.getKind();
            int gp = g.getPos();

            if (gp >= p)
            {
                if (k == CURSOR)
                    g.set( nextNonAttrSplay(), 0 );
                else
                    g.disconnect( r );
            }
        }

        c.invalidateText();

        r.invalidateVersion();

        assert validate();

        return cch;
    }

    /**
     * Insert text a position p in this splay
     */

    final void insertChars ( int p, Root r, Object txt, int off, int cch )
    {
        assert p > 0 && p <= getEndPos();

        if (cch == 0)
            return;

        r.startChange();

        Container container = getContainer( p );

        if (isContainer() && p == 1)
            p += ensureContentValid();

        r._text.insert( getCpForPos( r, p ), txt, off, cch );

        if (p >= getPosAfter())
            adjustCchAfter( cch );

        r.updateCch( this, cch );

        for ( Goober g = firstGoober() ; g != null ; g = nextGoober( g ) )
        {
            int gp = g.getPos();

            if (gp >= p)
                g.set( gp + cch );
        }

        container.invalidateText();

        r.invalidateVersion();

        assert validate();
    }

    private static final int START_STATE = 0;
    private static final int SPACE_SEEN_STATE = 1;
    private static final int NOSPACE_STATE = 2;

    public final int scrubText(
        Text text, int ws, int cp, int cch, StringBuffer sb, int state )
    {
        assert text != null;

        if (text._buf == null)
        {
            assert cch == 0;
            assert cp == 0;
            return state;
        }

        if (cch == 0)
            return state;

        boolean replace = false;
        boolean collapse = false;

        switch ( ws )
        {
        case TypeStore.WS_UNSPECIFIED :                            break;
        case TypeStore.WS_PRESERVE    :                            break;
        case TypeStore.WS_REPLACE     :            replace = true; break;
        case TypeStore.WS_COLLAPSE    : collapse = replace = true; break;

		default : assert false: "Unknown white space rule " +ws;
        }

        if (!replace && !collapse)
        {
            text.fetch(sb, cp, cch);
            return state;
        }

        int off = text.unObscure( cp, cch );
        int startpt = 0;

        for ( int i = 0 ; i < cch ; i++ )
        {
            char ch = text._buf[ off + i ];

            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t')
            {
                sb.append(text._buf, off + startpt, i - startpt);
                startpt = i + 1;

                if (collapse)
                {
                    if (state == NOSPACE_STATE)
                        state = SPACE_SEEN_STATE;
                }
                else
                    sb.append(' ');
            }
            else
            {
                if (state == SPACE_SEEN_STATE)
                    sb.append( ' ' );

                state = NOSPACE_STATE;
            }
        }

        sb.append( text._buf, off + startpt, cch - startpt );

        return state;
    }

    final String getText ( Root r )
    {
        return getText( r, TypeStore.WS_PRESERVE );
    }

//    final int getText ( Root r, char[] buf, int off, int len )
//    {
//        if (len <= 0)
//            return 0;
//
//        if (isInvalid())
//        {
//            if (isNormalAttr())
//                ensureValueValid();
//            else if (isLeaf())
//                ensureContentValid();
//        }
//
//        int cp = r.getCp( this );
//
//        if (isNormalAttr() || isComment() || isProcinst() || isLeaf())
//        {
//            int cch = getCchValue();
//
//            if (cch == 0)
//                return 0;
//
//            if (cch > len)
//                cch = len;
//
//            r._text.fetch( buf, off, pos, cch );
//
//            return cch;
//        }
//
//        if (!isContainer())
//            return 0;
//
//        int originalLen = len;
//        Splay last = getFinishSplay();
//
//        for ( Splay s = this ; len > 0 && s != last ; s = s.nextSplay() )
//        {
//            int srcCp, srcCch;
//
//            if (s.isBegin())
//            {
//                if (s.isInvalid())
//                    s.ensureContentValid();
//
//                srcCp = cp;
//                srcCch = s.getCch();
//            }
//            else
//            {
//                // It's ok for an attr to be invalid here, gets text after
//
//                srcCp = cp + s.getCchValue();
//                srcCch = s.getCchAfter();
//            }
//
//            if (srcCch > 0)
//            {
//                if (srcCch > len)
//                    srcCch = len;
//
//                int srcOff = r._text.unObscure( srcCp, srcCch );
//
//                System.arrayCopy( r._text._buf, srcOff, buf, off, srcCch );
//
//                len -= srcCch;
//                off += srcCch;
//            }
//
//            cp += s.getCch();
//        }
//
//        return sb.toString();
//    }

    final String getText ( Root r, int ws )
    {
        if (isInvalid())
        {
            if (isNormalAttr())
                ensureValueValid();
            else if (isLeaf())
                ensureContentValid();
        }

        int cp = r.getCp( this );

        if (isNormalAttr() || isComment() || isProcinst() || isLeaf())
        {
            int cch = getCchValue();

            if (cch == 0)
                return "";

            if (ws == TypeStore.WS_PRESERVE || ws == TypeStore.WS_UNSPECIFIED)
                return r._text.fetch( cp, cch );

            StringBuffer sb = new StringBuffer();

            scrubText( r._text, ws, cp, getCchValue(), sb, START_STATE );

            return sb.toString();
        }

        if (!isContainer())
            return null;

        Splay last = getFinishSplay();

        int scrubState = START_STATE;

        StringBuffer sb = new StringBuffer();

        for ( Splay s = this ; s != last ; s = s.nextSplay() )
        {
            if (s.isBegin())
            {
                if (s.isInvalid())
                    s.ensureContentValid();

                scrubState =
                    scrubText(
                        r._text, ws, cp, s.getCch(), sb, scrubState );
            }
            else
            {
                // It's ok for an attr to be invalid here, gets text after

                int cchAfter = s.getCchAfter();

                if (cchAfter > 0)
                {
                    scrubState =
                        scrubText(
                            r._text, ws, cp + s.getCchValue(),
                            cchAfter, sb, scrubState );
                }
            }

            cp += s.getCch();
        }

        return sb.toString();
    }

    /**
     * Gets an attr splay for a given container, null otherwise.
     */

    Splay getAttr ( QName name )
    {
        assert name != null;

        if (!isContainer())
            return null;

        for ( Splay s = nextSplay() ; s.isAttr() ; s = s.nextSplay() )
        {
            if (s.isNormalAttr() && s.getName().equals( name ))
                return s;
        }

        return null;
    }

    void setXsiNil ( Root r, boolean nil )
    {
        assert isContainer();

        if (getXsiNil( r ) == nil)
            return;

        // THT - this used to be: setAttr( r, _xsiNil, "true" );
        // once set to true, this could never be set to false, even if you were un-nilling
        // a value.
        setAttr( r, _xsiNil, Boolean.toString(nil) );
    }

    boolean getXsiNil ( Root r )
    {
        assert isContainer();

        Splay s;

        for ( s = nextSplay() ; s.isAttr() ; s = s.nextSplay() )
        {
            if (s.isXsiNil())
                break;
        }

        if (!s.isAttr())
            return false;

        String value = s.getText( r, TypeStore.WS_COLLAPSE );

        return value.equals( "true" ) ||value.equals( "1" );
    }

    QName getXsiTypeName ( Root r )
    {
        assert isContainer();

        Splay s;

        for ( s = nextSplay() ; s.isAttr() ; s = s.nextSplay() )
        {
            if (s.isXsiType())
                break;
        }

        if (!s.isAttr())
            return null;

        assert s.isXsiType();

        String value = s.getText( r, TypeStore.WS_COLLAPSE );

// TODO - unobscure the underlying text and use it directly
// TODO - should I make sure the prefix is wqell formed? ie. just text

        String prefix, localname;

        int firstcolon = value.indexOf( ':' );

        if (firstcolon >= 0)
        {
            prefix = value.substring( 0, firstcolon );
            localname = value.substring( firstcolon + 1 );
        }
        else
        {
            prefix = "";
            localname = value;
        }

        String uri = namespaceForPrefix( prefix, true );

        if (uri == null)
            return null; // no prefix definition found - that's illegal

        return new QName( uri, localname );
    }

    void setXsiType ( Root r, QName typeName )
    {
        assert isContainer();

        if (typeName == null)
        {
            removeAttr( r, _xsiType );
            return;
        }

        String value = typeName.getLocalPart();

        String ns = typeName.getNamespaceURI();

        String prefix = prefixForNamespace( r, ns, null, true);

        assert prefix != null : "Cannot establish prefix for " + ns;

        if (prefix.length() > 0)
            value = prefix + ":" + value;

        boolean set = false;

        setAttr( r, _xsiType, value );
    }

    void removeAttr ( Root r, QName attrName )
    {
        Splay next;
        for ( Splay s = nextSplay() ; s.isAttr() ; s = next )
        {
            next = s.nextSplay();

            if (s.getName().equals( attrName ))
                s.remove( r, true );
        }
    }

    void setAttr ( Root r, QName attrName, String value )
    {
        assert isContainer();
        assert attrName != null;
        assert value != null;

        boolean set = false;

        Splay next;
        for ( Splay s = nextSplay() ; s.isAttr() ; s = next )
        {
            next = s.nextSplay();

            if (s.getName().equals( attrName ))
            {
                if (set)
                    s.remove( r, true );
                else
                {
                    s.setText( r, value, 0, value.length() );
                    set = true;
                }
            }
        }

        if (!set)
        {
            Splay sInsert = this;
            int   pInsert = 1;

            if (!isLeaf() && getCch() == 0)
            {
                sInsert = nextSplay();
                pInsert = 0;
            }

            int cchValue = value.length();

            Attr attr = new Attr( attrName );
            attr.adjustCch( cchValue );

            sInsert.insert( r, pInsert, attr, value, 0, cchValue, false );
        }
    }


    /**
     * Sets the content/value of this splay to the given text.
     */

    final void setText ( Root r, Object txt, int off, int cch )
    {
        removeContent( r, false );

        if (txt == null || cch == 0)
            return;

        switch ( getKind() )
        {
        case DOC :
        case BEGIN :
        {
            assert !isBegin() || isLeaf();
            insertChars( 1, r, txt, off, cch );
            break;
        }
        case ATTR :
        {
            if (isXmlns())
            {
                assert false: "Unexpected kind for setText";
            }
// TODO - if setting text of xsi:type or xsi:nil must do stuff
// TODO - if setting text of xsi:type or xsi:nil must do stuff
// TODO - if setting text of xsi:type or xsi:nil must do stuff
// TODO - if setting text of xsi:type or xsi:nil must do stuff

            // Fall through
        }
        case COMMENT  :
        case PROCINST :
        {
            r.startChange();

            assert getCchValue() == 0;

            r._text.insert( r.getCp( this ), txt, off, cch );
            r.updateCch( this, cch );

            if (getKind() == ATTR)
            {
                invalidateText();

                if (isXsiType())
                    getContainer().disconnectTypes( r );
            }

            r.invalidateVersion();

            break;
        }
        default :
            assert false: "Unexpected kind for setText";
        }

        assert validate();
    }

    final Map lookupAllPrefixMappings ( )
    {
        Map mappings = null;

        Splay container = this.isContainer() ? this : getContainer();

        for ( ; container != null ; container = container.getContainer() )
        {
            for ( Splay s = container.nextSplay() ; s.isAttr() ;
                  s = s.nextSplay() )
            {
                if (s.isXmlns())
                {
                    if (mappings == null)
                        mappings = new HashMap();
                    else if (mappings.containsKey( s.getLocal() ))
                        continue;

                    mappings.put( s.getLocal(), s.getUri() );
                }
            }
        }

        return mappings;
    }

    final void applyNamespaces ( Root r, Map namespaces )
    {
        if (namespaces == null)
            return;

        Splay container = this.isContainer() ? this : getContainer();

        namespace_loop:
        for ( Iterator i = namespaces.keySet().iterator() ; i.hasNext() ; )
        {
            String prefix = (String) i.next();

            // Usually, this is the predefined xml namespace
            if (prefix.toLowerCase().startsWith( "xml" ))
                continue;

            String namespace = (String) namespaces.get( prefix );

            Splay candidate = container;

            candidate_loop:
            for ( Splay c = container.getContainer() ; c != null && !c.isDoc() ;
                  c = c.getContainer() )
            {
                for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                {
                    if (s.isXmlns() && prefix.equals( s.getLocal() ))
                    {
                        if (namespace.equals( s.getUri()))
                              continue namespace_loop;

                        break candidate_loop;
                    }
                }

                candidate = c;
            }

            // Insert the new namespace at the end of the attr list after
            // makeing sure all previous mappings are gone
            QName qname = new QName( namespace, prefix );

            removeAttr( r, qname );

            candidate.nextNonAttrSplay().insert(
                r, 0, new Xmlns( qname ), null, 0, 0, false );
        }
    }

    /**
     * Returns the prefix associated with this attr/element.
     */
    final String getPrefix ( Root r )
    {
        assert isBegin() || isAttr();

        if (isXmlns())
            return "xmlns";

        // TODO - have a prefix goober to override the mapping on the frontier.
        // Parser will add this when the natural prefix is not the real prefix.

        Splay c = isBegin() ? this : getContainer();

        // special case
        String ns = getUri();

        // last argument is false, which means, don't modify the tree
        return prefixForNamespace( r, ns, null, false );
    }

    /**
     * Given a prefix, returns the namespace corresponding to
     * the prefix at this location, or null if there is no mapping
     * for this prefix.
     * <p>
     * prefix="" indicates the absence of a prefix.  A return value
     * of "" indicates the no-namespace, and should not be confused
     * with a return value of null, which indicates an illegal
     * state, where there is no mapping for the given prefix.
     * <p>
     * If the the default namespace is not explicitly mapped in the xml,
     * the xml spec says that it should be mapped to the no-namespace.
     * When the 'defaultAlwaysMapped' parameter is true, the default namepsace
     * will return the no-namespace even if it is not explicity
     * mapped, otherwise the default namespace will return null.
     * <p>
     * This function intercepts the built-in prefixes "xml" and
     * "xmlns" and returns their well-known namespace URIs.
     *
     * @param prefix The prefix to look up.
     * @param mapDefault If true, return the no-namespace for the default namespace if not set.
     * @return The mapped namespace URI ("" if no-namespace), or null if no mapping.
     */
    final String namespaceForPrefix ( String prefix, boolean defaultAlwaysMapped )
    {
        // null same as "", means look up the default namespace
        if (prefix == null)
            prefix = "";

        // handle built-in prefixes
        if ("xml".equals(prefix))
            return _xml1998Uri;
        if ("xmlns".equals(prefix))
            return _xmlnsUri;

        assert isContainer();

        // find an xmlns decl
        for ( Container c = (Container) this ; c != null ; c = c.getContainer())
        {
            for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isXmlns() && prefix.equals( s.getLocal() ))
                    return s.getUri();
            }
        }

        // CR135193: if defaultAlwaysMapped, return no-namespace when no default namespace is found
        // otherwise, return null to indicate no default namespace was found.
        if (defaultAlwaysMapped && prefix.length() == 0) {
                return "";
        }

        // no namespace defn found: return null
        return null;
    }


    /**
     * Returns the prefix to use for a given namespace URI.  Can either
     * be allowed to modify the tree to ensure such a mapping (if r
     * is non-null), or can be told to return null if there is no
     * existing mapping (if r is null).
     * <p>
     * For the case where the tree may be modified, a suggested prefix
     * can be supplied.  The suggestion may be ignored for any reason,
     * for example, if the prefix is already in-use.
     * <p>
     * Returns "xml" and "xmlns" for the well-known namespace URIs
     * corresponding to those built-in prefixes.
     *
     * @param r       should be null if you don't want to modify the tree.
     *                if a prefix can't be found, null will be returned
     *                rather than introducing an xmlns.
     * @param ns      the namespace to look for or introduce a mapping for.
     *                careful with "", because it may have nonlocal effects
     *                if somebody has overridden the default namespace.
     * @param suggestion  a suggested prefix to try first; may be null.
     *
     * @param createIfMissing true (normally it's true) if you want to create
     *                an xmlns declaration when one doesn't already exist for
     *                your requested URI.  If you don't want to modify the tree,
     *                pass false.
     */
    final String prefixForNamespace (  Root r, String ns, String suggestion,
                                       boolean createIfMissing )
    {
        // null same as empty string, means no-namespace
        if (ns == null)
            ns = "";

        // special cases
        if (_xml1998Uri.equals( ns ))
            return "xml";
        if (_xmlnsUri.equals( ns ))
            return "xmlns";

        assert isContainer();

        Container c;
        Splay s;

        //
        // Special handling is ns is not specified (no namespace)
        //

        if (ns.length() == 0)
        {
            loop:
            for ( c = (Container) this ; c != null ; c = c.getContainer() )
            {
                for ( s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                {
                    if (s.isXmlns() && s.getLocal().length() == 0)
                    {
                        if (s.getUri().length() == 0)
                            return s.getLocal();

                        break loop;
                    }
                }
            }

            //
            // If there is no overridden default namespace in scope,
            // return "" - the default default namespace is the no-namepsace.
            //

            if ( c == null )
                return "";

            //
            // If we found a problematic default namespace but cannot modify
            // the tree, return null - no prefix available.
            //

            if ( !createIfMissing )
                return null;

            //
            // There is a default namespace which maps to a 'real' namespace,
            // create a default namespace on this container which is "".  This
            // can screw attributes and such, but, oh well.
            //

            for ( s = nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isXmlns() && s.getLocal().length() == 0)
                {
                    s.setName( r, new QName( "", "" ) );
                    return s.getLocal();
                }
            }

            // this is an xmlns="" declaration

            Attr a = new Xmlns( new QName( "", "" ) );

            r.startChange();

            r.insertSplay( a, this );

            r.invalidateVersion();

            return a.getLocal();
        }

        //
        // the ordinary not-a-no-namespace case
        //

        assert ns != null && ns.length() > 0;

        //
        // look for an existing prefix for the requested URI
        //

        for ( c = (Container) this ; c != null ; c = c.getContainer() )
        {
            findxmlns: for ( s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isXmlns() && s.getUri().equals( ns ))
                {
                    String result = s.getLocal();

                    // now check to verify that the prefix isn't masked
                    for ( Container c2 = (Container) this ; c2 != c ; c2 = c2.getContainer() )
                    {
                        for ( Splay s2 = c2.nextSplay() ; s2.isAttr() ; s2 = s2.nextSplay() )
                        {
                            if (s2.isXmlns() && s2.getLocal().equals(result))
                                continue findxmlns;
                        }
                    }

                    // not masked: OK.
                    return result;
                }
            }
        }

        //
        // We could not find an existing prefix in-scope that works.
        // If !createIfMissing, don't modify the tree - return null instead.
        //

        if (!createIfMissing)
            return null;

        //
        // We must add an xmlns:something="uri", so we must pick a prefix.
        // We will use the suggestion if it's not already in scope and if
        // it's not the empty string or an illegal prefix.
        //

        if (suggestion != null)
        {
            if (suggestion.length() == 0 || suggestion.toLowerCase().startsWith( "xml" ))
                suggestion = null; // bad suggestion
            else
            {
                for ( c = (Container) this ; c != null ; c = c.getContainer() )
                {
                    for ( s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                    {
                        if (s.isXmlns() &&
                                s.getLocal().equals( suggestion ))
                        {
                            suggestion = null;
                            break;
                        }
                    }
                }
            }
        }

        //
        // If no suggestion, come up with a safe prefix to use
        //

        if (suggestion == null)
        {
            String base = QNameHelper.suggestPrefix(ns);
            suggestion = base;
            for ( int i = 1 ; ; i++ )
            {
                loop:
                for ( c = (Container) this ; c != null ; c = c.getContainer() )
                {
                    for ( s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                    {
                        if (s.isXmlns() &&
                              s.getLocal().equals( suggestion ))
                        {
                            suggestion = null;
                            break loop;
                        }
                    }
                }

                if (suggestion != null)
                    break;

                suggestion = base + i;
            }
        }

        Container target = null;
        Container lastContainer = null;

        for ( c = (Container) this ; c != null ; c = c.getContainer() )
        {
            lastContainer = c;

            if (c.isBegin())
                target = c;
        }

        if (target == null)
            target = lastContainer;

        Attr a = new Xmlns( new QName( ns, suggestion ) );

        r.startChange();

        r.insertSplay( a, target );

        r.invalidateVersion();

        return a.getLocal();
    }

    /**
     * Gets the container associated with the content at pos in this
     * splay.
     */

    final Container getContainer ( int p )
    {
        assert p >= 0 && p <= getEndPos();
        assert p > 0 || !isDoc();

        if (p == 0)
            return getContainer();

        if (isLeaf())
            return p < getPosAfter() ? (Container) this : getContainer();

        if (isContainer())
            return (Container) this;

        if (isFinish())
            return getContainer().getContainer();

        return getContainer();
    }

    /**
     * Gets the splay which contains this splay.
     */

    abstract Container getContainer ( );

    /**
     * Helper fcn to locate the container this splay is container in.
     * Should only be called by implementations of getContainer in
     * cases where the container is not obviously computable.
     */

    Container findContainer ( )
    {
        assert !isContainer();
        assert !isFinish();

        Splay s = nextSplay();

        while ( !s.isFinish() && !s.isBegin() )
            s = s.nextSplay();

        return s.getContainer();
    }

    final void invalidateText ( )
    {
        assert isValid();

        Type t = peekType();

        if (t != null)
        {
            assert isTypeable();

            t.invalidateText();
        }
    }

    final void invalidateNil ( )
    {
        assert isTypeable();

        if (!isAttr())
        {
            Type t = peekType();

            if (t != null)
                t.invalidateNil();
        }
    }

    protected final void disconnectTypes ( Root r )
    {
        disconnectTypes( r, false );
    }
    
    protected final void disconnectTypes ( Root r, boolean disconnectDoc )
    {
        assert isTypeable();

        Splay last = isAttr() ? this : getFinishSplay();

        Splay s = this;

        for ( ; ; )
        {
            Type t = null;

            if (s.isNormalAttr() || s.isLeaf())
            {
                t = s.peekType();
            }
            else if (s.isFinish())
            {
                Container c = s.getContainer();

                if (!c.isDoc() || disconnectDoc)
                    t = c.peekType();
            }

            if (t != null)
            {
                Splay u = t.getSplay();

                if (u.isInvalid())
                {
                    if (u.isAttr())
                        t.validateValue( u, t );
                    else
                    {
                        // The finish splay might get whacked
                        Splay f = u.getFinishSplay();

                        t.validateContent( u, t );

                        if (f == last)
                            last = f;
                    }
                }

                t.disconnect( r );
            }

            if (s == last)
                break;

            if (s.isContainer() && !s.isLeaf() && s.peekType() == null)
                s = s.getFinishSplay();
            else
                s = s.nextSplay();
        }
    }

    /**
     * Sets the type of of this
     */

    final void setType ( Root r, SchemaType sType )
    {
        setType( r, sType, true );
    }
    
    final void setType ( Root r, SchemaType sType, boolean complain )
    {
        assert isTypeable();
        assert sType != null;

        //
        // Is the current type already this type?
        //

        Type t = peekType();

        if (t != null && t.get_schema_type() == sType)
            return;

        //
        // Can always set an arbitrary type at the doc level
        //

        if (isDoc())
        {
            disconnectTypes( r, true );

            assert peekType() == null;

            new Type( r, sType, this );

            assert validate();

            return;
        }

        //
        // Gotta get the parent type to do anything further.  If it
        // can't be found, then barf
        //

        Type parentType = getContainer().getType( r );

        assert parentType != null;

        //
        // You can set the attribute type, as long as it is the natural
        // type.  Attributes cannot have xsi:type
        //

        if (isAttr())
        {
            if (parentType.get_attribute_type( getName() ) != sType && complain)
            {
                throw new IllegalArgumentException(
                    "Can't set type of attribute to " + sType.toString() );
            }

            return;
        }

        assert isBegin();

        //
        // Now, for interior types, I have to deal with xsi:type and
        // whether or not the type is allowed at this point.
        //

        // First check to see if this type can be here sans xsi:type.
        // If so, make sure there is no xsi:type

        if (parentType.get_element_type( getName(), null ) == sType)
        {
            setXsiType( r, null );

            disconnectTypes( r );

            assert validate();

            return;
        }

        // If the desired type has no name, then it cannot be
        // referenced via xsi:type

        QName typeName = sType.getName();

        if (typeName == null)
        {
            if (complain)
            {
                throw new IllegalArgumentException(
                    "Can't set type of element, type is un-named" );
            }

            return;
        }

        if (parentType.get_element_type( getName(), typeName ) != sType)
        {
            if (complain)
            {
                throw new IllegalArgumentException(
                    "Can't set type of element, invalid type" );
            }

            return;
        }

        setXsiType( r, typeName );

        disconnectTypes( r );

        assert validate();
    }

    /**
     * Get the Type associated with this splay.  It does not attempt to create
     * one and will return null is non exists.
     */

    final Type peekType ( )
    {
        assert isTypeable();

        Type type = null;

        if (_goobers != null)
        {
            for ( Goober g = firstGoober() ; g != null ; g = nextGoober( g ) )
                if (g.getKind() == TYPE)
                    return (Type) g;
        }

        assert type != null || isValid();

        return type;
    }

    /**
     * Get the Type associated with this splay.  If non exists, it attempts
     * to create one.  Returns null if one could not be created.
     */

    final Type getType ( Root r )
    {
        assert isTypeable();

        Type type = peekType();

        if (!isDoc() && type == null)
        {
            Type parentType = getContainer().getType( r );

            assert parentType != null;

            // Defensive
            if (parentType == null)
                return null;

            TypeStoreUser newUser;

            if (isBegin())
            {
                newUser =
                    parentType.create_element_user(
                        getName(), getXsiTypeName( r ) );
            }
            else
            {
                assert isNormalAttr();

                newUser = parentType.create_attribute_user( getName() );
            }

            assert newUser != null;

            // Defensive
            if (newUser == null)
                return null;

            type = new Type( r, newUser, this );
        }

        assert type != null;

        return type;
    }

    /**
     * Is this inside of the content of sRange?  Does an inclusive check.
     */

    final boolean between ( Root r, int pThis, Splay sRange )
    {
        assert Root.dv > 0 || getRootSlow() == r;
        assert Root.dv > 0 || getRootSlow() == sRange.getRootSlow();

        if (sRange.isDoc())
            return true;

        if (compare( r, pThis, sRange, 0 ) < 0)
            return false;

        Splay sEnd;
        int   pEnd;

        assert !sRange.isDoc();

        if (sRange.isLeaf())
        {
            sEnd = sRange;
            pEnd = sRange.getPosLeafEnd() + 1;
        }
        else if (sRange.isBegin())
        {
            sEnd = sRange.getFinishSplay();
            pEnd = 1;
        }
        else
        {
            sEnd = sRange.nextSplay();
            pEnd = 0;
        }

        return compare( r, pThis, sEnd, pEnd ) <= 0;
    }

    /**
     * Compare two positions.  Attributes are a bitch.
     */

    final int compare ( Root r, int pThis, Splay sThat, int pThat )
    {
        Splay sThis = this;

        assert Root.dv > 0 || sThis.getRootSlow() == r;
        assert Root.dv > 0 || sThat.getRootSlow() == r;
        assert pThis >= 0 && pThis <= sThis.getEndPos();
        assert pThat >= 0 && pThat <= sThat.getEndPos();

        // Normalize positions

        if (pThis == sThis.getEndPos())
        {
            if (sThis.isAttr())
            {
                if ((sThis = sThis.nextSplay()).isAttr())
                    pThis = 0;
                else
                {
                    sThis = sThis.prevNonAttrSplay();
                    pThis = 1;
                }
            }
            else
            {
                sThis = sThis.nextNonAttrSplay();
                pThis = 0;
            }
        }

        if (pThat == sThat.getEndPos())
        {
            if (sThat.isAttr())
            {
                if ((sThat = sThat.nextSplay()).isAttr())
                    pThat = 0;
                else
                {
                    sThat = sThat.prevNonAttrSplay();
                    pThat = 1;
                }
            }
            else
            {
                sThat = sThat.nextNonAttrSplay();
                pThat = 0;
            }
        }

        assert pThis < sThis.getEndPos();
        assert pThat < sThat.getEndPos();

        if (sThis == sThat)
            return pThis < pThat ? -1 : pThis > pThat ? 1 : 0;

        //

        if (sThis.isAttr())
        {
            if (sThat.isAttr())
                return compare( r, sThat );

            if (sThis.prevNonAttrSplay() != sThat)
                return compare( r, sThat );

            return pThat == 0 ? 1 : -1;
        }
        else if (sThat.isAttr())
        {
            assert !sThis.isAttr();

            if (sThat.prevNonAttrSplay() != sThis)
                return compare( r, sThat );

            return pThis == 0 ? -1 : 1;
        }
        else
        {
            return compare( r, sThat );
        }
    }

    private int compare ( Root r, Splay sThat )
    {
        Splay sThis = this;

        if (sThis == sThat)
            return 0;

        if (r.isLeftOnly())
        {
            if (sThis.getCchLeft() < sThat.getCchLeft())
                return -1;

            if (sThis.getCchLeft() > sThat.getCchLeft())
                return 1;

            if (sThis.getCdocBeginLeft() < sThat.getCdocBeginLeft())
                return -1;

            if (sThis.getCdocBeginLeft() > sThat.getCdocBeginLeft())
                return 1;

            while ( sThis != r )
            {
                sThis = sThis.nextSplay();

                if (sThis == sThat)
                    return -1;

                if (sThis.getCch() > 0 || sThis.getCdocBegin() > 0)
                    break;
            }

            return 1;
        }

        if (sThat.isRoot())
            return -1;

        if (sThis.isRoot())
            return 1;

        sThis.splay( r, r );
        sThat.splay( r, sThis );

        assert sThis._leftSplay == sThat || sThis._rightSplay == sThat;

        return sThis._leftSplay == sThat ? 1 : -1;
    }

    final Splay nextNonAttrSplay ( )
    {
        Splay s = nextSplay();

        while ( s != null && s.isAttr() )
            s = s.nextSplay();

        return s;
    }

    final Splay prevNonAttrSplay ( )
    {
        Splay s = prevSplay();

        while ( s != null && s.isAttr() )
            s = s.prevSplay();

        return s;
    }

    final Splay nextSplay ( )
    {
        Splay s = this;
        Splay r = s._rightSplay;

        if (r != null)
        {
            for ( Splay l = s = r ; (l = l._leftSplay) != null ; )
                s = l;

            return s;
        }

        for ( Splay p = s._parentSplay ; ; p = (s = p)._parentSplay )
            if (p == null || p._leftSplay == s)
                return p;
    }

    final Splay prevSplay ( )
    {
        Splay s = this;
        Splay l = s._leftSplay;

        if (l != null)
        {
            for ( Splay r = s = l ; (r = r._rightSplay) != null ; )
                s = r;

            return s;
        }

        for ( Splay p = s._parentSplay ; ; p = (s = p)._parentSplay )
            if (p == null || p._rightSplay == s)
                return p;
    }

    final void rotateRight ( )
    {
        assert _parentSplay._leftSplay == this;

        Splay p = _parentSplay;
        Splay g = p._parentSplay;

        assert p != null;

        p._leftSplay = _rightSplay;

        if (_rightSplay != null)
            _rightSplay._parentSplay = p;

        _rightSplay = p;
        p._parentSplay = this;
        _parentSplay = g;

        if (g != null)
        {
            if (g._leftSplay == p)
                g._leftSplay = this;
            else
                g._rightSplay = this;
        }

        p.adjustCchLeft( - getCchLeft() - getCch() );
        p.adjustCdocBeginLeft( - getCdocBeginLeft() - getCdocBegin() );
    }

    final void rotateLeft ( )
    {
        assert _parentSplay._rightSplay == this;

        Splay p = _parentSplay;
        Splay g = p._parentSplay;

        assert p != null;

        p._rightSplay = _leftSplay;

        if (_leftSplay != null)
            _leftSplay._parentSplay = p;

        _leftSplay = p;
        p._parentSplay = this;
        _parentSplay = g;

        if (g != null)
        {
            if (g._leftSplay == p)
                g._leftSplay = this;
            else
                g._rightSplay = this;
        }

        adjustCchLeft( p.getCchLeft() + p.getCch() );
        adjustCdocBeginLeft( p.getCdocBeginLeft() + p.getCdocBegin() );
    }

    //
    // Splay this so that it's parent is pStop.
    //
    // This version of splay is capable of splaying this to the top.  There
    // are two cases.  One is there r is this and this is splayed to be
    // the left child of r.  The other case is where r is null.  In this case
    // the tree this is in is detached from the main document, and this will be
    // splayed to the top of that subtree such that it's parent will be null.
    //

    final void splay ( Root r, Splay pStop )
    {
        assert pStop != null;
        assert this != pStop;
        assert Root.dv > 0 || r == null || getRootSlow() == r;
        assert Root.dv > 0 || r == null || pStop.getRootSlow() == r;
        assert r == null || r.validateSplayTree();
        assert !isRoot();

        boolean rotated = false;

        for ( ; ; )
        {
            assert _parentSplay != null;

            Splay p = _parentSplay;

            assert p != null;

            // If this is a child of the stopper, then we are done

            if (p == pStop)
                break;

            Splay g = p._parentSplay;

            // If this is child of root, then simple rotate

            rotated = true;

            if (g == pStop)
            {
                if (p._leftSplay == this)
                    rotateRight();
                else
                    rotateLeft();

                break;
            }

            // Fancy splays

            if (g._leftSplay == p)
            {
                if (p._leftSplay == this)
                    p.rotateRight();
                else
                    rotateLeft();

                rotateRight();
            }
            else
            {
                if (p._rightSplay == this)
                    p.rotateLeft();
                else
                    rotateRight();

                rotateLeft();
            }
        }

        if (rotated && r != null)
            r._leftOnly = false;

        assert r == null || r.validateSplayTree();
    }

    /**
     * Remove this splay where root is the splay root of the tree this splay.
     * This may be a ROOT splay
     */

    final Splay removeSplay ( Splay r )
    {
        Root root = r.isRoot() ? (Root) r : null;

        assert r._parentSplay == null;
        assert root == null || root.validateSplayTree();
        assert !isRoot();
        assert !isDoc();
        assert Root.dv > 0 || getSplayRootSlow() == r;

        // If the splay to be removed has no cch or cbegin, then when the
        // tree is left only children, we need not splay.

        int cch = getCch();
        int cbegin = getCdocBegin();

        if (root != null && root._leftOnly && cch == 0 && cbegin == 0)
        {
            assert _rightSplay == null;
            assert _parentSplay != null;

            if ((_parentSplay._leftSplay = _leftSplay) != null)
                _leftSplay._parentSplay = _parentSplay;
        }
        else
        {
            splay( root, r );

            if (_leftSplay == null)
            {
                r.adjustCchLeft( - cch );
                r.adjustCdocBeginLeft( - cbegin );

                if ((r._leftSplay = _rightSplay) != null)
                    _rightSplay._parentSplay = r;
            }
            else
            {
                Splay p = prevSplay();

                p.splay( root, this );

                assert p._rightSplay == null;

                r._leftSplay = p;
                p._parentSplay = r;
                r.adjustCchLeft( - cch );
                r.adjustCdocBeginLeft( - cbegin );
                p._rightSplay = _rightSplay;

                if (p._rightSplay != null)
                    p._rightSplay._parentSplay = p;
            }
        }

        _leftSplay = _rightSplay = _parentSplay = null;
        adjustCchLeft( - getCchLeft() );
        adjustCdocBeginLeft( - getCdocBeginLeft() );

        assert root == null || root.validateSplayTree();

        return this;
    }

    //
    // The Zoo (more like a petting zoo now)
    //

    static abstract class QNameSplay extends Splay
    {
        QNameSplay ( int kind, boolean is, QName name )
        {
            super( kind, is );
            _name = name;
        }

        final QName getName ( ) { return _name; }
        final void  changeName ( QName name ) { _name = name; }

        final String getUri   ( ) { return _name.getNamespaceURI(); }
        final String getLocal ( ) { return _name.getLocalPart();    }

        private QName _name;
    }

    static abstract class Container extends QNameSplay
    {
        Container ( int kind, boolean is, QName name )
            { super( kind, is, name ); }

        abstract Finish getFinish ( );
    }

    static final class Doc extends Container
    {
        Doc ( Root r, QName name )
            { super( DOC, false, name ); _root = r; }

        Container getContainer ( ) { return null; }
        Finish    getFinish ( ) { return _root; }

        Root _root;
    }

    static class Begin extends Container
    {
        Begin ( QName name, Container container )
            { super( BEGIN, false, name ); _container = container; }

        final Container getContainer ( ) { return _container; }
        final Finish    getFinish    ( ) { return _end; }

        End         _end;
        Container   _container;
    }

    static abstract class Finish extends Splay
    {
        Finish ( int kind ) { super( kind, false ); }
    }

    static final class End extends Finish
    {
        End ( Begin begin ) { super( END ); _begin = begin; }

        Container getContainer ( ) { return _begin; }

        final Begin _begin;
    }

    static final class Comment extends Splay
    {
        Comment ( ) { super( COMMENT, false ); }

        Container getContainer ( ) { return findContainer(); }
    }

    static final class Fragment extends Splay
    {
        Fragment ( ) { super( COMMENT, true ); }

        Container getContainer ( ) { return null; }
    }

    static class Attr extends QNameSplay
    {
        Attr ( QName name ) { super( ATTR, false, name ); }

        Attr ( QName name, boolean isXmlns )
        {
            super( ATTR, true, name );
            assert isXmlns;
        }

        Container getContainer ( )
        {
            for ( Splay s = prevSplay() ; ; s = s.prevSplay() )
                if (!s.isAttr())
                    return (Container) s;
        }
    }

    static final class Xmlns extends Attr
    {
        Xmlns ( QName name ) { super( name, true ); }
    }

    static final class Procinst extends QNameSplay
    {
        Procinst ( QName target )
        {
            super( PROCINST, false, target );
        }

        Procinst ( String target )
        {
            super( PROCINST, false, new QName( "", target ) );
        }

        Container getContainer ( ) { return findContainer(); }
    }

    /**
     * Every splay points to a collection of goobers.  Goobers can be
     * aggregated into groups which share the same state (kind and position).
     */

    static final int CURSOR     = 0;
    static final int TYPE       = 1;
    static final int ANNOTATION = 2;
    static final int AGGREGATE  = 3;

    static abstract class Goober extends Goobers
    {
        Goober ( Root r, int kind )
        {
            _root = r;
            _state = kind;
        }

        final int   getKind  ( ) { return _state & 7;  }
        final Root  getRoot  ( ) { return _root;       }
        final Splay getSplay ( ) { return _splay;      }
        final int   getPos   ( ) { return _state >> 3; }
        final int   getState ( ) { return _state;      }

        final boolean isAnnotation ( ) { return getKind() == ANNOTATION; }

        final void set ( Root r, Splay s, int p )
        {
            assert s != null;
            doSet( r, s, p );
        }

        private final void doSet ( Root r, Splay s, int p )
        {
            assert s != null || p == 0;
            assert Root.dv > 0 || s == null || r == s.getRootSlow();
            assert Root.dv > 0 || _splay == null || _root == _splay.getRootSlow();
//            assert Root.dv > 0 || _root == r || _splay.getRootSlow() != s.getRootSlow();

            _root = r;

            if (_splay != s)
            {
                if (_splay != null)
                    remove();

                if (s != null)
                    append( s );
            }

            assert p >= 0;
            assert _splay == null || p <= _splay.getMaxPos();
            _state = p * 8 + (_state & 7);
        }

        final void set ( Root r )
        {
            assert r != null;
            _root = r;
        }

        final void set ( Splay s, int p ) { doSet( getRoot(), s,          p ); }
        final void set (          int p ) { doSet( getRoot(), getSplay(), p ); }

        final void set ( Goober g )
            { doSet( g.getRoot(), g.getSplay(), g.getPos() ); }

        void disconnect ( Root r )
        {
            doSet( r, null, 0 );
        }

        void release ( )
        {
            set( null, 0);
            _root = null;
        }

        private final void append ( Splay s )
        {
            assert s != null;
            assert Root.dv > 0 || s.getRootSlow() == _root;
            assert _splay == null;
            assert _parent == null;
            assert _next == null && _prev == null;

            if (s._goobers == null)
            {
                s._goobers = this;
                _next = _prev = this;
            }
            else
            {
                _next = s._goobers;
                _prev = _next._prev;
                _next._prev = this;
                _prev._next = this;
            }

            _splay = s;
            _parent = s;
        }

        private final void remove ( )
        {
            assert _splay != null;
            assert _goobers == null;

            if (_next == this)
            {
                assert _parent._goobers == this;

                _parent._goobers = null;

                // If parent is an aggregate goober, remove it too

                if (_parent != _splay)
                {
                    Goober g = (Goober) _parent;
                    assert g.getKind() == AGGREGATE;
                    g.remove();
                }
            }
            else
            {
                _prev._next = _next;
                _next._prev = _prev;

                if (_parent._goobers == this)
                    _parent._goobers = _next;
            }

            _next = _prev = null;

            _parent = null;
            _splay = null;
        }

        XmlBookmark getBookmark ( )
        {
            return
                getKind() == ANNOTATION
                    ? ((Annotation) this).getXmlBookmark()
                    : null;
        }

        String getKindName ( )
        {
            switch ( getKind() )
            {
            case CURSOR     : return "CURSOR";
            case TYPE       : return "TYPE";
            case ANNOTATION : return "ANNOTATION";
            default         : return "<unknow goober kind>";
            }
        }

        private Root _root;

        private Splay _splay;

        private int _state;  // pos and kind

        Goober  _next;
        Goober  _prev;
        Goobers _parent;
    }

    static final class CursorGoober extends Goober
    {
        CursorGoober ( Root r )
        {
            super( r, CURSOR );
        }

        private static final HashMap createDebugIdMap ( )
        {
            // Creepy way to discover is assert is enabled.
            HashMap map = null;
            assert (map = new HashMap()) != null;
            return map;
        }

        public int getDebugId ( )
        {
            if (_debugIds == null)
                return -1;

            synchronized ( _debugIds )
            {
                if (!_debugIds.containsKey( this ))
                    _debugIds.put( this, new Integer( _nextDebugId++ ) );

                return ((Integer) _debugIds.get( this )).intValue();
            }
        }

        private static final HashMap _debugIds = createDebugIdMap();
        private static int   _nextDebugId = 1;
    }

    static class Annotation extends Goober implements XmlMark
    {
        public Object monitor()
        {
            return getRoot();
        }

        Annotation ( Root r )
        {
            super( r, ANNOTATION );
        }

        Annotation ( Root r, XmlBookmark a )
        {
            this( r );

            if (a._ref == null)
                _annotation = a;
            else
                _ref = a._ref;

            _key = a.getKey();
        }

        void setKey ( Object key )
        {
            _key = key;
        }

        // called via bookmark (public via XmlMark)
        public final XmlCursor createCursor ( )
        {
            synchronized (monitor())
            {
                if (getSplay() == null)
                {
                    throw new IllegalStateException(
                        "Attempting to create a cursor on a bookmark that " +
                            "has been cleared or replaced.");
                }

                return new Cursor( getRoot(), getSplay(), getPos() );
            }
        }

        public XmlBookmark getXmlBookmark ( )
        {
            if (_annotation != null)
                return _annotation;

            if (_ref != null)
                return (XmlBookmark) _ref.get();

            return null;
        }

        void disconnect ( Root r )
        {
            super.disconnect( r );

            XmlBookmark xa = getXmlBookmark();

            if (xa != null)
                xa._currentMark = null;
        }

        XmlBookmark _annotation;
        Reference     _ref;

        Object _key;
    }

    final Goober firstGoober ( )
    {
        if (_goobers == null || _goobers.getKind() != AGGREGATE)
            return _goobers;

        assert _goobers._goobers != null;
        assert _goobers._goobers.getKind() != AGGREGATE;

        return _goobers._goobers;
    }

    final Goober nextGoober ( Goober g )
    {
        assert g != null;

        if ((g = g._next) != g._parent._goobers)
            return g;

        return g._parent == g._splay ? null : nextGoober( (Goober) g._parent );
    }

    static final boolean _assertEnabled = getAssertEnabled();

    private static boolean getAssertEnabled ( )
    {
        // Creepy way to discover is assert is enabled.
        boolean enabled = false;
        assert (enabled = true) || true;
        return enabled;
    }

    static void assertAssertEnabled ( )
    {
        if (!_assertEnabled)
        {
            throw
                new RuntimeException(
                    "Assert needs to be enabled for this operation" );
        }
    }

    // Associate id's with splays when asserts are enabled

    private static final HashMap _debugIds =
        _assertEnabled ? new HashMap() : null;

    private static int _nextDebugId = 1;

    public int getDebugId ( )
    {
        if (_debugIds == null)
            return -1;

        synchronized ( _debugIds )
        {
            if (!_debugIds.containsKey( this ))
                _debugIds.put( this, new Integer( _nextDebugId++ ) );

            return ((Integer) _debugIds.get( this )).intValue();
        }
    }

    void dump ( ) { getRootSlow().dump(); }
    void dump ( boolean verbose ) { getRootSlow().dump( verbose ); }

    public Splay getSplayRootSlow ( )
    {
        assertAssertEnabled();

        Splay s = this;

        while ( s._parentSplay != null )
            s = s._parentSplay;

        return s;
    }

    public Root getRootSlow ( )
    {
        assertAssertEnabled();

        Splay s = getSplayRootSlow();

        return s.isRoot() ? (Root) s : null;
    }

    int getCpSlow ( )
    {
        assertAssertEnabled();

        int cch = 0;

        for ( Splay s = this ; (s = s.prevSplay()) != null ; )
            cch += s.getCch();

        return cch;
    }

    int compareSlow ( Splay sThat )
    {
        assertAssertEnabled();

        assert Root.dv > 0 || getRootSlow() == sThat.getRootSlow();

        if (this == sThat)
            return 0;

        for ( Splay s = this ; s != null ; s = s.nextSplay() )
            if (s == sThat)
                return -1;

        for ( Splay s = this ; s != null ; s = s.prevSplay() )
            if (s == sThat)
                return 1;

        assert false: "Yikes!";

        return 0;
    }

    boolean validate ( )
    {
        assertAssertEnabled();

        return getRootSlow().validate();
    }

    //
    // Document splay tree members
    //

    Splay _leftSplay;
    Splay _rightSplay;
    Splay _parentSplay;

    private int _bits; // 27: cDocbeginLeft, 1: invalid, 1: multi, 3: kind
    private int _cch;
    private int _cchAfter;
    private int _cchLeft;
}
