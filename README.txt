XMLBeans v1.0.4

Welcome to XmlBeans!


If you've come here to learn more about XmlBeans, here are a few
starting points:




(1) Docs

If you "ant docs" then you can get javadocs built for a bunch
of our stuff. In particular, look at the org.apache.xmlbeans package,
starting with XmlObject and XmlCursor.

Not a lot is doc'ed yet, so you may want to check out the samples
to learn more - we really need a little tutorial.

In the mean time, if you've got more questions, please post your questions
to xmlbeans-user@xml.apache.org (you need to subscribe prior to posting,
by sending a blank mail to xmlbeans-user-subscribe@xml.apache.org).



(2) Build

The default target, aka "ant default", builds everything you need
to test and run.


Other targets:
  ant build: builds the public interface jars so other dependencies can compile
  ant deploy: builds xbean.jar too
  ant drt: builds drt tests too
  ant bootstrap: builds xbean.jar, then builds it again using itself.


Interface JARs:

xmlpublic.jar - the XBeans public interfaces, for use by our users.
wlxbean.jar - interfaces specific to the IDE/runtime, not for use by others.


Implementation JARs:

xbean.jar - the whole xbean.jar - everything you need to run. When this
 is on your classpath, you don't need xmlpublic.jar or wlxbean.jar


Testing JARs:

drt.jar - our DRT
enumtest.jar, easypo.jar, nameworld.jar, xstypes.jar - sample XBean JARs.


Other JARs:

oldxbean.jar - a fully built previous-version of xbean.jar, used for
  bootstrapping, since xbeans are used in building xbean.jar itself.





(3) Running

There are a few ways you can run.  There is some pretty plain-jane
JUnit testing code checked into xbean\test\src\drt that you can
use to see how to exercise our functionality. (The tests in that
directory are our Developer Regression Tests.)

You can run the drt using xbean\bin\drt[.sh|.cmd].

Another tool that you can use is "scomp", the XBean schema
compiler. The scomp script is at xbean\bin\scomp[.sh|.cmd].

To run scomp, just point it at an XSD file or a directory full
of XSD files, and it will produce an output .jar with all your
types.

Since scomp jars up its results, it can be hard to see what it
is doing. But if you use the "-src mydir" option with scomp,
you can specify a directory into which it will dump .java source
code files that correspond to the java types it is generating.

Finally, there is a utility called "dumpxsb". If you unjar
a built xbean jar, you'll notice a bunch of .xsb files scattered
about. Each one of these files represents a schema type, attribute,
or element definition, and you can use "dumpxsb" to see the
contents.

