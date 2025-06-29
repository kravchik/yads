package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static org.junit.Assert.*;

/**
 * Tests for YadsCstResolver - deserializing YadsCst back to data objects
 */
public class TestYadsCstResolver {

    @Test
    public void testSimpleLiterals() throws Exception {
        // Test integer
        YadsCstParser parser = new YadsCstParser("42");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        assertEquals(42, resolved.get(0));
        
        // Test float
        parser = new YadsCstParser("3.14f");
        result = parser.parseListBody();
        resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        assertEquals(3.14f, (Float) resolved.get(0), 0.001f);
        
        // Test double
        parser = new YadsCstParser("2.71d");
        result = parser.parseListBody();
        resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        assertEquals(2.71d, (Double) resolved.get(0), 0.001d);
        
        // Test string
        parser = new YadsCstParser("\"hello world\"");
        result = parser.parseListBody();
        resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        assertEquals("hello world", resolved.get(0));
    }

    @Test
    public void testComments() throws Exception {
        // Test single line comment
        YadsCstParser parser = new YadsCstParser("//this is a comment");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        Object comment = resolved.get(0);
        assertTrue(comment instanceof YadsEntity.YadsComment);
        YadsEntity.YadsComment yadsComment = (YadsEntity.YadsComment) comment;
        assertTrue(yadsComment.isOneLine);
        assertEquals("this is a comment", yadsComment.text);
        
        // Test multi-line comment
        parser = new YadsCstParser("/*multi\nline\ncomment*/");
        result = parser.parseListBody();
        resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        comment = resolved.get(0);
        assertTrue(comment instanceof YadsEntity.YadsComment);
        yadsComment = (YadsEntity.YadsComment) comment;
        assertFalse(yadsComment.isOneLine);
        assertEquals("multi\nline\ncomment", yadsComment.text);
    }

    @Test
    public void testNamedClass() throws Exception {
        YadsCstParser parser = new YadsCstParser("Person(\"John\" 25)");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity person = (YadsEntity) entity;
        assertEquals("Person", person.name);
        assertEquals(2, person.children.size());
        assertEquals("John", person.children.get(0));
        assertEquals(25, person.children.get(1));
    }

    @Test
    public void testUnnamedClass() throws Exception {
        YadsCstParser parser = new YadsCstParser("(\"data\" 123)");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity unnamed = (YadsEntity) entity;
        assertNull(unnamed.name);
        assertEquals(2, unnamed.children.size());
        assertEquals("data", unnamed.children.get(0));
        assertEquals(123, unnamed.children.get(1));
    }

    @Test
    public void testTupleConversion() throws Exception {
        // Test simple key=value
        YadsCstParser parser = new YadsCstParser("name = \"John\"");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        Object tuple = resolved.get(0);
        assertTrue(tuple instanceof Tuple);
        Tuple<?, ?> t = (Tuple<?, ?>) tuple;
        assertEquals("name", t.a);
        assertEquals("John", t.b);
    }

    @Test
    public void testComplexExample() throws Exception {
        // Test a complex structure with tuples and nested classes
        YadsCstParser parser = new YadsCstParser("Person(name = \"John\" age = 25 address = Address(\"123 Main St\"))");
        YadsCst result = parser.parseListBody();
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
        assertEquals(1, resolved.size());
        
        Object entity = resolved.get(0);
        assertTrue(entity instanceof YadsEntity);
        YadsEntity person = (YadsEntity) entity;
        assertEquals("Person", person.name);
        assertEquals(3, person.children.size());
        
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
        YList<Object> resolved = YadsCstResolver.resolveList(result.children);
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
}