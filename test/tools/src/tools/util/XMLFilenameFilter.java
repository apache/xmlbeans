package tools.util;

import java.io.FilenameFilter;
import java.io.File;

/**
 * @author jacobd
 * Date: Jan 20, 2004
 */
public class XMLFilenameFilter implements FilenameFilter{
    public boolean accept(File file, String s) {
        if (s.endsWith("xml"))
            return true;
        else
            return false;
    }
}
