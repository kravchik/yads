package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TestYadsEntityResolver {

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
        assertEquals("YadsComment{isOneLine=true, text=''}", getYadsList("//"));
        testComment("YadsComment{isOneLine=true, text=''}", "//\n");
        testComment("YadsComment{isOneLine=false, text=' '}", "/* */");

        assertException("(a//\n = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=4, beginOffset=2, endOffset=4}");
        assertException("(a = //\nb)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=7, beginOffset=5, endOffset=7}");

        assertException("(a/**/ = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=6, beginOffset=2, endOffset=6}");
        assertException("(a = /**/b)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=9, beginOffset=5, endOffset=9}");

    }

    private static void testComment(String exectedComment, String srcComment) {
        assertEquals("YadsEntity{children=[" + exectedComment + "]}", getYadsList("(" + srcComment + ")"));
        assertEquals("YadsEntity{children=[a, " + exectedComment + "]}", getYadsList("(a " + srcComment + ")"));
        assertEquals("YadsEntity{children=[" + exectedComment + ", a]}", getYadsList("(" + srcComment + " a)"));
        assertEquals("YadsEntity{children=[a, " + exectedComment + ", b]}", getYadsList("(a " + srcComment + " b)"));

        assertEquals("YadsEntity{children=[tuple(a b), " + exectedComment + "]}", getYadsList("(a = b" + srcComment + ")"));
        assertEquals("YadsEntity{children=[" + exectedComment + ", tuple(a b)]}", getYadsList("(" + srcComment + "a = b)"));
        assertEquals("YadsEntity{children=[tuple(a b), " + exectedComment + ", tuple(c d)]}", getYadsList("(a = b" + srcComment + "c = d)"));
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
        return YadsCstResolver.resolve(YadsCstParser.parse(s).children.first()).toString();
    }
}
