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

package org.apache.xmlbeans.impl.schema;

import org.w3.x2001.xmlSchema.RedefineDocument.Redefine;
import org.w3.x2001.xmlSchema.SchemaDocument.Schema;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.ImportDocument.Import;
import org.w3.x2001.xmlSchema.IncludeDocument.Include;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.CharArrayWriter;
import java.io.OutputStreamWriter;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.common.XmlEncodingSniffer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StscImporter
{
    public static SchemaToProcess[] resolveImportsAndIncludes(Schema[] startWith)
    {
        DownloadTable engine = new DownloadTable(startWith);
        return engine.resolveImportsAndIncludes();
    }

    public static class SchemaToProcess
    {
        private Schema schema;
        private String chameleonNamespace;
        private Redefine redefine;
        private int redefinedBy;

        public SchemaToProcess(Schema schema, String chameleonNamespace, Redefine redefine)
        {
            this.schema = schema;
            this.chameleonNamespace = chameleonNamespace;
            this.redefine = redefine;
            this.redefinedBy = -1;
        }

        // This makes sense only when part of a list, which is always the case
        public SchemaToProcess(Schema schema, String chameleonNamespace, Redefine redefine, int redefinedBy)
        {
            this.schema = schema;
            this.chameleonNamespace = chameleonNamespace;
            this.redefine = redefine;
            this.redefinedBy = redefinedBy;
        }

        /**
         * The schema to parse.
         */
        public Schema getSchema()
        {
            return schema;
        }

        /**
         * The base URI for this stp
         */
        public String getSourceName()
        {
            return schema.documentProperties().getSourceName();
        }
        
        /**
         * The redefine element, if processed via redefine
         */
        public Redefine getRedefine()
        {
            return redefine;
        }

        /**
         * The index of the redefining schema if processed via redefine
         */
        public int getRedefinedBy()
        {
            return redefinedBy;
        }

        /**
         * The chameleon namespace. Null if this schema is not being treated
         * as a chameleon. (The ordinary targetNamespace will just be extracted
         * from the syntax of the schema.)
         */
        public String getChameleonNamespace()
        {
            return chameleonNamespace;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof SchemaToProcess)) return false;

            final SchemaToProcess schemaToProcess = (SchemaToProcess) o;

            if (chameleonNamespace != null ? !chameleonNamespace.equals(schemaToProcess.chameleonNamespace) : schemaToProcess.chameleonNamespace != null) return false;
            if (redefine != null ? !redefine.equals(schemaToProcess.redefine) : schemaToProcess.redefine != null) return false;
            if (!schema.equals(schemaToProcess.schema)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = schema.hashCode();
            result = 29 * result + (chameleonNamespace != null ? chameleonNamespace.hashCode() : 0);
            result = 29 * result + (redefine != null ? redefine.hashCode() : 0);
            return result;
        }
    }

    private final static String PROJECT_URL_PREFIX = "project://local";

    private static String baseURLForDoc(XmlObject obj)
    {
        String path = obj.documentProperties().getSourceName();

        if (path == null)
            return null;

        if (path.startsWith("/"))
            return PROJECT_URL_PREFIX + path.replace('\\', '/');

        // looks like a URL?
        int colon = path.indexOf(':');
        if (colon > 1 && path.substring(0, colon).matches("^\\w+$"))
            return path;

        return PROJECT_URL_PREFIX + "/" + path.replace('\\', '/');
    }

    private static URI parseURI(String s)
    {
        if (s == null)
            return null;

        try
        {
            return new URI(s);
        }
        catch (URISyntaxException syntax)
        {
                return null;
        }
    }
    
    //workaround for Sun bug # 4723726
    private static URI resolve(URI base, String child)
    {
        URI childUri = URI.create(child);
        URI ruri = base.resolve(childUri);

        // if the child fragment is relative (which we'll assume is the case
        // if URI.resolve doesn't do anything useful with it) and the base
        // URI is pointing at something nested inside a jar, we seem to have
        // to this ourselves to make sure that the nested jar url gets
        // resolved correctly   
        if (childUri.equals(ruri) &&
          (base.getScheme().equals("jar") || base.getScheme().equals("zip"))) {
            String r = base.toString();
            int lastslash = r.lastIndexOf('/');
            r = r.substring(0,lastslash) + "/" + childUri;
            return URI.create(r);
        }

        //fix up normalization bug
        if ("file".equals(ruri.getScheme()) && ! child.equals(ruri))
        {
            if (base.getPath().startsWith("//") && !ruri.getPath().startsWith("//"))
            {
                String path = "///".concat(ruri.getPath());
                try
                {
                    ruri = new URI("file", null, path, ruri.getQuery(), ruri.getFragment());
                }
                catch(URISyntaxException uris)
                {}
            }
        }
        return ruri;
    }

    public static class DownloadTable
    {
        /**
         * Namespace/schemaLocation pair.
         *
         * Downloaded schemas are indexed by namespace, schemaLocation, and both.
         *
         * A perfect match is preferred, but a match-by-namespace is accepted.
         * A match-by-schemaLocation is only accepted for includes (not imports).
         */
        private static class NsLocPair
        {
            private String namespaceURI;
            private String locationURL;

            public NsLocPair(String namespaceURI, String locationURL)
            {
                this.namespaceURI = namespaceURI;
                this.locationURL = locationURL;
            }

            /**
             * Empty string for no-namespace, null for namespace-not-part-of-key
             */
            public String getNamespaceURI()
            {
                return namespaceURI;
            }

            public String getLocationURL()
            {
                return locationURL;
            }

            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (!(o instanceof NsLocPair)) return false;

                final NsLocPair nsLocPair = (NsLocPair) o;

                if (locationURL != null ? !locationURL.equals(nsLocPair.locationURL) : nsLocPair.locationURL != null) return false;
                if (namespaceURI != null ? !namespaceURI.equals(nsLocPair.namespaceURI) : nsLocPair.namespaceURI != null) return false;

                return true;
            }

            public int hashCode()
            {
                int result;
                result = (namespaceURI != null ? namespaceURI.hashCode() : 0);
                result = 29 * result + (locationURL != null ? locationURL.hashCode() : 0);
                return result;
            }
        }

        private static class DigestKey
        {
            byte[] _digest;
            int _hashCode;
            DigestKey(byte[] digest)
            {
                _digest = digest;
                for (int i = 0; i < 4 && i < digest.length; i++)
                {
                    _hashCode = _hashCode << 8;
                    _hashCode = _hashCode + digest[i];
                }
            }

            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (!(o instanceof DigestKey)) return false;
                return Arrays.equals(_digest, ((DigestKey)o)._digest);
            }

            public int hashCode()
            {
                return _hashCode;
            }
        }

        private Map schemaByNsLocPair = new HashMap();
        private Map schemaByDigestKey = new HashMap();
        private LinkedList scanNeeded = new LinkedList();
        private Set emptyNamespaceSchemas = new HashSet();
        private Set scannedAlready = new HashSet();
        private Set failedDownloads = new HashSet();

        private Schema downloadSchema(XmlObject referencedBy, String targetNamespace, String locationURL)
        {
            // no location URL provided?  Then nothing to do.
            if (locationURL == null)
                return null;
            
            // First resolve relative URLs with respect to base URL for doc
            URI baseURI = parseURI(baseURLForDoc(referencedBy));
            String absoluteURL = baseURI == null ? locationURL : resolve(baseURI, locationURL).toString();

            StscState state = StscState.get();

            // probe 0: this url is already processed, from a previous compile
            if (state.isFileProcessed(absoluteURL))
                return null;

            // probe 1: ns+url - perfect match
            if (absoluteURL != null && targetNamespace != null)
            {
                Schema result = (Schema)schemaByNsLocPair.get(new NsLocPair(targetNamespace, absoluteURL));
                if (result != null)
                    return result;
            }

            // probe 2: we have preexisting knowledge of this namespace,
            // either from another schema file or from the linker.
            // If we're not downloading the given URL, skip it silently if the
            // namespace is already represented by a file we have.
            // Also, suppress downloads of URLs to namespaces that are already
            // known by the linker.
            // (We never assume preexisting knowledge of the no-namespace,
            // even if we have some definitions, since it's likely that
            // more than one person is playing in the no-namespace at once.)
            if (targetNamespace != null && !targetNamespace.equals(""))
            {
                // the URL is not one to download; should we assume we know about the namespace?
                if (!state.shouldDownloadURI(absoluteURL))
                {
                    // If we already have a schema representing this namespace,
                    // then skip this URL silently without producing an error.
                    Schema result = (Schema)schemaByNsLocPair.get(new NsLocPair(targetNamespace, null));
                    if (result != null)
                        return result;
                }

                // If the linker already knows about this namespace, skip
                // this URL.
                if (state.linkerDefinesNamespace(targetNamespace))
                    return null;
            }

            // probe 3: url only
            if (absoluteURL != null)
            {
                Schema result = (Schema)schemaByNsLocPair.get(new NsLocPair(null, absoluteURL));
                if (result != null)
                    return result;
            }

            // no match: error if we can't or won't download.
            if (absoluteURL == null)
            {
                state.error("Could not find resource - no valid location URL.", XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
                return null;
            }

            if (previouslyFailedToDownload(absoluteURL))
            {
                // an error message has already been produced.
                return null;
            }

            if (!state.shouldDownloadURI(absoluteURL))
            {
                state.error("Could not load resource \"" + absoluteURL + "\" (network downloads disabled).", XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
                addFailedDownload(absoluteURL);
                return null;
            }

            // try to download
            download: try
            {
                XmlObject xdoc = downloadDocument(state.getS4SLoader(), targetNamespace, absoluteURL);

                Schema result = findMatchByDigest(xdoc);
                String shortname = state.relativize(absoluteURL);
                if (result != null)
                {
                    // if an exactly-the-same document has already been loaded, use the original and spew
                    String dupname = state.relativize(result.documentProperties().getSourceName());
                    if (dupname != null)
                        state.info(shortname + " is the same as " + dupname + " (ignoring the duplicate file)");
                    else
                        state.info(shortname + " is the same as another schema");
                }
                else
                {
                    // otherwise, it's a new document: validate it and grab the contents
                    XmlOptions voptions = new XmlOptions();
                    voptions.setErrorListener(state.getErrorListener());
                    if (!(xdoc instanceof SchemaDocument) || !xdoc.validate(voptions))
                    {
                        state.error("Referenced document is not a valid schema", XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
                        break download;
                    }

                    SchemaDocument sDoc = (SchemaDocument)xdoc;

                    result = sDoc.getSchema();
                    state.info("Loading referenced file " + shortname);
                }
                NsLocPair key = new NsLocPair(emptyStringIfNull(result.getTargetNamespace()), absoluteURL);
                addSuccessfulDownload(key, result);
                return result;
            }
            catch (MalformedURLException malformed)
            {
                state.error("URL \"" + absoluteURL + "\" is not well-formed", XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
            }
            catch (IOException connectionProblem)
            {
                state.error(connectionProblem.toString(), XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
            }
            catch (XmlException e)
            {
                state.error("Problem parsing referenced XML resource - " + e.getMessage(), XmlErrorCodes.CANNOT_FIND_RESOURCE, referencedBy);
            }

            // record failure so that we don't try to download this URL again
            addFailedDownload(absoluteURL);
            return null;
        }
        
        static XmlObject downloadDocument(SchemaTypeLoader loader, String namespace, String absoluteURL)
                throws MalformedURLException, IOException, XmlException
        {
            StscState state = StscState.get();
            
            EntityResolver resolver = state.getEntityResolver();
            if (resolver != null)
            {
                InputSource source = null;
                try
                {
                    source = resolver.resolveEntity(namespace, absoluteURL);
                }
                catch (SAXException e)
                {
                    throw new XmlException(e);
                }

                if (source != null)
                {
                    state.addSourceUri(absoluteURL, null);

                    // first preference for InputSource contract: character stream
                    Reader reader = source.getCharacterStream();
                    if (reader != null)
                    {
                        reader = copySchemaSource(absoluteURL, reader, state);
                        XmlOptions options = new XmlOptions();
                        options.setLoadLineNumbers();
                        options.setDocumentSourceName(absoluteURL);
                        return loader.parse(reader, null, options);
                    }
                
                    // second preference for InputSource contract: 
                    InputStream bytes = source.getByteStream();
                    if (bytes != null)
                    {
                        bytes = copySchemaSource(absoluteURL, bytes, state);
                        String encoding = source.getEncoding();
                        XmlOptions options = new XmlOptions();
                        options.setLoadLineNumbers();
                        options.setLoadMessageDigest();
                        options.setDocumentSourceName(absoluteURL);
                        if (encoding != null)
                            options.setCharacterEncoding(encoding);
                        return loader.parse(bytes, null, options);
                    }
                
                    // third preference: use the (possibly redirected) url
                    String urlToLoad = source.getSystemId();
                    if (urlToLoad == null)
                        throw new IOException("EntityResolver unable to resolve " + absoluteURL + " (for namespace " + namespace + ")");

                    copySchemaSource(absoluteURL, state);
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadMessageDigest();
                    options.setDocumentSourceName(absoluteURL);
                    URL urlDownload = new URL(urlToLoad);
                    return loader.parse(urlDownload, null, options);
                }
            }

            // no resolver - just use the URL directly, no substitution
            state.addSourceUri(absoluteURL, null);
            copySchemaSource(absoluteURL, state);

            XmlOptions options = new XmlOptions();
            options.setLoadLineNumbers();
            options.setLoadMessageDigest();
            URL urlDownload = new URL(absoluteURL);

            return loader.parse(urlDownload, null, options);
        }

        private void addSuccessfulDownload(NsLocPair key, Schema schema)
        {
            byte[] digest = schema.documentProperties().getMessageDigest();
            if (digest == null)
            {
                StscState.get().addSchemaDigest(null);
            }
            else
            {
                DigestKey dk = new DigestKey(digest);
                if (!schemaByDigestKey.containsKey(dk))
                {
                    schemaByDigestKey.put(new DigestKey(digest), schema);
                    StscState.get().addSchemaDigest(digest);
                }
            }

            schemaByNsLocPair.put(key, schema);
            NsLocPair key1 = new NsLocPair(key.getNamespaceURI(), null);
            if (!schemaByNsLocPair.containsKey(key1))
                schemaByNsLocPair.put(key1, schema);
            NsLocPair key2 = new NsLocPair(null, key.getLocationURL());
            if (!schemaByNsLocPair.containsKey(key2))
                schemaByNsLocPair.put(key2, schema);
        }

        private Schema findMatchByDigest(XmlObject original)
        {
            byte[] digest = original.documentProperties().getMessageDigest();
            if (digest == null)
                return null;
            return (Schema)schemaByDigestKey.get(new DigestKey(digest));
        }

        private void addFailedDownload(String locationURL)
        {
            failedDownloads.add(locationURL);
        }

        private boolean previouslyFailedToDownload(String locationURL)
        {
            return failedDownloads.contains(locationURL);
        }

        private static boolean nullableStringsMatch(String s1, String s2)
        {
            if (s1 == null && s2 == null)
                return true;
            if (s1 == null || s2 == null)
                return false;
            return (s1.equals(s2));
        }

        private static String emptyStringIfNull(String s)
        {
            if (s == null)
                return "";
            return s;
        }

        private void addScanNeeded(SchemaToProcess stp)
        {
            if (!scannedAlready.contains(stp))
            {
                scannedAlready.add(stp);
                scanNeeded.add(stp);
            }
        }

        private void addEmptyNamespaceSchema(Schema s)
        {
            emptyNamespaceSchemas.add(s);
        }

        private void usedEmptyNamespaceSchema(Schema s)
        {
            emptyNamespaceSchemas.remove(s);
        }

        private boolean fetchRemainingEmptyNamespaceSchemas()
        {
            if (emptyNamespaceSchemas.isEmpty())
                return false;

            for (Iterator i = emptyNamespaceSchemas.iterator(); i.hasNext();)
            {
                Schema schema = (Schema)i.next();
                addScanNeeded(new SchemaToProcess(schema, null, null));
            }

            emptyNamespaceSchemas.clear();
            return true;
        }

        private boolean hasNextToScan()
        {
            return !scanNeeded.isEmpty();
        }

        private SchemaToProcess nextToScan()
        {
            SchemaToProcess next = (SchemaToProcess)scanNeeded.removeFirst();
            return next;
        }

        public DownloadTable(Schema[] startWith)
        {
            for (int i = 0; i < startWith.length; i++)
            {
                String targetNamespace = startWith[i].getTargetNamespace();
                NsLocPair key = new NsLocPair(targetNamespace, baseURLForDoc(startWith[i]));
                addSuccessfulDownload(key, startWith[i]);
                if (targetNamespace != null)
                    addScanNeeded(new SchemaToProcess(startWith[i], null, null));
                else
                    addEmptyNamespaceSchema(startWith[i]);
            }
        }

        public SchemaToProcess[] resolveImportsAndIncludes()
        {
            StscState state = StscState.get();
            List result = new ArrayList();

            // algorithm is to scan through each schema document and
            // 1. download each import and include (if not already downloaded)
            // 2. queue each imported or included schema to be process (if not already queued)

            // The algorithm is run twice twice: first we begin with non-empty
            // namespace schemas only.  Then we repeat starting with any
            // empty empty-namespace schemas that have NOT been chameleon-
            // included by other schemas and process them.

            for (;;)
            {
                while (hasNextToScan())
                {
                    SchemaToProcess stp = nextToScan();
                    String uri = stp.getSourceName();
                    state.addSourceUri(uri, null);
                    result.add(stp);
                    copySchemaSource(uri, state);

                    {
                        // handle imports
                        Import[] imports = stp.getSchema().getImportArray();
                        for (int i = 0; i < imports.length; i++)
                        {
                            Schema imported = downloadSchema(imports[i], emptyStringIfNull(imports[i].getNamespace()), imports[i].getSchemaLocation());

                            // if download fails, an error has already been reported.
                            if (imported == null)
                                continue;
    
                            if (!nullableStringsMatch(imported.getTargetNamespace(), imports[i].getNamespace()))
                            {
                                StscState.get().error("Imported schema has a target namespace \"" + imported.getTargetNamespace() + "\" that does not match the specified \"" + imports[i].getNamespace() + "\"", XmlErrorCodes.MISMATCHED_TARGET_NAMESPACE, imports[i]);
                            }
                            else
                            {
                                addScanNeeded(new SchemaToProcess(imported, null, null));
                            }
                        }
                    }
                    
                    {
                        // handle includes
                        Include[] includes = stp.getSchema().getIncludeArray();
                        String sourceNamespace = stp.getChameleonNamespace();
                        if (sourceNamespace == null)
                            sourceNamespace = emptyStringIfNull(stp.getSchema().getTargetNamespace());
    
                        for (int i = 0; i < includes.length; i++)
                        {
                            Schema included = downloadSchema(includes[i], null, includes[i].getSchemaLocation());
                            // if download fails, an error has already been reported.
                            if (included == null)
                                continue;
    
                            if (emptyStringIfNull(included.getTargetNamespace()).equals(sourceNamespace))
                            {
                                // non-chameleon case - just like an import
                                addScanNeeded(new SchemaToProcess(included, null, null));
                            }
                            else if (included.getTargetNamespace() != null)
                            {
                                // illegal include: included schema in wrong namespace.
                                StscState.get().error("Included schema has a target namespace \"" + included.getTargetNamespace() + "\" that does not match the source namespace \"" + sourceNamespace + "\"", XmlErrorCodes.MISMATCHED_TARGET_NAMESPACE, includes[i]);
                            }
                            else
                            {
                                // chameleon include
                                addScanNeeded(new SchemaToProcess(included, sourceNamespace, null));
                                usedEmptyNamespaceSchema(included);
                            }
                        }
                    }
                    
                    {
                        // handle redefines
                        Redefine[] redefines = stp.getSchema().getRedefineArray();
                        String sourceNamespace = stp.getChameleonNamespace();
                        if (sourceNamespace == null)
                            sourceNamespace = emptyStringIfNull(stp.getSchema().getTargetNamespace());
                        for (int i = 0; i < redefines.length; i++)
                        {
                            Schema redefined = downloadSchema(redefines[i], null, redefines[i].getSchemaLocation());
                            // if download fails, an error has already been reported.
                            if (redefined == null)
                                continue;
    
                            if (emptyStringIfNull(redefined.getTargetNamespace()).equals(sourceNamespace))
                            {
                                // non-chameleon case
                                addScanNeeded(new SchemaToProcess(redefined, null, redefines[i], result.size()-1));
                            }
                            else if (redefined.getTargetNamespace() != null)
                            {
                                // illegal include: included schema in wrong namespace.
                                StscState.get().error("Redefined schema has a target namespace \"" + redefined.getTargetNamespace() + "\" that does not match the source namespace \"" + sourceNamespace + "\"", XmlErrorCodes.MISMATCHED_TARGET_NAMESPACE, redefines[i]);
                            }
                            else
                            {
                                // chameleon redefine
                                addScanNeeded(new SchemaToProcess(redefined, sourceNamespace, redefines[i], result.size()-1));
                                usedEmptyNamespaceSchema(redefined);
                            }
                        }
                    }
                }

                if (!fetchRemainingEmptyNamespaceSchemas())
                    break;
            }

            return (SchemaToProcess[])result.toArray(new SchemaToProcess[result.size()]);
        }

        private static Reader copySchemaSource(String url, Reader reader, StscState state)
        {
            //Copy the schema file if it wasn't already copied
            if (state.getSchemasDir() == null)
                return reader;

            String schemalocation = state.sourceNameForUri(url);
            File targetFile = new File(state.getSchemasDir(), schemalocation);
            if (targetFile.exists())
                return reader;

            try
            {
                File parentDir = new File(targetFile.getParent());
                IOUtil.createDir(parentDir, null);

                CharArrayReader car = copy(reader);
                XmlEncodingSniffer xes = new XmlEncodingSniffer(car, null);
                Writer out = new OutputStreamWriter(new FileOutputStream(targetFile), xes.getXmlEncoding());
                IOUtil.copyCompletely(car, out);

                car.reset();
                return car;
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                return reader;
            }
        }

        private static InputStream copySchemaSource(String url, InputStream bytes, StscState state)
        {
            //Copy the schema file if it wasn't already copied
            if (state.getSchemasDir() == null)
                return bytes;

            String schemalocation = state.sourceNameForUri(url);
            File targetFile = new File(state.getSchemasDir(), schemalocation);
            if (targetFile.exists())
                return bytes;

            try
            {
                File parentDir = new File(targetFile.getParent());
                IOUtil.createDir(parentDir, null);

                ByteArrayInputStream bais = copy(bytes);

                FileOutputStream out = new FileOutputStream(targetFile);
                IOUtil.copyCompletely(bais, out);

                bais.reset();
                return bais;
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                return bytes;
            }
        }

        private static void copySchemaSource(String urlLoc, StscState state)
        {
            //Copy the schema file if it wasn't already copied
            if (state.getSchemasDir()!=null)
            {
                String schemalocation = state.sourceNameForUri(urlLoc);

                File targetFile = new File(state.getSchemasDir(), schemalocation);
                if (!targetFile.exists())
                {
                    try
                    {
                        File parentDir = new File(targetFile.getParent());
                        IOUtil.createDir(parentDir, null);

                        InputStream in = null;
                        URL url = new URL(urlLoc);
                        // Copy the file from filepath to schema[METADATA_PACKAGE]/src/<schemaFile>
                        in = url.openStream();

                        FileOutputStream out = new FileOutputStream(targetFile);
                        IOUtil.copyCompletely(in, out);
                    }
                    catch (IOException e)
                    {
                        System.err.println("IO Error " + e);
                        // failure = true; - not cause for failure
                    }
                }
            }
        }

        private static ByteArrayInputStream copy(InputStream is) throws IOException
        {
            byte [] buf = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int bytesRead;
            while(( bytesRead = is.read(buf, 0, 1024)) > 0)
                baos.write(buf, 0, bytesRead);

            return new ByteArrayInputStream(baos.toByteArray());
        }

        private static CharArrayReader copy(Reader is) throws IOException
        {
            char[] buf = new char[1024];
            CharArrayWriter baos = new CharArrayWriter();

            int bytesRead;
            while(( bytesRead = is.read(buf, 0, 1024)) > 0)
                baos.write(buf, 0, bytesRead);

            return new CharArrayReader(baos.toCharArray());
        }
    }
}
