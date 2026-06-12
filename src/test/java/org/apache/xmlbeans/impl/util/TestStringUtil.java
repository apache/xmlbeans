/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.xmlbeans.impl.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStringUtil {
    @Test
    void equalsIgnoreCase() {
        assertTrue(StringUtil.equalsIgnoreCase(null, null), "2 nulls are equal");
        assertFalse(StringUtil.equalsIgnoreCase(null, ""), "null and empty strings are not equal");
        assertFalse(StringUtil.equalsIgnoreCase("", null), "null and empty strings are not equal");
        assertFalse(StringUtil.equalsIgnoreCase("abc", "abcd"), "different lengths do not match");
        assertFalse(StringUtil.equalsIgnoreCase("abcd", "abc"), "different lengths do not match");
        assertTrue(StringUtil.equalsIgnoreCase("abc", "abc"), "abc and abc match");
        assertTrue(StringUtil.equalsIgnoreCase("abc", "Abc"), "abc and Abc match");
        assertFalse(StringUtil.equalsIgnoreCase("İ", "i"), "Turkish dotted İ and i do not match (root locale)");
        assertFalse(StringUtil.equalsIgnoreCase("İ", "I"), "Turkish dotted İ and I do not match (root locale)");
        assertFalse(StringUtil.equalsIgnoreCase("ı", "I"), "Turkish dotless ı and I do not match (root locale)");
    }
}
