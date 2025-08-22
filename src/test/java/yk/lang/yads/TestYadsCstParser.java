package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import static org.junit.Assert.*;

/**
 * Tests for CongoCC-based YadsCst parser
 */
public class TestYadsCstParser {

    private YadsCst parseList(String input) {
        return YadsCstParser.parse(input);
    }

    private YadsCst parseClass(String input) {
        try {
            YadsCstParser parser = new YadsCstParser(input);
            return parser.parseClass();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse: " + input, e);
        }
    }

    private void assertCstType(String expectedType, YadsCst cst) {
        assertNotNull("CST should not be null", cst);
        assertEquals("CST type mismatch", expectedType, cst.type);
    }

    private void assertCstPosition(YadsCst cst) {
        assertNotNull("CST should have caret", cst.caret);
        assertTrue("Begin offset should be valid", cst.caret.beginOffset >= 0);
        assertTrue("End offset should be valid", cst.caret.endOffset >= cst.caret.beginOffset);
    }

    @Test
    public void testEmptyListBody() {
        YadsCst result = parseList("");
        assertCstType("LIST_BODY", result);
        assertCstPosition(result);
        assertTrue("Empty list should have no children", result.children.isEmpty());
    }

    @Test
    public void testSingleLiterals() {
        // Integer literal
        YadsCst result = parseList("42");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        YadsCst child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Integer value should be parsed correctly", 42, child.value);

        // Floating point literal
        result = parseList("3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Float value should be parsed correctly", 3.14f, (Float)child.value, 0.001f);

        // String literals
        result = parseList("\"hello\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertCstPosition(child);
        assertEquals("Double-quoted string value should be parsed correctly", "hello", child.value);

        result = parseList("'world'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertCstPosition(child);
        assertEquals("Single-quoted string value should be parsed correctly", "world", child.value);

        // Any literal
        result = parseList("identifier");
        child = result.children.get(0);
        assertCstType("ANY_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Identifier value should be parsed correctly", "identifier", child.value);

        // Operator
        result = parseList("+");
        child = result.children.get(0);
        assertCstType("ANY_OPERATOR", child);
        assertCstPosition(child);
        assertEquals("Operator value should be parsed correctly", "+", child.value);
    }

    @Test
    public void testMultipleLiterals() {
        YadsCst result = parseList("42 hello \"world\"");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());

        assertCstType("INTEGER_LITERAL", result.children.get(0));
        assertCstType("ANY_LITERAL", result.children.get(1));
        assertCstType("STRING_LITERAL_DQ", result.children.get(2));

        // Verify all children have valid positions
        for (YadsCst child : result.children) {
            assertCstPosition(child);
        }
    }

    @Test
    public void testUnnamedClass() {
        YadsCst clazz = parseClass("(hello world)");
        assertCstType("UNNAMED_CLASS", clazz);
        assertCstPosition(clazz);
        
        // Check class structure: left paren + body + right paren
        assertEquals("Class should have 3 children", 3, clazz.children.size());
        assertCstType("LEFT_PAREN", clazz.children.get(0));
        assertCstType("LIST_BODY", clazz.children.get(1));
        assertCstType("RIGHT_PAREN", clazz.children.get(2));

        // Check inner list body
        YadsCst body = clazz.children.get(1);
        assertEquals("Body should have 2 children", 2, body.children.size());
        assertCstType("ANY_LITERAL", body.children.get(0));
        assertCstType("ANY_LITERAL", body.children.get(1));
        
        // Test field map for unnamed class
        assertEquals("Should have 1 field", 1, clazz.childByField.size());
        assertTrue("Should contain body field", clazz.childByField.containsKey("body"));
        assertFalse("Should not contain name field", clazz.childByField.containsKey("name"));
        
        // Test that field points to same object as child
        assertSame("Body field should point to LIST_BODY child", 
                  clazz.children.get(1), clazz.childByField.get("body"));
        assertEquals("Body field should be LIST_BODY", "LIST_BODY", clazz.childByField.get("body").type);
    }

    @Test
    public void testNamedClass() {
        YadsCst clazz = parseClass("Vec2(x y)");
        assertCstType("NAMED_CLASS", clazz);
        assertCstPosition(clazz);

        // Check class structure: name + left paren + body + right paren
        assertEquals("Class should have 4 children", 4, clazz.children.size());
        assertCstType("ANY_LITERAL", clazz.children.get(0));
        assertCstType("LEFT_PAREN", clazz.children.get(1));
        assertCstType("LIST_BODY", clazz.children.get(2));
        assertCstType("RIGHT_PAREN", clazz.children.get(3));

        // Check inner list body
        YadsCst body = clazz.children.get(2);
        assertEquals("Body should have 2 children", 2, body.children.size());
        assertCstType("ANY_LITERAL", body.children.get(0));
        assertCstType("ANY_LITERAL", body.children.get(1));
        
        // Test field map for named class
        assertEquals("Should have 2 fields", 2, clazz.childByField.size());
        assertTrue("Should contain name field", clazz.childByField.containsKey("name"));
        assertTrue("Should contain body field", clazz.childByField.containsKey("body"));
        
        // Test that fields point to same objects as children
        assertSame("Name field should point to ANY_LITERAL child", 
                  clazz.children.get(0), clazz.childByField.get("name"));
        assertSame("Body field should point to LIST_BODY child", 
                  clazz.children.get(2), clazz.childByField.get("body"));
        
        // Verify field types
        assertEquals("Name field should be ANY_LITERAL", "ANY_LITERAL", clazz.childByField.get("name").type);
        assertEquals("Body field should be LIST_BODY", "LIST_BODY", clazz.childByField.get("body").type);
        
        // Verify field values
        assertEquals("Name field should have correct value", "Vec2", clazz.childByField.get("name").value);
    }

    @Test
    public void testNestedClasses() {
        YadsCst outerClass = parseClass("outer(inner(value))");
        assertCstType("NAMED_CLASS", outerClass);
        
        YadsCst outerBody = outerClass.children.get(2);
        assertCstType("LIST_BODY", outerBody);
        assertEquals("Outer body should have one child", 1, outerBody.children.size());
        
        YadsCst innerClass = outerBody.children.get(0);
        assertCstType("NAMED_CLASS", innerClass);
        
        YadsCst innerBody = innerClass.children.get(2);
        assertCstType("LIST_BODY", innerBody);
        assertEquals("Inner body should have one child", 1, innerBody.children.size());
        assertCstType("ANY_LITERAL", innerBody.children.get(0));
        
        // Test field maps for nested classes
        // Outer class fields
        assertEquals("Outer class should have 2 fields", 2, outerClass.childByField.size());
        assertTrue("Outer class should contain name field", outerClass.childByField.containsKey("name"));
        assertTrue("Outer class should contain body field", outerClass.childByField.containsKey("body"));
        assertSame("Outer body field should point to correct child", 
                  outerBody, outerClass.childByField.get("body"));
        
        // Inner class fields  
        assertEquals("Inner class should have 2 fields", 2, innerClass.childByField.size());
        assertTrue("Inner class should contain name field", innerClass.childByField.containsKey("name"));
        assertTrue("Inner class should contain body field", innerClass.childByField.containsKey("body"));
        assertSame("Inner body field should point to correct child", 
                  innerBody, innerClass.childByField.get("body"));
        
        // Verify field types
        assertEquals("Outer name field should be ANY_LITERAL", "ANY_LITERAL", outerClass.childByField.get("name").type);
        assertEquals("Outer body field should be LIST_BODY", "LIST_BODY", outerClass.childByField.get("body").type);
        assertEquals("Inner name field should be ANY_LITERAL", "ANY_LITERAL", innerClass.childByField.get("name").type);
        assertEquals("Inner body field should be LIST_BODY", "LIST_BODY", innerClass.childByField.get("body").type);
    }

    @Test
    public void testComments() {
        // Single line comment
        YadsCst result = parseList("//this is a comment");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        YadsCst comment = result.children.get(0);
        assertCstType("COMMENT_SINGLE_LINE", comment);
        assertCstPosition(comment);

        // Multi-line comment
        result = parseList("/*multi line comment*/");
        comment = result.children.get(0);
        assertCstType("COMMENT_MULTI_LINE", comment);
        assertCstPosition(comment);

        // Comment with other elements
        result = parseList("value //comment");
        assertEquals("Should have two children", 2, result.children.size());
        assertCstType("ANY_LITERAL", result.children.get(0));
        assertCstType("COMMENT_SINGLE_LINE", result.children.get(1));
    }

    @Test
    public void testComplexNumbers() {
        // Integer without suffix (should be Integer)
        YadsCst result = parseList("42");
        YadsCst child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Integer without suffix should be Integer", 42, child.value);

        // Long with suffix (should be Long)
        result = parseList("42L");
        child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Integer with L suffix should be Long", 42L, child.value);

        // Float without suffix (should be Float)
        result = parseList("3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Floating point without suffix should be Float", 3.14f, (Float)child.value, 0.001f);

        // Float with F suffix (should be Float)
        result = parseList("3.14F");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Floating point with F suffix should be Float", 3.14f, (Float)child.value, 0.001f);

        // Double with D suffix (should be Double)
        result = parseList("3.14D");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Floating point with D suffix should be Double", 3.14, (Double)child.value, 0.001);

        // Negative integers
        result = parseList("-42");
        child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Negative integer should be parsed correctly", -42, child.value);

        // Negative floats
        result = parseList("-3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Negative float should be parsed correctly", -3.14f, (Float)child.value, 0.001f);

        // Scientific notation
        result = parseList("1.5e10");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
        assertEquals("Scientific notation should be parsed correctly", 1.5e10f, (Float)child.value, 1000f);
    }

    @Test
    public void testOperators() {
        YadsCst result = parseList("+ - * / == < > ! & |");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have 10 operators", 10, result.children.size());
        
        // Test each operator value
        String[] expectedOperators = {"+", "-", "*", "/", "==", "<", ">", "!", "&", "|"};
        for (int i = 0; i < expectedOperators.length; i++) {
            YadsCst child = result.children.get(i);
            assertCstType("ANY_OPERATOR", child);
            assertCstPosition(child);
            assertEquals("Operator " + i + " should have correct value", expectedOperators[i], child.value);
        }
    }

    //sql style escapes
    //@Test
    //public void testStringLiterals() {
    //    // Double quoted with quote escape (SQL-style: "" -> ")
    //    YadsCst result = parseList("\"Say \"\"Hello\"\"\"");
    //    YadsCst child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_DQ", child);
    //    assertCstPosition(child);
    //    assertEquals("Say \"Hello\"", child.value); // Should be unescaped
    //
    //    // Single quoted with quote escape (SQL-style: '' -> ')
    //    result = parseList("'Don''t'");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_SQ", child);
    //    assertCstPosition(child);
    //    assertEquals("Don't", child.value); // Should be unescaped
    //
    //    // Double quoted with multiple quote escapes
    //    result = parseList("\"\"\"Start\"\" middle \"\"End\"\"\"");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_DQ", child);
    //    assertEquals("\"Start\" middle \"End\"", child.value); // Should be unescaped
    //
    //    // Single quoted with multiple quote escapes
    //    result = parseList("'''Start'' middle ''End'''");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_SQ", child);
    //    assertEquals("'Start' middle 'End'", child.value); // Should be unescaped
    //
    //    // String without escapes
    //    result = parseList("\"Simple string\"");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_DQ", child);
    //    assertEquals("Simple string", child.value);
    //
    //    // Single quoted string without escapes
    //    result = parseList("'Simple string'");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_SQ", child);
    //    assertEquals("Simple string", child.value);
    //
    //    // Strings with literal backslashes (no escaping in SQL-style)
    //    result = parseList("\"Path\\to\\file\"");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_DQ", child);
    //    assertEquals("Path\\to\\file", child.value); // Backslashes should be literal
    //
    //    // Strings with literal newline characters (no \n escaping)
    //    result = parseList("\"Line1\\nTab\\tEnd\"");
    //    child = result.children.get(0);
    //    assertCstType("STRING_LITERAL_DQ", child);
    //    assertEquals("Line1\\nTab\\tEnd", child.value); // Should be literal text
    //}

    //c-style escapes
    @Test
    public void testStringLiterals() {
        // Double quoted with escapes
        YadsCst result = parseList("\"hello\\nworld\"");
        YadsCst child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertCstPosition(child);
        assertEquals("hello\nworld", child.value); // Should be unescaped

        // Single quoted with escapes
        result = parseList("'hello\\tworld'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertCstPosition(child);
        assertEquals("hello\tworld", child.value); // Should be unescaped

        // Double quoted with quote escape
        result = parseList("\"Say \\\"Hello\\\"\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Say \"Hello\"", child.value); // Should be unescaped

        // Single quoted with quote escape
        result = parseList("'Don\\'t'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertEquals("Don't", child.value); // Should be unescaped

        // String with backslash escape
        result = parseList("\"Path\\\\to\\\\file\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Path\\to\\file", child.value); // Should be unescaped

        // String with multiple escapes
        result = parseList("\"Line1\\nTab\\tQuote\\\"End\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Line1\nTab\tQuote\"End", child.value); // Should be unescaped

        // String without escapes
        result = parseList("\"Simple string\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Simple string", child.value);
    }

    @Test
    public void testWhitespaceHandling() {
        // Whitespace should be skipped but positions should be correct
        YadsCst result = parseList("  a    b  ");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have two children", 2, result.children.size());
        
        YadsCst first = result.children.get(0);
        YadsCst second = result.children.get(1);
        
        assertCstType("ANY_LITERAL", first);
        assertCstType("ANY_LITERAL", second);
        
        // Second element should start after the first
        assertTrue("Second element should start after first", 
                   second.caret.beginOffset > first.caret.endOffset);
    }

    @Test
    public void testNewlineWhitespace() {
        // Test that actual newline character is treated as whitespace
        YadsCst result = parseList("\ntrue");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        YadsCst child = result.children.get(0);
        assertCstType("ANY_LITERAL", child);
        assertCstPosition(child);
        assertEquals(Boolean.TRUE, child.value);
        
        // Test multiple whitespace types including newlines
        result = parseList("hello\n\t world\r\n42");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());
        
        assertEquals("hello", result.children.get(0).value);
        assertEquals("world", result.children.get(1).value);
        assertEquals(42, result.children.get(2).value);
    }

    @Test
    public void testBooleanAndNullLiterals() {
        // Test true literal
        YadsCst result = parseList("true");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        YadsCst trueNode = result.children.get(0);
        assertCstType("ANY_LITERAL", trueNode);
        assertEquals("true value should be Boolean.TRUE", Boolean.TRUE, trueNode.value);
        
        // Test false literal
        result = parseList("false");
        YadsCst falseNode = result.children.get(0);
        assertCstType("ANY_LITERAL", falseNode);
        assertEquals("false value should be Boolean.FALSE", Boolean.FALSE, falseNode.value);
        
        // Test null literal
        result = parseList("null");
        YadsCst nullNode = result.children.get(0);
        assertCstType("ANY_LITERAL", nullNode);
        assertEquals("null value should be null", null, nullNode.value);
        
        // Test mixed with other literals
        result = parseList("42 true 'hello' false null");
        assertEquals("Should have five children", 5, result.children.size());
        
        assertEquals(42, result.children.get(0).value);
        assertEquals(Boolean.TRUE, result.children.get(1).value);
        assertEquals("hello", result.children.get(2).value);
        assertEquals(Boolean.FALSE, result.children.get(3).value);
        assertEquals(null, result.children.get(4).value);
    }

    @Test
    public void testStaticParseMethod() {
        // Test the convenient static parse method
        YadsCst result = YadsCstParser.parse("hello 42 true");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());
        
        assertEquals("hello", result.children.get(0).value);
        assertEquals(42, result.children.get(1).value);
        assertEquals(Boolean.TRUE, result.children.get(2).value);
    }

    @Test
    public void testPositionAccuracy() {
        String input = "hello(world)";
        YadsCst clazz = parseClass(input);
        
        // Check that positions correspond to actual text
        assertCstType("NAMED_CLASS", clazz);
        
        // The class should span the entire input
        assertEquals("Class should start at beginning", 0, clazz.caret.beginOffset);
        assertEquals("Class should end at end of input", input.length(), clazz.caret.endOffset);
    }

    @Test
    public void testComplexValueParsing() {
        // Test a complex example with various value types in a named class
        YadsCst clazz = parseClass("MyClass(42 \"hello\" world)");
        assertCstType("NAMED_CLASS", clazz);
        
        // Test class name value
        assertEquals("Class name should be parsed correctly", "MyClass", clazz.childByField.get("name").value);
        
        // Test body values
        YadsCst body = clazz.childByField.get("body");
        assertEquals("Body should have 3 children", 3, body.children.size());
        
        // Test integer value
        YadsCst intNode = body.children.get(0);
        assertCstType("INTEGER_LITERAL", intNode);
        assertEquals("Integer value should be parsed correctly", 42, intNode.value);
        
        // Test string value
        YadsCst stringNode = body.children.get(1);
        assertCstType("STRING_LITERAL_DQ", stringNode);
        assertEquals("String value should be parsed correctly", "hello", stringNode.value);
        
        // Test identifier value
        YadsCst identifierNode = body.children.get(2);
        assertCstType("ANY_LITERAL", identifierNode);
        assertEquals("Identifier value should be parsed correctly", "world", identifierNode.value);
    }


}