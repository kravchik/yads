package yk.lang.yads;

import yk.lang.yads.utils.BadException;

public class UtilsForTests {
    public static String readResource(String path) {
        String content = TestYadsEntityOutput.resourceAsString(path);
        if (content == null) {
            throw BadException.die("File " + path + " not found");
        }
        return content;
    }
}
