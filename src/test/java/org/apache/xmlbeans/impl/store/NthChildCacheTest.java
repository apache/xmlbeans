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
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.junit.jupiter.api.Test;
import org.openuri.nameworld.Loc;
import org.openuri.nameworld.NameworldDocument;
import org.openuri.nameworld.NameworldDocument.Nameworld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The store resumes an indexed walk from the last child it returned, per parent. What that is
 * worth cannot be seen from the outside - the answers are the same either way, only the number of
 * child lists walked differs - so these read the re-seed counter the store keeps.
 *
 * <p>A pass that resumes properly steps over each child about once. A pass that keeps losing its
 * place walks the child list again from the start on every step, which is quadratic.</p>
 */
class NthChildCacheTest {

    private static Nameworld nameworld(int islands, int locations) throws XmlException {
        StringBuilder xml = new StringBuilder("<nameworld xmlns='http://openuri.org/nameworld'>");
        for (int i = 0; i < islands; i++) {
            xml.append("<island targetNamespace='is").append(i).append("'>");
            for (int l = 0; l < locations; l++) {
                xml.append("<location name='loc").append(l).append("'/>");
            }
            xml.append("</island>");
        }
        xml.append("</nameworld>");

        return NameworldDocument.Factory.parse(xml.toString()).getNameworld();
    }

    private static Locale locale(Nameworld world) {
        return ((Xobj) ((TypeStoreUser) world).get_store())._locale;
    }

    /** A nested indexed pass, returning how many children the caches stepped over. */
    private static long stepsForNestedPass(int islands, int locations) throws XmlException {
        Nameworld world = nameworld(islands, locations);
        Locale locale = locale(world);
        locale._nthCacheSteps = 0;

        for (int i = 0; i < islands; i++) {
            Nameworld.Island island = world.getIslandArray(i);

            for (int l = 0; l < locations; l++) {
                Loc location = island.getLocationArray(l);
                assertEquals("loc" + l, location.getName());
            }
        }

        return locale._nthCacheSteps;
    }

    @Test
    void aNestedPassWalksEachChildAboutOnce() throws Exception {
        int islands = 400;
        int locations = 4;

        long steps = stepsForNestedPass(islands, locations);

        // a pass that keeps its place steps over each child about once: islands * locations,
        // plus the islands themselves. Losing the outer position makes the outer walk quadratic.
        long walked = (long) islands * locations + islands;

        assertTrue(steps <= 4 * walked,
            "expected about " + walked + " steps, got " + steps);
    }

    @Test
    void theWalkGrowsWithTheWorkNotWithItsSquare() throws Exception {
        // twice the islands is twice the parents, so twice the re-seeds - not four times
        long small = stepsForNestedPass(200, 4);
        long large = stepsForNestedPass(400, 4);

        assertTrue(large < small * 3,
            "steps went from " + small + " to " + large + " when the work only doubled");
    }

    @Test
    void deeperNestingStillResumesEachLevel() throws Exception {
        // three levels of parents at once, which is what the cache has to hold
        int islands = 400;
        Nameworld world = nameworld(islands, 6);
        Locale locale = locale(world);
        locale._nthCacheSteps = 0;

        for (int i = 0; i < islands; i++) {
            for (int l = 0; l < 6; l++) {
                assertEquals("loc" + l, world.getIslandArray(i).getLocationArray(l).getName());
            }
        }

        assertTrue(locale._nthCacheSteps <= 4 * ((long) islands * 6 + islands),
            "steps: " + locale._nthCacheSteps);
    }
}
