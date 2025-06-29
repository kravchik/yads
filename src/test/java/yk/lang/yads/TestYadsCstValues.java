package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import static org.junit.Assert.assertEquals;

public class TestYadsCstValues {

    @Test
    public void testIntegerLiteralValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("42");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst intNode = result.children.get(0);
        assertEquals("INTEGER_LITERAL", intNode.type);
        assertEquals(42, intNode.value); // Parser returns Long for integers
    }

    @Test
    public void testLongLiteralValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("42L");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst longNode = result.children.get(0);
        assertEquals("INTEGER_LITERAL", longNode.type);
        assertEquals(42L, longNode.value); // With 'L' suffix it's Long
    }

    @Test
    public void testFloatLiteralValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("3.14");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst floatNode = result.children.get(0);
        assertEquals("FLOATING_POINT_LITERAL", floatNode.type);
        assertEquals(3.14f, (Float)floatNode.value, 0.001f); // Default is Float
    }

    @Test
    public void testFloatWithSuffixValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("3.14F");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst floatNode = result.children.get(0);
        assertEquals("FLOATING_POINT_LITERAL", floatNode.type);
        assertEquals(3.14f, (Float)floatNode.value, 0.001f); // Explicit 'F' suffix
    }

    @Test
    public void testDoubleLiteralValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("3.14D");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst doubleNode = result.children.get(0);
        assertEquals("FLOATING_POINT_LITERAL", doubleNode.type);
        assertEquals(3.14, (Double)doubleNode.value, 0.001); // With 'D' suffix it's Double
    }

    @Test
    public void testStringLiteralValues() throws Exception {
        YadsCstParser parser = new YadsCstParser("\"hello\" 'world'");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(2, result.children.size());

        YadsCst dqStringNode = result.children.get(0);
        assertEquals("STRING_LITERAL_DQ", dqStringNode.type);
        assertEquals("hello", dqStringNode.value);

        YadsCst sqStringNode = result.children.get(1);
        assertEquals("STRING_LITERAL_SQ", sqStringNode.type);
        assertEquals("world", sqStringNode.value);
    }

    @Test
    public void testAnyLiteralValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("myIdentifier");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(1, result.children.size());

        YadsCst literalNode = result.children.get(0);
        assertEquals("ANY_LITERAL", literalNode.type);
        assertEquals("myIdentifier", literalNode.value);
    }

    @Test
    public void testOperatorValue() throws Exception {
        YadsCstParser parser = new YadsCstParser("+ - * / ==");
        YadsCst result = parser.parseListBody();

        assertEquals("LIST_BODY", result.type);
        assertEquals(5, result.children.size());

        assertEquals("ANY_OPERATOR", result.children.get(0).type);
        assertEquals("+", result.children.get(0).value);

        assertEquals("ANY_OPERATOR", result.children.get(1).type);
        assertEquals("-", result.children.get(1).value);

        assertEquals("ANY_OPERATOR", result.children.get(2).type);
        assertEquals("*", result.children.get(2).value);

        assertEquals("ANY_OPERATOR", result.children.get(3).type);
        assertEquals("/", result.children.get(3).value);

        assertEquals("ANY_OPERATOR", result.children.get(4).type);
        assertEquals("==", result.children.get(4).value);
    }

    @Test
    public void testNamedClassWithValues() throws Exception {
        YadsCstParser parser = new YadsCstParser("MyClass(42 \"hello\" world)");
        YadsCst result = parser.parseClass();

        assertEquals("NAMED_CLASS", result.type);
        assertEquals("MyClass", result.childByField.get("name").value);

        YadsCst body = result.childByField.get("body");
        assertEquals(3, body.children.size());

        assertEquals("INTEGER_LITERAL", body.children.get(0).type);
        assertEquals(42, body.children.get(0).value); // Parser returns Long for integers

        assertEquals("STRING_LITERAL_DQ", body.children.get(1).type);
        assertEquals("hello", body.children.get(1).value);

        assertEquals("ANY_LITERAL", body.children.get(2).type);
        assertEquals("world", body.children.get(2).value);
    }
}