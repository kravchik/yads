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

        // Floating point literal
        result = parseList("3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);

        // String literals
        result = parseList("\"hello\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertCstPosition(child);

        result = parseList("'world'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertCstPosition(child);

        // Any literal
        result = parseList("identifier");
        child = result.children.get(0);
        assertCstType("ANY_LITERAL", child);
        assertCstPosition(child);

        // Operator
        result = parseList("+");
        child = result.children.get(0);
        assertCstType("ANY_OPERATOR", child);
        assertCstPosition(child);
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
        // Negative integers
        YadsCst result = parseList("-42");
        YadsCst child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child);

        // Negative floats
        result = parseList("-3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);

        // Scientific notation
        result = parseList("1.5e10");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);

        // Float with suffix
        result = parseList("3.14f");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child);
    }

    @Test
    public void testOperators() {
        YadsCst result = parseList("+ - * / = < > ! & |");
        assertCstType("LIST_BODY", result);
        
        // All should be parsed as operators
        for (YadsCst child : result.children) {
            assertCstType("ANY_OPERATOR", child);
            assertCstPosition(child);
        }
    }

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
    public void testInvalidEscapeSequences() {
        // Test invalid escape sequence in double quotes
        try {
            parseList("\"hello\\x\"");
            fail("Expected RuntimeException for invalid escape sequence \\x");
        } catch (RuntimeException e) {
            assertTrue("Should contain 'Unsupported escaped symbol'", 
                       e.getMessage().contains("Unsupported escaped symbol"));
        }

        // Test invalid escape sequence in single quotes
        try {
            parseList("'hello\\z'");
            fail("Expected RuntimeException for invalid escape sequence \\z");
        } catch (RuntimeException e) {
            assertTrue("Should contain 'Unsupported escaped symbol'", 
                       e.getMessage().contains("Unsupported escaped symbol"));
        }
    }


}