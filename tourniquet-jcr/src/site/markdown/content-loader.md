ContentLoader
--------------------------------------------

The ContentLoader rule may be used to load predefined content into the repository. The content may be defined in an 
XML file, including node types, properties and binary content.

The content definition must be valid against the XSD schema.
An example:

    <?xml version="1.0" encoding="UTF-8"?>
    <ink:rootNode xmlns:ink="http://tourniquet.io/schemas/jcr-import"
                  primaryType="nt:unstructured" name="root">
        <ink:mixin name="mix:title"/>
        <ink:property name="jcr:title" jcrType="STRING">TestTitle</ink:property>
    </ink:rootNode>

This will create a node named "root" as child of the root node "/". The node is of type ```nt:unstructured``` and 
```mix:title``` with the ```jcr:title``` property being set to "TestTitle".

Further nodes can be added by adding the ```<node>``` element with the same semantics as the root node

    <?xml version="1.0" encoding="UTF-8"?>
    <ink:rootNode xmlns:ink="http://tourniquet.io/schemas/jcr-import"
                  primaryType="nt:unstructured" name="root">
        <ink:mixin name="mix:title"/>
        <ink:property name="jcr:title" jcrType="STRING">TestTitle</ink:property>
        <ink:node primaryType="nt:unstructured" name="child">
            <ink:property name="myProperty" jcrType="STRING">PropertyValue</ink:property>
        </ink:node>
    </ink:rootNode>

### Structure

The basic structure for the content definition is

    <rootNode>
        <mixin>*|<property>*|<node>*
        +name (string)
        +path? (string)
        +primaryType (string)
        +id (ID)
     
    <node>
        <mixin>*|<property>*|<node>*
        +name (string)
        +path? (string)
        +primaryType (string)
        +id (ID)
     
    <mixin>
        +name (string)
     
    <property>
        +name
        +jcrType
        +ref?

### Property Types

The schema accepts the following property types, which map to the according JCR property types:

- BINARY (has to be embedded as CDATA block in base64 encoding)
- DATE
- DECIMAL
- DOUBLE
- LONG
- NAME
- PATH
- REFERENCE (not supported yet)
- WEAKREFERENCE (not supported yet)
- STRING
- UNDEFINED
- URI

All values accept a string representation that must be convertible using the JCR ValueFactory.
