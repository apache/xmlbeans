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

import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.store.Splay.Attr;
import org.apache.xmlbeans.impl.store.Splay.Begin;
import org.apache.xmlbeans.impl.store.Splay.Container;
import org.apache.xmlbeans.impl.store.Splay.CopyContext;
import org.apache.xmlbeans.impl.store.Splay.Goober;
import org.apache.xmlbeans.impl.values.NamespaceManager;
import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;
import org.apache.xmlbeans.impl.values.TypeStoreVisitor;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

public final class Type extends Goober implements TypeStore
{
    Type ( Root r, SchemaType sType, Splay s )
    {
        this( r, ((TypeStoreUserFactory) sType).createTypeStoreUser(), s );
    }
    
    Type ( Root r, TypeStoreUser user, Splay s )
    {
        super( r, Splay.TYPE );
        
        assert s.peekType() == null;
        assert user != null;
        assert s.isTypeable();
        
        _user = user;
        
        set( s, 0 );

        if (user.uses_invalidate_value())
            r._cInvalidatableTypes++;

        if (user.is_child_element_order_sensitive())
            r._cElemOrderSensitiveTypes++;
        
        user.attach_store( this );
    }

    void disconnect ( Root r )
    {
        super.disconnect( r );
        
        if (_user.uses_invalidate_value())
            r._cInvalidatableTypes--;

        if (_user.is_child_element_order_sensitive())
            r._cElemOrderSensitiveTypes--;
        
        _user.disconnect_store();
    }

    //
    //
    //

    SchemaType get_schema_type ( )
    {
        return _user.get_schema_type();
    }
    
    SchemaType get_element_type ( QName eltName, QName xsiType )
    {
        return _user.get_element_type( eltName, xsiType );
    }
    
    SchemaType get_attribute_type ( QName attrName )
    {
        return _user.get_attribute_type( attrName );
    }
    
    TypeStoreUser create_element_user ( QName eltName, QName xsiType )
    {
        TypeStoreUser ret = null;
        if (getRoot()._factory != null)
            ret = getRoot()._factory.createElementUser(get_schema_type(), eltName, xsiType);

        if (ret == null)
            ret = _user.create_element_user( eltName, xsiType );

        return ret;
    }
            
    TypeStoreUser create_attribute_user( QName attrName )
    {
        TypeStoreUser ret = null;

        if (getRoot()._factory != null)
            ret = getRoot()._factory.createAttributeUser(get_schema_type(),  attrName);

        if (ret == null)
            ret = _user.create_attribute_user( attrName );

        return ret;
    }

    XmlObject getXmlObject ( )
    {
        return (XmlObject) _user;
    }

    boolean uses_invalidate_value ( )
    {
        return _user.uses_invalidate_value();
    }

    boolean is_child_element_order_sensitive ( )
    {
        return _user.is_child_element_order_sensitive();
    }

    void invalidateText ( )
    {
        if (_inhibitUserInvalidate == 0)
            _user.invalidate_value();
    }

    void invalidateNil ( )
    {
        if (_inhibitUserInvalidate == 0)
            _user.invalidate_nilvalue();
    }
    
    void invalidateElement ( Container container, Splay s )
    {
        assert s.getContainer() == container;
        
        if (_inhibitUserInvalidate > 0)
            return;

        Type containerType = container.peekType();

        if (containerType == null)
            return;

        if (!containerType.is_child_element_order_sensitive())
            return;

        containerType._user.invalidate_value();

        for ( ; ; s = s.nextSplay() )
        {
            if (s.isFinish())
            {
                assert s.getContainer() == container;
                return;
            }

            if (s.isBegin())
            {
                Type childType = s.peekType();

                if (childType != null)
                    childType._user.invalidate_element_order();

                s = s.getFinishSplay();
            }

            break;
        }
    }

    String build_text ( NamespaceManager nsm )
    {
        return _user.build_text( nsm );
    }

    boolean build_nil ( )
    {
        return _user.build_nil();
    }
    
    int validateContent ( Splay s, NamespaceManager nsm )
    {
        Root r = getRoot();
        
        assert s.isInvalid();
        assert s.isLeaf() || s.isDoc();

        String text = build_text( nsm );

        assert text != null;

        _inhibitUserInvalidate++;

        // Validating does not logically change the doc
        long oldVersion = r.getVersion();
        
        s.toggleIsInvalid(); // Prevents infinite recursion
        
        s.insertChars( 1, r, text, 0, text.length() );

        r.restoreVersion( oldVersion );
        
        _inhibitUserInvalidate--;

        return text.length();
    }
    
    void validateValue ( Splay s, NamespaceManager nsm )
    {
        Root r = getRoot();
        
        assert s.isInvalid();
        assert s.isNormalAttr();

        String text = build_text( nsm );

        assert text != null;

        // Validating does not logically change the doc
        long oldVersion = r.getVersion();
        
        _inhibitUserInvalidate++;
        
        s.toggleIsInvalid(); // Prevents infinite recursion
        
        s.setText( r, text, 0, text.length() );
        
        r.restoreVersion( oldVersion );
        
        _inhibitUserInvalidate--;
    }

    QNameSet get_element_ending_delimiters( QName qname )
    {
        return _user.get_element_ending_delimiters( qname );
    }

    //
    //
    //
    
    public boolean is_attribute ( )
    {
        return getSplay().isAttr();
    }

    public boolean validate_on_set()
    {
        return getRoot().validateOnSet();
    }

    public XmlCursor new_cursor ( )
    {
        return new Cursor( getRoot(), getSplay() );
    }

    public void validate ( ValidatorListener vEventSink )
    {
        new Saver.ValidatorSaver( getRoot(), getSplay(), 0, null, vEventSink );
    }
    
    public SchemaTypeLoader get_schematypeloader ( )
    {
        return getRoot().getSchemaTypeLoader();
    }
    
    public QName get_xsi_type ( )
    {
        Splay s = getSplay();

        assert s.isContainer() || s.isNormalAttr();

        if (s.isNormalAttr())
            return null;

        return s.getXsiTypeName( getRoot() );
    }
    
    public TypeStoreUser change_type ( SchemaType sType )
    {
        Splay s = getSplay();

        s.setType( getRoot(), sType, false );

        Type t = s.getType( getRoot() );

        assert t != null;
        
        return t._user;
    }
    
    public void invalidate_text ( )
    {
        Splay s = getSplay();

        assert s.isTypeable();

        if (s.isInvalid())
            return;
        
        _inhibitUserInvalidate++;

        s.removeContent( getRoot(), false );

        s.toggleIsInvalid();
        assert s.isInvalid();
        
        _inhibitUserInvalidate--;
    }
    
    public String fetch_text ( int whitespaceRule )
    {
        Splay s = getSplay();

        assert !s.isInvalid();
        
        if (s.isInvalid())
            throw new RuntimeException( "Can't fetch text when invalid" );

        return s.getText( getRoot(), whitespaceRule );
    }
    
    public void store_text ( String text )
    {
        _inhibitUserInvalidate++;
        getSplay().setText( getRoot(), text, 0, text.length() );
        _inhibitUserInvalidate--;
    }
    
    public int compute_flags ( )
    {
        Splay s = getSplay();
        
        assert s.isTypeable();

        if (s.isDoc())
            return 0;

        Container parentContainer = s.getContainer();

        Type parentType = parentContainer.getType( getRoot() );

        assert parentType != null;

        TypeStoreUser parentUser = parentType._user;

        if (s.isAttr())
            return parentUser.get_attributeflags( s.getName() );

        int f = parentUser.get_elementflags( s.getName() );

        if (f != -1)
            return f;

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return 0;

        assert !parentContainer.isLeaf();
        
        for ( Splay t = parentContainer.nextSplay() ; ; t = t.nextSplay() )
        {
            switch ( t.getKind() )
            {
            case Splay.END :
                assert false;
                break;

            case Splay.BEGIN :
                visitor.visit( t.getName() );

                if (t == s)
                    return visitor.get_elementflags();

                t = t.getFinishSplay();

                break;
            }
        }
    }

    public SchemaField get_schema_field ( )
    {
        Splay s = getSplay();
        
        assert s.isTypeable();

        if (s.isDoc())
            return null;
        
        Container parentContainer = s.getContainer();

        TypeStoreUser parentUser = parentContainer.getType( getRoot() )._user;
        
        if (s.isAttr())
            return parentUser.get_attribute_field( s.getName() );

        assert s.isBegin();

        assert !parentContainer.isLeaf();
        
        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return null;

        for ( Splay t = parentContainer.nextSplay() ; ; t = t.nextSplay() )
        {
            switch ( t.getKind() )
            {
            case Splay.END :
                assert false;
                break;

            case Splay.BEGIN :
                visitor.visit( t.getName() );

                if (t == s)
                    return visitor.get_schema_field();

                t = t.getFinishSplay();

                break;
            }
        }
    }
    
    public int count_elements ( QName name )
    {
        return getRoot().count( (Container) getSplay(), name, null );
    }

    public int count_elements(QNameSet names)
    {
        return getRoot().count((Container)getSplay(),  null, names);
    }
    
    public TypeStoreUser find_element_user ( QName name, int i )
    {
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        Begin nthBegin = getRoot().findNthBegin( s, name, null, i );

        if (nthBegin == null)
            return null;

        Type t = nthBegin.getType( getRoot() );

        assert t != null;
        
        return t._user;
    }

    public TypeStoreUser find_element_user ( QNameSet names, int i )
    {
        Splay s = getSplay();
        assert s.isContainer();

        Begin nthBegin = getRoot().findNthBegin(s,  null, names, i);
        if (nthBegin == null) return null;

        Type t = nthBegin.getType(getRoot());
        assert t != null;

        return t._user;
    }
    
    private void findAllElementTypes ( QName name, QNameSet set, List fillMeUp )
    {
        assert getSplay().isContainer();

        Splay s = getSplay();

        if (s.isLeaf())
            return;

        loop:
        for ( s = s.nextSplay() ; ; s = s.nextSplay() )
        {
            switch ( s.getKind() )
            {
            case Splay.END  :
            case Splay.ROOT :
            {
                break loop;
            }        
            case Splay.BEGIN :
            {
                if (set == null)
                {
                    if (s.getName().equals( name ))
                    {
                        Type type = s.getType( getRoot() );

                        assert type != null;
                    
                        fillMeUp.add( type );
                    }
                }
                else
                {
                    if (set.contains(s.getName()))
                    {
                        Type type = s.getType(getRoot());
                        assert type != null;
                        fillMeUp.add(type);
                    }
                }

                s = s.getFinishSplay();
                break;
            }
            }
        }
    }

    public void find_all_element_users ( QName name, List fillMeUp )
    {
        int i = fillMeUp.size();
        
        findAllElementTypes( name, null, fillMeUp );

        for ( int j = i ; j < fillMeUp.size() ; j++ )
            fillMeUp.set( j, ((Type) fillMeUp.get( j ))._user );
    }

    public void find_all_element_users (QNameSet names, List fillMeUp)
    {
        int i = fillMeUp.size();
        findAllElementTypes(null, names, fillMeUp);

        for ( int j = i ; j < fillMeUp.size() ; j++ )
            fillMeUp.set( j, ((Type) fillMeUp.get( j ))._user );
    }

    public TypeStoreUser find_attribute_user ( QName name )
    {
        assert getSplay().isContainer();

        Splay a = getSplay().getAttr( name );

        if (a == null)
            return null;

        Type t = a.getType( getRoot() );

        assert t != null;

        return t._user;
    }
    
    public String compute_default_text ( )
    {
        Splay s = getSplay();
        
        assert s.isTypeable();

        if (s.isDoc())
            return null;

        Container parentContainer = s.getContainer();

        Type parentType = parentContainer.getType( getRoot() );

        assert parentType != null;

        TypeStoreUser parentUser = parentType._user;

        if (s.isAttr())
            return parentUser.get_default_attribute_text( s.getName() );

        String result = parentUser.get_default_element_text( s.getName() );

        if (result != null)
            return result;

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null)
            return null;

        assert !parentContainer.isLeaf();
        
        for ( Splay t = parentContainer.nextSplay() ; ; t = t.nextSplay() )
        {
            switch ( t.getKind() )
            {
            case Splay.END :
                assert false;
                break;

            case Splay.BEGIN :
                visitor.visit( t.getName() );

                if (t == s)
                    return visitor.get_default_text();

                t = t.getFinishSplay();
                
                break;
            }
        }
    }
    
    public void invalidate_nil ( )
    {
        Splay s = getSplay();

        assert s.isTypeable();

        if (!s.isAttr())
            s.setXsiNil( getRoot(), build_nil() );
    }
      
    public boolean find_nil ( )
    {
        Splay s = getSplay();

        assert s.isTypeable();
  
        return s.isAttr() ? false : s.getXsiNil( getRoot() );
    }
    
    public String find_prefix_for_nsuri (
        String nsuri, String suggested_prefix )
    {
        Splay s = getSplay();

        if (s.isAttr())
            s = s.getContainer();

        String result = s.prefixForNamespace( getRoot(), nsuri, suggested_prefix, true);

        assert result != null;

        return result;
    }
    
    public String getNamespaceForPrefix ( String prefix )
    {
        Splay s = getSplay();

        if (s.isAttr())
            s = s.getContainer();
        
        return s.namespaceForPrefix( prefix, true );
    }

    public TypeStoreUser insert_element_user ( QName name, int i )
    {
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        if (i < 0)
            throw new IndexOutOfBoundsException();

        Root r = getRoot();
        Container c = (Container) s;

        Begin nthBegin = r.findNthBegin( c, name, null, i );

        if (nthBegin == null)
        {
            if (i > r.count( c, name, null ) + 1)
                throw new IndexOutOfBoundsException();

            return add_element_user( name );
        }

        return insertElement( name, nthBegin, 0 );
    }

    public TypeStoreUser insert_element_user(QNameSet set, QName name, int i)
    {
        Splay s = getSplay();

        assert s.isContainer();

        if (!s.isContainer())
            throw new IllegalStateException();

        if (i < 0)
            throw new IllegalStateException();

        Root r = getRoot();
        Container c = (Splay.Container) s;

        Begin nthBegin = r.findNthBegin(c, null, set, i);

        if (nthBegin == null)
        {
            if (i > r.count(c, null, set))
                throw new IndexOutOfBoundsException();

            return add_element_user(name);
        }

        return insertElement(name, nthBegin, 0);
    }
    
// TODO - consolidate names, this fcn is very expensive in creating names
    public TypeStoreUser add_element_user ( QName qname )
    {
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        Splay candidateSplay;
        int candidatePos;
            
        if (s.isLeaf())
        {
            candidateSplay = s;
            candidatePos = s.getPosLeafEnd();
        }
        else
        {
            candidateSplay = s.getFinishSplay();
            candidatePos = 0;

            QNameSet endSet = null;
            
            loop:
            for ( Splay t = candidateSplay ; ; )
            {
                for ( ; ; )
                {
                    t = t.prevSplay();
                    
                    if (t == s)
                        break loop;
                    
                    if (t.isContainer())
                        break;

                    if (t.isEnd())
                    {
                        t = t.getContainer();
                        break;
                    }
                }

                assert t.isContainer();

                if (t.getName().equals( qname ))
                    break;

                if (endSet == null)
                    endSet = get_element_ending_delimiters( qname );

                if (endSet.contains( t.getName() ))
                    candidateSplay = t;
            }
        }
        
        return insertElement( qname, candidateSplay, candidatePos );
    }

    private TypeStoreUser insertElement ( QName name, Splay s, int p )
    {
        Root r = getRoot();
        
        Begin b = new Begin( name, null );
        b.toggleIsLeaf();
        
        s.insert( r, p, b, null, 0, 0, true );
        
        Type t = b.getType( r );

        assert t != null;
        
        return t._user;
    }
    
    public void remove_element ( QName qname, int i )
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();
        
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        if (s.isLeaf())
            throw new IndexOutOfBoundsException();

        Begin b = getRoot().findNthBegin( s, qname, null, i );

        if (b == null)
            throw new IndexOutOfBoundsException();

        b.remove( getRoot(), true );
    }

    public void remove_element(QNameSet names, int i)
    {
        if (i < 0)
            throw new IndexOutOfBoundsException();

        Splay s = getSplay();

        assert s.isContainer();

        if (!s.isContainer())
            throw new IllegalStateException();

        if (s.isLeaf())
            throw new IndexOutOfBoundsException();

        Begin b = getRoot().findNthBegin(s, null, names, i);

        if (b == null)
            throw new IndexOutOfBoundsException();

        b.remove(getRoot(), true);

    }
    
    public TypeStoreUser add_attribute_user ( QName qname )
    {
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        Splay a = s.getAttr( qname );

        if (a != null)
            throw new IndexOutOfBoundsException();
        
        s.nextSplay().insert(
            getRoot(), 0, a = new Attr( qname ), null, 0, 0, true );
        
        Type t = a.getType( getRoot() );

        assert t != null;

        return t._user;
    }
    
    public void remove_attribute ( QName qname )
    {
        Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        Splay a = s.getAttr( qname );

        if (a == null)
            throw new IndexOutOfBoundsException();
        
        a.remove( getRoot(), true );
    }
    
    public TypeStoreUser copy_contents_from ( TypeStore source )
    {
        assert source instanceof Type;

        Type sourceType = (Type) source;

        Splay s = getSplay();

        s.replaceContents( getRoot(), sourceType.getSplay(), sourceType.getRoot(), true, true );
        
        return s.getType( getRoot() )._user;
    }

    public void array_setter ( XmlObject[] sources, QName elementName )
    {
        // TODO - this is the quick and dirty implementation, make this faster
        
        int m = sources.length;

        ArrayList copies = new ArrayList();
        ArrayList types = new ArrayList();

        for ( int i = 0 ; i < m ; i++ )
        {
// TODO - deal with null sources[ i ] here -- what to do?

            if (sources[ i ] == null)
                throw new IllegalArgumentException( "Array element null" );
            
            else if (sources[ i ].isImmutable())
            {
                copies.add( null );
                types.add( null );
            }
            else
            {
                Type type = (Type) ((TypeStoreUser) sources[ i ]).get_store();
                
                copies.add( type.getSplay().copySplayContents( type.getRoot()));
                
                types.add( sources[ i ].schemaType() );
            }
        }

        int n = count_elements( elementName );

        for ( ; n > m ; n-- )
            remove_element( elementName, m );

        for ( ; m > n ; n++ )
            add_element_user( elementName );

        assert m == n;
        
        ArrayList elements = new ArrayList();
        
        findAllElementTypes( elementName, null, elements );

        Root r = getRoot();
        
        assert elements.size() == n;

        for ( int i = 0 ; i < n ; i++ )
        {
            Type type = (Type) elements.get( i );

            if (sources[ i ].isImmutable())
                type.getXmlObject().set( sources[ i ] );
            else
            {
                Splay s = type.getSplay();

                assert r == type.getRoot();

                assert s.isContainer();
                
                s.removeContent( r, true );
                
                CopyContext copyContext = (CopyContext) copies.get( i );

                Splay copyTree = copyContext.getTree();

                if (copyTree != null)
                {
                    char[] textCopy = copyContext.getText();

                    s.insert(
                        r, 1, copyTree, textCopy, 0, textCopy == null ? 0 : textCopy.length, true );
                }

                type.change_type( (SchemaType) types.get( i ) );
            }
        }
    }
    
    public void visit_elements ( TypeStoreVisitor visitor )
    {
       Splay s = getSplay();

        assert s.isContainer();
        
        if (!s.isContainer())
            throw new IllegalStateException();

        if (s.isLeaf())
            return;
        
        for ( s = s.nextSplay() ; ; s = s.nextSplay() )
        {
            switch ( s.getKind() )
            {
            case Splay.END :
                break;

            case Splay.BEGIN :
                visitor.visit( s.getName() );
                s = s.getFinishSplay();
                break;
            }
        }
    }

    public XmlObject[] exec_query ( String queryExpr, XmlOptions options )
        throws XmlException
    {
        return Path.query( this, queryExpr, options );
    }
    
    //
    //
    //
    
    /**
     * Returns the monitor object, used for synchronizing access to the doc.
     */
    public Object get_root_object()
    {
        return getRoot();
    }

    private final TypeStoreUser _user;
    private int _inhibitUserInvalidate;
}
