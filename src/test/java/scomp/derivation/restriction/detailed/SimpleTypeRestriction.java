
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

package scomp.derivation.restriction.detailed;

import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.facets.dateTimePattern.DateTimes;
import xbean.scomp.derivation.facets.dateTimePattern.DateTimesDocument;
import xbean.scomp.derivation.simpleTypeRestriction.SmallPantSizeEltDocument;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class SimpleTypeRestriction {

    @Test
    void testPatternRestriction() throws Throwable {
        SmallPantSizeEltDocument doc = SmallPantSizeEltDocument.Factory.newInstance();
        doc.setSmallPantSizeElt(8);
        //doc.setSmallPantSizeElt(6);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testDateTimeRestriction() throws Throwable {
        DateTimesDocument doc = DateTimesDocument.Factory.newInstance();
        Calendar c = new GregorianCalendar(2004, Calendar.SEPTEMBER, 10);
        DateTimes date = DateTimes.Factory.newInstance();
        date.setExtendedDate1(c);
        date.setExtendedDate2("2004-08-10");
        c.set(2004, Calendar.SEPTEMBER, 10, 12, 10);
        date.setExtendedDateTime1(c);
        date.setExtendedDateTime2(c);
        date.setExtendedDateTimeAny3(c);

        // TODO: add asserts
    }
}
