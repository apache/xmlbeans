package tools.util;

import java.io.BufferedReader;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.StringTokenizer;

/**
 * @author jacobd
 * Date: Dec 8, 2003
 */
public class JarUtil {
    /**
     * returns an File Object within the given jarFile as a String. jarFile must exist in classpath
     * @param jarFile
     * @param pathToResource
     * @return File
     */

    final static String  EOL=System.getProperty("line.separator");
    public static File getResourceFromJarasFile(String jarFile, String pathToResource)
            throws IOException{
       /* URL url = new URL("jar:file:"+getFilePath(jarFile)+"!/");

        JarURLConnection jarConnection = (JarURLConnection)url.openConnection();

        JarFile jar = jarConnection.getJarFile();
        if (jar.getJarEntry(pathToResource) == null){
            throw new FileNotFoundException("Path: "+pathToResource+" was not found in "+jarFile);
        }

        File foo =  new File(url.toString()+pathToResource);
        if (!foo.exists()) {
           // throw new IOException("File: "+url.toString()+pathToResource+" does not exist");




        }
        else
            return foo;
            */
        String[] tokens=pathToResource.split("/");
        String fileName=tokens[tokens.length-1];
        tokens=fileName.split("\\.");
        File temp=File.createTempFile(tokens[0],"."+tokens[1]);
        temp.deleteOnExit();
        PrintWriter pr=null;
        try{
            pr=new PrintWriter(new FileWriter(temp));
            String content= getResourceFromJar(jarFile, pathToResource);
            pr.write(content);

        }finally{
             if (pr != null) pr.close();
        }
      return temp;
    }

    /**
     * returns an item within the given jarFile as a String. jarFile must exist in classpath
     * @param jarFile
     * @param pathToResource
     * @return String
     */
    public static String getResourceFromJar(String jarFile, String pathToResource)
            throws IOException{

        URL url = new URL("jar:file:"+getFilePath(jarFile)+"!/");
        JarURLConnection jarConnection = (JarURLConnection)url.openConnection();

        JarFile jar = jarConnection.getJarFile();
        if (jar.getJarEntry(pathToResource) == null){
            throw new FileNotFoundException("Path: "+pathToResource+" was not found in "+jarFile);
        }

        JarEntry item = jar.getJarEntry(pathToResource);


        BufferedReader in = new BufferedReader(new InputStreamReader(jar.getInputStream(item)));
        StringBuffer stb = new StringBuffer();
        String buffer;

        while(!((buffer=in.readLine())==null)) {
            stb.append(buffer+EOL);
        }

        return stb.toString();
    }

        /**
     * returns an item within the given jarFile as a String. jarFile must exist in classpath
     * @param jarFile
     * @param pathToResource
     * @return String
     */
    public static InputStreamReader getResourceFromJarasStream(String jarFile, String pathToResource)
            throws IOException{

        URL url = new URL("jar:file:"+getFilePath(jarFile)+"!/");
        JarURLConnection jarConnection = (JarURLConnection)url.openConnection();

        JarFile jar = jarConnection.getJarFile();
        if (jar.getJarEntry(pathToResource) == null){
            throw new FileNotFoundException("Path: "+pathToResource+" was not found in "+jarFile);
        }

        JarEntry item = jar.getJarEntry(pathToResource);


        return new InputStreamReader(jar.getInputStream(item));

    }

    /**
     * Returns the classpath entry of a given item on the classpath. The item should be a jarFile reference
     * @param jarFile
     * @return String
     * @throws FileNotFoundException
     */
    public static String getFilePath(String jarFile) throws FileNotFoundException {
        String sClassPath = System.getProperty("java.class.path");
        int jarIndex = sClassPath.indexOf(jarFile);

        if (jarIndex <= 0 ){
                throw new FileNotFoundException("File: "+jarFile+" was not found on the classpath");
        }

        String P = File.pathSeparator;

        String[] pathList = sClassPath.split(P);

        for (int i=0; i < pathList.length; i++){
            //System.out.println("["+i+"] - "+pathList[i]);
            if (pathList[i].toLowerCase().endsWith(jarFile.toLowerCase())){
                return pathList[i];
            }
        }

        throw new FileNotFoundException("File: "+jarFile+" was not found when iterating classpath");
    }


/*// Create a URL that refers to a jar file on the net
    URL url = new URL("jar:http://hostname/my.jar!/");

    // Create a URL that refers to a jar file in the file system
    url = new URL("jar:file:/c:/almanac/my.jar!/");

    // Get the jar file
    JarURLConnection conn = (JarURLConnection)url.openConnection();
    JarFile jarfile = conn.getJarFile();

    // When no entry is specified on the URL, the entry name is null
    String entryName = conn.getEntryName();  // null


    // Create a URL that refers to an entry in the jar file
    url = new URL("jar:file:/c:/almanac/my.jar!/com/mycompany/MyClass.class");

    // Get the jar file
    conn = (JarURLConnection)url.openConnection();
    jarfile = conn.getJarFile();

    // Get the entry name; it should be the same as specified on URL
    entryName = conn.getEntryName();

    // Get the jar entry
    JarEntry jarEntry = conn.getJarEntry();
  */

}
