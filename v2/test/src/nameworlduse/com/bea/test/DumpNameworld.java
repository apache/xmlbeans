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

package com.bea.test;

import org.openuri.nameworld.*;

import java.io.File;

import org.apache.xmlbeans.XmlBeans;

public class DumpNameworld
{
    public static void main(String[] args) throws Exception
    {
        for (int i = 0; i < args.length; i++)
        {
            System.out.println("======================");
            System.out.println("Loading file " + args[i]);
            dumpDocument(NameworldDocument.Factory.parse(new File(args[i])));
        }
    }

    public static void dumpDocument(NameworldDocument doc)
    {
        Nameworld world = doc.getNameworld();
        Nameworld.Island[] islands = world.getIsland();
        for (int i = 0; i < islands.length; i++)
        {
            System.out.println("----------------------");
            System.out.println("Island target namespace: " + islands[i].getTargetNamespace());
            Loc[] locs = islands[i].getLocation();
            for (int j = 0; j < locs.length; j++)
            {
                System.out.println("  Location: " + locs[j].getName());
                Loc.Reference[] refs = locs[j].getReference();
                for (int k = 0; k < refs.length; k++)
                {
                    System.out.println("  Reference: " + refs[k].getTo());
                }
            }
        }
    }
}