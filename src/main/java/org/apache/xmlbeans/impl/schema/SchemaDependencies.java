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

package org.apache.xmlbeans.impl.schema;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaDependencies {
    // This class is NOT synchronized

    /**
     * Records the list of files associated to each namespace.
     * This is needed so that we can return a list of files that
     * need to be compiled once we get a set of altered namespaces
     */
    private final Map<String, List<String>> _contributions = new HashMap<>();

    /**
     * Records anti-dependencies. Keys are namespaces and values are
     * the lists of namespaces that depend on each key
     */
    private final Map<String, Set<String>> _dependencies = new HashMap<>();

    void registerDependency(String source, String target) {
        _dependencies.computeIfAbsent(target, k -> new HashSet<>()).add(source);
    }


    Set<String> computeTransitiveClosure(List<String> modifiedNamespaces) {
        List<String> nsList = new ArrayList<>(modifiedNamespaces);
        Set<String> result = new HashSet<>(modifiedNamespaces);
        for (int i = 0; i < nsList.size(); i++) {
            Set<String> deps = _dependencies.get(nsList.get(i));
            if (deps == null) {
                continue;
            }
            for (String ns : deps) {
                if (!result.contains(ns)) {
                    nsList.add(ns);
                    result.add(ns);
                }
            }
        }
        return result;
    }

    SchemaDependencies() {
    }

    SchemaDependencies(SchemaDependencies base, Set<String> updatedNs) {
        for (String target : base._dependencies.keySet()) {
            if (updatedNs.contains(target)) {
                continue;
            }
            Set<String> depSet = new HashSet<>();
            _dependencies.put(target, depSet);
            Set<String> baseDepSet = base._dependencies.get(target);
            for (String source : baseDepSet) {
                if (updatedNs.contains(source)) {
                    continue;
                }
                depSet.add(source);
            }
        }
        for (String ns : base._contributions.keySet()) {
            if (updatedNs.contains(ns)) {
                continue;
            }
            List<String> fileList = new ArrayList<>();
            _contributions.put(ns, fileList);
            fileList.addAll(base._contributions.get(ns));
        }
    }

    void registerContribution(String ns, String fileURL) {
        _contributions.computeIfAbsent(ns, k -> new ArrayList<>()).add(fileURL);
    }

    boolean isFileRepresented(String fileURL) {
        return _contributions.values().stream().anyMatch(l -> l.contains(fileURL));
    }

    List<String> getFilesTouched(Set<String> updatedNs) {
        return updatedNs.stream().map(_contributions::get).
            filter(Objects::nonNull).flatMap(List::stream).
            collect(Collectors.toList());
    }

    List<String> getNamespacesTouched(Set<String> modifiedFiles) {
        return _contributions.entrySet().stream().
            filter(e -> e.getValue().stream().anyMatch(modifiedFiles::contains)).
            map(Map.Entry::getKey).
            collect(Collectors.toList());
    }
}
