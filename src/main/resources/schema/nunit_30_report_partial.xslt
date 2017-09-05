<?xml version="1.0" encoding="UTF-8" ?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
    <xs:element name="test-run" type="testRunType"/>

    <xs:complexType name="testRunType">
        <xs:sequence>
            <xs:element name="test-suite"/>
            <xs:any minOccurs="0"/>
        </xs:sequence>
        <xs:anyAttribute/>
    </xs:complexType>
</xs:schema>