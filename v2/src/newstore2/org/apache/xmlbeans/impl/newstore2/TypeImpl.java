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

package org.apache.xmlbeans.impl.newstore2;

import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreVisitor;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;

import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.QNameSet;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.common.ValidatorListener;

final class TypeImpl
{
//    static void setType ( Cur c, SchemaType type )
//    {
//        assert c.isTypeable();
//
//        TypeStoreUser user = c.getTypeStoreUser();
//
//        if (user != null && user.get_schema_type() == type)
//            return;
//
//        if (c.isRoot())
//        {
//            disconnectTree( c );
//            c.setTypeStoreUser( ((TypeStoreUserFactory) type).createTypeStoreUser() );
//            return;
//        }
//        
//        throw new RuntimeException( "Not impl" );
//    }

//    private static void disconnectTree ( Cur c )
//    {
//        assert c.isTypeable();
//        
//        // Disconnect all type store uses in this tree.  If there is no
//        // user at the top, then there can be no children.
//
//        for ( c.push() ; ! c.isAtEndOfLastPush() || !c.pop() ; )
//        {
//            if (!c.isNode())
//                c.next();
//            else if (c.getTypeStoreUser() != null)
//            {
//                c.setTypeStoreUser( null );
//                c.nextWithAttrs();
//            }
//            else
//                c.toEnd();
//        }
//    }
    
    public static void typeStore_invalidate_text ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static XmlCursor typeStore_new_cursor ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_validate ( TypeStore typeStore, ValidatorListener vEventSink )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static SchemaTypeLoader typeStore_get_schematypeloader ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_change_type ( TypeStore typeStore, SchemaType sType )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static QName typeStore_get_xsi_type ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static String typeStore_fetch_text ( TypeStore typeStore, int whitespaceRule )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_store_text ( TypeStore typeStore, String text )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static String typeStore_compute_default_text ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static int typeStore_compute_flags ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static boolean typeStore_validate_on_set ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static SchemaField typeStore_get_schema_field ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_invalidate_nil ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static boolean typeStore_find_nil ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static int typeStore_count_elements ( TypeStore typeStore, QName name )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static int typeStore_count_elements ( TypeStore typeStore, QNameSet names )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_find_element_user ( TypeStore typeStore, QName name, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_find_element_user ( TypeStore typeStore, QNameSet names, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_find_all_element_users ( TypeStore typeStore, QName name, List fillMeUp )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_find_all_element_users ( TypeStore typeStore, QNameSet name, List fillMeUp )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_insert_element_user ( TypeStore typeStore, QName name, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_insert_element_user ( TypeStore typeStore, QNameSet set, QName name, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_add_element_user ( TypeStore typeStore, QName name )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_remove_element ( TypeStore typeStore, QName name, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_remove_element ( TypeStore typeStore, QNameSet names, int i )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_find_attribute_user ( TypeStore typeStore, QName name )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_add_attribute_user ( TypeStore typeStore, QName name )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_remove_attribute ( TypeStore typeStore, QName name )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static TypeStoreUser typeStore_copy_contents_from ( TypeStore typeStore, TypeStore source )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_array_setter ( TypeStore typeStore, XmlObject[] sources, QName elementName )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static void typeStore_visit_elements ( TypeStore typeStore, TypeStoreVisitor visitor )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static XmlObject[] typeStore_exec_query ( TypeStore typeStore, String queryExpr, XmlOptions options ) throws XmlException
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static Object typeStore_get_root_object ( TypeStore typeStore )
    {
        throw new RuntimeException( "Not implemeneted" );
    }

    public static String typeStore_find_prefix_for_nsuri ( TypeStore typeStore, String nsuri, String suggested_prefix )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
    
    public static String typeStore_getNamespaceForPrefix ( TypeStore typeStore, String prefix )
    {
        throw new RuntimeException( "Not implemeneted" );
    }
}