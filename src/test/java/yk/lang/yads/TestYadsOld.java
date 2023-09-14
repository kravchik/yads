package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.utils.Caret;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YArrayList.toYList;
import static yk.ycollections.YHashMap.hm;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 08/02/15
 * Time: 16:40
 */
public class TestYadsOld {//TODO restore

    //@Test
    public void yast() {
        assertEqualNodes(new YadsNode(NODE_TYPE, YADS_ARRAY, ARGS, al(), CARET, new Caret(1, 1, 1, 2)), YadsNodeParser.parse("(1 + 2)"));

        assertEqualNodes(new YadsNode(NODE_TYPE, YADS_ARRAY, ARGS, al(), CARET, new Caret(1, 1, 1, 2)), YadsNodeParser.parse("()"));
        assertEqualNodes(new YadsNode(NODE_TYPE, YADS_MAP, NAMED_ARGS, hm(), CARET, new Caret(1, 1, 1, 3)), YadsNodeParser.parse("(:)"));
    }

    public static void assertEqualNodes(YadsNode expected, YadsNode actual) {
        //TODO assert nodes hierarchically
        //TODO assert maps inclusive
        //TODO use pattern matching!!!

        if (!actual.map.keySet().containsAll(expected.map.keySet())) {
            fail("Actual expected to cantain at least these keys: " + toYList(expected.map.keySet()) + " but was: " + toYList(actual.map.keySet()));
        }

        for (Map.Entry<String, Object> entry : expected.map.entrySet()) {
            assertTrue(actual.map.containsKey(entry.getKey()));
            assertEquals(entry.getValue(), actual.map.get(entry.getKey()));
        }
    }

    @Test
    public void parsePrimitives() {
        //TODO 
        //assertEquals(al("hello", "hello", "hello", "", ""), YadsNodeParser.parseBody("hello 'hello' \"hello\" '' \"\""));
        //
        //assertEquals(al(hm()), YadsNodeParser.parseBody("(:)"));
        //assertEquals(al(al()), YadsNodeParser.parseBody("()"));
        //
        //assertEquals(al("\"hello\"", "'hello'"), YadsNodeParser.parseBody(" '\\\"hello\\\"'  '\\'hello\\''"));
        //assertEquals(al("hello\nworld", "hello\tworld"), YadsNodeParser.parseBody(" 'hello\\nworld'  'hello\\tworld'"));
        //
        //assertEquals(al("hello\nworld", "hello\tworld"), YadsNodeParser.parseBody(" 'hello\nworld'  'hello\tworld'"));
        //
        //assertEquals(al("\"'hello world'\"", "\"'hello world'\""), YadsNodeParser.parseBody(" \"\\\"'hello world'\\\"\"  '\"\\'hello world\\'\"'"));
        //assertEquals(al("\\'"), YadsNodeParser.parseBody(" \"\\\\'\" "));
        //assertEquals(al("\\\""), YadsNodeParser.parseBody(" '\\\\\"' "));
        //
        //assertEquals(al(10, 10l, 10L, 10f, 10d, 10D, 10.1d, 10.1f, 10.1f), YadsNodeParser.parseBody("10 10l 10L 10f 10d 10D 10.1d 10.1f 10.1"));
        //assertEquals(al(1424083792130l), YadsNodeParser.parseBody("1424083792130l"));
        //assertEquals(al(-10), YadsNodeParser.parseBody("-10"));
        //assertEquals(al(true, false), YadsNodeParser.parseBody("true false"));
    }

    @Test
    public void testPositions() throws Exception {
        //ByteArrayInputStream stream = new ByteArrayInputStream("(hello world) (hello world)".getBytes("UTF-8"));
        //YadsNodeParser parser = new YadsNodeParser(stream).setRememberPositions(true);
        //parser.unnamedClassBody();
        //
        //assertEquals("{[hello, world]=Caret{beginLine=1, beginColumn=1, endLine=1, endColumn=13}, [hello, world]=Caret{beginLine=1, beginColumn=15, endLine=1, endColumn=27}}", parser.positions.toString());

    }
    //@Test
    //public void serializePrimitives() {
    //    assertEquals("10l", YadsJavaSerializer.serialize(10l));
    //    assertEquals("hello", YadsJavaSerializer.serialize("hello"));
    //    assertEquals("'hello\\n'", YadsJavaSerializer.serialize("hello\n"));//TODO don't escape?
    //}
    //
    ////@Test
    ////public void deserializer() {
    ////    assertEquals(al("hello", "world"), YadsJavaSerializer.deserializeList("hello world"));
    ////    assertEquals(al(al("hello", "world"), al("hello2", "world2")), YadsJavaSerializer.deserializeList("{hello world} {hello2 world2}"));
    ////    assertEquals(hm("hello", "world"), YadsJavaSerializer.deserializeMap("hello=world"));
    ////
    ////    assertEquals(new YadsClass(null, al(new Tuple("a", "b"), "c")), YadsJavaSerializer.deserialize("{a=b c}"));
    ////    assertEquals(new YadsClass("name", al(new Tuple("a", "b"), "c")), YadsJavaSerializer.deserialize("name{a=b c}"));
    ////
    ////    assertEquals(al(new TestEnumClass(TestEnum.ENUM1)), YadsJavaSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnumClass{enumField=ENUM1}"));
    ////    assertEquals(al(TestEnum.ENUM1), YadsJavaSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnum{ENUM1}"));
    ////
    ////    assertEquals(al(new TestEnumClass(null)), YadsJavaSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnumClass{enumField=null}"));
    ////}
    //
    ////@Test
    ////public void deserializerImports() {
    ////    assertEquals(new YadsClass("XY", al(1, 2)), YadsJavaSerializer.deserialize("XY{1 2}"));
    ////    assertEquals(new Vec2f(1, 2), YadsJavaSerializer.deserialize("yk.jcommon.fastgeom.Vec2f{x=1 y=2}"));
    ////    assertEquals(new Vec2f(1, 2), YadsJavaSerializer.deserialize("import=yk.jcommon.fastgeom \n\n Vec2f{x=1 y=2}"));
    ////    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsJavaSerializer.deserializeClassBody(TestClass.class, "import=yk.jcommon.fastgeom, yk.jcommon.fastgeom someList=1, 2 someMap={key1=value1 'key2'=value2} someInt=3"));
    ////}
    //
    //@Test
    //public void serializer() {
    //    assertEquals("import={yk.jcommon.fastgeom}\n\nVec2f{x= 1.0 y= 2.0}", YadsJavaSerializer.serialize(new Vec2f(1, 2)));
    //    assertEquals("{\n  hello\n  world\n}\n", YadsJavaSerializer.serialize(al("hello", "world")));
    //    assertEquals("{\n  k= v\n}\n", YadsJavaSerializer.serialize(hm("k", "v")));
    //
    //    assertEquals("import={yk.lang.yads}\n\nTestEnumClass{enumField= ENUM1}", YadsJavaSerializer.serialize(new TestEnumClass(TestEnum.ENUM1)));
    //    assertEquals("import={yk.lang.yads}\n\nTestEnumClass{}", YadsJavaSerializer.serialize(new TestEnumClass(null)));
    //
    //    assertEquals("{\n  hello\n  null\n}\n", YadsJavaSerializer.serialize(al("hello", null)));
    //
    //    assertEquals("{\n  'h\"e\\'l\\nl\\to'\n}\n", YadsJavaSerializer.serialize(al("h\"e'l\nl\to")));
    //
    //    assertEquals("enumField= ENUM1\n", YadsJavaSerializer.serializeClassBody(new TestEnumClass(TestEnum.ENUM1)));
    //}
    //
    //@Test
    //public void testClass() {
    //    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsJavaSerializer.deserializeClassBody(TestClass.class, "someList=1, 2 someMap={key1=value1 'key2'=value2} someInt=3"));
    //
    //    TestClass test1 = new TestClass();
    //    test1.someList2 = al(1, 2);
    //    test1.someList3 = al(3, 4);
    //    assertEquals(test1, YadsJavaSerializer.deserializeClassBody(TestClass.class, "someList2=1, 2 someList3=3,4"));
    //
    //    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsJavaSerializer.deserializeClassBody(TestClass.class, "1, 2 {key1=value1 'key2'=value2} 3"));
    //
    //    assertEquals(new TestClass(false), YadsJavaSerializer.deserializeClassBody(TestClass.class, "someBoolean=false"));
    //
    //    try {
    //        assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsJavaSerializer.deserializeClassBody(TestClass.class, "someList=1, 2 someMap=hello someInt=3"));
    //        fail();
    //    } catch (BadException ignore) {
    //        assertEquals("found instance hello of class class java.lang.String but expected object of class yk.ycollections.YHashMap", ignore.getMessage());
    //    }
    //
    //    TestClass tc = new TestClass();
    //    tc.tc2 = new TestClass2(1);
    //    assertEquals(tc, YadsJavaSerializer.deserializeClassBody(TestClass.class, "tc2=1f, 1f"));
    //    //assertEquals(tc, YadsJavaSerializer.deserializeClass(TestClass.class, "tc2={1f, 1f}"));
    //    //assertEquals(tc, YadsJavaSerializer.deserializeClass(TestClass.class, "tc2=TestClass2{1f, 1f}"));
    //    assertEquals(tc, YadsJavaSerializer.deserializeClassBody(TestClass.class, "tc2=1f"));
    //    assertEquals(tc, YadsJavaSerializer.deserializeClassBody(TestClass.class, "tc2={1f}"));
    //    assertEquals(tc, YadsJavaSerializer.deserializeClassBody(TestClass.class, "tc2=TestClass2{1f}"));
    //
    //    tc.tc2 = new TestClass2(al("hello", "world"));
    //    tc.tc2.a = 4;
    //    TestClass des = YadsJavaSerializer.deserializeClassBody(TestClass.class, "tc2={a=4 hello world}");
    //    assertEquals(tc, des);
    //}
    //
    //@Test
    //public void testYadsAware() {
    //    assertEquals("import={yk.lang.yads}\n\nTestClass2{1.0}", YadsJavaSerializer.serialize(new TestClass2(1)));
    //    assertEquals("import={yk.lang.yads}\n\nTestClass2{1.0 2.0}", YadsJavaSerializer.serialize(new TestClass2(1, 2)));
    //
    //    TestClass tc = new TestClass();
    //    tc.tc2 = new TestClass2(1);
    //    assertEquals("import={yk.lang.yads}\n\nTestClass{someInt= 0 tc2={1.0}someBoolean= false}", YadsJavaSerializer.serialize(tc));
    //
    //}
    //
    //@Test
    //public void testFieldTypeNotImported() {
    //    assertEquals(fill(new TestClass3(), "pos", new Vec2f(3, 3)), YadsJavaSerializer.deserialize("import=yk.lang.yads TestClass3{pos=3f, 3f}"));
    //    assertEquals(fill(new TestClass3(), "pos", new Vec2f(3, 3)), YadsJavaSerializer.deserializeClassBody(TestClass3.class, "pos=3f, 3f"));
    //}
    //
    //@Test
    //public void testFieldTypeIsKnown() {
    //    assertEquals("import={yk.lang.yads}\n\nTestClass3{pos={x= 3.0 y= 3.0}}", YadsJavaSerializer.serialize(fill(new TestClass3(), "pos", new Vec2f(3, 3))));
    //}
    //
    //private static <T> T fill(T obj, Object... values) {
    //    for (int i = 0; i < values.length; i += 2) Reflector.set(obj, (String) values[i], values[i+1]);
    //    return obj;
    //}
    //
    //@Test
    //public void test() {
    //    System.out.println("'\u005cn'");
    //
    //    System.out.println(YadsNodeParser.parseList("hello world"));
    //    System.out.println(YadsNodeParser.parseClass("XY{10 20}"));
    //    System.out.println(YadsNodeParser.parseClass("HBox{pos = 10, 20 VBox{size= 50, 50}}"));
    //    System.out.println(YadsJavaSerializer.deserialize("import=yk.lang.yads HBox{pos = 10, 20 VBox{size= 50, 50}}"));
    //    System.out.println(HBox.class.getName());
    //    //System.out.println(YADSSerializer.deserializeClass(null, YadsNodeParser.parseClass("HBox{pos = 10, 20}")).toStringPrefixInfix());
    //
    //    //System.out.println(YADSSerializer.deserializeList(YadsNodeParser.parseList("import= yk.lang.yads HBox{pos = 10, 20}")).toStringPrefixInfix());
    //
    //    //TODO convert with respect to method call arguments types!
    //    //TODO map or YAD if class not defined and unknown
    //
    //}
    //
    ////@Test
    ////public void testDeserializeComfort() {
    ////    assertEquals(hm("hello", "world", "someOther", al()), YadsJavaSerializer.deserializeMap("import=yk.lang.yads hello=world someOther={}"));
    ////    assertEquals(al("hello", new NamedMap("world")), YadsJavaSerializer.deserializeList("import=yk.lang.yads hello world {}"));
    ////    assertEquals(al("hello", "world", al()), YadsJavaSerializer.deserializeList("import=yk.lang.yads hello 'world' {}"));
    ////    assertEquals(al("hello", new NamedMap("world", hm("hello", "world"))), YadsJavaSerializer.deserializeList("import=yk.lang.yads hello world {hello=world}"));
    ////    assertEquals(al("hello", new YadsClass("world", al("hello"))), YadsJavaSerializer.deserializeList("import=yk.lang.yads hello world {hello}"));
    ////    assertEquals(al("hello", new YadsClass("world", al("hello", "world"))), YadsJavaSerializer.deserializeList("import=yk.lang.yads hello world {hello world}"));
    ////    assertEquals(al("hello", new YadsClass("world", al(new Tuple<>("hello", "world"), "someother"))),
    ////            YadsJavaSerializer.deserializeList("import=yk.lang.yads hello world {hello=world someother}"));
    ////
    ////    assertEquals(new NamedMap("world", hm("hello", "world")), YadsJavaSerializer.deserializeNamedMap("import=yk.lang.yads world {hello=world}"));
    ////}
    //
    //@Test
    //public void multipleImports() {
    //    //TODO fix for  "import= yk.jcommon.fastgeom, yk.jcommon.utils"
    //    //? don't write import list with comma ?
    //
    //    String ser = YadsJavaSerializer.serialize(al(v3(0, 0, 0), XYit.wh(10, 10)));
    //    System.out.println(ser);
    //
    //    //ser = "import= {yk.jcommon.fastgeom yk.jcommon.utils}\n" +
    //    //        "\n" +
    //    //        "{\n" +
    //    //        "  Vec3f{x= 0.0 y= 0.0 z= 0.0}\n" +
    //    //        "  XYit{r= 10 t= 10 l= 0 b= 0 x= 0 y= 0}\n" +
    //    //        "}\n";
    //    YadsJavaSerializer.deserialize(ser);
    //
    //    ser = YadsJavaSerializer.serialize(al(v3(0, 0, 0)));
    //    System.out.println(ser);
    //    YadsJavaSerializer.deserialize(ser);
    //}
    //
    //@Test
    //public void serDesList() {
    //
    //    assertEquals(al(), YadsJavaSerializer.deserializeList(YadsJavaSerializer.serializeList(al())));
    //    assertEquals(al("a"), YadsJavaSerializer.deserializeList(YadsJavaSerializer.serializeList(al("a"))));
    //    assertEquals(al("a", "b"), YadsJavaSerializer.deserializeList(YadsJavaSerializer.serializeList(al("a", "b"))));
    //
    //    assertEquals("", YadsJavaSerializer.serializeList(al()));
    //    assertEquals("  a\n", YadsJavaSerializer.serializeList(al("a")));
    //    assertEquals("  a\n  b\n", YadsJavaSerializer.serializeList(al("a", "b")));
    //
    //
    //    assertEquals(al(), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(al())));
    //    assertEquals(al("a"), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(al("a"))));
    //    assertEquals(al("a", "b"), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(al("a", "b"))));
    //
    //    assertEquals("{\n}\n", YadsJavaSerializer.serialize(al()));
    //    assertEquals("{\n  a\n}\n", YadsJavaSerializer.serialize(al("a")));
    //    assertEquals("{\n  a\n  b\n}\n", YadsJavaSerializer.serialize(al("a", "b")));
    //
    //}
    //
    //@Test
    //public void serDesMap() {
    //    assertEquals(hm(), YadsJavaSerializer.deserializeMap(YadsJavaSerializer.serializeMap(hm())));
    //    assertEquals(hm("a", "b"), YadsJavaSerializer.deserializeMap(YadsJavaSerializer.serializeMap(hm("a", "b"))));
    //    assertEquals(hm("a", "b", "c", "d"), YadsJavaSerializer.deserializeMap(YadsJavaSerializer.serializeMap(hm("a", "b", "c", "d"))));
    //
    //    assertEquals("", YadsJavaSerializer.serializeMap(hm()));
    //    assertEquals("a= b\n", YadsJavaSerializer.serializeMap(hm("a", "b")));
    //    assertEquals("a= b\nc= d\n", YadsJavaSerializer.serializeMap(hm("a", "b", "c", "d")));
    //
    //    //!! can't distinguish between empty map and empty list
    //    assertEquals(al(), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(hm())));
    //    assertEquals(hm("a", "b"), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(hm("a", "b"))));
    //    assertEquals(hm("a", "b", "c", "d"), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(hm("a", "b", "c", "d"))));
    //
    //    assertEquals("{\n}\n", YadsJavaSerializer.serialize(hm()));
    //    assertEquals("{\n  a= b\n}\n", YadsJavaSerializer.serialize(hm("a", "b")));
    //    assertEquals("{\n  a= b\n  c= d\n}\n", YadsJavaSerializer.serialize(hm("a", "b", "c", "d")));
    //
    //}
    //
    //@Test
    //public void serDesClass() {
    //    TestClass testClass = new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3);
    //
    //    assertEquals(testClass, YadsJavaSerializer.deserializeClassBody(TestClass.class, YadsJavaSerializer.serializeClassBody(testClass)));
    //    assertEquals(testClass, YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(testClass)));
    //
    //    assertEquals(al(testClass), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(al(testClass))));
    //    assertEquals(al(testClass, testClass), YadsJavaSerializer.deserialize(YadsJavaSerializer.serialize(al(testClass, testClass))));
    //}
    //


}
