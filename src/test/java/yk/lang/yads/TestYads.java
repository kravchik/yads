package yk.lang.yads;

import org.junit.Test;
import yk.ycollections.YList;

import static org.junit.Assert.assertEquals;
import static yk.ycollections.YArrayList.al;

/**
 * 13.07.2024
 */
public class TestYads {
    @Test
    public void testStructure() {
        assertEquals(al(), Yads.readYadsEntities(""));

        testObj(entity(), "()");
        testObj(entity("foo"), "foo()");
        testObj(entity("foo", "bar"), "foo(bar)");
        testObj(entity("foo", entity()), "foo(())");
        testObj(entity("foo", entity("bar", "hello")), "foo(bar(hello))");
        testObj(entity("foo", "bar", entity(null, "hello")), "foo(bar (hello))");
    }

    @Test
    public void testComments() {
        testObj(new YadsEntity.YadsComment(false, "comment"), "/*comment*/");
        Object oneLine = new YadsEntity.YadsComment(true, "comment");
        assertEquals(al(oneLine), Yads.readYadsEntities("//comment"));
        assertEquals(al(oneLine, oneLine), Yads.readYadsEntities("//comment" + "\n" + "//comment"));
        assertEquals(oneLine, Yads.readYadsEntity("//comment"));
    }

    @Test
    public void testNumbers() {
        testNumbers(al(0, -0, 1, -1, 100, -100), "0 0 1 -1 100 -100", "0 -0 1 -1 100 -100");
        testNumbers(al(0f, -0f, 1f, -1f, 100f, -100f, 0.1f, 1.1f, 100000f),
            "0f -0f 1f -1f 100f -100f 0.1f 1.1f 100000f",
            "0.0 -0.0 1.0 -1.0 100.0 -100.0 0.1 1.1 100000.0",
            "0F -0F 1F -1F 100F -100F 0.1F 1.1F 1e5F"
        );
        testNumbers(al(0d, -0d, 1d, -1d, 100d, -100d, 0.1d, 1.1d, 100000d),
            "0d -0d 1d -1d 100d -100d 0.1d 1.1d 100000d",
            "0D -0D 1D -1D 100D -100D 0.1D 1.1D 1e5D"
        );
    }

    private void testNumbers(YList<Object> j, String out, String... alts) {
        for (String alt : alts) {
            assertEquals(j, Yads.readYadsEntities(alt));
        }
        assertEquals(out, Yads.printYadsEntities(j));
    }

    @Test
    public void testEscapes() {
        testStrings("", "''", "\"\"");
        testStrings(" ", "' '", "\" \"");
        testStrings("' \" \\ \\t\\n", "\"' \\\" \\\\ \\\\t\\\\n\"", "'\\' \" \\\\ \\\\t\\\\n'");

        testStrings("\n", "'\n'", "\"\n\"");
        testStrings("\t", "'\t'", "\"\t\"");
        testStrings("\\", "'\\\\'", "\"\\\\\"");

        testStrings("hello", "hello", "\"hello\"");
        testStrings("hello", "hello", "'hello'");

        testStrings("hello world", "'hello world'", "\"hello world\"");
    }

    private static void testObj(Object expected, String s) {
        assertEquals(al(expected), Yads.readYadsEntities(s));
        assertEquals(al(expected, expected), Yads.readYadsEntities(s + " " + s));
        assertEquals(al(expected, expected), Yads.readYadsEntities(s + "\n" + s));
        assertEquals(expected, Yads.readYadsEntity(s));
    }

    private static void testStrings(String j, String out, String alt) {
        assertEquals(j, Yads.readYadsEntity(alt));
        assertEquals(al(j), Yads.readYadsEntities(alt));
        assertEquals(al(j, j), Yads.readYadsEntities(alt + " " + alt));
        assertEquals(al(j, j), Yads.readYadsEntities(alt + "\n" + alt));

        assertEquals(j, Yads.readYadsEntity(out));
        assertEquals(al(j), Yads.readYadsEntities(out));
        assertEquals(al(j, j), Yads.readYadsEntities(out + " " + out));
        assertEquals(al(j, j), Yads.readYadsEntities(out + "\n" + out));

        assertEquals(out, Yads.printYadsEntity(j));
        assertEquals(out, Yads.printYadsEntities(al(j)));
        assertEquals(out + " " + out, Yads.printYadsEntities(al(j, j)));
    }

    public static YadsEntity entity() {
        return new YadsEntity(null, al());
    }

    public static YadsEntity entity(String name, Object... values) {
        return new YadsEntity(name, al(values));
    }
}
