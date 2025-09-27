package yk.lang.yads;

import yk.lang.yads.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Deserializes YadsEntity objects back to Java objects.
 * 
 * Converts YadsEntity objects (created by YadsJavaToEntity or parsed from text)
 * back to their original Java object representations.
 * 
 * Current support:
 * - String: passes through unchanged
 * - Primitives (Integer, Long, Float, Double, Boolean, Character): pass through unchanged
 * - YList/List: converts to YArrayList
 * - YadsEntity without name: treats as lists if no Tuples, as YHashMap if contains Tuples
 * - YadsEntity with name: deserializes as object (only for explicitly allowed classes)
 */
public class YadsJavaFromEntity {
    
    private final Map<String, Class<?>> classByName;
    private Map<Integer, Object> refs = new HashMap<>();

    private YMap<String, Function<YadsEntity, Object>> deserializerByName = hm();
    
    /**
     * Constructor that specifies which classes are allowed for object deserialization.
     * 
     * @param classes classes that can be deserialized as objects
     */
    public YadsJavaFromEntity(Class<?>... classes) {
        this.classByName = new HashMap<>();
        addImport(classes);
    }

    public YadsJavaFromEntity addImport(String name, Class<?> clazz) {
        classByName.put(name, clazz);
        return this;
    }

    public YadsJavaFromEntity addImport(Class<?>... cc) {
        for (Class<?> c : cc) classByName.put(c.getSimpleName(), c);
        return this;
    }

    public YadsJavaFromEntity addDeserializerByName(String name, Function<YadsEntity, Object> converter) {
        deserializerByName.put(name, converter);
        return this;
    }

    /**
     * Main entry point for deserialization.
     * 
     * @param obj YadsEntity or primitive to deserialize
     * @return Java object
     */
    public Object deserialize(Object obj) {
        refs.clear();
        return deserializeImpl(null, obj);
    }
    
    private Object deserializeImpl(Integer refId, Object obj) {
        if (obj == null) return null;
        if (obj instanceof List) return deserializeList(refId, (List) obj);
        if (obj instanceof Map) return obj;//special case, empty map returned as a map, not YadsEntity
        if (obj instanceof String) return obj;
        if (obj instanceof Number) return obj;
        if (obj instanceof Boolean) return obj;

        if (obj instanceof YadsEntity) {
            YadsEntity entity = (YadsEntity) obj;
            
            // Named entities can be objects or references
            if (entity.name != null) {
                // Handle references
                if ("ref".equals(entity.name)) {
                    if (refId != null) throw new RuntimeException("Ref inside a ref");
                    return handleReference(entity);
                }

                if (deserializerByName.containsKey(entity.name)) {
                    return deserializerByName.get(entity.name).apply(entity);
                }

                // Check if this is a known class
                if (classByName.containsKey(entity.name)) {
                    Class<?> clazz = classByName.get(entity.name);
                    if (clazz == null) throw new RuntimeException("Unknown class: " + entity.name);
                    return deserializeObject(refId, clazz, entity.children);
                }
                // Throw exception for unknown named entities
                throw new RuntimeException("Unsupported entity type for deserialization: " + entity.name + ", entity: " + entity);
            }

            if (entity.children.isAny(child -> child instanceof Tuple)) return deserializeMap(refId, entity.children);
            else return deserializeList(refId, entity.children);
        }

        throw new RuntimeException("Unsupported object type: " + obj.getClass());
    }

    private YList<Object> deserializeList(Integer refId, List list) {
        YList<Object> result = al();
        if (refId != null) refs.put(refId, list);
        for (Object child : (List<?>) list) {
            if (child instanceof Tuple) throw new RuntimeException("Unexpecte list element type: " + child.getClass());
            result.add(deserializeImpl(null, child));
        }
        return result;
    }
    
    /**
     * Deserializes YList containing Tuples back to YHashMap.
     */
    private Map<Object, Object> deserializeMap(Integer refId, YList<?> children) {
        Map<Object, Object> result = hm();
        if (refId != null) refs.put(refId, result);

        for (Object child : children) {
            if (child instanceof Tuple) {
                result.put(deserializeImpl(null, ((Tuple) child).a), deserializeImpl(null, ((Tuple) child).b));
            } else if (child instanceof YadsEntity.YadsComment) {
                // Skip comments
            } else throw new RuntimeException("Invalid element in map deserialization: "
                    + child.getClass().getName() + ", value: " + child);
        }
        
        return result;
    }

    public Object deserializeObject(Integer refId, Class<?> clazz, YList children) {
        //fix for external call
        if (List.class.isAssignableFrom(clazz)) return deserializeList(refId, children);
        if (Map.class.isAssignableFrom(clazz)) return deserializeMap(refId, children);

        Object instance = Reflector.newInstanceArgless(clazz);
        if (refId != null) refs.put(refId, instance);
        deserializeObjectFields(instance, children);
        return instance;
    }

    /**
     * Deserializes fields of an object from YadsEntity.
     * Separated method to allow proper reference handling.
     */
    private void deserializeObjectFields(Object instance, YList children) {
        Class<?> clazz = instance.getClass();
        
        // Set fields from tuples
        for (Object child : children) {
            if (child instanceof Tuple) {
                Tuple<?, ?> tuple = (Tuple<?, ?>) child;
                String fieldName = (String) tuple.a;
                Object fieldValue = deserializeImpl(null, tuple.b);
                
                Field field = Reflector.getField(clazz, fieldName);
                if (field == null) throw new RuntimeException("Unknown field: " + fieldName);
                Reflector.set(instance, field, fieldValue);
            } else if (child instanceof YadsEntity.YadsComment) {
                // Skip comments
            } else {
                // Everything else is invalid in object context
                throw new RuntimeException("Invalid element in object deserialization: " + child.getClass().getName() + ", value: " + child);
            }
        }
    }
    
    /**
     * Handles reference entities: ref(id) or ref(id, object).
     */
    private Object handleReference(YadsEntity entity) {
        if (entity.children.size() == 1) {
            Integer refId = (Integer) entity.children.get(0);
            if (!refs.containsKey(refId)) throw new RuntimeException("Undefined reference id: " + refId);
            return refs.get(refId);
        } else if (entity.children.size() == 2) {
            // ref(id, object), pass refId to store it BEFORE deserializing its body
            Integer refId = (Integer) entity.children.get(0);
            if (refs.containsKey(refId)) throw new RuntimeException("Reference id " + refId + " is already defined");
            return deserializeImpl(refId, entity.children.get(1));
        } else {
            throw new RuntimeException("Unexpected number of arguments in ref: " + entity.children.size());
        }
    }
}