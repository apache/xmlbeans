<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2004 The Apache Software Foundation

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License. -->

<xs:schema
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://xbean/scomp/redefine/GroupRedefine"
    targetNamespace="http://xbean/scomp/redefine/GroupRedefine"
    >
    <xs:redefine schemaLocation="BaseModelGroup.xs">
        <xs:complexType name="GroupT">
         <xs:complexContent>
             <xs:restriction base="GroupT">
                <xs:sequence>
                   <xs:element name="child2" type="xs:string"/>
            </xs:sequence>
             </xs:restriction>
         </xs:complexContent>
        </xs:complexType>
    </xs:redefine>

    <xs:element name="GroupSubElt" type="GroupT"/>

</xs:schema>