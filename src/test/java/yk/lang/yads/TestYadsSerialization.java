package yk.lang.yads;

import org.junit.Test;
import yk.jcommon.collections.YList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YHashMap.hm;
import static yk.lang.yads.TestYadsListOutput.readResource;
import static yk.lang.yads.Yads.deserialize;
import static yk.lang.yads.Yads.serialize;

public class TestYadsSerialization {
    //TODO ? remove operators ?
    //  ((a=1) but (a= -1) or won't deserialize)
    //TODO get rid of commons.lang3

    //TODO Object[], Object[][][], byte[], byte[][][]
    //TODO typed array
    //TODO typed map
    //TODO called constructor for List<String> but we have List<Int>
    //TODO common words instead of YADS_*

    //TODO custom serializers
    //TODO implement/test array with numbers of different types
    //TODO simple convertions for constructor/function

    //TODO readme

    @Test
    public void someTest() {
        System.out.println(Yads.serialize(LocalDateTime.now()));
        System.out.println(Yads.deserialize("import java.time.LocalDateTime LocalDateTime(date=(year=2020 month=1 day=6) time=(hour=17 minute=26 second=35 nano=401000000))"));
    }

    @Test
    public void testS12() {
        assertS12(al(), "()", "import yk.lang.yads.TestClass ()");
        assertS12(hm(), "(=)", "import yk.lang.yads.TestClass (=)");
        assertS12(hm("a", "b"), "(a=b)", "import yk.lang.yads.TestClass (a = b)");
        assertS12(al(1, 2, 3), "(1 2 3)", "import yk.lang.yads.TestClass (1 2 3)");
        assertS12(al("a"), "(a)", "( a )", "\n(\na\n)");
        assertS12(al(al(), "a", "b"), "(() a b)");
        assertS12(al("a", "b", al()), "(a b ())");
        assertS12(al(al(al())), "((()))");
        assertS12(al(al(), al()), "(() ())", "(()())");
        assertS12(al(0.0001f), "(1.0E-4f)");
        assertS12(al(1f), "(1f)");
        assertS12(al("Hello"), "(Hello)");

        assertS12(al(al(hm("a", "b", "c", al(al(hm()))))), "(((a=b c=(((=))))))");
        assertS12(al(al(hm(al(al(hm())), "a", "b", "c"))), "((((((=)))=a b=c)))");

        assertS12(al((String)null), "(null)");
        assertS12(al(null, null), "(null null)");
        assertS12(hm("a", null), "(a=null)");
        assertS12(hm(null, "b"), "(null=b)");
        assertS12(al("null"), "('null')");
        assertS12(null, "null");

        assertEquals("import java.lang.Object\nObject()", serialize(new Object()));
        assertTrue(deserialize("import java.lang.Object\nObject()").getClass() == Object.class);

        assertS12(new TestClass2(1), "TestClass2(a=1f b=1f)", al("yk.lang.yads.TestClass2"), "TestClass2(1f)");

        assertS12(new TestClass2(1), "import yk.lang.yads.TestClass2\nTestClass2(a=1f b=1f)",
                "yk.lang.yads.TestClass2(1f)");

        assertS12(new TestClass2(1, 3), "import yk.lang.yads.TestClass2\nTestClass2(a=1f b=3f)",
                "yk.lang.yads.TestClass2(1f b=3)");

        assertS12(new TestClass2(1).setSs(al("hello", "world")),
                "import yk.lang.yads.TestClass2\nTestClass2(a=1f b=1f ss=(hello world))",
                "yk.lang.yads.TestClass2(1f ss=(hello world))");

        assertS12(new TestClass().setTc2(new TestClass2(1)),
                "import yk.lang.yads.TestClass\nTestClass(tc2=(a=1f b=1f))",
                "yk.lang.yads.TestClass(tc2 = (1f))");

        assertS12(new TestClass().setTc2(new TestClass2(1)), "TestClass(tc2=(a=1f b=1f))", al("yk.lang.yads.TestClass"),
                "TestClass(tc2 = (1f))");

        assertS12(new TestClass().setTc2(new TestClass2()), "TestClass(tc2=())", al("yk.lang.yads.TestClass"),
                "TestClass(tc2 = ())", "TestClass(tc2=(=))");

        assertS12(new TestClass().setTc2(new TestClass2().setA(1)), "TestClass(tc2=(a=1f))", al("yk.lang.yads.TestClass"));

        assertS12(new TestClass().setTc2(new TestClass2(1, 3)), "TestClass(tc2=(a=1f b=3f))", al("yk.lang.yads.TestClass"), "TestClass(tc2 = (1f b=3))");

        assertS12(al(new TestClass(), new TestClass()), "import yk.lang.yads.TestClass\n(TestClass() TestClass())", al(), "import yk.lang.yads.TestClass\n (TestClass() TestClass())");

        assertException("TestClass(asdf = 1f)", al("yk.lang.yads.TestClass"),
                "Error at 1:11, Class 'class yk.lang.yads.TestClass' has no field 'asdf'");

        assertException("TestClass(tc2 = 1f)", al("yk.lang.yads.TestClass"),
                "Error at 1:17, Expected type class yk.lang.yads.TestClass2, but was class java.lang.Float");

        assertEquals("import yk.lang.yads.TestClass\nTestClass()", Yads.serialize(new TestClass().setSomeTransient(5)));

        assertEquals("import yk.lang.yads.Vec2f\nVec2f(x=1f y=1f)", Yads.serialize(Vec2f.v2(1, 1)));
        assertEquals("import yk.lang.yads.Vec2f\nVec2f(x=1f y= -0.5f)", Yads.serialize(Vec2f.v2(1, -0.5f)));
        assertS12(al("N--------N--------", "N--------N--------N--------"), "('N--------N--------' 'N--------N--------N--------')");
    }

    @Test
    public void testBodyS12() {

        assertBodyS12(al(), "");
        assertBodyS12(hm("a", "b"), "a=b", "import yk.lang.yads.TestClass a=b");
        assertBodyS12(al("a", "b"), "a b", "import yk.lang.yads.TestClass a b");

        assertS12Exception(null, "Can't serialize body of null");
        assertS12Exception("hello", "Can't serialize body of String");
        assertS12Exception(3, "Can't serialize body of Number");

        //assertEquals(hm("a", "b"), deserializeBody("a=b c d"));

        assertBodyS12(hm("a", "b", "c", "d"), "a=b c=d",
                "import yk.lang.yads.TestClass import yk.lang.yads.TestClass2 a=b c=d");

        assertBodyS12(new TestClass().setTc2(new TestClass2(1)),
                "tc2=(a=1f b=1f)",
                "tc2 = (1f)");

        assertBodyS12(
                new TestClass().setTc2(new TestClass25(1)),
                "import yk.lang.yads.TestClass25\ntc2=TestClass25(a=1f b=1f)",
                "tc2 = yk.lang.yads.TestClass25(1f)",
                "import yk.lang.yads.TestClass25 tc2 = TestClass25(1f)");

        assertBodyS12(al(new TestClass().setTc2(new TestClass25(1))),
                "import yk.lang.yads.TestClass import yk.lang.yads.TestClass25\nTestClass(tc2=TestClass25(a=1f b=1f))",
                "import yk.lang.yads.TestClass TestClass(tc2 = yk.lang.yads.TestClass25(1f))");

        assertBodyS12(al(new TestClass().setTc2(new TestClass2(1)), new TestClass().setTc2(new TestClass2(1))),
                "import yk.lang.yads.TestClass\nTestClass(tc2=(a=1f b=1f)) TestClass(tc2=(a=1f b=1f))",
                "import yk.lang.yads.TestClass TestClass(tc2 = (1f)) TestClass(tc2 = (1f))");

        assertBodyS12(al(new TestClass().setTc2(new TestClass2(1)), al(new TestClass().setTc2(new TestClass2(1)))),
                "import yk.lang.yads.TestClass\nTestClass(tc2=(a=1f b=1f)) (TestClass(tc2=(a=1f b=1f)))",
                "import yk.lang.yads.TestClass TestClass(tc2 = (1f)) (TestClass(tc2 = (1f)))");

        assertBodyS12(hm("a", new TestClass().setTc2(new TestClass2(1))),
                "import yk.lang.yads.TestClass\na=TestClass(tc2=(a=1f b=1f))",
                "import yk.lang.yads.TestClass a = TestClass(tc2 = (1f))");
        assertBodyS12(hm("a", new TestClass().setTc2(new TestClass2(1)), "b", new TestClass().setTc2(new TestClass2(1))),
                "import yk.lang.yads.TestClass\na=TestClass(tc2=(a=1f b=1f)) b=TestClass(tc2=(a=1f b=1f))",
                "import yk.lang.yads.TestClass a = TestClass(tc2 = (1f)) b = TestClass(tc2 = (1f))");

        assertBodyS12(hm("a", new TestClass().setTc2(new TestClass2(1)), "b", new TestClass().setTc2(new TestClass2(1))),
                "a=TestClass(tc2=(a=1f b=1f)) b=TestClass(tc2=(a=1f b=1f))",
                al("yk.lang.yads.TestClass"),
                "import yk.lang.yads.TestClass a = TestClass(tc2 = (1f)) b = TestClass(tc2 = (1f))");

        assertBodyS12(al(new TestClass().setTc2(new TestClass2(1))),
                "TestClass(tc2=(a=1f b=1f))",
                al("yk.lang.yads.TestClass"),
                "import yk.lang.yads.TestClass TestClass(tc2 = (1f))");

        System.out.println(Yads.serializeBody(al(new TestClass().setTc2(new TestClass2(1)))));

        //assertEquals(hm("a", new TestClass().setTc2(new TestClass25(1)), "b", new TestClass().setTc2(new TestClass25(1))),
        //        deserializeBody("a=b c d"));
    }

    @Test
    public void testDataTypes() {
        assertS12(al("a \" ' b"), "(\"a \\\" ' b\")", "('a \" \\' b')");
        assertS12(al("a ' b"), "(\"a ' b\")", "('a \\' b')");
        assertS12(al("a \" b"), "('a \" b')", "(\"a \\\" b\")");
        assertS12(al("Hello"), "(Hello)", "('Hello')", "(\"Hello\")");

        assertS12(al(1), "(1)", "(01)");
        assertS12(al(1.0f), "(1f)", "(1.0)", "(1.0f)", "(1.0F)", "(1.f)", "(1.F)");
        assertS12(1.0f, "1f", "1.0", "1.0f", "1.0F", "1.f", "1.F");

        assertS12(1L, "1l", "1L");

        assertS12(al(0.00001d), "(1.0E-5d)");
        assertS12(al(1.0d), "(1d)");
        assertS12(al(1.0d), "(1d)", "(1.0d)", "(1.0D)");

        assertS12(new TestClassNumbers(),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers()");
        assertS12(new TestClassNumbers().setI(1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(i=1)",
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(i=1f)");
        assertS12(new TestClassNumbers().setI((Integer)1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(I=1)");
        assertS12(new TestClassNumbers().setF(1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(f=1f)",
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(f=1)");
        assertS12(new TestClassNumbers().setF((Float)1f),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(F=1f)");
        assertS12(new TestClassNumbers().setL(1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(l=1l)");
        assertS12(new TestClassNumbers().setL((Long)1L),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(L=1l)",
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(L=1)");
        assertS12(new TestClassNumbers().setD(1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(d=1d)");
        assertS12(new TestClassNumbers().setD((Double)1.),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(D=1d)",
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(D=1)");

        assertS12(new TestClassNumbers().setS((short)1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(s=1)");
        assertS12(new TestClassNumbers().setS((Short)(short)1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(S=1)");

        assertS12(new TestClassNumbers().setB((byte)0),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers()");
        assertS12(new TestClassNumbers().setB((byte)1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(b=1)");
        assertS12(new TestClassNumbers().setB((byte)255),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(b=255)");
        assertException("import yk.lang.yads.TestClassNumbers\nTestClassNumbers(b= -1)",
                "Error at 2:21, Can't properly convert class java.lang.Integer type to byte type, value -1 becomes 255");

        assertException("import yk.lang.yads.TestClassNumbers\nTestClassNumbers(b=256)",
                "Error at 2:20, Can't properly convert class java.lang.Integer type to byte type, value 256 becomes 0");
        assertS12(new TestClassNumbers().setB((Byte)(byte)1),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(B=1)");

        assertS12(new TestClassNumbers().setC(' '),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(c=' ')");
        assertS12(new TestClassNumbers().setC((Character)' '),
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(C=' ')");
        assertException("import yk.lang.yads.TestClassNumbers\nTestClassNumbers(C='  ')",
                "Error at 2:20, Expected string with one symbol to convert it to char, but was'  '");

        assertS12(new TestClassNumbers().setC((char)0), "import yk.lang.yads.TestClassNumbers\nTestClassNumbers()",
                "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(c='\\u0000')");

        //TODO unicode
        //assertS12(new TestClassNumbers().setC((char)1), "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(c='\\u0001')");
        //TODO unicode
        //assertS12(new TestClassNumbers().setC((char)13), "import yk.lang.yads.TestClassNumbers\nTestClassNumbers(c='\\u000D')");


        assertS12(al("true"), "('true')");
        assertS12(al("false"), "('false')");

        assertS12(al(-5), "(-5)");
        assertS12(al(-5f), "(-5f)", "(-5.0)");
        assertS12(al(-5d), "(-5d)");

        assertS12(al(true), "(true)");
        assertS12(al(false), "(false)");

        //TODO fix after simple constructors cast
        //assertS12(al((short) 5), "import java.lang.Short\n(Short(5))", "(Short(value=5))");
        //TODO Byte

    }

    @Test
    public void testRefs() {
        assertRefInPrint(null, false, true);
        assertRefInPrint(new TestClass2(1), true, true);
        assertRefInPrint(999, 999, false, false);
        assertRefInPrint(999, false, false);
        assertRefInPrint(1, 1, false, true);//because of Java's Integer cache
        assertRefInPrint("h", false, false);
        assertRefInPrint(true, false, true);//because of Java's Boolean cache

        assertRefInSerialize(null, false, true);
        assertRefInSerialize(new TestClass2(1), true, true);
        assertRefInSerialize(999, 999, false, false);
        assertRefInSerialize(999, true, true);
        assertRefInSerialize(1, 1, true, true);//because of Java's Integer cache
        assertRefInSerialize(1, true, true);
        assertRefInSerialize("h", "h", true, true);//because Java reuses constant strings
        assertRefInSerialize("h", true, true);
        assertRefInSerialize(true, false, true);//we never want ref Boolean, because it always is cached in Java

        {//self-reference
            TestClass testClass = new TestClass();
            testClass.tc = testClass;
            assertEquals("ref(1 TestClass(tc=ref(1)))",
                serialize(al("yk.lang.yads.TestClass"), testClass));

            TestClass des12ed = (TestClass) Yads.deserialize(al("yk.lang.yads.TestClass"), "ref(1 TestClass(tc=ref(1)))");
            assertTrue(des12ed.tc == des12ed);
        }

        {//circular ref
            TestClass testClass1 = new TestClass();
            TestClass testClass2 = new TestClass();
            testClass1.tc = testClass2;
            testClass2.tc = testClass1;
            assertEquals("(ref(1 TestClass(tc=ref(2 (tc=ref(1))))) ref(2))",
                    serialize(al("yk.lang.yads.TestClass"), al(testClass1, testClass2)));

            YList des12ed = (YList) Yads.deserialize(al("yk.lang.yads.TestClass"),
                    "(ref(1 TestClass(tc=ref(2 (tc=ref(1))))) ref(2))");

            TestClass res1 = (TestClass) des12ed.first();
            TestClass res2 = (TestClass) des12ed.last();
            assertTrue(res1.tc == res2);
            assertTrue(res2.tc == res1);
        }
    }

    @Test
    public void testImports() {
        assertEquals(new TestClass().setTc2(new TestClass2(1)),
                deserialize("import yk.lang.yads.TestClass TestClass(tc2 = (1f))"));

        assertException("import", "Error at 1:1, Expected type name after 'import'");
        assertException("import ()", "Error at 1:8, Element after 'import' should be a string constant");
        assertException("import()", "Error at 1:1, Can't use class name 'import'");
        assertException("import yk.lang.yads.TestClass()", "Error at 1:8, Element after 'import' should be a string constant");

        assertException("import yk.lang.yads.TestClass TestClass(tc2 = (1f)) TestClass(tc2 = (1f))", "Error at null, Unexpected count of elements: 2, expected exactly 1 element. Do you meant using deserializeBody?");
    }

    @Test
    public void testTabs() {
        assertEquals(readResource("formatting.yads").trim(), new YadsNodeOutput().withMaxWidth(30).toString(new YadsObjectSerializer(true).serialize(al(new TestHierarchy("key1", "value1", "key2", new TestHierarchy("key3", "value3")), new TestHierarchy("key1", "value1", "key2", new TestHierarchy("key3", "value3"))))));
    }

    private static void assertRefInPrint(Object a, boolean haveRef, boolean exact) {
        assertRefInPrint(a, a, haveRef, exact);
    }

    private static void assertRefInPrint(Object a, Object b, boolean haveRef, boolean exact) {
        String text = Yads.print(al(a, b));
        assertTrue(text.contains("ref") == haveRef);
        YList des12ed = (YList) Yads.deserialize(text);
        assertTrue((des12ed.get(0) == des12ed.get(1) == exact));
    }

    private static void assertRefInSerialize(Object a, boolean haveRef, boolean exact) {
        assertRefInSerialize(a, a, haveRef, exact);
    }

    private static void assertRefInSerialize(Object a, Object b, boolean haveRef, boolean exact) {
        String text = Yads.serialize(al(a, b));
        assertTrue(text.contains("ref") == haveRef);
        YList des12ed = (YList) Yads.deserialize(text);
        assertTrue((des12ed.get(0) == des12ed.get(1) == exact));
    }


    
    private static void assertNotReferencing(Object instance, final String text) {
        assertFalse(serialize(al("yk.lang.yads.TestClass2"), al(instance, instance)).contains("ref"));
        YList des12ed = (YList) Yads.deserialize(al("yk.lang.yads.TestClass2"), "(ref(1 " + text + ") ref(1))");
        assertTrue(des12ed.get(0) == des12ed.get(1));
    }

    private static void assertException(String text, String errorText) {
        try {
            deserialize(text);
            fail();
        } catch (RuntimeException re) {
            assertEquals(errorText, re.getMessage());
        }
    }

    private static void assertException(String text, YList<String> imports, String exceptionText) {
        try {
            Yads.deserialize(imports, text);
            fail();
        } catch (RuntimeException re) {
            if (!exceptionText.equals(re.getMessage())) {
                re.printStackTrace();
                assertEquals(exceptionText, re.getMessage());
            }
        }
    }

    private static void assertS12(Object expectedObject, String expectedText, String... variants) {
        if (expectedText != null) {
            assertEquals(expectedText, serialize(expectedObject));
            assertEquals("Expected deserialized object", expectedObject, deserialize(expectedText));
        }
        for (String s : variants) assertEquals(expectedObject, deserialize(s));
    }

    private static void assertS12(Object expectedObject, String expectedText, YList<String> imports, String... variants) {
        if (expectedText != null) {
            assertEquals(expectedText, serialize(imports, expectedObject));
            assertEquals(expectedObject, Yads.deserialize(imports, expectedText));
        }
        for (String s : variants) assertEquals(expectedObject, Yads.deserialize(imports, s));
    }

    private static void assertBodyS12(Object expectedObject, String expectedText, String... variants) {
        if (expectedText != null) {
            assertEquals(expectedText, Yads.serializeBody(expectedObject));
            assertEquals(expectedObject, Yads.deserializeBody(expectedObject.getClass(), expectedText));
        }
        for (String s : variants) {
            assertEquals(expectedObject, Yads.deserializeBody(expectedObject.getClass(), s));
            if (expectedObject instanceof Map) {
                assertEquals(expectedObject, Yads.deserializeBody(s));
                assertEquals(expectedObject, Yads.deserializeMapBody(s));
            }
            if (expectedObject instanceof List) {
                assertEquals(expectedObject, Yads.deserializeBody(s));
                assertEquals(expectedObject, Yads.deserializeListBody(s));
            }
        }
    }

    private static void assertBodyS12(Object expectedObject, String expectedText, YList<String> imports, String... variants) {
        if (expectedText != null) {
            assertEquals(expectedText, Yads.serializeBody(imports, expectedObject));
            assertEquals(expectedObject, Yads.deserializeBody(imports, expectedObject.getClass(), expectedText));
        }
        for (String s : variants) {
            assertEquals(expectedObject, Yads.deserializeBody(imports, expectedObject.getClass(), s));
            if (expectedObject instanceof Map) {
                assertEquals(expectedObject, Yads.deserializeBody(imports, s));
                assertEquals(expectedObject, Yads.deserializeMapBody(imports, s));
            }
            if (expectedObject instanceof List) {
                assertEquals(expectedObject, Yads.deserializeBody(imports, s));
                assertEquals(expectedObject, Yads.deserializeListBody(imports, s));
            }
        }
    }

    private static void assertS12Exception(Object someObject, String exceptionText) {
        try {
            Yads.serializeBody(someObject);
            fail();
        } catch (RuntimeException re) {
            assertEquals(exceptionText, re.getMessage());
        }
    }

    //public static void assertPattern(Object expected, YastNode actual) {
    //    Matcher matcher = new Matcher();
    //    matcher.classMatchers.put(YastNode.class, (matcher1, data, pattern, cur) -> {
    //        YMap<String, Object> d = ((YastNode) data).map;
    //        Object p = pattern instanceof YastNode ? (((YastNode) pattern).map) : pattern;
    //        return matcher1.match(d, p, cur);
    //    });
    //    assertTrue(matcher.match(actual, expected).notEmpty());
    //
    //    //TODO system out
    //}

}
