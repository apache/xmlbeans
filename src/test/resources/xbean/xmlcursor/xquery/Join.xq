(: Copyright 2004 The Apache Software Foundation

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License. :)

for $a in .//employee
return <result>
		{ $a/ssn },
		{ $a/name },
		{
		for $b in .//employee
		where $b/ssn=$a/ssn and $a/name !=$b/name
		return $b/name
		}
	</result>
