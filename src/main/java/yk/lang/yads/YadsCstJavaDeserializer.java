package yk.lang.yads;

import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.util.List;
import java.util.Map;

import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Deserializes YadsEntity objects back to Java objects.
 * 
 * Converts YadsEntity objects (created by YadsCstJavaSerializer or parsed from text)
 * back to their original Java object representations.
 * 
 * Current support:
 * - String: passes through unchanged
 * - YList/List: converts to YArrayList
 * - YadsEntity without name: treats as lists if no Tuples, as YHashMap if contains Tuples
 */
public class YadsCstJavaDeserializer {
    
    /**
     * Main entry point for deserialization.
     * 
     * @param obj YadsEntity or primitive to deserialize
     * @return Java object
     */
    public static Object deserialize(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof String) {
            // Strings pass through unchanged
            return obj;
        }
        
        if (obj instanceof List) {
            // Lists (without key-value pairs) are deserialized to YArrayList
            return deserializeList(obj);
        }
        
        if (obj instanceof Map) {
            // Maps are passed through unchanged (already YHashMap from resolver)
            return obj;
        }
        
        if (obj instanceof YadsEntity) {
            YadsEntity entity = (YadsEntity) obj;
            
            // Unnamed entities (name == null) can be lists or maps
            if (entity.name == null) {
                // Check if contains Tuples - if so, it's a map
                boolean containsTuples = entity.children.isAny(child -> child instanceof Tuple);
                if (containsTuples) {
                    return deserializeMap(entity.children);
                } else {
                    return deserializeList(entity.children);
                }
            }
            
            // Throw exception for unknown named entities
            throw new RuntimeException("Unsupported entity type for deserialization: " + entity.name + ", entity: " + entity);
        }
        
        // For other objects (primitives, etc.), pass through unchanged
        return obj;
    }
    
    /**
     * Deserializes a List back to YArrayList.
     * 
     * @param list List to deserialize
     * @return YArrayList with deserialized children
     */
    private static YList<Object> deserializeList(Object list) {
        YList<Object> result = al();
        
        if (list instanceof List) {
            for (Object child : (List<?>) list) {
                // Recursively deserialize each child
                result.add(deserialize(child));
            }
        }
        
        return result;
    }
    
    /**
     * Deserializes YList containing Tuples back to YHashMap.
     * 
     * @param children YList containing Tuple objects
     * @return YHashMap with deserialized key-value pairs
     */
    private static Map<Object, Object> deserializeMap(YList<?> children) {
        Map<Object, Object> result = hm();
        
        for (Object child : children) {
            if (child instanceof Tuple) {
                Tuple<?, ?> tuple = (Tuple<?, ?>) child;
                
                // Recursively deserialize both key and value
                Object deserializedKey = deserialize(tuple.a);
                Object deserializedValue = deserialize(tuple.b);
                result.put(deserializedKey, deserializedValue);
            } else if (child instanceof YadsEntity.YadsComment) {
                // Skip comments
                continue;
            } else {
                // Everything else is invalid in map context
                throw new RuntimeException("Invalid element in map deserialization: " + child.getClass().getName() + ", value: " + child);
            }
        }
        
        return result;
    }
}