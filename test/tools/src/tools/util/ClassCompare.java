package tools.util;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * @author jacobd
 * Date: Jan 23, 2004
 */
public class ClassCompare {

    public String compareClass(Class actual, Class expected) throws Exception {
        Method[] actualMethods = actual.getMethods();
        Method[] expectedMethods = expected.getMethods();
        Field[] actualField = actual.getFields();
        Field[] bField = expected.getFields();

        if (actual.getName() != expected.getName())
            throw new Exception("Class Names were not equal:\n" +
                                "Actual: " + actual.getName() + "\n" +
                                "Expected: " + expected.getName());


        return null;
    }

    public String compareMethods(Method[] actual, Method[] expected) throws Exception {
        if (actual.length != expected.length)
            throw new Exception("Method Count was not equal");

        //for (int i=0; i < actual.length)


        return null;
    }

    public String compareFields(Field[] actual, Field[] expected) throws Exception {


        return null;
    }


}
