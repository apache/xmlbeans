/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package misc.common;

import java.io.File;
import common.Common;

/**
 *
 */
public class JiraTestBase extends Common
{
    //location of files under "cases folder"
    public static String JIRA_CASES = XBEAN_CASE_ROOT + P + "xbean" + P + "misc" +
                                        P + "jira" + P;
    public static File outputroot = new File(OUTPUTROOT+P+"jiraoutput");

    public static String scompTestFilesRoot = XBEAN_CASE_ROOT + P + "misc" + P + "jira" + P;
    public static String schemaCompOutputDirPath = OUTPUTROOT+ P + "jira" + P;
    public static File schemaCompOutputDirFile = null;
    public static File schemaCompSrcDir = null;
    public static File schemaCompClassesDir = null;

    public static final int THREAD_COUNT = 150;
    public static final int ITERATION_COUNT = 2;

    public JiraTestBase(String name){
        super(name);
    }
}
