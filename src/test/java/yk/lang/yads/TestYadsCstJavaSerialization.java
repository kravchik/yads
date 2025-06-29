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
     * Performs a complete round-trip test with assertions.
     * 
     * @param original the original Java object
     * @param expectedSerialized the expected serialized text format
     * @param availableClasses classes allowed for object serialization/deserialization
     */
    private void roundTripAssert(Object original, String expectedSerialized, Class<?>... availableClasses) {
        // Create serializer and deserializer instances
        YadsCstJavaSerializer serializer = new YadsCstJavaSerializer(availableClasses);
        YadsCstJavaDeserializer deserializer = new YadsCstJavaDeserializer(availableClasses);
        
        // Step 1: Serialize Java object to YadsEntity
        Object serialized = serializer.serialize(original);
        
        // Step 2: Print YadsEntity to text using existing infrastructure
        String text = new YadsCstOutput().print(serialized);
        assertEquals("Serialized format should match expected", expectedSerialized, text);
        
        // Step 3: Parse text back to YadsCst using existing infrastructure
        YadsCst parsed = YadsCstParser.parse(text);
        
        // Step 4: Resolve YadsCst to YadsEntity using existing infrastructure
        Object resolved = YadsCstResolver.resolveList(parsed.children).get(0);
        
        // Step 5: Deserialize YadsEntity back to Java object
        Object deserialized = deserializer.deserialize(resolved);
        
        // Assert the content - type check is implicit in equals
        assertEquals("Round-trip should preserve data", original, deserialized);
    }
    
    /**
     * Convenience method for testing primitives without available classes.
     */
    private void roundTripAssert(Object original, String expectedSerialized) {
        roundTripAssert(original, expectedSerialized, new Class<?>[0]);
    }

    @Test
    public void testStringRoundTrip() {
        roundTripAssert("hello world", "'hello world'");
    }
    
    @Test
    public void testPrimitivesRoundTrip() {
        // Test various primitive types
        roundTripAssert(42, "42");
        roundTripAssert(123L, "123l");
        roundTripAssert(3.14f, "3.14f");
        roundTripAssert(2.71d, "2.71d");
        roundTripAssert(true, "true");
        roundTripAssert(false, "false");
        
        // Character becomes string (known limitation) - test manually
        YadsCstJavaSerializer serializer = new YadsCstJavaSerializer();
        YadsCstJavaDeserializer deserializer = new YadsCstJavaDeserializer();
        
        Object serialized = serializer.serialize('a');
        String text = new YadsCstOutput().print(serialized);
        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsCstResolver.resolveList(parsed.children).get(0);
        Object charResult = deserializer.deserialize(resolved);
        assertEquals("Character should become string", "a", charResult);
        
        serialized = serializer.serialize(' ');
        text = new YadsCstOutput().print(serialized);
        parsed = YadsCstParser.parse(text);
        resolved = YadsCstResolver.resolveList(parsed.children).get(0);
        charResult = deserializer.deserialize(resolved);
        assertEquals("Space character should become string", " ", charResult);
    }
    
    @Test
    public void testMixedPrimitivesListRoundTrip() {
        roundTripAssert(Arrays.asList(42, "hello", true, 3.14f), "(42 hello true 3.14f)");
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
        YadsCstJavaSerializer serializer = new YadsCstJavaSerializer(); // no Date.class allowed
        
        try {
            serializer.serialize(unsupported);
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
        YadsCstJavaDeserializer deserializer = new YadsCstJavaDeserializer(); // no unknown classes allowed
        
        try {
            deserializer.deserialize(unsupportedEntity);
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
        
        YadsCstJavaDeserializer deserializer = new YadsCstJavaDeserializer(); // no classes needed for map test
        
        try {
            deserializer.deserialize(invalidMapEntity);
            fail("Expected RuntimeException for invalid map element");
        } catch (RuntimeException e) {
            assertEquals("Should have expected error message", 
                        "Invalid element in map deserialization: java.lang.String, value: invalid_string_in_map", 
                        e.getMessage());
        }
    }
    
    // Test classes for object serialization
    public static class Person {
        public String name;
        public int age;
        public Address address;
        
        // Override equals for testing
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Person)) return false;
            Person other = (Person) obj;
            return age == other.age && 
                   java.util.Objects.equals(name, other.name) && 
                   java.util.Objects.equals(address, other.address);
        }
    }
    
    public static class Address {
        public String street;
        public String city;
        
        // Override equals for testing
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Address)) return false;
            Address other = (Address) obj;
            return java.util.Objects.equals(street, other.street) && 
                   java.util.Objects.equals(city, other.city);
        }
    }
    
    @Test
    public void testSimpleObjectRoundTrip() {
        Person person = new Person();
        person.name = "John";
        person.age = 30;
        
        roundTripAssert(person, "Person(name = John age = 30 address = null)", Person.class);
    }
    
    @Test
    public void testNestedObjectRoundTrip() {
        Address address = new Address();
        address.street = "Main St";
        address.city = "NYC";
        
        Person person = new Person();
        person.name = "John";
        person.age = 25;
        person.address = address;
        
        roundTripAssert(person, "Person(name = John age = 25 address = Address(street = 'Main St' city = NYC))", Person.class, Address.class);
    }
    
    @Test
    public void testObjectWithPrimitivesRoundTrip() {
        Person person = new Person();
        person.name = "Alice";
        person.age = 0; // default int value
        
        roundTripAssert(person, "Person(name = Alice age = 0 address = null)", Person.class);
    }
}