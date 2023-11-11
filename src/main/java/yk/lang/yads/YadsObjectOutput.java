package yk.lang.yads;

import yk.lang.yads.utils.YadsUtils;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.util.Map;

import static yk.lang.yads.YadsEntityOutput.withoutQuotes;
import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Serializes yads objects into a YADS string. Performs formatting: introduces new-lines, adds tabs, tries to avoid unnecessary new-lines where possible.
 */
//TODO YadsObject -> YadsEntity
public class YadsObjectOutput {
    public static final YMap<Character, Character> JAVA_ESCAPES = hm(
            '\t', 't', '\b', 'b', '\n', 'n', '\r', 'r', '\f', 'f', '\"', '\"', '\\', '\\');
    public static final YMap<Character, Character> JAVA_UNESCAPES = JAVA_ESCAPES.map((k, v) -> v, (k, v) -> k);
    private int maxWidth = 100;
    private String tab = "  ";

    public String toString(YList<YadsObject> nodes) {
        return nodes.map(n -> toString(0, n).toString("\n")).toString("\n");
    }

    public String toStringBody(YadsObject objects) {
        YList<String> result = al();
        YList<YadsObject> args = objects.getNodeList(ARGS);
        YList<YadsObject> imports = args.filter(n -> n.isType(IMPORT));
        YList<YadsObject> other = args.filter(n -> !n.isType(IMPORT));
        objects = objects.with(ARGS, other);
        boolean possibleCompact = toStringBody(0, objects, result);
        if (possibleCompact) result = compact(0, result);
        YList<String> result1 = al();
        if (addList(0, imports, result1)) result1 = compact(0, result1);
        String si = result1.toString("\n");
        return (si.equals("") ? "" : (si + "\n")) + result.toString("\n");
    }

    public YList<String> toString(int width, YadsObject node) {
        if (node.isType(IMPORT)) return al("import " + node.getString(VALUE));
        if (node.isType(CONST)) return al(valueToString(node.map.get(VALUE)));

        if (node.isType(YADS_ARRAY)) {
            YList<YadsObject> oo = node.getNodeList(ARGS);
            YList<String> result = al();
            result.add("(");
            boolean possibleMerge = addList(width + tab.length(), oo, result);
            result.add(")");
            if (possibleMerge) result = compact(width, result);

            int size = result.size();
            if (size > 1) result = result.mapWithIndex((i, s) -> (i > 0 && i < size - 1) ? tab + s : s);

            return result;
        }

        if (node.isType(YADS_MAP)) {
            YMap<YadsObject, YadsObject> oo = (YMap) node.map.get(NAMED_ARGS);
            if (oo.isEmpty()) return al("(=)");
            return toStringMap(width, node, "(");
        }

        if (node.isType(YADS_NAMED)) return toStringMap(width, node, node.getString(NAME) + "(");

        throw new RuntimeException("Not implemented for " + node);
    }

    public static String valueToString(Object valObj) {
        String value;
        if (valObj == null) {
            value = "null";
        } else if (valObj instanceof String || valObj instanceof Character) {
            String value1;
            value1 = valObj.toString();
            boolean woQuotes = withoutQuotes(value1);
            if (!woQuotes) {
                //always serialize java strings as "", so that easy copy-paste
                value1 = "\"" + YadsUtils.escape(value1, YadsObjectOutput.JAVA_ESCAPES) + "\"";
            }
            value = value1;
        } else {
            value = YadsEntityOutput.valueToString(valObj);
        }
        return value;
    }

    private YList<String> toStringMap(int width, YadsObject node, String classPrefix) {
        YList<String> result = classPrefix == null ? al() : al(classPrefix);
        boolean possibleMerge = toStringBody(width + tab.length(), node, result);
        if (classPrefix != null) result.add(")");
        if (possibleMerge) result = compact(width, result);

        int size = result.size();
        if (size > 1) result = result.mapWithIndex((i, s) -> (i > 0 && i < size - 1) ? tab + s : s);

        return result;
    }

    private boolean toStringBody(int tab, YadsObject obj, YList<String> result) {
        YMap<YadsObject, YadsObject> objects = (YMap<YadsObject, YadsObject>) obj.map.get(NAMED_ARGS);
        boolean possibleMerge = true;
        if (obj.map.get(ARGS) != null) possibleMerge &= addList(tab, obj.getNodeList(ARGS), result);
        if (obj.map.get(NAMED_ARGS) != null) possibleMerge &= addMap(tab, objects, result);
        return possibleMerge;
    }

    private boolean addList(int tab, YList<YadsObject> nodes, YList<String> result) {
        boolean possibleMerge = true;
        for (YadsObject n : nodes) {
            YList<String> children = toString(tab, n);
            if (children.size() > 1) possibleMerge = false;
            result.addAll(children);
        }
        return possibleMerge;
    }

    private boolean addMap(int tab, YMap<YadsObject, YadsObject> oo, YList<String> result) {
        boolean possibleMerge = true;
        for (Map.Entry<YadsObject, YadsObject> entry : oo.entrySet()) {
            YList<String> key = toString(0, entry.getKey());
            if (key.size() != 1) throw new RuntimeException("Unexpected key: " + key);
            String keyPrefix = key.first() + "=";
            YList<String> children = toString(tab + keyPrefix.length(), entry.getValue());
            if (children.size() > 1) possibleMerge = false;
            for (int i = 0; i < children.size(); i++) {
                String child = children.get(i);
                if (i == 0) result.add(keyPrefix + (child.startsWith("-") ? " " : "") + child);
                else result.add(child);
            }
            //TODO fix for (=== = +++)
        }
        return possibleMerge;
    }

    public static boolean needSpaceBetween(String prev, String next) {
        if (prev.endsWith("(")) return false;
        if (next.startsWith(")")) return false;
        return true;
    }

    public static int estimateLength(YList<String> ss) {
        int total = 0;
        String last = null;
        for (String s : ss) {
            total += s.length();
            if (last != null && needSpaceBetween(last, s)) total += 1;
            last = s;
        }
        return total;
    }

    private static String compact(YList<String> ss) {
        StringBuilder result = new StringBuilder();
        String prev = null;
        for (String s : ss) {
            if (prev == null) result.append(s);
            else {
                if (needSpaceBetween(prev, s)) result.append(" ");
                result.append(s);
            }
            prev = s;
        }
        return result.toString();
    }

    private YList<String> compact(int width, YList<String> ss) {
        if (width + estimateLength(ss) > maxWidth) return ss;
        return al(compact(ss));
    }

    public YadsObjectOutput withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }
}
