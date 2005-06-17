Sample: XQueryXPath
Author: Steven Traut (straut@bea.com)
Last Updated: June 8, 2005

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample illustrates how you can use the XMLBeans API to execute
XPath and XQuery expressions. The sample illustrates these features:

- Using the XmlObject.selectPath and XmlCursor.selectPath methods
to execute XPath expressions. The selectPath method's results (if
any) are always chunks of the instance queried against. In other
words, changes to query results change the original instance.
However, you work with results differently depending on whether
selectPath was called from an XmlObject or XmlCursor instance. See
the SelectPath class for more information.

- Using the XmlObject.execQuery and XmlCursor.execQuery methods
to execute XQuery expressions. Results of these queries are copied
into new XML, meaning that changes to results do not change the 
original instance. Here again, you work with results differently
depending how which method you used to query. See the ExecQuery
class for more information.

A note about dependencies. Very simple XPath expressions -- e.g.,
expressions without predicates or function calls -- require only
the xbean.jar on your class path. More complex expressions require
xbean_xpath.jar. XQuery expressions require the Saxon 8.1.1 JAR. 
Both xbean_xpath.jar and saxon8.jar are created for you when you build
XMLBeans from Apache source code. These files are required on the class
path for code in this sample to run.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. xbean_xpath.jar and saxon8.jar must be on your class path.
   These files are created in the build/lib directory when you 
   build XMLBeans from source.
4. To compile the schemas and sample source, run "ant build"
5. To execute the sample, run "ant run"
