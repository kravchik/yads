package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Tests for YadsCstJavaSerializer and YadsCstJavaDeserializer.
 * 
 * All tests follow the round-trip pattern:
 * originalData → serialize → YadsEntity → print → text → parse → resolve → YadsEntity → deserialize → restoredData
 */
public class TestYadsCstJavaSerialization {

    /**
     * Performs a complete round-trip test of serialization and deserialization.
     * 
     * @param original the original Java object
     * @return the restored object after round-trip
     */
    private Object roundTrip(Object original) {
        // Step 1: Serialize Java object to YadsEntity
        Object serialized = YadsCstJavaSerializer.serialize(original);
        
        // Step 2: Print YadsEntity to text using existing infrastructure
        String text = new YadsCstOutput().print(serialized);
        
        // Step 3: Parse text back to YadsCst using existing infrastructure
        YadsCst parsed = YadsCstParser.parse(text);
        
        // Step 4: Resolve YadsCst to YadsEntity using existing infrastructure
        Object resolved = YadsCstResolver.resolveList(parsed.children).get(0);
        
        // Step 5: Deserialize YadsEntity back to Java object
        Object deserialized = YadsCstJavaDeserializer.deserialize(resolved);
        
        return deserialized;
    }
    
    /**
     * Performs a complete round-trip test with assertions.
     * 
     * @param original the original Java object
     * @param expectedSerialized the expected serialized text format
     */
    private void roundTripAssert(Object original, String expectedSerialized) {
        // Step 1: Serialize Java object to YadsEntity
        Object serialized = YadsCstJavaSerializer.serialize(original);
        
        // Step 2: Print YadsEntity to text using existing infrastructure
        String text = new YadsCstOutput().print(serialized);
        assertEquals("Serialized format should match expected", expectedSerialized, text);
        
        // Step 3: Parse text back to YadsCst using existing infrastructure
        YadsCst parsed = YadsCstParser.parse(text);
        
        // Step 4: Resolve YadsCst to YadsEntity using existing infrastructure
        Object resolved = YadsCstResolver.resolveList(parsed.children).get(0);
        
        // Step 5: Deserialize YadsEntity back to Java object
        Object deserialized = YadsCstJavaDeserializer.deserialize(resolved);
        
        // Assert the content - type check is implicit in equals
        assertEquals("Round-trip should preserve data", original, deserialized);
    }

    @Test
    public void testStringRoundTrip() {
        roundTripAssert("hello world", "'hello world'");
    }

    @Test
    public void testEmptyListRoundTrip() {
        roundTripAssert(new ArrayList<>(), "()");
    }

    @Test
    public void testStringListRoundTrip() {
        roundTripAssert(Arrays.asList("hello", "world", "test"), "(hello world test)");
    }

    @Test
    public void testSingleElementListRoundTrip() {
        roundTripAssert(Arrays.asList("single"), "(single)");
    }

    @Test
    public void testNestedListRoundTrip() {
        roundTripAssert(Arrays.asList("start", Arrays.<Object>asList("a", "b"), "middle", Arrays.<Object>asList("c", "d"), "end"), "(start (a b) middle (c d) end)");
    }

    @Test
    public void testDeeplyNestedListRoundTrip() {
        roundTripAssert(Arrays.asList("top", Arrays.asList("mid", Arrays.<Object>asList("deep"))), "(top (mid (deep)))");
    }

    @Test
    public void testMixedListRoundTrip() {
        roundTripAssert(Arrays.asList(
            "string",
            Arrays.asList("nested", "list"),
            "another string",
            Arrays.asList()  // empty list
        ), "(string (nested list) 'another string' ())");
    }

    @Test
    public void testEmptyMapRoundTrip() {
        roundTripAssert(hm(), "(=)");
    }

    @Test
    public void testSimpleMapRoundTrip() {
        roundTripAssert(hm("key1", "value1", "key2", "value2"), "(key1 = value1 key2 = value2)");
    }

    @Test
    public void testNestedMapRoundTrip() {
        roundTripAssert(hm("string", "test", "nested", hm("inner1", "value1", "inner2", "value2"), "list", Arrays.asList("a", "b")), 
                       "(string = test nested = (inner1 = value1 inner2 = value2) list = (a b))");
    }

    @Test
    public void testOutputFormat() {
        // This test is now covered by the individual roundTripAssert calls
        // Test simple list format
        roundTripAssert(Arrays.asList("hello", "world"), "(hello world)");
        
        // Test nested list format
        roundTripAssert(Arrays.asList("start", Arrays.asList("a", "b"), "end"), "(start (a b) end)");
    }

    @Test
    public void testUnsupportedSerializationType() {
        // Test that unsupported types throw exceptions during serialization
        Object unsupported = new java.util.Date();
        
        try {
            YadsCstJavaSerializer.serialize(unsupported);
            fail("Expected RuntimeException for unsupported serialization type");
        } catch (RuntimeException e) {
            assertTrue("Should mention unsupported object type", 
                      e.getMessage().startsWith("Unsupported object type for serialization: java.util.Date"));
        }
    }

    @Test
    public void testUnsupportedDeserializationType() {
        // Test that unsupported entity types throw exceptions during deserialization
        YadsEntity unsupportedEntity = new YadsEntity("com.unknown.Class", 
                                                        al("data"));
        
        try {
            YadsCstJavaDeserializer.deserialize(unsupportedEntity);
            fail("Expected RuntimeException for unsupported deserialization type");
        } catch (RuntimeException e) {
            assertTrue("Should have expected error message", 
                      e.getMessage().startsWith("Unsupported entity type for deserialization: com.unknown.Class"));
        }
    }

    @Test
    public void testInvalidMapDeserialization() {
        // Test that invalid elements in map throw exceptions during deserialization
        // Create entity with both Tuple and invalid string - this will be treated as map
        YadsEntity invalidMapEntity = new YadsEntity(null, 
                                                      al(tuple("key", "value"), "invalid_string_in_map"));
        
        try {
            YadsCstJavaDeserializer.deserialize(invalidMapEntity);
            fail("Expected RuntimeException for invalid map element");
        } catch (RuntimeException e) {
            assertEquals("Should have expected error message", 
                        "Invalid element in map deserialization: java.lang.String, value: invalid_string_in_map", 
                        e.getMessage());
        }
    }
}