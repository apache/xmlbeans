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

package org.apache.xmlbeans;

import org.xml.sax.EntityResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Used to supply options for loading, saving, and compiling, and validating.
 * <p>
 * There are two styles for using XmlOptions: multiline setup, and single-line use.
 * Here are two examples.  First, multiline style:
 * <pre>
 * XmlOptions opts = new XmlOptions();
 * opts.setSavePrettyPrint();
 * opts.setSavePrettyPrintIndent(4);
 * System.out.println(xobj.xmlText(opts));
 * </pre>
 * 
 * The alternative is single-line usage:
 * <pre>
 * System.out.println(xobj.xmlText(
 *     new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4)));
 * </pre>
 *
 * Table showing where each option gets used.
 * Note that:
 * <ul>
 * <li>options available for <code>newInstance</code> methods will also
 * apply for <code>parse</code> methods</li>
 * <li>options used for <code>validate</code> methods are also used for
 * <code>compile</code> methods, since compilation usually implies
 * validation against Schema for Schemas</li>
 * </ul>
 *
 * <table border="1">
 * <tr>
 *   <th align="center"><code>newInstance</code> methods</th>
 *   <th align="center"><code>parse</code> methods</th>
 *   <th align="center"><code>validate</code> methods</th>
 *   <th align="center"><code>compile</code> methods</th>
 *   <th align="center"><code>save</code> and <code>xmlText</code>methods</th>
 * </tr>
 * <tr>
 *   <td align="center"><code>setDocumentType</code><br/>
 *                      <code>setDocumentSourceName</code><br/>
 *                      <code>setValidateOnSet</code></td>
 *   <td align="center"><code>setLoad***</code><br/>
 *                      <code>setEntityResolver</code></td>
 *   <td align="center"><code>setErrorListener</code><br/>
 *   <td align="center"><code>setErrorListener</code><br/>
 *                      <code>setCompile***</code><br/>
 *                      <code>setEntityResolver</code><br/>
 *   <td align="center"><code>setSave***</code><br/>
 *                      <code>setUseDefaultNamespace</code><br/>
 *                      <code>setCharacterEncoding</code></td>
 * </tr>
 * </table>
 */
public class XmlOptions implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Map _map = new HashMap();


    /**
     * Construct a new blank XmlOptions.
     */
    public XmlOptions ( ) { }

    /**
     * Construct a new XmlOptions, copying the options.
     */
    public XmlOptions (XmlOptions other) {
        if (other != null) _map.putAll(other._map);
    }
            
    //
    // Handy-dandy helper methods for setting some options
    //

    /**
     * This option will cause the saver to save namespace attributes first.
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveNamespacesFirst() { 
        return set( SAVE_NAMESPACES_FIRST ); 
    }
    /**
     * This option will cause the saver to reformat white space for easier reading.
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSavePrettyPrint() { 
        return set( SAVE_PRETTY_PRINT ); 
    }

    /**
     * When used with <code>setSavePrettyPrint</code> this sets the indent
     * amount to use.
     * 
     * @param indent the indent amount to use
     * @see #setSavePrettyPrint
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSavePrettyPrintIndent(int indent) { 
        return set( SAVE_PRETTY_PRINT_INDENT, indent ); 
    }

    /**
     * When used with <code>setSavePrettyPrint</code> this sets the offset
     * amount to use.
     * 
     * @param offset the offset amount to use
     * @see #setSavePrettyPrint
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSavePrettyPrintOffset(int offset) { 
        return set( SAVE_PRETTY_PRINT_OFFSET, offset ); 
    }

    /**
     * When parsing or writing a document, this sets the character
     * encoding to use.
     * 
     * @param encoding the character encoding
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     */
    public XmlOptions setCharacterEncoding(String encoding) { 
        return set( CHARACTER_ENCODING, encoding ); 
    }

    /**
     * When parsing a document, this sets the type of the root
     * element. If this is set, the parser will not try to guess
     * the type based on the document's <code>QName</code>.
     * 
     * @param type The root element's document type.
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setDocumentType(SchemaType type) {
        return set( DOCUMENT_TYPE, type ); 
    }

    /**
     * <p>Sets a collection object for collecting {@link XmlError} objects 
     * during parsing, validation, and compilation. When set, the collection 
     * will contain all the errors after the operation takes place.  Notice that
     * the errors will only have line numbers if the document was
     * loaded with line numbers enabled.</p>
     * 
     * <p>The following simple example illustrates using an error listener
     * during validation.</p>
     * 
     * <pre>
     * // Create an XmlOptions instance and set the error listener.
     * XmlOptions validateOptions = new XmlOptions();
     * ArrayList errorList = new ArrayList();
     * validateOptions.setErrorListener(errorList);
     * 
     * // Validate the XML.
     * boolean isValid = newEmp.validate(validateOptions);
     * 
     * // If the XML isn't valid, loop through the listener's contents,
     * // printing contained messages.
     * if (!isValid)
     * {
     *      for (int i = 0; i < errorList.size(); i++)
     *      {
     *          XmlError error = (XmlError)errorList.get(i);
     *          
     *          System.out.println("\n");
     *          System.out.println("Message: " + error.getMessage() + "\n");
     *          System.out.println("Location of invalid XML: " + 
     *              error.getCursorLocation().xmlText() + "\n");
     *      }
     * }
     * </pre>
     * 
     * @param c A collection that will be filled with {@link XmlError} objects 
     * via {@link Collection#add}
     * 
     * @see XmlError
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     * @see XmlObject#validate(XmlOptions)
     * @see XmlBeans#compileXsd
     * @see XmlOptions#setLoadLineNumbers
     */
    public XmlOptions setErrorListener (Collection c) { 
        return set( ERROR_LISTENER, c ); 
    }

    /**
     * Causes the saver to reduce the number of namespace prefix declarations.
     * The saver will do this by passing over the document twice, first to
     * collect the set of needed namespace declarations, and then second
     * to actually save the document with the declarations collected
     * at the root.
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveAggresiveNamespaces() { 
        return set( SAVE_AGGRESSIVE_NAMESPACES ); 
    }

    /**
     * This option causes the saver to wrap the current fragment in
     * an element with the given name.
     * 
     * @param name the name to use for the top level element
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveSyntheticDocumentElement (QName name) { 
        return set( SAVE_SYNTHETIC_DOCUMENT_ELEMENT, name ); 
    }

    /**
     * If this option is set, the saver will try to use the default
     * namespace for the most commonly used URI. If it is not set
     * the saver will always created named prefixes.
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setUseDefaultNamespace () { 
        return set( SAVE_USE_DEFAULT_NAMESPACE ); 
    }

    /**
     * If namespaces have already been declared outside the scope of the
     * fragment being saved, this allows those mappings to be passed
     * down to the saver, so the prefixes are not re-declared.
     * 
     * @param implicitNamespaces a map of prefixes to uris that can be
     *  used by the saver without being declared
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */ 
    public XmlOptions setSaveImplicitNamespaces (Map implicitNamespaces) { 
        return set( SAVE_IMPLICIT_NAMESPACES, implicitNamespaces ); 
    }

    /**
     * A map of hints to pass to the saver for which prefixes to use
     * for which namespace URI.
     * 
     * @param suggestedPrefixes a map from URIs to prefixes
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveSuggestedPrefixes (Map suggestedPrefixes) { 
        return set( SAVE_SUGGESTED_PREFIXES, suggestedPrefixes ); 
    }

    /**
     * This option causes the saver to filter a Processing Instruction
     * with the given target
     * 
     * @param filterProcinst the name of a Processing Instruction to filter
     *   on save
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveFilterProcinst (String filterProcinst) { 
        return set( SAVE_FILTER_PROCINST, filterProcinst ); 
    }

    /**
     * When saving a fragment, this option changes the qname of the synthesized
     * root element.  Normally &lt;xml-fragment&gt; is used.
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveUseOpenFrag () { 
        return set( SAVE_USE_OPEN_FRAGMENT ); 
    }

    /**
     * This option controls whether saving begins on the element or its contents
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveOuter () { 
        return set( SAVE_OUTER ); 
    }

    /**
     * This option controls whether saving begins on the element or its contents
     * 
     * @see XmlTokenSource#save(java.io.File, XmlOptions)
     * @see XmlTokenSource#xmlText(XmlOptions)
     */
    public XmlOptions setSaveInner () { 
        return set( SAVE_INNER ); 
    }

    /**
     * If this option is set, the document element is replaced with the
     * given QName when parsing.  If null is supplied, the document element
     * is removed.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadReplaceDocumentElement ( QName replacement ) { 
        return set( LOAD_REPLACE_DOCUMENT_ELEMENT, replacement ); 
    }

    /**
     * If this option is set, all insignificant whitespace is stripped
     * when parsing a document.  Can be used to save memory on large
     * documents when you know there is no mixed content.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadStripWhitespace () { 
        return set( LOAD_STRIP_WHITESPACE); 
    }

    /**
     * If this option is set, all comments are stripped when parsing
     * a document.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadStripComments() {
        return set( LOAD_STRIP_COMMENTS ); 
    }

    /**
     * If this option is set, all processing instructions 
     * are stripped when parsing a document.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadStripProcinsts () { 
        return set( LOAD_STRIP_PROCINSTS ); 
    }

    /**
     * If this option is set, line number annotations are placed
     * in the store when parsing a document.  This is particularly
     * useful when you want {@link XmlError} objects to contain
     * line numbers.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     * @see XmlError
     */
    public XmlOptions setLoadLineNumbers () { 
        return set( LOAD_LINE_NUMBERS ); 
    }

    /**
     * This option sets a map of namespace uri substitutions that happen
     * when parsing a document.
     * <p>
     * This is particularly useful if you
     * have documents that use no namespace, but you wish to avoid
     * the name collision problems that occur when you introduce
     * schema definitions without a target namespace.
     * <p>
     * By mapping the empty string "" (the absence of a URI) to a specific
     * namespace, you can force the parser to behave as if a no-namespace
     * document were actually in the specified namespace. This allows you
     * to type the instance according to a schema in a nonempty namespace,
     * and therefore avoid the problematic practice of using schema
     * definitions without a target namespace.
     * 
     * @param substNamespaces a map of document URIs to replacement URIs
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadSubstituteNamespaces (Map substNamespaces) { 
        return set( LOAD_SUBSTITUTE_NAMESPACES, substNamespaces ); 
    }

    /**
     * If this option is set, the underlying xml text buffer is trimmed
     * immediately after parsing a document resulting in a smaller memory
     * footprint.  Use this option if you are loading a large number
     * of unchanging documents that will stay in memory for some time.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadTrimTextBuffer () { 
        return set( LOAD_TRIM_TEXT_BUFFER ); 
    }

    /**
     * Set additional namespace mappings to be added when parsing
     * a document.
     * 
     * @param nses additional namespace mappings
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadAdditionalNamespaces (Map nses) { 
        return set( LOAD_ADDITIONAL_NAMESPACES, nses ); 
    }

    /**
     * If this option is set when loading from an InputStream or File, then
     * the loader will compute a 160-bit SHA-1 message digest of the XML
     * file while loading it and make it available via
     * XmlObject.documentProperties().getMessageDigest();
     * <br>
     * The schema compiler uses message digests to detect and eliminate
     * duplicate imported xsd files.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setLoadMessageDigest () { 
        return set( LOAD_MESSAGE_DIGEST ); 
    }

    /**
     * Sets the name of the variable that represents
     * the current node in a query expression.
     * 
     * @param varName The new variable name to use for the query.
     * 
     * @see XmlObject#execQuery
     * @see XmlCursor#execQuery
     */
    public XmlOptions setXqueryCurrentNodeVar (String varName) { 
        return set( XQUERY_CURRENT_NODE_VAR, varName ); 
    }

    /**
     * Map the names and values of external variables in an xquery
     * expression.  The keys of the map are the variable names
     * in the query without the '$' prefix.  The values of the map
     * are objects and can be any of the primitive wrapper classes,
     * String, XmlObject, or XmlCursor. The mapping only applies to
     * xquery and has no effect on xpath expressions.
     *
     * @param varMap a map from <code>String</code>s to variable instances.
     *
     * @see XmlObject#execQuery
     * @see XmlCursor#execQuery
     */
    public XmlOptions setXqueryVariables (Map varMap) { 
        return set( XQUERY_VARIABLE_MAP, varMap ); 
    }

    /**
     * This option sets the document source name into the xml store
     * when parsing a document.  If a document is parsed from a
     * File or URI, it is automatically set to the URI of the
     * source; otherwise, for example, when parsing a String,
     * you can use this option to specify the source name yourself. 
     * 
     * @see XmlObject.Factory#parse(java.lang.String, XmlOptions)
     */
    public XmlOptions setDocumentSourceName (String documentSourceName) { 
        return set( DOCUMENT_SOURCE_NAME, documentSourceName ); 
    }

    /**
     * This option allows for <code>QName</code> substitution during schema compilation.
     * 
     * @param nameMap a map from <code>QName</code>s to substitute <code>QName</code>s.
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setCompileSubstituteNames (Map nameMap) { 
        return set( COMPILE_SUBSTITUTE_NAMES, nameMap ); 
    }
    
    /**
     * If this option is set, validation is not done on the Schema XmlBeans
     * when building a <code>SchemaTypeSystem</code>
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setCompileNoValidation () { 
        return set( COMPILE_NO_VALIDATION ); 
    }

    /**
     * If this option is set, the unique particle attribution rule is not
     * enforced when building a <code>SchemaTypeSystem</code>. See
     * <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#non-ambig">Appendix H of the XML Schema specification</a>
     * for information on the UPA rule.
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setCompileNoUpaRule () { 
        return set( COMPILE_NO_UPA_RULE ); 
    }
    
    /**
     * If this option is set, the particle valid (restriciton) rule is not
     * enforced when building a <code>SchemaTypeSystem</code>. See
     * <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#cos-particle-restrict">Section 3.9.6 of the XML Schema specification</a>
     * for information on the PVR rule.
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setCompileNoPvrRule () { 
        return set( COMPILE_NO_PVR_RULE ); 
    }
    
    /**
     * If this option is set, then the schema compiler will try to download
     * schemas that appear in imports and includes from network based URLs.
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setCompileDownloadUrls () { 
        return set( COMPILE_DOWNLOAD_URLS); 
    }
    
    /**
     * If this option is set, then the schema compiler will permit and
     * ignore multiple definitions of the same component (element, attribute,
     * type, etc) names in the given namespaces.  If multiple definitions
     * with the same name appear, the definitions that happen to be processed
     * last will be ignored.
     * 
     * @param mdefNamespaces a set of namespace URIs as Strings
     * 
     * @see XmlBeans#compileXsd
     */ 
    public XmlOptions setCompileMdefNamespaces(Set mdefNamespaces)
    {
        return set( COMPILE_MDEF_NAMESPACES, mdefNamespaces );
    }

    /**
     * If this option is set when an instance is created, then value
     * facets will be checked on each call to a setter or getter
     * method on instances of XmlObject within the instance document.
     * If the facets are not satisfied, then an unchecked exception is
     * thrown immediately.  This option is useful for finding code that
     * is introducing invalid values in an XML document, but it
     * slows performance.
     * 
     * @see XmlObject.Factory#parse(java.io.File, XmlOptions)
     */
    public XmlOptions setValidateOnSet() {
        return set( VALIDATE_ON_SET );
    }

    /**
     * If this option is set when compiling a schema, then the given
     * EntityResolver will be consulted in order to resolve any
     * URIs while downloading imported schemas.
     *
     * EntityResolvers are currently only used by compileXsd; they
     * are not consulted by other functions, for example, parse.
     * This will likely change in the future.
     * 
     * @see XmlBeans#compileXsd
     */
    public XmlOptions setEntityResolver(EntityResolver resolver) {
        return set( ENTITY_RESOLVER, resolver );
    }


    //
    // Complete set of XmlOption's
    //
            
    // TODO - Add selectPath option to track the seletion (deault is to clean selections fast). 
    
    /** @exclude */
    public static final String SAVE_NAMESPACES_FIRST           =  "SAVE_NAMESPACES_FIRST";
    /** @exclude */
    public static final String SAVE_SYNTHETIC_DOCUMENT_ELEMENT =  "SAVE_SYNTHETIC_DOCUMENT_ELEMENT";
    /** @exclude */
    public static final String SAVE_PRETTY_PRINT               =  "SAVE_PRETTY_PRINT";
    /** @exclude */
    public static final String SAVE_PRETTY_PRINT_INDENT        =  "SAVE_PRETTY_PRINT_INDENT";
    /** @exclude */
    public static final String SAVE_PRETTY_PRINT_OFFSET        =  "SAVE_PRETTY_PRINT_OFFSET";
    /** @exclude */
    public static final String SAVE_AGGRESSIVE_NAMESPACES      =  "SAVE_AGGRESSIVE_NAMESPACES";
    /** @exclude */
    public static final String SAVE_USE_DEFAULT_NAMESPACE      =  "SAVE_USE_DEFAULT_NAMESPACE";
    /** @exclude */
    public static final String SAVE_IMPLICIT_NAMESPACES        =  "SAVE_IMPLICIT_NAMESPACES";
    /** @exclude */
    public static final String SAVE_SUGGESTED_PREFIXES         =  "SAVE_SUGGESTED_PREFIXES";
    /** @exclude */
    public static final String SAVE_FILTER_PROCINST            =  "SAVE_FILTER_PROCINST";
    /** @exclude */
    public static final String SAVE_USE_OPEN_FRAGMENT          =  "SAVE_USE_OPEN_FRAGMENT";
    /** @exclude */
    public static final String SAVE_OUTER                      =  "SAVE_OUTER";
    /** @exclude */
    public static final String SAVE_INNER                      =  "SAVE_INNER";
    
    /** @exclude */
    public static final String LOAD_REPLACE_DOCUMENT_ELEMENT   =  "LOAD_REPLACE_DOCUMENT_ELEMENT";
    /** @exclude */
    public static final String LOAD_STRIP_WHITESPACE           =  "LOAD_STRIP_WHITESPACE";
    /** @exclude */
    public static final String LOAD_STRIP_COMMENTS             =  "LOAD_STRIP_COMMENTS";
    /** @exclude */
    public static final String LOAD_STRIP_PROCINSTS            =  "LOAD_STRIP_PROCINSTS";
    /** @exclude */
    public static final String LOAD_LINE_NUMBERS               =  "LOAD_LINE_NUMBERS";
    /** @exclude */
    public static final String LOAD_SUBSTITUTE_NAMESPACES      =  "LOAD_SUBSTITUTE_NAMESPACES";
    /** @exclude */
    public static final String LOAD_TRIM_TEXT_BUFFER           =  "LOAD_TRIM_TEXT_BUFFER";
    /** @exclude */
    public static final String LOAD_ADDITIONAL_NAMESPACES      =  "LOAD_ADDITIONAL_NAMESPACES";
    /** @exclude */
    public static final String LOAD_MESSAGE_DIGEST             =  "LOAD_MESSAGE_DIGEST";

    /** @exclude */
    public static final String XQUERY_CURRENT_NODE_VAR         =  "XQUERY_CURRENT_NODE_VAR";
    /** @exclude */
    public static final String XQUERY_VARIABLE_MAP             =  "XQUERY_VARIABLE_MAP";

    /** @exclude */
    public static final String CHARACTER_ENCODING              =  "CHARACTER_ENCODING";
    /** @exclude */
    public static final String ERROR_LISTENER                  =  "ERROR_LISTENER";
    /** @exclude */
    public static final String DOCUMENT_TYPE                   =  "DOCUMENT_TYPE";
    /** @exclude */
    public static final String DOCUMENT_SOURCE_NAME            =  "DOCUMENT_SOURCE_NAME";
    /** @exclude */
    public static final String COMPILE_SUBSTITUTE_NAMES        =  "COMPILE_SUBSTITUTE_NAMES";
    /** @exclude */
    public static final String COMPILE_NO_VALIDATION           =  "COMPILE_NO_VALIDATION";
    /** @exclude */
    public static final String COMPILE_NO_UPA_RULE             =  "COMPILE_NO_UPA_RULE";
    /** @exclude */
    public static final String COMPILE_NO_PVR_RULE             =  "COMPILE_NO_PVR_RULE";
    /** @exclude */
    public static final String COMPILE_DOWNLOAD_URLS           =  "COMPILE_DOWNLOAD_URLS";
    /** @exclude */
    public static final String COMPILE_MDEF_NAMESPACES         =  "COMPILE_MDEF_NAMESPACES";
    /** @exclude */
    public static final String VALIDATE_ON_SET                 =  "VALIDATE_ON_SET";
    /** @exclude */
    public static final String ENTITY_RESOLVER                 =  "ENTITY_RESOLVER";
    

    private static final XmlOptions EMPTY_OPTIONS;
    static {
        EMPTY_OPTIONS = new XmlOptions();
        EMPTY_OPTIONS._map = Collections.unmodifiableMap(EMPTY_OPTIONS._map);
    }

    /** If passed null, returns an empty options object.  Otherwise, returns its argument. */
    public static XmlOptions maskNull(XmlOptions o) {
        return (o == null) ? EMPTY_OPTIONS : o;
    }

    
    /** Used to set a generic option */
    public void  put ( Object option               ) { put( option, null ); }
    /** Used to set a generic option */
    public void  put ( Object option, Object value ) { _map.put(option, value); }
    /** Used to set a generic option */
    public void put  ( Object option, int value    ) { put( option, new Integer( value ) ); }

    private XmlOptions set(Object option)               { return set(option, null); }
    private XmlOptions set(Object option, Object value) { _map.put(option, value); return this;}
    private XmlOptions set(Object option, int value)    { return set(option, new Integer(value)); }

    /** Used to test a generic option */
    public boolean hasOption   ( Object option ) { return _map.containsKey( option ); }
    /** Used to get a generic option */
    public Object  get         ( Object option ) { return _map.get( option ); }

    /** Used to test a generic option on an options object that may be null */
    public static Object safeGet(XmlOptions o, Object option) {
        return o == null ? null : o.get(option);
    }

}
