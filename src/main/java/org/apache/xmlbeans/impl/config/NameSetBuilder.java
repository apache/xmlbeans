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
/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Apr 25, 2004
 */
package org.apache.xmlbeans.impl.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to build {@link NameSet NameSets}.
 */
public class NameSetBuilder
{
    private boolean _isFinite = true;
    private final Set<String> _finiteSet = new HashSet<>();

    /**
     * Inverts the representing NameSet
     */
    public void invert()
    {
        _isFinite = !_isFinite;
    }

    /**
     * Adds a name to the representing NameSet
     * @param name
     */
    public void add(String name) {
        if (_isFinite) {
            _finiteSet.add(name);
        } else {
            _finiteSet.remove(name);
        }
    }

    /**
     * Creates a new NameSet with the current state.
     *
     * @return created NameSet
     */
    public NameSet toNameSet() {
        if (_finiteSet.size() == 0) {
            if (_isFinite) {
                return NameSet.EMPTY;
            } else {
                return NameSet.EVERYTHING;
            }
        } else {
            return NameSet.newInstance(_isFinite, _finiteSet);
        }
    }
}