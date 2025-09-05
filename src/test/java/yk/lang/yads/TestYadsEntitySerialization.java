package yk.lang.yads;

import junit.framework.TestCase;
import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;
import yk.lang.yads.utils.Caret;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static yk.ycollections.YArrayList.al;

/**
 * 13.07.2024
 */
public class TestYadsEntitySerialization {
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
        testStrings(" ' ", "\" ' \"",       "' \\' '", "\" \\' \"");
        testStrings(" \" ", "' \" '",       "\" \\\" \"", "' \\\" '");

        testStrings(" \\ ", "' \\\\ '",     "\" \\\\ \"");
        testStrings(" \t ", "' \\t '",      "\" \t \"", "' \t '", "\" \\t \"", "' \\t '");
        testStrings(" \b ", "' \\b '",      "\" \b \"", "' \b '", "\" \\b \"", "' \\b '");
        testStrings(" \n ", "' \n '",       "\" \n \"", "\" \\n \"", "' \\n '");

        // \r is removed to enforce platform independence
        testStrings(" \r ", "' \\r '");
        testStrings("--", "--", "'-\r-'", "\"-\r-\"");
        testStrings(" \n ", "' \n '", "' \n\r '", "\" \r\n \"");

        testStrings(" \f ", "' \\f '",      "\" \f \"", "' \f '", "\" \\f \"", "' \\f '");

        testStrings("hello", "hello", "\"hello\"", "'hello'");
        testStrings("hello world", "'hello world'", "\"hello world\"");
    }

    private static void testObj(Object expected, String s) {
        assertEquals(al(expected), Yads.readYadsEntities(s));
        assertEquals(al(expected, expected), Yads.readYadsEntities(s + " " + s));
        assertEquals(al(expected, expected), Yads.readYadsEntities(s + "\n" + s));
        assertEquals(expected, Yads.readYadsEntity(s));
    }

    private static void testStrings(String data, String canonicForm, String... alternativeForms) {
        assertEquals(data, Yads.readYadsEntity(canonicForm));
        assertEquals(al(data), Yads.readYadsEntities(canonicForm));
        assertEquals(al(data, data), Yads.readYadsEntities(canonicForm + " " + canonicForm));
        assertEquals(al(data, data), Yads.readYadsEntities(canonicForm + "\n" + canonicForm));

        assertEquals(canonicForm, Yads.printYadsEntity(data));
        assertEquals(canonicForm, Yads.printYadsEntities(al(data)));
        assertEquals(canonicForm + " " + canonicForm, Yads.printYadsEntities(al(data, data)));

        for (String alt : alternativeForms) {
            assertEquals(data, Yads.readYadsEntity(alt));
            assertEquals(al(data), Yads.readYadsEntities(alt));
            assertEquals(al(data, data), Yads.readYadsEntities(alt + " " + alt));
            assertEquals(al(data, data), Yads.readYadsEntities(alt + "\n" + alt));
        }
    }

    public static YadsEntity entity() {
        return new YadsEntity(null, al());
    }

    public static YadsEntity entity(String name, Object... values) {
        return new YadsEntity(name, al(values));
    }
    
    /**
     * Helper method to assert all caret fields with exact values
     */
    private void assertCaret(String description, Caret caret, 
                           int expectedBeginOffset, int expectedEndOffset,
                           int expectedBeginLine, int expectedBeginColumn,
                           int expectedEndLine, int expectedEndColumn) {
        assertNotNull(description + " should have caret", caret);
        assertEquals(description + " beginOffset", expectedBeginOffset, caret.beginOffset);
        assertEquals(description + " endOffset", expectedEndOffset, caret.endOffset);
        assertEquals(description + " beginLine", expectedBeginLine, caret.beginLine);
        assertEquals(description + " beginColumn", expectedBeginColumn, caret.beginColumn);
        assertEquals(description + " endLine", expectedEndLine, caret.endLine);
        assertEquals(description + " endColumn", expectedEndColumn, caret.endColumn);
    }

    @Test
    public void testSimpleLiterals() throws Exception {
        // Test integer
        YadsCstParser parser = new YadsCstParser("42");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(42, resolved.get(0));

        // Test float
        parser = new YadsCstParser("3.14f");
        result = parser.parseListBody();
        resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(3.14f, (Float) resolved.get(0), 0.001f);

        // Test double
        parser = new YadsCstParser("2.71d");
        result = parser.parseListBody();
        resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(2.71d, (Double) resolved.get(0), 0.001d);

        // Test string
        parser = new YadsCstParser("\"hello world\"");
        result = parser.parseListBody();
        resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals("hello world", resolved.get(0));
    }

    @Test
    public void testNamedClass() throws Exception {
        YadsCstParser parser = new YadsCstParser("Person(\"John\" 25)");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity person = (YadsEntity) entity;
        TestCase.assertEquals("Person", person.name);
        TestCase.assertEquals(2, person.children.size());
        TestCase.assertEquals("John", person.children.get(0));
        TestCase.assertEquals(25, person.children.get(1));
        
        assertCaret("Person entity", person.caret, 0, 17, 1, 1, 1, 17);
        assertNotNull("Person entity should have childrenCarets", person.childrenCarets);
        assertEquals(2, person.childrenCarets.size());
        assertCaret("First child (\"John\")", person.childrenCarets.get(0), 7, 13, 1, 8, 1, 13);
        assertCaret("Second child (25)", person.childrenCarets.get(1), 14, 16, 1, 15, 1, 16);
    }

    @Test
    public void testUnnamedClass() throws Exception {
        YadsCstParser parser = new YadsCstParser("(\"data\" 123)");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        TestCase.assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity unnamed = (YadsEntity) entity;
        assertNull(unnamed.name);
        assertEquals(2, unnamed.children.size());
        assertEquals("data", unnamed.children.get(0));
        assertEquals(123, unnamed.children.get(1));
        
        assertCaret("Unnamed entity", unnamed.caret, 0, 12, 1, 1, 1, 12);
        assertNotNull("Unnamed entity should have childrenCarets", unnamed.childrenCarets);
        assertEquals(2, unnamed.childrenCarets.size());
        assertCaret("First child (\"data\")", unnamed.childrenCarets.get(0), 1, 7, 1, 2, 1, 7);
        assertCaret("Second child (123)", unnamed.childrenCarets.get(1), 8, 11, 1, 9, 1, 11);
    }

    @Test
    public void testTupleConversion() throws Exception {
        // Test simple key=value
        YadsCstParser parser = new YadsCstParser("name = \"John\"");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        assertEquals(1, resolved.size());

        Object tuple = resolved.get(0);
        assertTrue(tuple instanceof Tuple);
        Tuple<?, ?> t = (Tuple<?, ?>) tuple;
        assertEquals("name", t.a);
        assertEquals("John", t.b);
    }

    @Test
    public void testComplexExample() throws Exception {
        // Test a complex structure with tuples and nested classes - multiline to test line tracking
        YadsCstParser parser = new YadsCstParser("Person(name = \"John\"\nage = 25\naddress = Address(\"123 Main St\"))");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity person = (YadsEntity) entity;
        assertEquals("Person", person.name);
        assertEquals(3, person.children.size());
        
        // Check caret positions for multiline "Person(name = \"John\"\nage = 25\naddress = Address(\"123 Main St\"))"
        assertNotNull("Person entity should have caret", person.caret);
        assertEquals(0, person.caret.beginOffset);
        assertEquals(63, person.caret.endOffset); // full string length with \n characters
        
        // Check children carets - multiline with line number verification
        assertNotNull("Person entity should have childrenCarets", person.childrenCarets);
        assertEquals(3, person.childrenCarets.size());
        
        // name = "John" tuple spans positions 7-20 (line 1, name to "John")
        assertCaret("First child (name tuple)", person.childrenCarets.get(0), 7, 20, 1, 8, 1, 20);
        
        // age = 25 tuple spans positions 21-29 (line 2, age to 25) 
        assertCaret("Second child (age tuple)", person.childrenCarets.get(1), 21, 29, 2, 1, 2, 8);
        
        // address = Address(...) tuple spans positions 30-62 (line 3, address to Address(...))
        assertCaret("Third child (address tuple)", person.childrenCarets.get(2), 30, 62, 3, 1, 3, 32);

        // Check name tuple
        Tuple<?, ?> nameTuple = (Tuple<?, ?>) person.children.get(0);
        assertEquals("name", nameTuple.a);
        assertEquals("John", nameTuple.b);

        // Check age tuple
        Tuple<?, ?> ageTuple = (Tuple<?, ?>) person.children.get(1);
        assertEquals("age", ageTuple.a);
        assertEquals(25, ageTuple.b);

        // Check address tuple with nested entity
        Tuple<?, ?> addressTuple = (Tuple<?, ?>) person.children.get(2);
        assertEquals("address", addressTuple.a);
        assertTrue(addressTuple.b instanceof YadsEntity);
        YadsEntity address = (YadsEntity) addressTuple.b;
        assertEquals("Address", address.name);
        assertEquals(1, address.children.size());
        assertEquals("123 Main St", address.children.get(0));
    }

    @Test
    public void testMixedContent() throws Exception {
        // Test mix of comments, literals, and classes
        YadsCstParser parser = new YadsCstParser("//header comment\nPerson(\"John\") 42 //inline comment");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        assertEquals(4, resolved.size());

        // Header comment
        assertTrue(resolved.get(0) instanceof YadsEntity.YadsComment);
        YadsEntity.YadsComment headerComment = (YadsEntity.YadsComment) resolved.get(0);
        assertTrue(headerComment.isOneLine);
        assertEquals("header comment", headerComment.text);

        // Person entity
        assertTrue(resolved.get(1) instanceof YadsEntity);
        YadsEntity person = (YadsEntity) resolved.get(1);
        assertEquals("Person", person.name);
        assertEquals("John", person.children.get(0));

        // Number literal
        assertEquals(42, resolved.get(2));

        // Inline comment
        assertTrue(resolved.get(3) instanceof YadsEntity.YadsComment);
        YadsEntity.YadsComment inlineComment = (YadsEntity.YadsComment) resolved.get(3);
        assertTrue(inlineComment.isOneLine);
        assertEquals("inline comment", inlineComment.text);
    }

    @Test
    public void test1() {
        assertEquals("YadsEntity{children=[]}", getYadsList("()"));
        assertEquals("YadsEntity{children=[a]}", getYadsList("(a)"));
        assertEquals("YadsEntity{children=[a, b]}", getYadsList("(a b)"));
        assertEquals("YadsEntity{children=[tuple(a b)]}", getYadsList("(a=b)"));
        assertEquals("YadsEntity{children=[a, tuple(b c)]}", getYadsList("(a b=c)"));
        assertEquals("YadsEntity{children=[tuple(b c), d]}", getYadsList("(b=c d)"));
        assertEquals("YadsEntity{children=[tuple(a b), tuple(c d)]}", getYadsList("(a=b c=d)"));
        assertEquals("YadsEntity{children=[tuple(a b), e, tuple(c d)]}", getYadsList("(a=b e c=d)"));
        assertEquals("YadsEntity{children=[tuple(a b), e, tuple(c d), f]}", getYadsList("(a=b e c=d f)"));
        assertEquals("YadsEntity{children=[tuple(a b), tuple(c d), f]}", getYadsList("(a=b c=d f)"));

        assertEquals("YadsEntity{children=[tuple(a =)]}", getYadsList("(a='=')"));
        assertEquals("YadsEntity{children=[tuple(null value)]}", getYadsList("(null=value)"));
        assertEquals("YadsEntity{children=[tuple(key null)]}", getYadsList("(key=null)"));
        assertEquals("YadsEntity{children=[tuple(key false)]}", getYadsList("(key=false)"));
        assertEquals("YadsEntity{children=[tuple(true false)]}", getYadsList("(true=false)"));

        assertEquals("YadsEntity{name='name', children=[]}", getYadsList("name()"));
        assertEquals("YadsEntity{name='name', children=[a]}", getYadsList("name(a)"));
    }

    @Test
    public void testComments() {
        // Test single line comment
        YadsCstParser parser = new YadsCstParser("//this is a comment");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        assertEquals(1, resolved.size());

        Object comment = resolved.get(0);
        assertTrue(comment instanceof YadsEntity.YadsComment);
        YadsEntity.YadsComment yadsComment = (YadsEntity.YadsComment) comment;
        assertTrue(yadsComment.isOneLine);
        assertEquals("this is a comment", yadsComment.text);

        // Test multi-line comment
        parser = new YadsCstParser("/*multi\nline\ncomment*/");
        result = parser.parseListBody();
        resolved = YadsEntityDeserializer.resolveKeyValues(result.children);
        assertEquals(1, resolved.size());

        comment = resolved.get(0);
        assertTrue(comment instanceof YadsEntity.YadsComment);
        yadsComment = (YadsEntity.YadsComment) comment;
        assertFalse(yadsComment.isOneLine);
        assertEquals("multi\nline\ncomment", yadsComment.text);

        assertEquals("YadsComment{isOneLine=true, text=''}", getYadsList("//"));
        testComment("YadsComment{isOneLine=true, text=''}", "//\n");
        testComment("YadsComment{isOneLine=false, text=' '}", "/* */");

        assertException("(a//\n = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=4, beginOffset=2, endOffset=4}");
        assertException("(a = //\nb)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=7, beginOffset=5, endOffset=7}");

        assertException("(a/**/ = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=6, beginOffset=2, endOffset=6}");
        assertException("(a = /**/b)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=9, beginOffset=5, endOffset=9}");

        testObj(new YadsEntity.YadsComment(false, "comment"), "/*comment*/");
        Object oneLine = new YadsEntity.YadsComment(true, "comment");
        assertEquals(al(oneLine), Yads.readYadsEntities("//comment"));
        assertEquals(al(oneLine, oneLine), Yads.readYadsEntities("//comment" + "\n" + "//comment"));
        assertEquals(oneLine, Yads.readYadsEntity("//comment"));
    }

    private static void testComment(String expectedComment, String srcComment) {
        assertEquals("YadsEntity{children=[" + expectedComment + "]}", getYadsList("(" + srcComment + ")"));
        assertEquals("YadsEntity{children=[a, " + expectedComment + "]}", getYadsList("(a " + srcComment + ")"));
        assertEquals("YadsEntity{children=[" + expectedComment + ", a]}", getYadsList("(" + srcComment + " a)"));
        assertEquals("YadsEntity{children=[a, " + expectedComment + ", b]}", getYadsList("(a " + srcComment + " b)"));

        assertEquals("YadsEntity{children=[tuple(a b), " + expectedComment + "]}", getYadsList("(a = b" + srcComment + ")"));
        assertEquals("YadsEntity{children=[" + expectedComment + ", tuple(a b)]}", getYadsList("(" + srcComment + "a = b)"));
        assertEquals("YadsEntity{children=[tuple(a b), " + expectedComment + ", tuple(c d)]}", getYadsList("(a = b" + srcComment + "c = d)"));
    }

    @Test
    public void testErrors() {
        assertException("(= a)",
            "Expected key before '=' at Caret{beginLine=1, beginColumn=2, endLine=1, endColumn=2, beginOffset=1, endOffset=2}");
        assertException("(a =)",
            "Expected value after '=' at Caret{beginLine=1, beginColumn=4, endLine=1, endColumn=4, beginOffset=3, endOffset=4}");
        assertException("(= =)",
            "Expected key before '=' at Caret{beginLine=1, beginColumn=2, endLine=1, endColumn=2, beginOffset=1, endOffset=2}");
        assertException("(a = =)",
            "Expected value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=6, beginOffset=5, endOffset=6}");
        assertException("(a = b = c)",
            "Expected key before '=' at Caret{beginLine=1, beginColumn=8, endLine=1, endColumn=8, beginOffset=7, endOffset=8}");
        assertException("(a = b =)",
            "Expected key before '=' at Caret{beginLine=1, beginColumn=8, endLine=1, endColumn=8, beginOffset=7, endOffset=8}");
    }

    public static void assertException(String src, String errorText) {
        try {
            String result = getYadsList(src);
            fail(result);
        } catch (Exception e) {
            assertEquals(errorText, e.getMessage());
        }
    }

    private static String getYadsList(String s) {
        return YadsEntityDeserializer.resolve(YadsCstParser.parse(s).children.first()).toString();
    }

}
