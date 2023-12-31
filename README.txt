/*  Copyright 2004-2024 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

Welcome to XMLBeans!

Layout of the tree:
- README.txt    this file
- build.gradle  gradle build file for building XmlBeans code and utilities
- STATUS        essential info about the project
- xbeanenv.cmd  sets the XMLBEANS_HOME environment variable on Windows
- xbeanenv.sh   sets the XMLBEANS_HOME environment variable on Unix
- bin           contains useful command-line scripts for Win and Unix
- docs          contains several documentation files
- javadocs      API documentation
- maven-plugin  source and documentation on using the xmlbeans2 Maven plugin
- samples       contains ... samples
- src           the source code for XmlBeans, organized by area
- test          contains test cases and infrastructure
- xkit          contains the README file for the binary distribution

To quickly get started run:
"gradlew jar" to build XmlBeans or
"gradlew tasks" to see the most useful build targets or
"cd bin" and then "<tool_name> -help" for a description of what the
     tool does and what parameters it accepts
 
For further information check out:
http://xmlbeans.apache.org
http://wiki.apache.org/xmlbeans
./samples/<sample_name>/README.txt  for running samples
./test/docs/*                       for running tests

XmlBeans depends on the following external libraries:
- log4j-api [https://logging.apache.org/log4j/2.x/] for logging
- javaparser-core (O) [https://github.com/javaparser/javaparser]
- Saxon-HE (O) [http://saxon.sourceforge.net/]
  for XPath/XQuery in XmlBeans
(O) means that the library is optional

For licensing information, see LICENSE.txt
Updated: 1 January 2024.
