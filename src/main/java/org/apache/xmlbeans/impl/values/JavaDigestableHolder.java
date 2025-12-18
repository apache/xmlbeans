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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.Stream;

abstract class JavaDigestableHolder extends XmlObjectBase {
    protected byte[] _value;

    //because computing hashcode is expensive we'll cache it
    protected boolean _hashcached = false;
    protected int hashcode = 0;
    protected static final MessageDigest digest;

    static {
        digest = Stream.of("MD5", "SHA-1")
            .map(algorithm -> {
                try {
                    return MessageDigest.getInstance(algorithm);
                }
                catch (NoSuchAlgorithmException ex) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find any suitable hash algorithm"));
    }

    protected int value_hash_code() {
        if (_hashcached) {
            return hashcode;
        }

        _hashcached = true;

        if (_value == null) {
            return hashcode = 0;
        }

        byte[] res = digest.digest(_value);
        return hashcode = (res[0] << 24) | (res[1] << 16) | (res[2] << 8) | res[3];
    }
}
