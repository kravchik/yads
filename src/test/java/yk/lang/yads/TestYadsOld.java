package yk.lang.yads;

import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YArrayList.toYList;
import static yk.jcommon.collections.YHashMap.hm;
import static yk.lang.yads.YadsWords.*;

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
    //    assertEquals("10l", YadsObjectSerializer.serialize(10l));
    //    assertEquals("hello", YadsObjectSerializer.serialize("hello"));
    //    assertEquals("'hello\\n'", YadsObjectSerializer.serialize("hello\n"));//TODO don't escape?
    //}
    //
    ////@Test
    ////public void deserializer() {
    ////    assertEquals(al("hello", "world"), YadsObjectSerializer.deserializeList("hello world"));
    ////    assertEquals(al(al("hello", "world"), al("hello2", "world2")), YadsObjectSerializer.deserializeList("{hello world} {hello2 world2}"));
    ////    assertEquals(hm("hello", "world"), YadsObjectSerializer.deserializeMap("hello=world"));
    ////
    ////    assertEquals(new YadsClass(null, al(new Tuple("a", "b"), "c")), YadsObjectSerializer.deserialize("{a=b c}"));
    ////    assertEquals(new YadsClass("name", al(new Tuple("a", "b"), "c")), YadsObjectSerializer.deserialize("name{a=b c}"));
    ////
    ////    assertEquals(al(new TestEnumClass(TestEnum.ENUM1)), YadsObjectSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnumClass{enumField=ENUM1}"));
    ////    assertEquals(al(TestEnum.ENUM1), YadsObjectSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnum{ENUM1}"));
    ////
    ////    assertEquals(al(new TestEnumClass(null)), YadsObjectSerializer.deserialize(Namespaces.packages("yk.lang.yads"), "TestEnumClass{enumField=null}"));
    ////}
    //
    ////@Test
    ////public void deserializerImports() {
    ////    assertEquals(new YadsClass("XY", al(1, 2)), YadsObjectSerializer.deserialize("XY{1 2}"));
    ////    assertEquals(new Vec2f(1, 2), YadsObjectSerializer.deserialize("yk.jcommon.fastgeom.Vec2f{x=1 y=2}"));
    ////    assertEquals(new Vec2f(1, 2), YadsObjectSerializer.deserialize("import=yk.jcommon.fastgeom \n\n Vec2f{x=1 y=2}"));
    ////    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsObjectSerializer.deserializeClassBody(TestClass.class, "import=yk.jcommon.fastgeom, yk.jcommon.fastgeom someList=1, 2 someMap={key1=value1 'key2'=value2} someInt=3"));
    ////}
    //
    //@Test
    //public void serializer() {
    //    assertEquals("import={yk.jcommon.fastgeom}\n\nVec2f{x= 1.0 y= 2.0}", YadsObjectSerializer.serialize(new Vec2f(1, 2)));
    //    assertEquals("{\n  hello\n  world\n}\n", YadsObjectSerializer.serialize(al("hello", "world")));
    //    assertEquals("{\n  k= v\n}\n", YadsObjectSerializer.serialize(hm("k", "v")));
    //
    //    assertEquals("import={yk.lang.yads}\n\nTestEnumClass{enumField= ENUM1}", YadsObjectSerializer.serialize(new TestEnumClass(TestEnum.ENUM1)));
    //    assertEquals("import={yk.lang.yads}\n\nTestEnumClass{}", YadsObjectSerializer.serialize(new TestEnumClass(null)));
    //
    //    assertEquals("{\n  hello\n  null\n}\n", YadsObjectSerializer.serialize(al("hello", null)));
    //
    //    assertEquals("{\n  'h\"e\\'l\\nl\\to'\n}\n", YadsObjectSerializer.serialize(al("h\"e'l\nl\to")));
    //
    //    assertEquals("enumField= ENUM1\n", YadsObjectSerializer.serializeClassBody(new TestEnumClass(TestEnum.ENUM1)));
    //}
    //
    //@Test
    //public void testClass() {
    //    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsObjectSerializer.deserializeClassBody(TestClass.class, "someList=1, 2 someMap={key1=value1 'key2'=value2} someInt=3"));
    //
    //    TestClass test1 = new TestClass();
    //    test1.someList2 = al(1, 2);
    //    test1.someList3 = al(3, 4);
    //    assertEquals(test1, YadsObjectSerializer.deserializeClassBody(TestClass.class, "someList2=1, 2 someList3=3,4"));
    //
    //    assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsObjectSerializer.deserializeClassBody(TestClass.class, "1, 2 {key1=value1 'key2'=value2} 3"));
    //
    //    assertEquals(new TestClass(false), YadsObjectSerializer.deserializeClassBody(TestClass.class, "someBoolean=false"));
    //
    //    try {
    //        assertEquals(new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3), YadsObjectSerializer.deserializeClassBody(TestClass.class, "someList=1, 2 someMap=hello someInt=3"));
    //        fail();
    //    } catch (BadException ignore) {
    //        assertEquals("found instance hello of class class java.lang.String but expected object of class yk.jcommon.collections.YHashMap", ignore.getMessage());
    //    }
    //
    //    TestClass tc = new TestClass();
    //    tc.tc2 = new TestClass2(1);
    //    assertEquals(tc, YadsObjectSerializer.deserializeClassBody(TestClass.class, "tc2=1f, 1f"));
    //    //assertEquals(tc, YadsObjectSerializer.deserializeClass(TestClass.class, "tc2={1f, 1f}"));
    //    //assertEquals(tc, YadsObjectSerializer.deserializeClass(TestClass.class, "tc2=TestClass2{1f, 1f}"));
    //    assertEquals(tc, YadsObjectSerializer.deserializeClassBody(TestClass.class, "tc2=1f"));
    //    assertEquals(tc, YadsObjectSerializer.deserializeClassBody(TestClass.class, "tc2={1f}"));
    //    assertEquals(tc, YadsObjectSerializer.deserializeClassBody(TestClass.class, "tc2=TestClass2{1f}"));
    //
    //    tc.tc2 = new TestClass2(al("hello", "world"));
    //    tc.tc2.a = 4;
    //    TestClass des = YadsObjectSerializer.deserializeClassBody(TestClass.class, "tc2={a=4 hello world}");
    //    assertEquals(tc, des);
    //}
    //
    //@Test
    //public void testYadsAware() {
    //    assertEquals("import={yk.lang.yads}\n\nTestClass2{1.0}", YadsObjectSerializer.serialize(new TestClass2(1)));
    //    assertEquals("import={yk.lang.yads}\n\nTestClass2{1.0 2.0}", YadsObjectSerializer.serialize(new TestClass2(1, 2)));
    //
    //    TestClass tc = new TestClass();
    //    tc.tc2 = new TestClass2(1);
    //    assertEquals("import={yk.lang.yads}\n\nTestClass{someInt= 0 tc2={1.0}someBoolean= false}", YadsObjectSerializer.serialize(tc));
    //
    //}
    //
    //@Test
    //public void testFieldTypeNotImported() {
    //    assertEquals(fill(new TestClass3(), "pos", new Vec2f(3, 3)), YadsObjectSerializer.deserialize("import=yk.lang.yads TestClass3{pos=3f, 3f}"));
    //    assertEquals(fill(new TestClass3(), "pos", new Vec2f(3, 3)), YadsObjectSerializer.deserializeClassBody(TestClass3.class, "pos=3f, 3f"));
    //}
    //
    //@Test
    //public void testFieldTypeIsKnown() {
    //    assertEquals("import={yk.lang.yads}\n\nTestClass3{pos={x= 3.0 y= 3.0}}", YadsObjectSerializer.serialize(fill(new TestClass3(), "pos", new Vec2f(3, 3))));
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
    //    System.out.println(YadsObjectSerializer.deserialize("import=yk.lang.yads HBox{pos = 10, 20 VBox{size= 50, 50}}"));
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
    ////    assertEquals(hm("hello", "world", "someOther", al()), YadsObjectSerializer.deserializeMap("import=yk.lang.yads hello=world someOther={}"));
    ////    assertEquals(al("hello", new NamedMap("world")), YadsObjectSerializer.deserializeList("import=yk.lang.yads hello world {}"));
    ////    assertEquals(al("hello", "world", al()), YadsObjectSerializer.deserializeList("import=yk.lang.yads hello 'world' {}"));
    ////    assertEquals(al("hello", new NamedMap("world", hm("hello", "world"))), YadsObjectSerializer.deserializeList("import=yk.lang.yads hello world {hello=world}"));
    ////    assertEquals(al("hello", new YadsClass("world", al("hello"))), YadsObjectSerializer.deserializeList("import=yk.lang.yads hello world {hello}"));
    ////    assertEquals(al("hello", new YadsClass("world", al("hello", "world"))), YadsObjectSerializer.deserializeList("import=yk.lang.yads hello world {hello world}"));
    ////    assertEquals(al("hello", new YadsClass("world", al(new Tuple<>("hello", "world"), "someother"))),
    ////            YadsObjectSerializer.deserializeList("import=yk.lang.yads hello world {hello=world someother}"));
    ////
    ////    assertEquals(new NamedMap("world", hm("hello", "world")), YadsObjectSerializer.deserializeNamedMap("import=yk.lang.yads world {hello=world}"));
    ////}
    //
    //@Test
    //public void multipleImports() {
    //    //TODO fix for  "import= yk.jcommon.fastgeom, yk.jcommon.utils"
    //    //? don't write import list with comma ?
    //
    //    String ser = YadsObjectSerializer.serialize(al(v3(0, 0, 0), XYit.wh(10, 10)));
    //    System.out.println(ser);
    //
    //    //ser = "import= {yk.jcommon.fastgeom yk.jcommon.utils}\n" +
    //    //        "\n" +
    //    //        "{\n" +
    //    //        "  Vec3f{x= 0.0 y= 0.0 z= 0.0}\n" +
    //    //        "  XYit{r= 10 t= 10 l= 0 b= 0 x= 0 y= 0}\n" +
    //    //        "}\n";
    //    YadsObjectSerializer.deserialize(ser);
    //
    //    ser = YadsObjectSerializer.serialize(al(v3(0, 0, 0)));
    //    System.out.println(ser);
    //    YadsObjectSerializer.deserialize(ser);
    //}
    //
    //@Test
    //public void serDesList() {
    //
    //    assertEquals(al(), YadsObjectSerializer.deserializeList(YadsObjectSerializer.serializeList(al())));
    //    assertEquals(al("a"), YadsObjectSerializer.deserializeList(YadsObjectSerializer.serializeList(al("a"))));
    //    assertEquals(al("a", "b"), YadsObjectSerializer.deserializeList(YadsObjectSerializer.serializeList(al("a", "b"))));
    //
    //    assertEquals("", YadsObjectSerializer.serializeList(al()));
    //    assertEquals("  a\n", YadsObjectSerializer.serializeList(al("a")));
    //    assertEquals("  a\n  b\n", YadsObjectSerializer.serializeList(al("a", "b")));
    //
    //
    //    assertEquals(al(), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(al())));
    //    assertEquals(al("a"), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(al("a"))));
    //    assertEquals(al("a", "b"), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(al("a", "b"))));
    //
    //    assertEquals("{\n}\n", YadsObjectSerializer.serialize(al()));
    //    assertEquals("{\n  a\n}\n", YadsObjectSerializer.serialize(al("a")));
    //    assertEquals("{\n  a\n  b\n}\n", YadsObjectSerializer.serialize(al("a", "b")));
    //
    //}
    //
    //@Test
    //public void serDesMap() {
    //    assertEquals(hm(), YadsObjectSerializer.deserializeMap(YadsObjectSerializer.serializeMap(hm())));
    //    assertEquals(hm("a", "b"), YadsObjectSerializer.deserializeMap(YadsObjectSerializer.serializeMap(hm("a", "b"))));
    //    assertEquals(hm("a", "b", "c", "d"), YadsObjectSerializer.deserializeMap(YadsObjectSerializer.serializeMap(hm("a", "b", "c", "d"))));
    //
    //    assertEquals("", YadsObjectSerializer.serializeMap(hm()));
    //    assertEquals("a= b\n", YadsObjectSerializer.serializeMap(hm("a", "b")));
    //    assertEquals("a= b\nc= d\n", YadsObjectSerializer.serializeMap(hm("a", "b", "c", "d")));
    //
    //    //!! can't distinguish between empty map and empty list
    //    assertEquals(al(), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(hm())));
    //    assertEquals(hm("a", "b"), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(hm("a", "b"))));
    //    assertEquals(hm("a", "b", "c", "d"), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(hm("a", "b", "c", "d"))));
    //
    //    assertEquals("{\n}\n", YadsObjectSerializer.serialize(hm()));
    //    assertEquals("{\n  a= b\n}\n", YadsObjectSerializer.serialize(hm("a", "b")));
    //    assertEquals("{\n  a= b\n  c= d\n}\n", YadsObjectSerializer.serialize(hm("a", "b", "c", "d")));
    //
    //}
    //
    //@Test
    //public void serDesClass() {
    //    TestClass testClass = new TestClass(al(1, 2), hm("key1", "value1", "key2", "value2"), 3);
    //
    //    assertEquals(testClass, YadsObjectSerializer.deserializeClassBody(TestClass.class, YadsObjectSerializer.serializeClassBody(testClass)));
    //    assertEquals(testClass, YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(testClass)));
    //
    //    assertEquals(al(testClass), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(al(testClass))));
    //    assertEquals(al(testClass, testClass), YadsObjectSerializer.deserialize(YadsObjectSerializer.serialize(al(testClass, testClass))));
    //}
    //


}
