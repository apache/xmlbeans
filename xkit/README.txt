XMLBeans v1.0.4 Development Kit


Welcome to XMLBeans!


Kit contents:

(1) One copy of xbean.jar, which contains XMLBeans.
    Should work on any JDK 1.4.x.
    ./lib/xbean.jar

(2) License information for XML Beans and included libraries
    ./license.txt

(3) One folder full of command-line scripts, pointing to the
    useful main() functions in the JAR.
    ./bin

(4) One bit of ant task documentation.
    ./anttask.html

(5) A copy of the plain javadoc tree for org.apache.xmlbeans.*
    ./docs/reference

(6) A preliminary collection of nicely formatted user-level
    documentation HTML (includes reformatted and nicely
    organized javadoc as well)
    ./docs/guide

(7) A few sample schemas
    ./schemas


Where to start?

(1) Setup.

    1. make sure you have a JDK 1.4.x installed; that java[.exe]
       is on your path and that JAVA_HOME/bin contains java[.exe],
       javac[.exe], and jar[.exe].

    2. set your XMLBEANS_HOME env variable to point to the directory
       containing xbean.jar (i.e., ./lib).

    3. put the scripts in ./bin on your path.

    4. run "scomp" with no arguments.  You should get a "usage"
       message.

(2) Try some schema compilation

    1. In the ./schemas directory you'll find some collections of
       schemas you can try out.

         - easypo: a contrived simple starter "purchase order"
         - s4s: the Schema for Schema

       To compile them, you can just send the whole directory to
       scomp, for example, "cd samples"; then "scomp easypo".
       You will get an "xmltypes.jar" out that contains all the
       compiled XMLBeans.  To pick your own JAR filename just say

       scomp -out myeasypo.jar easypo


    2. Especially as you get started, you will want to see the
       .java source code for the generated code.  To get that,
       use a command-line like

       scomp -src mysrcdir -out myeasypo.jar easypo

       The "mysrcdir" will contain all the .java source code
       for the generated XMLBeans.

(3) Try using your compiled XMLBeans

    Now, armed with the XMLBeans source code and the basic
    docs, you're ready to program.  Things you need to know:

    * The org.apache.xmlbeans package has all the public classes
      for XMLBeans.  Programs should not need to call anything
      else in xbean.jar directly.

    * XmlObject is the base class for all XMLBeans.  It
      corresponds to xs:anyType.

    * Every schema type corresponds to an XMLBean interface,
      e.g., XmlAnySimpleType corresponds to xs:anySimpleType, and
      XmlInt corresponds to xs:int, etc.. And of course this
      extends to the XMLBean classes compiled from user-defined
      schemas.

    * Every XMLBean interface has an inner Factory class for
      creating or parsing instances, e.g., to load a file of
      generic type, use XmlObject.Factory.parse(myfile); to
      parse a string you expect to be a purcahse-order, use
      PurchaseOrderDocument.Factory.parse("<ep:purchase-o...");

    * XmlCursor is the API for full XML infoset treewalking.
      It is obtained via xmlobject.newCursor(). Using it is
      less convenient, but faster than using XML Objects,
      because it does not create objects as it traverses
      the XML tree.

    * SchemaType is the basic "schema reflection" API (just like
      Class, but for Schema).  Get the actual schema type of any
      instance by saying "xobj.schemaType();" get the static
      constant schema type corresponding to any XMLBean class
      by saying "MyPurchaseOrder.type" or "XmlInt.type".
      (Analogous to "obj.getClass()" and "Object.class".)

    * A number of utility methods are avaliable on
      org.apache.xmlbeans.XmlBeans, including a function that can be
      used to determine whether a Java class is an XmlBean and
      functions to manage runtime-loading of schema type
      systems.

    With that, you're ready to navigate the javadoc and play
    with the code.  Also, try reading some of our 
    docs that are included in ./docs

(4) Try some of the other utilities; you can also see a few
    examples of XMLBean techniques in their source code.

    1. "xpretty instance.xml" pretty-prints an XML instance
       document.

       The code is in xml.apache.org.tool.PrettyPrinter and is
       a reasonable example of how to load and save out an
       arbitrary XML document.  XmlOptions are used to produce
       the pretty-printing.

    2. "validate instance.xml schema.xsd" will validate the
       instance against the schema.  XMLBeans is intended to
       be a very accurate XML schema validator.

       The code is in xml.apache.org.tool.InstanceValidator.
       It is an excellent example of how to load a schema
       type system dynamically at runtime, load and validate
       an instance within that type system, and how to obtain
       lists of and locations for validation errors.

    3. "xsdtree easypo" will show the inheritance hierarchy
       of the schema types in that directory.

       The code is in xml.apache.org.tool.TypeHierarchyPrinter
       and is a good introduction to how to traverse the
       metadata in a schema type system.

    4. "dumpxsb xbean.jar" or "dumpxsb myfile.xsb" will dump
       the contents of "xsb" (binary schema metadata) files
       in a human-readable form.  These .xsb files contain
       the compiled metadata resulting from the .xsd files
       of a type system.  They are analogous to .class files
       for .java.

