package tools.xml;

import org.apache.xmlbeans.XmlError;

import java.util.Collection;
import java.util.Iterator;

/**
 * Collection of Utility methods
 * e.g. printXMLErrors();
 */
public class Utils
{

    /**
     * Iterators over the Collection and prints out XmlErrors
     * @param errors Collection object containing XmlError objects
     */
    public static void printXMLErrors(Collection errors)
    {
        for (Iterator i = errors.iterator(); i.hasNext();)
        {
            Object obj = i.next();
            if (!(obj instanceof XmlError))
                continue;
            XmlError err = (XmlError) obj;
            String sev = (err.getSeverity() == XmlError.SEVERITY_WARNING ?
                            "WARNING" :
                            (err.getSeverity() == XmlError.SEVERITY_INFO ?
                            "INFO" :
                            "ERROR"));
            System.out.println(sev + " " + err.getLine() + ":" + err.getColumn()
                               + " " + err.getMessage() + " ");
        }
    }

}
