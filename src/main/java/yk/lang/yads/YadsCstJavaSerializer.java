package yk.lang.yads;

import yk.lang.yads.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

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
 * - Objects: converts to YadsEntity with class simple name and field tuples (only for explicitly allowed classes)
 */
public class YadsCstJavaSerializer {
    
    private final Set<Class<?>> availableClasses;
    private IdentityHashMap<Object, Tuple<YadsEntity, Integer>> identity = new IdentityHashMap<>();
    private int nextRefId = 1;
    public boolean skipDefaultValues = true;
    
    /**
     * Constructor that specifies which classes are allowed for object serialization.
     * 
     * @param classes classes that can be serialized as objects
     */
    public YadsCstJavaSerializer(Class<?>... classes) {
        this.availableClasses = new HashSet<>(Arrays.asList(classes));
    }
    
    /**
     * Main entry point for serialization.
     * 
     * @param obj Java object to serialize
     * @return YadsEntity representation or the object unchanged if it's a primitive
     */
    public Object serialize(Object obj) {
        // Clear identity map for each top-level serialization
        identity.clear();
        nextRefId = 1;
        
        Object result = serializeImpl(obj);
        
        // Resolve references after serialization
        resolveRefs();
        
        return result;
    }
    
    /**
     * Internal serialization with reference tracking.
     * 
     * @param obj Java object to serialize
     * @return YadsEntity representation or the object unchanged if it's a primitive
     */
    private Object serializeImpl(Object obj) {
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
        
        // For reference-tracked objects (Lists, Maps, Objects), check if already serialized
        if (obj instanceof List || obj instanceof Map || availableClasses.contains(obj.getClass())) {
            if (identity.containsKey(obj)) {
                Tuple<YadsEntity, Integer> tuple = identity.get(obj);
                if (tuple.b == 0) {
                    // Mark for reference creation
                    tuple.b = nextRefId++;
                }
                // Return reference placeholder
                return new YadsEntity("ref", al(tuple.b));
            }
            
            // Create tuple for tracking
            Tuple<YadsEntity, Integer> tuple = new Tuple<>(null, 0);
            identity.put(obj, tuple);
            
            YadsEntity result;
            if (obj instanceof List) {
                result = new YadsEntity(null, serializeList((List<?>) obj));
            } else if (obj instanceof Map) {
                Object mapResult = serializeMap((Map<?, ?>) obj);
                if (mapResult instanceof YadsEntity) {
                    result = (YadsEntity) mapResult;
                } else {
                    // Empty map case - return directly without wrapper
                    return mapResult;
                }
            } else {
                result = serializeObject(obj);
            }
            
            tuple.a = result;
            return result;
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
    private YList<Object> serializeList(List<?> list) {
        YList<Object> serializedChildren = al();
        
        for (Object item : list) {
            // Recursively serialize each child
            serializedChildren.add(serializeImpl(item));
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
    private Object serializeMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            // Return the map directly so YadsCstOutput can print it as (=)
            return map;
        }
        
        YList<Object> serializedChildren = al();
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // Each entry becomes a Tuple with serialized key and value
            Object serializedKey = serializeImpl(entry.getKey());
            Object serializedValue = serializeImpl(entry.getValue());
            serializedChildren.add(tuple(serializedKey, serializedValue));
        }
        
        return new YadsEntity(null, serializedChildren);
    }
    
    /**
     * Serializes an object to YadsEntity with class simple name and field tuples.
     * 
     * @param obj the object to serialize
     * @return YadsEntity with class name and field tuples
     */
    private YadsEntity serializeObject(Object obj) {
        YList<Object> children = al();
        
        // Create instance with default values for comparison if skipDefaultValues is enabled
        Object defaults = skipDefaultValues ? Reflector.newInstanceArgless(obj.getClass()) : null;
        
        // Get all fields using reflection
        for (Field field : Reflector.getAllFieldsInHierarchy(obj.getClass())) {
            // Skip static and transient fields
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            
            field.setAccessible(true);
            Object value = Reflector.get(obj, field);
            
            // Skip fields with default values if skipDefaultValues is enabled
            if (defaults != null) {
                Object defaultValue = Reflector.get(defaults, field);
                if (value == defaultValue) continue; // Same reference (both null, etc.)
                if (value != null && value.equals(defaultValue)) continue; // Equal values
            }
            
            Object serializedValue = serializeImpl(value); // recursive serialization
            children.add(tuple(field.getName(), serializedValue));
        }
        
        return new YadsEntity(obj.getClass().getSimpleName(), children);
    }
    
    /**
     * Resolves references after serialization is complete.
     * Replaces duplicate objects with ref(id, original) constructs.
     */
    private void resolveRefs() {
        for (Tuple<YadsEntity, Integer> tuple : identity.values()) {
            if (tuple.b > 0) {
                // Create a copy of the original entity with same children
                YList<Object> childrenCopy = al();
                childrenCopy.addAll(tuple.a.children);
                YadsEntity copy = new YadsEntity(tuple.a.name, childrenCopy);
                
                // Replace the original with ref(id, copy)
                tuple.a.name = "ref";
                tuple.a.children = al(tuple.b, copy);
            }
        }
    }

    public YadsCstJavaSerializer setSkipDefaultValues(boolean skipDefaultValues) {
        this.skipDefaultValues = skipDefaultValues;
        return this;
    }

}