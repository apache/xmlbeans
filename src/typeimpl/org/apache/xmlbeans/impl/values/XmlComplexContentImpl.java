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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.*;

import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import javax.xml.namespace.QName;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeVisitorImpl;

public class XmlComplexContentImpl extends XmlObjectBase
{
    public XmlComplexContentImpl(SchemaType type)
    {
        _schemaType = (SchemaTypeImpl)type;
        initComplexType(true, true);
    }

    public SchemaType schemaType()
        { return _schemaType; }

    private SchemaTypeImpl _schemaType;

    public String compute_text(NamespaceManager nsm)
        { return null; }

    protected final void set_String(String v)
    {
        assert _schemaType.getContentType() != SchemaType.SIMPLE_CONTENT;

        if (_schemaType.getContentType() != SchemaType.MIXED_CONTENT &&
                !_schemaType.isNoType())
        {
            throw new IllegalArgumentException(
                "Type does not allow for textual content: " + _schemaType );
        }

        super.set_String(v);
    }
    
    public void set_text(String str)
    {
        assert
            _schemaType.getContentType() == SchemaType.MIXED_CONTENT ||
                _schemaType.isNoType();
    }

    protected void update_from_complex_content()
    {
        // No complex caching yet ...
    }
    
    public void set_nil()
        { /* BUGBUG: what to do? */ }

    // LEFT
    public boolean equal_to(XmlObject complexObject)
    {
        if (!_schemaType.equals(complexObject.schemaType()))
            return false;

        // BUGBUG: by-value structure comparison undone
        return true;
    }

    // LEFT
    protected int value_hash_code()
    {
        throw new IllegalStateException("Complex types cannot be used as hash keys");
    }

    // DONE
    public TypeStoreVisitor new_visitor()
    {
        return new SchemaTypeVisitorImpl(_schemaType.getContentModel());
    }

    // DONE
    public boolean is_child_element_order_sensitive()
    {
        return schemaType().isOrderSensitive();
    }

    public int get_elementflags(QName eltName)
    {
        SchemaProperty prop = schemaType().getElementProperty(eltName);
        if (prop == null)
            return 0;
        if (prop.hasDefault() == SchemaProperty.VARIABLE ||
            prop.hasFixed() == SchemaProperty.VARIABLE ||
            prop.hasNillable() == SchemaProperty.VARIABLE)
            return -1;
        return
            (prop.hasDefault() == SchemaProperty.NEVER ? 0 : TypeStore.HASDEFAULT) |
            (prop.hasFixed() == SchemaProperty.NEVER ? 0 : TypeStore.FIXED) |
            (prop.hasNillable() == SchemaProperty.NEVER ? 0 : TypeStore.NILLABLE);
    }

    // DONE
    public String get_default_attribute_text(QName attrName)
    {
        return super.get_default_attribute_text(attrName);
    }

    // DONE
    public String get_default_element_text(QName eltName)
    {
        SchemaProperty prop = schemaType().getElementProperty(eltName);
        if (prop == null)
            return "";
        return prop.getDefaultText();
    }

    //
    // Code gen helpers
    //
    // So much redundant code ..... what I'd give for generics!
    //

    protected void unionArraySetterHelper ( Object[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).objectSet( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( boolean[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( float[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( double[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( byte[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( short[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( int[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( long[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( BigDecimal[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( BigInteger[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( String[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( byte[][] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( GDate[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( GDuration[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( Calendar[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( Date[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( QName[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( StringEnumAbstractBase[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( List[] sources, QName elemName )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( elemName );

        for ( ; m > n ; m-- )
            store.remove_element( elemName, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( elemName, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void unionArraySetterHelper ( Object[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).objectSet( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( boolean[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( float[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( double[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( byte[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( short[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( int[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( long[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( BigDecimal[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( BigInteger[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( String[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( byte[][] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( GDate[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( GDuration[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( Calendar[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( Date[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;

        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;

            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( QName[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( StringEnumAbstractBase[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }
    
    protected void arraySetterHelper ( List[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

    protected void arraySetterHelper ( XmlObject[] sources, QName elemName )
    {
        get_store().array_setter( sources, elemName );
    }

    protected void arraySetterHelper ( XmlObject[] sources, QName elemName, QNameSet set )
    {
        int n = sources == null ? 0 : sources.length;
        
        TypeStore store = get_store();

        int m = store.count_elements( set );

        for ( ; m > n ; m-- )
            store.remove_element( set, m - 1 );

        for ( int i = 0 ; i < n ; i++ )
        {
            TypeStoreUser user;
            
            if (i >= m)
                user = store.add_element_user( elemName );
            else
                user = store.find_element_user( set, i );

            ((XmlObjectBase) user).set( sources[ i ] );
        }
    }

}
