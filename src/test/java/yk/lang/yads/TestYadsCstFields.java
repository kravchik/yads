package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import static org.junit.Assert.*;

public class TestYadsCstFields {

    @Test
    public void testNamedClassFields() {
        YadsCstParser parser = new YadsCstParser("test(42 hello)");
        YadsCst cst = parser.parseClass();
        
        assertEquals("NAMED_CLASS", cst.type);
        assertEquals(4, cst.children.size()); // name + leftParen + body + rightParen
        
        // Test field map
        assertEquals(2, cst.childByField.size());
        assertTrue(cst.childByField.containsKey("name"));
        assertTrue(cst.childByField.containsKey("body"));
        
        // Test that fields point to same objects as children
        assertSame(cst.children.get(0), cst.childByField.get("name")); // ANY_LITERAL
        assertSame(cst.children.get(2), cst.childByField.get("body")); // LIST_BODY
        
        // Verify field types
        assertEquals("ANY_LITERAL", cst.childByField.get("name").type);
        assertEquals("LIST_BODY", cst.childByField.get("body").type);
    }

    @Test
    public void testUnnamedClassFields() {
        YadsCstParser parser = new YadsCstParser("(42 hello)");
        YadsCst cst = parser.parseClass();
        
        assertEquals("UNNAMED_CLASS", cst.type);
        assertEquals(3, cst.children.size()); // leftParen + body + rightParen
        
        // Test field map
        assertEquals(1, cst.childByField.size());
        assertTrue(cst.childByField.containsKey("body"));
        assertFalse(cst.childByField.containsKey("name")); // No name for unnamed class
        
        // Test that field points to same object as child
        assertSame(cst.children.get(1), cst.childByField.get("body")); // LIST_BODY
        
        // Verify field type
        assertEquals("LIST_BODY", cst.childByField.get("body").type);
    }

    @Test
    public void testNestedClassFields() {
        YadsCstParser parser = new YadsCstParser("outer(inner(42))");
        YadsCst outerCst = parser.parseClass();
        
        assertEquals("NAMED_CLASS", outerCst.type);
        
        // Get inner class from body
        YadsCst body = outerCst.childByField.get("body");
        assertEquals("LIST_BODY", body.type);
        
        YadsCst innerCst = body.children.get(0); // First element in list body
        assertEquals("NAMED_CLASS", innerCst.type);
        
        // Test inner class fields
        assertEquals(2, innerCst.childByField.size());
        assertEquals("ANY_LITERAL", innerCst.childByField.get("name").type);
        assertEquals("LIST_BODY", innerCst.childByField.get("body").type);
    }
}