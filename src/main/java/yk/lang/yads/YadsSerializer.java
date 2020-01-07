package yk.lang.yads;

import yk.jcommon.collections.Tuple;
import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.jcommon.collections.YSet;
import yk.jcommon.utils.Reflector;
import yk.yast.common.YastNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YArrayList.toYList;
import static yk.jcommon.collections.YHashMap.hm;
import static yk.jcommon.collections.YHashSet.hs;
import static yk.jcommon.utils.Reflector.newInstanceArgless;
import static yk.lang.yads.YadsShorts.*;
import static yk.yast.common.Words.*;
import static yk.yast.common.YastNode.node;

public class YadsSerializer {
    private YSet<String> imports = hs();
    private YSet<String> defaultImports = hs();

    public void addDefaultImports(YList<String> imports) {
        defaultImports.addAll(imports);
    }

    private IdentityHashMap<Object, Tuple<YastNode, Integer>> identity = new IdentityHashMap<>();
    private int nextRefId = 1;

    public YList<YastNode> serialize(Object object) {
        YastNode result = serializeImpl(object, null);
        imports.removeAll(defaultImports);
        resolveRefs();
        return imports
                .toList()
                .map(i -> node(IMPORT, VALUE, i))
                .with(result);
    }

    public YastNode serializeBody(Object object) {
        if (object == null) throw new RuntimeException("Can't serialize body of null");
        if (object instanceof Number) throw new RuntimeException("Can't serialize body of Number");
        if (object instanceof String) throw new RuntimeException("Can't serialize body of String");

        if (object != null) defaultImports.add(object.getClass().getCanonicalName());
        YastNode result = serializeImpl(object, null);
        resolveRefs();
        return node(YADS_UNNAMED,
                ARGS, imports
                        .without(defaultImports).toList()
                        .map(i -> node(IMPORT, VALUE, i))
                        .with((Collection<YastNode>) result.map.getOr(ARGS, al())),
                NAMED_ARGS, result.map.get(NAMED_ARGS) == null ? hm() : result.map.get(NAMED_ARGS));
    }

    private void resolveRefs() {
        for (Tuple<YastNode, Integer> tuple : identity.values()) {
            if (tuple.b > 0) {
                YastNode copy = tuple.a.with(hm());
                tuple.a.map = hm(NODE_TYPE, YADS_NAMED, NAME, "ref", ARGS, al(constNode(tuple.b), copy));
            }
        }
    }

    private YastNode serializeImpl(Object object, Class knownType) {
        if (identity.containsKey(object)) {
            Tuple<YastNode, Integer> tuple = identity.get(object);
            if (tuple.b == 0) tuple.b = nextRefId++;
            return node(YADS_NAMED, NAME, "ref", ARGS, al(constNode(tuple.b)));
        }
        Tuple<YastNode, Integer> tuple = new Tuple<>(null, 0);//to avoid stack overflow when self-references

        //we don't want null to be referenced everywhere (TO DO the same for constants?)
        if (object != null) identity.put(object, tuple);

        YastNode yastNode = serializeImpl2(object, knownType);
        tuple.a = yastNode;
        return yastNode;
    }

    private YastNode serializeImpl2(Object object, Class knownType) {
        if (object == null) {
            return node(CONST, VALUE, null);
        } else if (object instanceof List) {//TODO better
            return node(YADS_ARRAY, ARGS, toYList(((List) object)).map(o -> serializeImpl(o, null)));
        } else if (object instanceof Map) {//TODO better
            YMap<Object, Object> result = hm();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet())
                result.put(serializeImpl(entry.getKey(), null), serializeImpl(entry.getValue(), null));
            return node(YADS_MAP, NAMED_ARGS, result);
        } else if (object instanceof String) {
            return node(CONST, VALUE, object);
        } else if (object instanceof Number) {
            return node(CONST, VALUE, object);
        } else if (object instanceof Character) {
            return node(CONST, VALUE, object);
        } else if (object instanceof Boolean) {
            return node(CONST, VALUE, object);
        } else {
            //TO DO use constructor ?
            if (object.getClass() != knownType) imports.add(object.getClass().getCanonicalName());
            YMap<YastNode, YastNode> fields = hm();
            Object defaults = newInstanceArgless(object.getClass());
            for (Field field : Reflector.getAllFieldsInHierarchy(object.getClass())) {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) continue;
                Object value = Reflector.get(object, field);
                if (defaults != null) {
                    Object defaultValue = Reflector.get(defaults, field);
                    if (value == defaultValue) continue;
                    if (value != null) {
                        if (value.equals(defaultValue)) continue;
                    }
                }
                fields.put(node(CONST, VALUE, field.getName()), serializeImpl(value, field.getType()));
            }

            if (object.getClass() != knownType) {
                return node(YADS_NAMED, NAME, object.getClass().getSimpleName(), NAMED_ARGS, fields);
            } else {
                if (fields.isEmpty()) return node(YADS_ARRAY, ARGS, al());
                else return node(YADS_MAP, NAMED_ARGS, fields);
            }
        }
    }


}
