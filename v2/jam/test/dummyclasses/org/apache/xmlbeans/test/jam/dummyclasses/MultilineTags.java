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
package org.apache.xmlbeans.test.jam.dummyclasses;

/**
 * @foo x = " this is a tag value which
 * spans several
 *
 * lines indeed"
 *
 * @bar y="  This is also a tag which spans
 *
 * a few lines but also has its closing
 *   quote in a weird place  "  see?
 *
 * @baz z=                       "
 *
 *                  I guess even something weird like
 *                       this should work
 *
 *
 *                              "
 * @boo a=   "  Here is a tag with multiple
 *   multiline values"
 * b = " here is another"
 * c = " and another
 * l = but this is still the same one
 * whoa that was = tricky"
 * d = "yup"
 *
 * @ejbgen:cmp-field column = "LicensorEntityID, CompanyID"
 * table-name= "LicensorEntity, Company"
 *
 * @ejbgen:finder
 *   signature = "java.util.Collection findByName(java.lang.String name)"
 *   ejb-ql = "SELECT OBJECT(o)
 *    FROM BandEJB AS o
 *    WHERE o.name = ?1"
 *
 * @ejbgen:cmp-field ordering-number="10" primkey-field="true" column="seth"
 *
 * @bee question = Can we confuse the parser?   "
 *     " =  "
 *   " ""
 *                                           " = = "
 *
 * @oldstyleSingleMemberValueTag I should be called 'value'
 *
 */
public interface MultilineTags {
}
