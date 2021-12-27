/*
 * An XML document type.
 * Localname: root
 * Namespace: partials
 * Java type: partials.RootDocument
 *
 * Automatically generated - do not modify.
 */
package partials.impl;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * A document containing one root(@partials) element.
 *
 * This is a complex type.
 */
public class RootDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements partials.RootDocument {
    private static final long serialVersionUID = 1L;

    public RootDocumentImpl(org.apache.xmlbeans.SchemaType sType) {
        super(sType);
    }

    private static final QName[] PROPERTY_QNAME = {
        new QName("partials", "root"),
    };

// <GET>

    /**
     * Gets the "root" element
     */
    @Override
    public partials.RootDocument.Root getRoot() {
        synchronized (monitor()) {
            check_orphaned();
            partials.RootDocument.Root target = null;
            target = (partials.RootDocument.Root)get_store().find_element_user(PROPERTY_QNAME[0], 0);
            return (target == null) ? null : target;
        }
    }
// </GET>
// <SET>

    /**
     * Sets the "root" element
     */
    @Override
    public void setRoot(partials.RootDocument.Root root) {
        generatedSetterHelperImpl(root, PROPERTY_QNAME[0], 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
// </SET>
// <ADD_NEW>

    /**
     * Appends and returns a new empty "root" element
     */
    @Override
    public partials.RootDocument.Root addNewRoot() {
        synchronized (monitor()) {
            check_orphaned();
            partials.RootDocument.Root target = null;
            target = (partials.RootDocument.Root)get_store().add_element_user(PROPERTY_QNAME[0]);
            return target;
        }
    }
// </ADD_NEW>
    /**
     * An XML root(@partials).
     *
     * This is a complex type.
     */
    public static class RootImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements partials.RootDocument.Root {
        private static final long serialVersionUID = 1L;

        public RootImpl(org.apache.xmlbeans.SchemaType sType) {
            super(sType);
        }

        private static final QName[] PROPERTY_QNAME = {
            new QName("partials", "single"),
            new QName("partials", "complex"),
            new QName("partials", "primitiveList"),
            new QName("partials", "complexList"),
        };

// <GET>

        /**
         * Gets the "single" element
         */
        @Override
        public java.math.BigDecimal getSingle() {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                return (target == null) ? null : target.getBigDecimalValue();
            }
        }
// </GET>
// <XGET>

        /**
         * Gets (as xml) the "single" element
         */
        @Override
        public org.apache.xmlbeans.XmlDecimal xgetSingle() {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                return target;
            }
        }
// </XGET>
// <IS_NIL>

        /**
         * Tests for nil "single" element
         */
        @Override
        public boolean isNilSingle() {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                return target != null && target.isNil();
            }
        }
// </IS_NIL>
// <IS_SET>

        /**
         * True if has "single" element
         */
        @Override
        public boolean isSetSingle() {
            synchronized (monitor()) {
                check_orphaned();
                return get_store().count_elements(PROPERTY_QNAME[0]) != 0;
            }
        }
// </IS_SET>
// <SET>

        /**
         * Sets the "single" element
         */
        @Override
        public void setSingle(java.math.BigDecimal single) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                if (target == null) {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROPERTY_QNAME[0]);
                }
                target.setBigDecimalValue(single);
            }
        }
// </SET>
// <XSET>

        /**
         * Sets (as xml) the "single" element
         */
        @Override
        public void xsetSingle(org.apache.xmlbeans.XmlDecimal single) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                if (target == null) {
                    target = (org.apache.xmlbeans.XmlDecimal)get_store().add_element_user(PROPERTY_QNAME[0]);
                }
                target.set(single);
            }
        }
// </XSET>
// <SET_NIL>

        /**
         * Nils the "single" element
         */
        @Override
        public void setNilSingle() {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[0], 0);
                if (target == null) {
                    target = (org.apache.xmlbeans.XmlDecimal)get_store().add_element_user(PROPERTY_QNAME[0]);
                }
                target.setNil();
            }
        }
// </SET_NIL>
// <UNSET>

        /**
         * Unsets the "single" element
         */
        @Override
        public void unsetSingle() {
            synchronized (monitor()) {
                check_orphaned();
                get_store().remove_element(PROPERTY_QNAME[0], 0);
            }
        }
// </UNSET>
// <GET>

        /**
         * Gets the "complex" element
         */
        @Override
        public partials.XmlBeanchen getComplex() {
            synchronized (monitor()) {
                check_orphaned();
                partials.XmlBeanchen target = null;
                target = (partials.XmlBeanchen)get_store().find_element_user(PROPERTY_QNAME[1], 0);
                return (target == null) ? null : target;
            }
        }
// </GET>
// <IS_SET>

        /**
         * True if has "complex" element
         */
        @Override
        public boolean isSetComplex() {
            synchronized (monitor()) {
                check_orphaned();
                return get_store().count_elements(PROPERTY_QNAME[1]) != 0;
            }
        }
// </IS_SET>
// <SET>

        /**
         * Sets the "complex" element
         */
        @Override
        public void setComplex(partials.XmlBeanchen complex) {
            generatedSetterHelperImpl(complex, PROPERTY_QNAME[1], 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
        }
// </SET>
// <ADD_NEW>

        /**
         * Appends and returns a new empty "complex" element
         */
        @Override
        public partials.XmlBeanchen addNewComplex() {
            synchronized (monitor()) {
                check_orphaned();
                partials.XmlBeanchen target = null;
                target = (partials.XmlBeanchen)get_store().add_element_user(PROPERTY_QNAME[1]);
                return target;
            }
        }
// </ADD_NEW>
// <UNSET>

        /**
         * Unsets the "complex" element
         */
        @Override
        public void unsetComplex() {
            synchronized (monitor()) {
                check_orphaned();
                get_store().remove_element(PROPERTY_QNAME[1], 0);
            }
        }
// </UNSET>
// <GET_LIST>

        /**
         * Gets a List of "primitiveList" elements
         */
        @Override
        public java.util.List<java.math.BigDecimal> getPrimitiveListList() {
            synchronized (monitor()) {
                check_orphaned();
                return new org.apache.xmlbeans.impl.values.JavaListObject<>(
// <GET_IDX>
                    this::getPrimitiveListArray,
// </GET_IDX>
// <GET_IDX_ELSE>
                    null,
// </GET_IDX_ELSE>
// <SET_IDX>
                    this::setPrimitiveListArray,
// </SET_IDX>
// <SET_IDX_ELSE>
                    null,
// </SET_IDX_ELSE>
// <INSERT_IDX>
                    this::insertPrimitiveList,
// </INSERT_IDX>
// <INSERT_IDX_ELSE>
                    null,
// </INSERT_IDX_ELSE>
// <REMOVE_IDX>
                    this::removePrimitiveList,
// </REMOVE_IDX>
// <REMOVE_IDX_ELSE>
                    null,
// </REMOVE_IDX_ELSE>
// <SIZE_OF_ARRAY>
                    this::sizeOfPrimitiveListArray
// </SIZE_OF_ARRAY>
// <SIZE_OF_ARRAY_ELSE>
                    null
// </SIZE_OF_ARRAY_ELSE>
                );
            }
        }
// </GET_LIST>
// <GET_ARRAY>

        /**
         * Gets array of all "primitiveList" elements
         */
        @Override
        public java.math.BigDecimal[] getPrimitiveListArray() {
            return getObjectArray(PROPERTY_QNAME[2], org.apache.xmlbeans.SimpleValue::getBigDecimalValue, java.math.BigDecimal[]::new);
        }
// </GET_ARRAY>
// <GET_IDX>

        /**
         * Gets ith "primitiveList" element
         */
        @Override
        public java.math.BigDecimal getPrimitiveListArray(int i) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                return target.getBigDecimalValue();
            }
        }
// </GET_IDX>
// <XGET_LIST>

        /**
         * Gets (as xml) a List of "primitiveList" elements
         */
        @Override
        public java.util.List<org.apache.xmlbeans.XmlDecimal> xgetPrimitiveListList() {
            synchronized (monitor()) {
                check_orphaned();
                return new org.apache.xmlbeans.impl.values.JavaListXmlObject<>(
// <XGET_IDX>
                    this::xgetPrimitiveListArray,
// </XGET_IDX>
// <XGET_IDX_ELSE>
                    null,
// </XGET_IDX_ELSE>
// <XSET_IDX>
                    this::xsetPrimitiveListArray,
// </XSET_IDX>
// <XSET_IDX_ELSE>
                    null,
// </XSET_IDX_ELSE>
// <INSERT_NEW_IDX>
                    this::insertNewPrimitiveList,
// </INSERT_NEW_IDX>
// <INSERT_NEW_IDX_ELSE>
                    null,
// </INSERT_NEW_IDX_ELSE>
// <REMOVE_IDX>
                    this::removePrimitiveList,
// </REMOVE_IDX>
// <REMOVE_IDX_ELSE>
                    null,
// </REMOVE_IDX_ELSE>
// <SIZE_OF_ARRAY>
                    this::sizeOfPrimitiveListArray
// </SIZE_OF_ARRAY>
// <SIZE_OF_ARRAY_ELSE>
                    null
// </SIZE_OF_ARRAY_ELSE>
                );
            }
        }
// </XGET_LIST>
// <XGET_ARRAY>

        /**
         * Gets (as xml) array of all "primitiveList" elements
         */
        @Override
        public org.apache.xmlbeans.XmlDecimal[] xgetPrimitiveListArray() {
            return xgetArray(PROPERTY_QNAME[2], org.apache.xmlbeans.XmlDecimal[]::new);
        }
// </XGET_ARRAY>
// <XGET_IDX>

        /**
         * Gets (as xml) ith "primitiveList" element
         */
        @Override
        public org.apache.xmlbeans.XmlDecimal xgetPrimitiveListArray(int i) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
// </XGET_IDX>
// <IS_NIL_IDX>

        /**
         * Tests for nil ith "primitiveList" element
         */
        @Override
        public boolean isNilPrimitiveListArray(int i) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                return target.isNil();
            }
        }
// </IS_NIL_IDX>
// <SIZE_OF_ARRAY>

        /**
         * Returns number of "primitiveList" element
         */
        @Override
        public int sizeOfPrimitiveListArray() {
            synchronized (monitor()) {
                check_orphaned();
                return get_store().count_elements(PROPERTY_QNAME[2]);
            }
        }
// </SIZE_OF_ARRAY>
// <SET_ARRAY>

        /**
         * Sets array of all "primitiveList" element
         */
        @Override
        public void setPrimitiveListArray(java.math.BigDecimal[] primitiveListArray) {
            synchronized (monitor()) {
                check_orphaned();
                arraySetterHelper(primitiveListArray, PROPERTY_QNAME[2]);
            }
        }
// </SET_ARRAY>
// <SET_IDX>

        /**
         * Sets ith "primitiveList" element
         */
        @Override
        public void setPrimitiveListArray(int i, java.math.BigDecimal primitiveList) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                target.setBigDecimalValue(primitiveList);
            }
        }
// </SET_IDX>
// <XSET_ARRAY>

        /**
         * Sets (as xml) array of all "primitiveList" element
         */
        @Override
        public void xsetPrimitiveListArray(org.apache.xmlbeans.XmlDecimal[]primitiveListArray) {
            synchronized (monitor()) {
                check_orphaned();
                arraySetterHelper(primitiveListArray, PROPERTY_QNAME[2]);
            }
        }
// </XSET_ARRAY>
// <XSET_IDX>

        /**
         * Sets (as xml) ith "primitiveList" element
         */
        @Override
        public void xsetPrimitiveListArray(int i, org.apache.xmlbeans.XmlDecimal primitiveList) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                target.set(primitiveList);
            }
        }
// </XSET_IDX>
// <SET_NIL_IDX>

        /**
         * Nils the ith "primitiveList" element
         */
        @Override
        public void setNilPrimitiveListArray(int i) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().find_element_user(PROPERTY_QNAME[2], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                target.setNil();
            }
        }
// </SET_NIL_IDX>
// <INSERT_IDX>

        /**
         * Inserts the value as the ith "primitiveList" element
         */
        @Override
        public void insertPrimitiveList(int i, java.math.BigDecimal primitiveList) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target =
                    (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(PROPERTY_QNAME[2], i);
                target.setBigDecimalValue(primitiveList);
            }
        }
// </INSERT_IDX>
// <ADD>

        /**
         * Appends the value as the last "primitiveList" element
         */
        @Override
        public void addPrimitiveList(java.math.BigDecimal primitiveList) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROPERTY_QNAME[2]);
                target.setBigDecimalValue(primitiveList);
            }
        }
// </ADD>
// <INSERT_NEW_IDX>

        /**
         * Inserts and returns a new empty value (as xml) as the ith "primitiveList" element
         */
        @Override
        public org.apache.xmlbeans.XmlDecimal insertNewPrimitiveList(int i) {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().insert_element_user(PROPERTY_QNAME[2], i);
                return target;
            }
        }
// </INSERT_NEW_IDX>
// <ADD_NEW>

        /**
         * Appends and returns a new empty value (as xml) as the last "primitiveList" element
         */
        @Override
        public org.apache.xmlbeans.XmlDecimal addNewPrimitiveList() {
            synchronized (monitor()) {
                check_orphaned();
                org.apache.xmlbeans.XmlDecimal target = null;
                target = (org.apache.xmlbeans.XmlDecimal)get_store().add_element_user(PROPERTY_QNAME[2]);
                return target;
            }
        }
// </ADD_NEW>
// <REMOVE_IDX>

        /**
         * Removes the ith "primitiveList" element
         */
        @Override
        public void removePrimitiveList(int i) {
            synchronized (monitor()) {
                check_orphaned();
                get_store().remove_element(PROPERTY_QNAME[2], i);
            }
        }
// </REMOVE_IDX>
// <GET_LIST>

        /**
         * Gets a List of "complexList" elements
         */
        @Override
        public java.util.List<partials.XmlBeanchen> getComplexListList() {
            synchronized (monitor()) {
                check_orphaned();
                return new org.apache.xmlbeans.impl.values.JavaListXmlObject<>(
// <GET_IDX>
                    this::getComplexListArray,
// </GET_IDX>
// <GET_IDX_ELSE>
                    null,
// </GET_IDX_ELSE>
// <SET_IDX>
                    this::setComplexListArray,
// </SET_IDX>
// <SET_IDX_ELSE>
                    null,
// </SET_IDX_ELSE>
// <INSERT_NEW_IDX>
                    this::insertNewComplexList,
// </INSERT_NEW_IDX>
// <INSERT_NEW_IDX_ELSE>
                    null,
// </INSERT_NEW_IDX_ELSE>
// <REMOVE_IDX>
                    this::removeComplexList,
// </REMOVE_IDX>
// <REMOVE_IDX_ELSE>
                    null,
// </REMOVE_IDX_ELSE>
// <SIZE_OF_ARRAY>
                    this::sizeOfComplexListArray
// </SIZE_OF_ARRAY>
// <SIZE_OF_ARRAY_ELSE>
                    null
// </SIZE_OF_ARRAY_ELSE>
                );
            }
        }
// </GET_LIST>
// <GET_ARRAY>

        /**
         * Gets array of all "complexList" elements
         */
        @Override
        public partials.XmlBeanchen[] getComplexListArray() {
            return getXmlObjectArray(PROPERTY_QNAME[3], new partials.XmlBeanchen[0]);
        }
// </GET_ARRAY>
// <GET_IDX>

        /**
         * Gets ith "complexList" element
         */
        @Override
        public partials.XmlBeanchen getComplexListArray(int i) {
            synchronized (monitor()) {
                check_orphaned();
                partials.XmlBeanchen target = null;
                target = (partials.XmlBeanchen)get_store().find_element_user(PROPERTY_QNAME[3], i);
                if (target == null) {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
// </GET_IDX>
// <SIZE_OF_ARRAY>

        /**
         * Returns number of "complexList" element
         */
        @Override
        public int sizeOfComplexListArray() {
            synchronized (monitor()) {
                check_orphaned();
                return get_store().count_elements(PROPERTY_QNAME[3]);
            }
        }
// </SIZE_OF_ARRAY>
// <SET_ARRAY>

        /**
         * Sets array of all "complexList" element  WARNING: This method is not atomicaly synchronized.
         */
        @Override
        public void setComplexListArray(partials.XmlBeanchen[] complexListArray) {
            check_orphaned();
            arraySetterHelper(complexListArray, PROPERTY_QNAME[3]);
        }
// </SET_ARRAY>
// <SET_IDX>

        /**
         * Sets ith "complexList" element
         */
        @Override
        public void setComplexListArray(int i, partials.XmlBeanchen complexList) {
            generatedSetterHelperImpl(complexList, PROPERTY_QNAME[3], i, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_ARRAYITEM);
        }
// </SET_IDX>
// <INSERT_NEW_IDX>

        /**
         * Inserts and returns a new empty value (as xml) as the ith "complexList" element
         */
        @Override
        public partials.XmlBeanchen insertNewComplexList(int i) {
            synchronized (monitor()) {
                check_orphaned();
                partials.XmlBeanchen target = null;
                target = (partials.XmlBeanchen)get_store().insert_element_user(PROPERTY_QNAME[3], i);
                return target;
            }
        }
// </INSERT_NEW_IDX>
// <ADD_NEW>

        /**
         * Appends and returns a new empty value (as xml) as the last "complexList" element
         */
        @Override
        public partials.XmlBeanchen addNewComplexList() {
            synchronized (monitor()) {
                check_orphaned();
                partials.XmlBeanchen target = null;
                target = (partials.XmlBeanchen)get_store().add_element_user(PROPERTY_QNAME[3]);
                return target;
            }
        }
// </ADD_NEW>
// <REMOVE_IDX>

        /**
         * Removes the ith "complexList" element
         */
        @Override
        public void removeComplexList(int i) {
            synchronized (monitor()) {
                check_orphaned();
                get_store().remove_element(PROPERTY_QNAME[3], i);
            }
        }
// </REMOVE_IDX>
    }
}
