package yk.lang.yads;

import yk.lang.yads.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Deserializes YadsEntity objects back to Java objects.
 * 
 * Converts YadsEntity objects (created by YadsJavaSerializer or parsed from text)
 * back to their original Java object representations.
 * 
 * Current support:
 * - String: passes through unchanged
 * - Primitives (Integer, Long, Float, Double, Boolean, Character): pass through unchanged
 * - YList/List: converts to YArrayList
 * - YadsEntity without name: treats as lists if no Tuples, as YHashMap if contains Tuples
 * - YadsEntity with name: deserializes as object (only for explicitly allowed classes)
 */
public class YadsJavaDeserializer {
    
    private final Map<String, Class<?>> classBySimpleName;
    private Map<Integer, Object> refs = new HashMap<>();
    
    /**
     * Constructor that specifies which classes are allowed for object deserialization.
     * 
     * @param classes classes that can be deserialized as objects
     */
    public YadsJavaDeserializer(Class<?>... classes) {
        this.classBySimpleName = new HashMap<>();
        for (Class<?> clazz : classes) {
            classBySimpleName.put(clazz.getSimpleName(), clazz);
        }
    }
    
    /**
     * Main entry point for deserialization.
     * 
     * @param obj YadsEntity or primitive to deserialize
     * @return Java object
     */
    public Object deserialize(Object obj) {
        // Clear refs for each top-level deserialization
        refs.clear();
        
        return deserializeImpl(obj);
    }
    
    /**
     * Internal deserialization with reference tracking.
     * 
     * @param obj YadsEntity or primitive to deserialize
     * @return Java object
     */
    private Object deserializeImpl(Object obj) {
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
            
            // Named entities can be objects or references
            if (entity.name != null) {
                // Handle references
                if ("ref".equals(entity.name)) {
                    return handleReference(entity);
                }
                
                // Check if this is a known class
                if (classBySimpleName.containsKey(entity.name)) {
                    return deserializeObject(entity);
                }
                // Throw exception for unknown named entities
                throw new RuntimeException("Unsupported entity type for deserialization: " + entity.name + ", entity: " + entity);
            }
            
            // Unnamed entities (name == null) can be lists or maps
            // Check if contains Tuples - if so, it's a map
            boolean containsTuples = entity.children.isAny(child -> child instanceof Tuple);
            if (containsTuples) {
                return deserializeMap(entity.children);
            } else {
                return deserializeList(entity.children);
            }
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
    private YList<Object> deserializeList(Object list) {
        YList<Object> result = al();
        
        if (list instanceof List) {
            for (Object child : (List<?>) list) {
                // Recursively deserialize each child
                result.add(deserializeImpl(child));
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
    private Map<Object, Object> deserializeMap(YList<?> children) {
        Map<Object, Object> result = hm();
        
        for (Object child : children) {
            if (child instanceof Tuple) {
                Tuple<?, ?> tuple = (Tuple<?, ?>) child;
                
                // Recursively deserialize both key and value
                Object deserializedKey = deserializeImpl(tuple.a);
                Object deserializedValue = deserializeImpl(tuple.b);
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
    
    /**
     * Deserializes YadsEntity with name back to Java object.
     * 
     * @param entity YadsEntity with class name and field tuples
     * @return Java object with deserialized fields
     */
    private Object deserializeObject(YadsEntity entity) {
        Class<?> clazz = classBySimpleName.get(entity.name);
        if (clazz == null) {
            throw new RuntimeException("Unknown class: " + entity.name);
        }
        
        // Create instance without calling constructor
        Object instance = Reflector.newInstanceArgless(clazz);
        
        // Deserialize fields
        deserializeObjectFields(instance, entity);
        
        return instance;
    }
    
    /**
     * Deserializes fields of an object from YadsEntity.
     * Separated method to allow proper reference handling.
     * 
     * @param instance the object instance to populate
     * @param entity YadsEntity with field tuples
     */
    private void deserializeObjectFields(Object instance, YadsEntity entity) {
        Class<?> clazz = instance.getClass();
        
        // Set fields from tuples
        for (Object child : entity.children) {
            if (child instanceof Tuple) {
                Tuple<?, ?> tuple = (Tuple<?, ?>) child;
                String fieldName = (String) tuple.a;
                Object fieldValue = deserializeImpl(tuple.b); // recursive deserialization
                
                Field field = Reflector.getField(clazz, fieldName);
                if (field != null) {
                    Reflector.set(instance, field, fieldValue);
                } else {
                    // Skip unknown fields (for forward compatibility)
                }
            } else if (child instanceof YadsEntity.YadsComment) {
                // Skip comments
                continue;
            } else {
                // Everything else is invalid in object context
                throw new RuntimeException("Invalid element in object deserialization: " + child.getClass().getName() + ", value: " + child);
            }
        }
    }
    
    /**
     * Handles reference entities: ref(id) or ref(id, object).
     * 
     * @param entity the reference entity
     * @return the referenced object
     */
    private Object handleReference(YadsEntity entity) {
        if (entity.children.size() == 1) {
            // ref(id) - return existing reference
            int refId = (Integer) entity.children.get(0);
            if (!refs.containsKey(refId)) {
                throw new RuntimeException("Undefined reference id: " + refId);
            }
            return refs.get(refId);
        } else if (entity.children.size() == 2) {
            // ref(id, object) - create object and store reference before deserializing fields
            int refId = (Integer) entity.children.get(0);
            if (refs.containsKey(refId)) {
                throw new RuntimeException("Reference id " + refId + " is already defined");
            }
            
            Object objectToDeserialize = entity.children.get(1);
            Object result;
            
            if (objectToDeserialize instanceof YadsEntity) {
                YadsEntity objectEntity = (YadsEntity) objectToDeserialize;
                
                // If it's a named entity (object), create instance first and store in refs
                if (objectEntity.name != null && classBySimpleName.containsKey(objectEntity.name)) {
                    Class<?> clazz = classBySimpleName.get(objectEntity.name);
                    result = Reflector.newInstanceArgless(clazz);
                    refs.put(refId, result);
                    
                    // Now deserialize fields - this allows circular references
                    deserializeObjectFields(result, objectEntity);
                    return result;
                } else if (objectEntity.name == null) {
                    // Handle anonymous entities - these could be lists or maps that need reference tracking
                    // For now, deserialize normally and store the reference
                    result = deserializeImpl(objectToDeserialize);
                    refs.put(refId, result);
                    return result;
                }
            }
            
            // For other types, deserialize normally
            result = deserializeImpl(objectToDeserialize);
            refs.put(refId, result);
            return result;
        } else {
            throw new RuntimeException("Unexpected number of arguments in ref: " + entity.children.size());
        }
    }
}