package yk.lang.yads;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;
import static yk.lang.yads.YadsWords.ARGS;

public class TestYadsListResolver {

    @Test
    public void test1() {
        assertEquals("YadsList{children=[]}", getYadsList("()"));
        assertEquals("YadsList{children=[a]}", getYadsList("(a)"));
        assertEquals("YadsList{children=[a, b]}", getYadsList("(a b)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}]}", getYadsList("(a=b)"));
        assertEquals("YadsList{children=[a, Tuple{a=b, b=c}]}", getYadsList("(a b=c)"));
        assertEquals("YadsList{children=[Tuple{a=b, b=c}, d]}", getYadsList("(b=c d)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}, Tuple{a=c, b=d}]}", getYadsList("(a=b c=d)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}, e, Tuple{a=c, b=d}]}", getYadsList("(a=b e c=d)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}, e, Tuple{a=c, b=d}, f]}", getYadsList("(a=b e c=d f)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}, Tuple{a=c, b=d}, f]}", getYadsList("(a=b c=d f)"));

        assertEquals("YadsList{children=[Tuple{a=a, b==}]}", getYadsList("(a='=')"));
        assertEquals("YadsList{children=[Tuple{a=null, b=value}]}", getYadsList("(null=value)"));
        assertEquals("YadsList{children=[Tuple{a=key, b=null}]}", getYadsList("(key=null)"));
        assertEquals("YadsList{children=[Tuple{a=key, b=false}]}", getYadsList("(key=false)"));
        assertEquals("YadsList{children=[Tuple{a=true, b=false}]}", getYadsList("(true=false)"));

        assertEquals("YadsList{name='name', children=[]}", getYadsList("name()"));
        assertEquals("YadsList{name='name', children=[a]}", getYadsList("name(a)"));
    }

    @Test
    public void testComments() {
        assertEquals("YadsComment{isOneLine=true, text=''}", getYadsList("//"));
        testComment("YadsComment{isOneLine=true, text=''}", "//\n");
        testComment("YadsComment{isOneLine=false, text=' '}", "/* */");

        assertException("(a//\n = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=4}");
        assertException("(a = //\nb)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=7}");

        assertException("(a/**/ = b)", "Comment instead of key at Caret{beginLine=1, beginColumn=3, endLine=1, endColumn=6}");
        assertException("(a = /**/b)", "Comment instead of value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=9}");

    }

    private static void testComment(String exectedComment, String srcComment) {
        assertEquals("YadsList{children=[" + exectedComment + "]}", getYadsList("(" + srcComment + ")"));
        assertEquals("YadsList{children=[a, " + exectedComment + "]}", getYadsList("(a " + srcComment + ")"));
        assertEquals("YadsList{children=[" + exectedComment + ", a]}", getYadsList("(" + srcComment + " a)"));
        assertEquals("YadsList{children=[a, " + exectedComment + ", b]}", getYadsList("(a " + srcComment + " b)"));

        assertEquals("YadsList{children=[Tuple{a=a, b=b}, " + exectedComment + "]}", getYadsList("(a = b" + srcComment + ")"));
        assertEquals("YadsList{children=[" + exectedComment + ", Tuple{a=a, b=b}]}", getYadsList("(" + srcComment + "a = b)"));
        assertEquals("YadsList{children=[Tuple{a=a, b=b}, " + exectedComment + ", Tuple{a=c, b=d}]}", getYadsList("(a = b" + srcComment + "c = d)"));
    }

    @Test
    public void testErrors() {
        assertException("(=)",
                "Expected key before = at Caret{beginLine=1, beginColumn=2, endLine=1, endColumn=2}");
        assertException("(= a)",
                "Expected key before = at Caret{beginLine=1, beginColumn=2, endLine=1, endColumn=2}");
        assertException("(a =)",
                "Expected value after = at Caret{beginLine=1, beginColumn=4, endLine=1, endColumn=4}");
        assertException("(= =)",
                "Expected key before = at Caret{beginLine=1, beginColumn=2, endLine=1, endColumn=2}");
        assertException("(a = =)",
                "Expected value at Caret{beginLine=1, beginColumn=6, endLine=1, endColumn=6}");
        assertException("(a = b = c)",
                "Expected key before = at Caret{beginLine=1, beginColumn=8, endLine=1, endColumn=8}");
        assertException("(a = b =)",
                "Expected key before = at Caret{beginLine=1, beginColumn=8, endLine=1, endColumn=8}");
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
        return YadsListResolver.toYadsList(YadsParser.parse(s).getNodeList(ARGS).first()).toString();
    }
}
