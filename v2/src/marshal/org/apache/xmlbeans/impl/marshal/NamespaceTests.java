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

package org.apache.xmlbeans.impl.marshal;

class NamespaceTests
{
    public static void main(String[] args)
    {

        ScopedNamespaceContext ctx = new ScopedNamespaceContext();

        assert (0 == ctx.getCurrentScopeNamespaceCount());
        ctx.openScope();
        assert (0 == ctx.getCurrentScopeNamespaceCount());

        ctx.bindNamespace("p1.1", "n1.1");
        assert (1 == ctx.getCurrentScopeNamespaceCount());

        ctx.bindNamespace("p1.2", "n1.2");
        assert (2 == ctx.getCurrentScopeNamespaceCount());

        ctx.openScope();

        ctx.bindNamespace("p2.1", "n2.1");
        assert (1 == ctx.getCurrentScopeNamespaceCount());
        ctx.bindNamespace("p2.2", "n2.2");
        assert (2 == ctx.getCurrentScopeNamespaceCount());

        assert "n1.1".equals(ctx.getNamespaceURI("p1.1"));
        assert "n2.1".equals(ctx.getNamespaceURI("p2.1"));

        ctx.closeScope();

        assert "n1.1".equals(ctx.getNamespaceURI("p1.1"));
        assert (ctx.getNamespaceURI("p2.1") == null);

//        final int trials = 100000000;
//        for(int i = 1 ; i < trials ; i++) {
//            ctx.openScope();
//            ctx.bindNamespace("p1.1", "n2.1");
//            ctx.openScope();
//            ctx.bindNamespace("p2.2", "n2.2");
//            ctx.closeScope();
//            ctx.closeScope();
//        }


    }

}
