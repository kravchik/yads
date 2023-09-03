package yk.lang.yads;

import yk.lang.yads.utils.Reflector;
import yk.ycollections.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static yk.lang.yads.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;
import static yk.ycollections.YHashSet.hs;

//YadsNode -> objects
public class YadsObjectDeserializer {

    private YList<Caret> caretStack = al();
    public Namespaces namespaces = new Namespaces();
    private YMap<Integer, Object> refs = hm();

    private void pushCaret(YadsNode node) {
        Caret e = (Caret) node.map.get(CARET);
        caretStack.add(e);
    }

    private void popCaret() {
        caretStack.remove(caretStack.size() - 1);
    }

    public <T> T deserializeSpecificType(Class<T> c, YadsNode node) {
        try {
            Object result = deserialize(c, node);
            if (c != null && !c.isAssignableFrom(result.getClass())) {
                throw new RuntimeException(String.format("Result class (%s) is not what was expected (%s)", result.getClass(), c));
            }
            return (T) result;
        } catch (RuntimeException re) {
            handleException(re);
            return null;
        }
    }

    void handleException(RuntimeException re) {
        if (caretStack.notEmpty()) {
            if (caretStack.last() == null) {
                Caret atCaret = caretStack.first(c -> c != null);
                if (atCaret != null) throw new RuntimeException("Error somewhere inside " + atCaret.toStringInside() + ", " + re.getMessage(), re);
                else throw new RuntimeException("Error somewhere inside file" + ", " + re.getMessage(), re);
            } else {
                throw new RuntimeException("Error at " + caretStack.last().toStringBegin() + ", " + re.getMessage(), re);
            }
        }

        Caret atCaret = caretStack.first(c -> c != null);
        throw new RuntimeException("Error at " + atCaret + ", " + re.getMessage(), re);
    }


    private Object deserialize(Class knownType, YadsNode node) {
        if (node.isType(YADS_NAMED) && node.getString(NAME).equals("ref")) {
            YList<YadsNode> args = node.getNodeList(ARGS);
            if (args.size() == 1) {
                int refId = ((Number)args.get(0).map.get(VALUE)).intValue();
                if (!refs.containsKey(refId)) throw new RuntimeException("Undefined (yet?) ref id: " + refId);
                return refs.get(refId);
            } else if (args.size() == 2) {
                int refId = ((Number)args.get(0).map.get(VALUE)).intValue();
                if (refs.containsKey(refId)) throw new RuntimeException("Ref id " + refId + " is already defined");
                Object result = deserialize2(knownType, args.get(1), refId);
                refs.put(refId, result);
                return result;
            } else {
                throw new RuntimeException("Unexpected count of args in ref (should be either 1 or 2)");
            }
        }
        return deserialize2(knownType, node, 0);
    }

    private Object deserialize2(Class knownType, YadsNode node, int refIndex) {
        pushCaret(node);
        Object result;
        if (node.isType(YADS_NAMED)) {
            result = construct(resolveName(knownType, node), node, refIndex);
        } else if (knownType == List.class || knownType == YList.class || knownType == YArrayList.class) {
            result = deserializeNodeList(node, refIndex);
        } else if (knownType == null && node.isType(YADS_ARRAY)) {
            result = deserializeNodeList(node, refIndex);
        } else if (knownType == Set.class || knownType == YSet.class || knownType == YHashSet.class) {
            result = deserializeNodeSet(node, refIndex);
        } else if (knownType == Map.class || knownType == YMap.class || knownType == YHashMap.class) {
            result = deserializeNodeMap(node, refIndex);
        } else if (knownType == null && (node.isType(YADS_MAP) || node.isType(YADS_UNNAMED))) {
            result = deserializeNodeMap(node, refIndex);
        } else if (node.isType(CONST)) {
            result = node.map.get(VALUE);
            if (knownType != null && result instanceof Number) {
                if (!Number.class.isAssignableFrom(knownType) && !knownType.isPrimitive()) throw new RuntimeException(String.format("Expected type %s, but was %s", knownType, result.getClass()));

                Number newNumber = convert(knownType, (Number) result);
                Number newNewNumber = convert(result.getClass(), newNumber);

                if (!newNewNumber.equals(result)) {
                    throw new RuntimeException(String.format("Can't properly convert %s type to %s type, value %s becomes %s", result.getClass(), knownType, result, newNewNumber));
                }
                result = newNumber;
            }
            if (knownType == Character.class || knownType == char.class) {
                if (result instanceof Number) result = (char)((Number) result).intValue();
                else if (result instanceof String) {
                    String s = (String) result;
                    if (s.length() != 1) throw new RuntimeException("Expected string with one symbol to convert it to char, but was'" + s + "'");
                    result = s.charAt(0);
                }
            }
            if (refIndex > 0) refs.put(refIndex, result);
        } else {
            if (knownType == null) throw new RuntimeException("Can't continue, type is still unknown");
            result = construct(knownType, node, refIndex);
        }
        popCaret();
        return result;
    }

    private Number convert(Class knownType, Number result) {
        Number newNumber = null;
        //TO DO assert ranges? only precision upscale?
        if (result instanceof Byte) {
            int i = result.intValue();
            if (i < 0) i = i + 256;
            result = i;
        }
        if (knownType == double.class || knownType == Double.class) newNumber = result.doubleValue();
        if (knownType == float.class || knownType == Float.class) newNumber = result.floatValue();
        if (knownType == long.class || knownType == Long.class) newNumber = result.longValue();
        if (knownType == int.class || knownType == Integer.class) newNumber = result.intValue();
        if (knownType == short.class || knownType == Short.class) newNumber = result.shortValue();
        if (knownType == byte.class || knownType == Byte.class) newNumber = result.byteValue();
        if (newNumber == null) throw new RuntimeException("Should never reach here");
        return newNumber;
    }

    private Class resolveName(Class knownType, YadsNode node) {
        Class typeByNode = null;
        String stringName = node.getString(NAME);
        if ("import".equals(stringName)) throw new RuntimeException("Can't use class name 'import'");
        if (stringName != null) typeByNode = namespaces.findClass(stringName);
        if (typeByNode != null && knownType != null && !knownType.isAssignableFrom(typeByNode)) throw new RuntimeException(String.format("Can't assign %s to %s", typeByNode, knownType));
        if (typeByNode != null) knownType = typeByNode;
        if (knownType == null) throw new RuntimeException("Unresolved type for node " + node.map.get(NAME));
        return knownType;
    }

    private Object construct(Class type, YadsNode node, int refIndex) {
        pushCaret(node);
        YList array = node.map.get(ARGS) == null ? al() : deserializeRawList((YList) node.map.get(ARGS));

        Object instance;
        if (array.notEmpty()) {
            Constructor constructor = Reflector.getApropriateConstructor(type, array.toArray());
            if (constructor != null) instance = Reflector.newInstance(type, array.toArray());
            else instance = Reflector.newInstance(type, array);
        } else instance = Reflector.newInstanceArgless(type);

        if (refIndex > 0) refs.put(refIndex, instance);

        //set fields from named args
        if (node.map.get(NAMED_ARGS) != null && ((YMap)node.map.get(NAMED_ARGS)).notEmpty()) {
            for (Map.Entry<YadsNode, YadsNode> entry1 : ((YMap<YadsNode, YadsNode>) node.map.get(NAMED_ARGS)).entrySet()) {

                pushCaret(entry1.getKey());
                String key = (String) deserialize(String.class, entry1.getKey());
                Field field = Reflector.getField(type, key);
                if (field == null) {
                    throw new RuntimeException(String.format("Class '%s' has no field '%s'", type, key));
                }

                pushCaret(entry1.getValue());
                Reflector.set(instance, key, deserialize(field.getType(), entry1.getValue()));
                popCaret();
                popCaret();
            }
        }
        popCaret();
        return instance;
    }

    private Object deserializeNodeMap(YadsNode node, int refIndex) {
        pushCaret(node);
        Object result;

        if (null != node.map.get(ARGS)) {
            YList list = deserializeRawList(node.getNodeList(ARGS));//to extract imports
            if (list.notEmpty()) throw new RuntimeException("Unexpected unnamed args in a map");
        }

        if (!(node.map.get(NAMED_ARGS) instanceof Map)) throw new RuntimeException("Unexpected type of named args " + node.map.get(ARGS).getClass() + " in map");

        result = deserializeRawMap((YMap) node.map.get(NAMED_ARGS), refIndex);
        popCaret();
        return result;
    }

    private YMap deserializeRawMap(YMap<YadsNode, YadsNode> rawMap, int refIndex) {
        YMap result = hm();
        if (refIndex > 0) refs.put(refIndex, result);
        //TO DO typed map
        if (rawMap != null) for (Map.Entry<YadsNode, YadsNode> entry : rawMap.entrySet()) {
            result.put(deserialize(null, entry.getKey()), deserialize(null, entry.getValue()));
        }
        return result;
    }

    private Object deserializeNodeSet(YadsNode node, int refIndex) {
        pushCaret(node);
        YSet result;
        if (null != node.map.get(NAMED_ARGS)) throw new RuntimeException("Unexpected named args in set");
        else if (node.map.get(ARGS) != null && !(node.map.get(ARGS) instanceof List)) throw new RuntimeException("Unexpected type of args " + node.map.get(ARGS).getClass() + " in set");
        else {
            result = hs();
            if (refIndex > 0) refs.put(refIndex, result);
            YList<YadsNode> argNodes = (YList<YadsNode>) node.map.get(ARGS);
            if (argNodes != null) {
                for (YadsNode argNode : argNodes) result.add(deserialize(null, argNode));
                if (result.size() != argNodes.size()) throw new RuntimeException("Set contains equal elements");
            }
            //TO DO typed array
        }

        popCaret();
        return result;
    }

    private Object deserializeNodeList(YadsNode node, int refIndex) {
        pushCaret(node);
        Object result;

        if (null != node.map.get(NAMED_ARGS)) {
            throw new RuntimeException("Unexpected named args in list");
        } else if (null == node.map.get(ARGS)) {
            result = al();
        } else if (!(node.map.get(ARGS) instanceof List)) {
            throw new RuntimeException("Unexpected type of args " + node.map.get(ARGS).getClass() + " in array");
        } else {
            result = deserializeRawList((YList<YadsNode>) node.map.get(ARGS), refIndex);
        }
        popCaret();
        return result;
    }

    YList deserializeRawList(YList<YadsNode> args) {
        return deserializeRawList(args, 0);
    }

    private YList deserializeRawList(YList<YadsNode> args, int refIndex) {
        YList resultList = al();
        if (refIndex > 0) refs.put(refIndex, resultList);
        for (int i = 0; i < (args).size(); i++) {
            YadsNode n = args.get(i);
            if ("import".equals(n.map.get(VALUE))) {
                namespaces.enterScope();//TODO exit scope, or require imports to be the first element
                pushCaret(n);
                if (args.size() <= i + 1) throw new RuntimeException("Expected type name after 'import'");
                popCaret();
                YadsNode nextNode = args.get(i + 1);
                pushCaret(nextNode);
                if (!nextNode.isType(CONST)
                        || !nextNode.map.containsKey(VALUE)
                        || !(nextNode.map.get(VALUE) instanceof String)) {
                    throw new RuntimeException("Element after 'import' should be a string constant");
                }
                namespaces.addClass(args.get(++i).getString(VALUE));
                popCaret();
                continue;
            }
            Object deserialized = deserialize(null, n);
            resultList.add(deserialized);
        }
        return resultList;
    }

    public static class Namespaces {

        public YList<NamespaceEntry> entries = al();

        public static Namespaces packages(String... ss) {
            Namespaces result = new Namespaces();
            result.enterScope();
            for (String s : ss) result.addPackage(s);
            return result;
        }

        public void enterScope() {
            entries.add(new NamespaceEntry());
        }

        public void exitScope() {
            entries.remove(entries.size() - 1);
        }

        public void addPackage(String p) {
            if (entries.isEmpty()) throw new RuntimeException("Not in any scope");
            entries.get(entries.size() - 1).packages.add(p);
        }

        public void addClass(String className) {
            if (entries.isEmpty()) throw new RuntimeException("Not in any scope");
            String cn = al(className.split("\\.")).last();
            entries.get(entries.size() - 1).classes.put(cn, className);
        }

        public Class findClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignore) { }

            for (int i = entries.size() - 1; i >= 0; i--) {
                NamespaceEntry entry = entries.get(i);
                if (entry.classes.containsKey(className)) try {
                    return Class.forName(entry.classes.get(className));
                } catch (ClassNotFoundException ignore) { }

                for (String p : entry.packages) try {
                    return Class.forName((p.length() > 0 ? p + "." : "") + className);
                } catch (ClassNotFoundException ignore) { }
            }
            return null;
        }

    }

    public static class NamespaceEntry {
        public YSet<String> packages = hs();
        public YMap<String, String> classes = hm();
    }
}
