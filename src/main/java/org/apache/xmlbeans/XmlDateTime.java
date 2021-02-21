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

package org.apache.xmlbeans;

import org.apache.xmlbeans.impl.schema.XmlObjectFactory;

import java.util.Calendar;
import java.util.Date;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#dateTime">xs:dateTime</a> type.
 * <p>
 * Convertible to {@link Calendar}, {@link Date}, and {@link GDate}.
 *
 * <p>
 * The XmlDateTime class only encapsulates a schema DateTime value, if you need to perform operations
 * on dates, see the GDate class
 *
 * @see XmlCalendar
 * @see GDate
 * @see GDuration
 */
public interface XmlDateTime extends XmlAnySimpleType {
    XmlObjectFactory<XmlDateTime> Factory = new XmlObjectFactory<>("_BI_dateTime");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();

    /**
     * Returns this value as a {@link Calendar}
     */
    Calendar getCalendarValue();

    /**
     * Sets this value as a {@link Calendar}
     */
    void setCalendarValue(Calendar c);

    /**
     * Returns this value as a {@link GDate}
     */
    GDate getGDateValue();

    /**
     * Sets this value as a {@link GDateSpecification}
     */
    void setGDateValue(GDate gd);

    /**
     * Returns this value as a {@link Date}
     */
    Date getDateValue();

    /**
     * Sets this value as a {@link Date}
     */
    void setDateValue(Date d);
}

