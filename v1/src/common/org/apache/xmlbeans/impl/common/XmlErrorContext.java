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

package org.apache.xmlbeans.impl.common;

public abstract class XmlErrorContext
{
    public static final int CANNOT_LOAD_XSD_FILE              =   1;
    public static final int CANNOT_LOAD_XSD_CONFIG_FILE       =   2;
    public static final int NO_SCHEMA_ELEMENT                 =   3;
    public static final int MISMATCHED_TARGET_NAMESPACE       =   4;
    public static final int GLOBAL_TYPE_MISSING_NAME          =   5;
    public static final int TYPE_NOT_FOUND                    =   6;
    public static final int REDUNDANT_NESTED_TYPE             =   7;
    public static final int ELEMENT_MISSING_NAME              =   8;
    public static final int REDUNDANT_DEFAULT_FIXED           =   9;
    public static final int MODEL_GROUP_MISSING_NAME          =  10;
    public static final int ATTRIBUTE_MISSING_NAME            =  11;
    public static final int ATTRIBUTE_GROUP_MISSING_NAME      =  12;
    public static final int CYCLIC_DEPENDENCY                 =  13;
    public static final int UNION_MEMBER_NOT_SIMPLE           =  14;
    public static final int RESTRICTION_REDUNDANT_BASE        =  15;
    public static final int RESTRICTION_MISSING_BASE          =  16;
    public static final int SIMPLE_RESTRICTION_NOT_SIMPLE     =  17;
    public static final int FACET_DOES_NOT_APPLY              =  18;
    public static final int FACET_DUPLICATED                  =  19;
    public static final int FACET_VALUE_MALFORMED             =  20;
    public static final int FACET_FIXED                       =  21;
    public static final int MALFORMED_NUMBER                  =  21;
    public static final int LIST_MISSING_ITEM                 =  22;
    public static final int LIST_ITEM_NOT_SIMPLE              =  23;
    public static final int LIST_OF_LIST                      =  24;
    public static final int LIST_WHITESPACE                   =  25;
    public static final int REDUNDANT_CONTENT_MODEL           =  26;
    public static final int MISSING_RESTRICTION_OR_EXTENSION  =  27;
    public static final int MISSING_BASE                      =  28;
    public static final int ELEMENT_REF_NOT_FOUND             =  29;
    public static final int ELEMENT_EXTRA_REF                 =  30;
    public static final int ALL_CONTENTS                      =  31;
    public static final int EXPLICIT_GROUP_NEEDED             =  32;
    public static final int GROUP_MISSING_REF                 =  33;
    public static final int MODEL_GROUP_NOT_FOUND             =  34;
    public static final int MIN_MAX_OCCURS                    =  35;
    public static final int ATTRIBUTE_REF_NOT_FOUND           =  36;
    public static final int DUPLICATE_ATTRIBUTE_NAME          =  37;
    public static final int DUPLICATE_ANY_ATTRIBUTE           =  38;
    public static final int ATTRIBUTE_GROUP_MISSING_REF       =  39;
    public static final int ATTRIBUTE_GROUP_NOT_FOUND         =  40;
    public static final int COMPLEX_BASE_NOT_COMPLEX          =  41;
    public static final int CANNOT_EXTEND_ALL                 =  42;
    public static final int SIMPLE_BASE_NOT_SIMPLE            =  43;
    public static final int RESERVED_TYPE_NAME                =  44;
    public static final int ILLEGAL_RESTRICTION               =  45;
    public static final int INVALID_SCHEMA                    =  46;
    public static final int DUPLICATE_GLOBAL_ELEMENT          =  47;
    public static final int DUPLICATE_GLOBAL_ATTRIBUTE        =  48;
    public static final int DUPLICATE_GLOBAL_TYPE             =  49;
    public static final int INCONSISTENT_TYPE                 =  50;
    public static final int UNSUPPORTED_FEATURE               =  51;
    public static final int MALFORMED_SIMPLE_TYPE_DEFN        =  52;
    public static final int INVALID_NAME                      =  53;
    public static final int CANNOT_DERIVE_FINAL               =  54;
    public static final int IDC_NOT_FOUND                     =  55;
    public static final int CANNOT_FIND_RESOURCE              =  56;
    public static final int NONDETERMINISTIC_MODEL            =  57;
    public static final int XPATH_COMPILATION_FAILURE         =  58;
    public static final int GENERIC_ERROR                     =  60;
}
