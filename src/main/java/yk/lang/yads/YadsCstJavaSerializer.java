package yk.lang.yads;

import yk.ycollections.YList;

import java.util.List;
import java.util.Map;

import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;

/**
 * Serializes Java objects to YadsEntity representation.
 * 
 * Converts Java objects to YadsEntity objects that can be printed by YadsCstOutput
 * and later deserialized back by YadsCstJavaDeserializer.
 * 
 * Current support:
 * - String: passes through unchanged
 * - Primitives (Integer, Long, Float, Double, Boolean, Character): pass through unchanged
 * - List: converts to YList (without wrapper)
 * - Map: converts to YadsEntity without name, all elements as Tuple
 */
public class YadsCstJavaSerializer {
    
    /**
     * Main entry point for serialization.
     * 
     * @param obj Java object to serialize
     * @return YadsEntity representation or the object unchanged if it's a primitive
     */
    public static Object serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof String) {
            // Strings pass through unchanged
            return obj;
        }
        
        // Primitive types pass through unchanged
        if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || 
            obj instanceof Double || obj instanceof Boolean || obj instanceof Character) {
            return obj;
        }
        
        if (obj instanceof List) {
            return serializeList((List<?>) obj);
        }
        
        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }
        
        // Throw exception for unsupported types
        throw new RuntimeException("Unsupported object type for serialization: " + obj.getClass().getName() + ", value: " + obj);
    }
    
    /**
     * Serializes a List to YList (without wrapper).
     * 
     * @param list the List to serialize
     * @return YList with serialized children
     */
    private static YList<Object> serializeList(List<?> list) {
        YList<Object> serializedChildren = al();
        
        for (Object item : list) {
            // Recursively serialize each child
            serializedChildren.add(serialize(item));
        }
        
        return serializedChildren;
    }
    
    /**
     * Serializes a Map to YadsEntity without name, all elements as Tuple.
     * For empty maps, returns the map directly so YadsCstOutput can handle the (=) special case.
     * 
     * @param map the Map to serialize
     * @return YadsEntity with serialized key-value pairs as Tuples, or the Map itself if empty
     */
    private static Object serializeMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            // Return the map directly so YadsCstOutput can print it as (=)
            return map;
        }
        
        YList<Object> serializedChildren = al();
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // Each entry becomes a Tuple with serialized key and value
            Object serializedKey = serialize(entry.getKey());
            Object serializedValue = serialize(entry.getValue());
            serializedChildren.add(tuple(serializedKey, serializedValue));
        }
        
        return new YadsEntity(null, serializedChildren);
    }
}