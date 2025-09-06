package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Tests for YadsJavaToEntity and YadsJavaFromEntity.
 * 
 * All tests follow the round-trip pattern:
 * originalData → serialize → YadsEntity → print → text → parse → resolve → YadsEntity → deserialize → restoredData
 */
@SuppressWarnings("deprecation")
public class TestYadsJavaSerialization {

    
    /**
     * Performs a complete round-trip test with assertions.
     * 
     * @param original the original Java object
     * @param expectedSerialized the expected serialized text format
     * @param availableClasses classes allowed for object serialization/deserialization
     */
    private void roundTripAssert(Object original, String expectedSerialized, Class<?>... availableClasses) {
        // Create serializer and deserializer instances
        YadsJavaToEntity serializer = new YadsJavaToEntity(availableClasses);
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(availableClasses);
        
        // Step 1: Serialize Java object to YadsEntity
        Object serialized = serializer.serialize(original);
        
        // Step 2: Print YadsEntity to text using existing infrastructure
        String text = new YadsPrinter().print(serialized);
        assertEquals("Serialized format should match expected", expectedSerialized, text);
        
        // Step 3: Parse text back to YadsCst using existing infrastructure
        YadsCst parsed = YadsCstParser.parse(text);
        
        // Step 4: Resolve YadsCst to YadsEntity using existing infrastructure
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        
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
        YadsJavaToEntity serializer = new YadsJavaToEntity();
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity();
        
        Object serialized = serializer.serialize('a');
        String text = new YadsPrinter().print(serialized);
        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        Object charResult = deserializer.deserialize(resolved);
        assertEquals("Character should become string", "a", charResult);
        
        serialized = serializer.serialize(' ');
        text = new YadsPrinter().print(serialized);
        parsed = YadsCstParser.parse(text);
        resolved = YadsEntityFromCst.translate(parsed.children).get(0);
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
        // Test simple list format
        roundTripAssert(Arrays.asList("hello", "world"), "(hello world)");
        
        // Test nested list format
        roundTripAssert(Arrays.asList("start", Arrays.asList("a", "b"), "end"), "(start (a b) end)");
    }

    @Test
    public void testUnsupportedSerializationType() {
        // Test that unsupported types throw exceptions during serialization
        Object unsupported = new java.util.Date();
        YadsJavaToEntity serializer = new YadsJavaToEntity().setAllClassesAvailable(false); // no Date.class allowed
        
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
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(); // no unknown classes allowed
        
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
        
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(); // no classes needed for map test
        
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
        public Person friend; // For circular reference testing
        
        // Override equals for testing
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Person)) return false;
            Person other = (Person) obj;
            return age == other.age && 
                   java.util.Objects.equals(name, other.name) && 
                   java.util.Objects.equals(address, other.address) &&
                   java.util.Objects.equals(friend, other.friend);
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
        
        roundTripAssert(person, "Person(name = John age = 30)", Person.class);
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
        
        roundTripAssert(person, "Person(name = Alice)", Person.class);
    }
    
    @Test
    public void testSharedObjectReferences() {
        // Create shared address object
        Address sharedAddress = new Address();
        sharedAddress.street = "Shared St";
        sharedAddress.city = "Boston";
        
        // Create two people sharing the same address
        Person person1 = new Person();
        person1.name = "John";
        person1.age = 30;
        person1.address = sharedAddress;
        
        Person person2 = new Person();
        person2.name = "Jane";
        person2.age = 28;
        person2.address = sharedAddress; // Same address object
        
        // Create a list with both people
        List<Person> people = Arrays.asList(person1, person2);
        
        // Serialize and deserialize
        YadsJavaToEntity serializer = new YadsJavaToEntity(Person.class, Address.class);
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(Person.class, Address.class);
        
        Object serialized = serializer.serialize(people);
        String text = new YadsPrinter().print(serialized);
        
        // Verify that the serialized form contains references
        assertTrue("Should contain reference", text.contains("ref("));
        
        // Parse and deserialize
        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        @SuppressWarnings("unchecked")
        List<Person> result = (List<Person>) deserializer.deserialize(resolved);
        
        // Verify structure is preserved
        assertEquals("Should have 2 people", 2, result.size());
        assertEquals("First person name", "John", result.get(0).name);
        assertEquals("Second person name", "Jane", result.get(1).name);
        
        // Most importantly: verify that both people share the same address object
        assertSame("Both people should share the same address object", 
                  result.get(0).address, result.get(1).address);
        assertEquals("Shared address street", "Shared St", result.get(0).address.street);
    }
    
    @Test
    public void testCircularReferences() {
        // Create objects with circular references
        Person person = new Person();
        person.name = "Bob";
        person.age = 25;
        
        // Create a list containing the person and reference it from the person
        List<Object> data = new ArrayList<>();
        data.add(person);
        data.add("some string");
        
        // This would create a circular reference, but since Person doesn't have a list field,
        // let's create a simpler test with shared objects
        Address address1 = new Address();
        address1.street = "First St";
        
        Address address2 = new Address();
        address2.street = "Second St";
        
        // Create a list with duplicate addresses
        List<Address> addresses = Arrays.asList(address1, address2, address1); // address1 appears twice
        
        // Test round-trip
        YadsJavaToEntity serializer = new YadsJavaToEntity(Address.class);
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(Address.class);
        
        Object serialized = serializer.serialize(addresses);
        String text = new YadsPrinter().print(serialized);
        
        // Should contain references for duplicate address1
        assertTrue("Should contain reference for duplicate object", text.contains("ref("));

        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        @SuppressWarnings("unchecked")
        List<Address> result = (List<Address>) deserializer.deserialize(resolved);
        
        // Verify that first and third elements are the same object
        assertEquals("Should have 3 addresses", 3, result.size());
        assertSame("First and third should be same object", result.get(0), result.get(2));
        assertEquals("First address street", "First St", result.get(0).street);
        assertEquals("Second address street", "Second St", result.get(1).street);
    }
    
    @Test
    public void testTrueCircularReferences() {
        // Create a person who is their own friend (self-reference)
        Person person = new Person();
        person.name = "Alice";
        person.age = 30;
        person.friend = person; // Circular reference to self!
        
        // Test round-trip serialization
        YadsJavaToEntity serializer = new YadsJavaToEntity(Person.class);
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(Person.class);
        
        Object serialized = serializer.serialize(person);
        String text = new YadsPrinter().print(serialized);
        
        // Verify that the serialized form contains references
        assertTrue("Should contain reference for circular self-reference", text.contains("ref("));
        
        // Parse and deserialize
        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        Person result = (Person) deserializer.deserialize(resolved);
        
        // Verify that the circular reference is preserved
        assertEquals("Name should be preserved", "Alice", result.name);
        assertEquals("Age should be preserved", 30, result.age);
        assertSame("Person should be their own friend", result, result.friend);
    }
    
    @Test
    public void testMutualCircularReferences() {
        // Create two people who are friends with each other (mutual circular reference)
        Person alice = new Person();
        alice.name = "Alice";
        alice.age = 25;
        
        Person bob = new Person();
        bob.name = "Bob";
        bob.age = 30;
        
        // Set up mutual friendship
        alice.friend = bob;
        bob.friend = alice;
        
        // Create a list with both people
        List<Person> friends = Arrays.asList(alice, bob);
        
        // Test round-trip serialization
        YadsJavaToEntity serializer = new YadsJavaToEntity(Person.class);
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(Person.class);
        
        Object serialized = serializer.serialize(friends);
        String text = new YadsPrinter().print(serialized);
        
        // Verify that the serialized form contains references
        assertTrue("Should contain references for circular references", text.contains("ref("));
        
        // Parse and deserialize
        YadsCst parsed = YadsCstParser.parse(text);
        Object resolved = YadsEntityFromCst.translate(parsed.children).get(0);
        @SuppressWarnings("unchecked")
        List<Person> result = (List<Person>) deserializer.deserialize(resolved);
        
        // Verify that the circular references are preserved
        assertEquals("Should have 2 people", 2, result.size());
        
        Person resultAlice = result.get(0);
        Person resultBob = result.get(1);
        
        assertEquals("Alice's name should be preserved", "Alice", resultAlice.name);
        assertEquals("Bob's name should be preserved", "Bob", resultBob.name);
        
        // Most importantly: verify mutual circular references
        assertSame("Alice's friend should be Bob", resultBob, resultAlice.friend);
        assertSame("Bob's friend should be Alice", resultAlice, resultBob.friend);
    }
    
    @Test
    public void testSkipDefaultValues() {
        Person person = new Person();
        person.name = "Charlie";
        // age = 0 (default), address = null (default), friend = null (default)
        
        // Test with skipDefaultValues = true (default behavior)
        YadsJavaToEntity serializerSkip = new YadsJavaToEntity(Person.class);
        Object serializedSkip = serializerSkip.serialize(person);
        String textSkip = new YadsPrinter().print(serializedSkip);
        
        // Test with skipDefaultValues = false
        YadsJavaToEntity serializerNoSkip = new YadsJavaToEntity(Person.class).setSkipDefaultValues(false);
        Object serializedNoSkip = serializerNoSkip.serialize(person);
        String textNoSkip = new YadsPrinter().print(serializedNoSkip);
        
        // With skipDefaultValues = true, should only show non-default fields
        assertEquals("With skipDefaultValues=true", "Person(name = Charlie)", textSkip);
        
        // With skipDefaultValues = false, should show all fields
        assertEquals("With skipDefaultValues=false", "Person(name = Charlie age = 0 address = null friend = null)", textNoSkip);
        
        // Both should deserialize to the same result
        YadsJavaFromEntity deserializer = new YadsJavaFromEntity(Person.class);

        YadsCst parsedSkip = YadsCstParser.parse(textSkip);
        Object resolvedSkip = YadsEntityFromCst.translate(parsedSkip.children).get(0);
        Person resultSkip = (Person) deserializer.deserialize(resolvedSkip);

        YadsCst parsedNoSkip = YadsCstParser.parse(textNoSkip);
        Object resolvedNoSkip = YadsEntityFromCst.translate(parsedNoSkip.children).get(0);
        Person resultNoSkip = (Person) deserializer.deserialize(resolvedNoSkip);
        
        // Both results should be equivalent
        assertEquals("Both should have same name", "Charlie", resultSkip.name);
        assertEquals("Both should have same age", 0, resultSkip.age);
        assertNull("Both should have null address", resultSkip.address);
        assertNull("Both should have null friend", resultSkip.friend);
        
        assertEquals("Results should be equal", resultSkip.name, resultNoSkip.name);
        assertEquals("Results should be equal", resultSkip.age, resultNoSkip.age);
        assertEquals("Results should be equal", resultSkip.address, resultNoSkip.address);
        assertEquals("Results should be equal", resultSkip.friend, resultNoSkip.friend);
    }

    @Test
    public void testSpecificConverters() {
        assertEquals("hello!", new YadsJavaToEntity()
                .addByClassConverter(String.class, s -> s + "!")
                .serialize("hello"));

        assertEquals(al(2000, 3, 3), new YadsJavaToEntity()
                .addByClassConverter(Date.class, d -> al(d.getYear(), d.getMonth(), d.getDay()))
                .serialize(new Date(2000, 3, 4)));

        assertEquals("(2000 3 3)", Yads.printYadsEntity(new YadsJavaToEntity()
                .addByClassConverter(Date.class, d -> al(d.getYear(), d.getMonth(), d.getDay()))
                .serialize(new Date(2000, 3, 4))));

    }
}