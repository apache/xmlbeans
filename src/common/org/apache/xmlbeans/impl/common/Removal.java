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

package org.apache.xmlbeans.impl.common;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Program elements annotated &#64;Removal track the earliest final release
 * when a deprecated feature will be removed. This is an internal decoration:
 * a feature may be removed in a release earlier or later than the release
 * number specified by this annotation.<p>
 *
 * The XmlBeans project policy is to deprecate an element for on the next major release
 * before removing. This annotation exists to make it easier to follow up on the
 * second step of the two-step deprecate and remove process.<p>
 *
 * A deprecated feature may be removed in nightly release prior
 * to the major release for which it is eligible, but may be removed later for
 * various reasons. If it is known in advance that the feature will not be
 * removed in the next major release, a later version should be specified by this
 * annotation.<p>
 *
 * For example, a feature with a {@code &#64;deprecated XmlBeans 3.1}
 * is deprecated in XmlBeans 3.1 and may be deleted in the preparation for XmlBeans 4.0.
 * This would be annotated {@code &#64;Removal(version="3.17")}.
 *
 * @since XmlBeans 3.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Removal {
    /**
     * The XmlBeans version when this feature may be removed.
     *
     * To ensure that the version number can be compared to the current version
     * and a unit test can generate a warning if a removal-eligible feature has
     * not been removed yet, the version number should adhere to the following format:
     * Format: "(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)"
     * Example: "3.1.5"
     */
    String version() default "";
    // TODO: Verify that the version syntax is valid by parsing with a version-aware parser like
    // org.apache.maven.artifact.versioning.DefaultArtifactVersion
}
