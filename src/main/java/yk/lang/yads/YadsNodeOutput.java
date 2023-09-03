package yk.lang.yads;

import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.jcommon.utils.Tab;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static yk.jcommon.collections.YArrayList.al;
import static yk.lang.yads.YadsUtils.ESCAPE_YADS_DOUBLE_QUOTES;
import static yk.lang.yads.YadsUtils.ESCAPE_YADS_SINGLE_QUOTES;
import static yk.lang.yads.YadsWords.*;

/**
 * Serializes nodes into a YADS string. Performs formatting: introduces new-lines, adds tabs, tries to avoid unnecessary new-lines where possible.
 * Nodes can contain
 */
public class YadsNodeOutput {
    private int maxWidth = 100;
    private Tab tab = new Tab("  ");

    public String toString(YList<YadsNode> nodes) {
        return nodes.map(n -> toString(0, n).toString("\n")).toString("\n");
    }

    public String toStringBody(YadsNode nodes) {
        YList<String> result = al();
        YList<YadsNode> args = nodes.getNodeList(ARGS);
        YList<YadsNode> imports = args.filter(n -> n.isType(IMPORT));
        YList<YadsNode> other = args.filter(n -> !n.isType(IMPORT));
        nodes = nodes.with(ARGS, other);
        boolean possibleCompact = toStringBody(nodes, result);
        if (possibleCompact) result = compact(0, result);
        String si = addListAndCompact(imports);
        return (si.equals("") ? "" : (si + "\n")) + result.toString("\n");
    }

    private String addListAndCompact(YList<YadsNode> nn) {
        YList<String> result = al();
        if (addList(nn, result)) result = compact(0, result);
        return result.toString("\n");
    }

    public YList<String> toString(int width, YadsNode node) {
        if (node.isType(IMPORT)) {
            return al(tab + "import " + node.getString(VALUE));
        }

        if (node.isType(CONST)) {
            return al(valueToString(node.map.get(VALUE)));
        }

        if (node.isType(YADS_ARRAY)) {
            YList<YadsNode> nodes = node.getNodeList(ARGS);
            YList<String> result = al();
            result.add("(");
            tab.inc();
            boolean possibleMerge = addList(nodes, result);
            tab.dec();
            result.add(")");
            if (possibleMerge) result = compact(width, result);
            return result;
        }

        if (node.isType(YADS_MAP)) {
            YMap<YadsNode, YadsNode> nodes = (YMap) node.map.get(NAMED_ARGS);
            if (nodes.isEmpty()) return al("(=)");
            return toStringMap(width, node, "(");
        }

        if (node.isType(YADS_NAMED)) {
            return toStringMap(width, node, node.getString(NAME) + "(");
        }

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
                if (value1.contains("'")) value1 = "\"" + ESCAPE_YADS_DOUBLE_QUOTES.translate(value1) + "\"";
                else value1 = "'" + ESCAPE_YADS_SINGLE_QUOTES.translate(value1) + "'";
            }
            value = value1;
        } else if (valObj instanceof Number) {
            Number valObj1 = (Number) valObj;
            String value1;
            if (valObj1 instanceof Byte) {
                int i = ((Byte) valObj1).intValue();
                if (i < 0) i = i + 256;
                valObj1 = i;
            }
            //TODO fix for other types of number, tests
            String space = valObj1.floatValue() < 0 ? " " : "";
            if (valObj1 instanceof Float) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format(space + "%sf", s);
            } else if (valObj1 instanceof Double) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format(space + "%sd", s);
            } else if (valObj1 instanceof Long) {
                value1 = space + valObj1 + "l";
            } else if (valObj1 instanceof Integer) {
                value1 = space + valObj1;
            } else if (valObj1 instanceof Short) {
                value1 = space + valObj1;
            } else {
                throw new RuntimeException("Should never reach here");
            }
            value = value1;
        } else if (valObj instanceof Boolean) {
            value = valObj.toString();
        } else {
            throw new RuntimeException(String.format("Not implemented const type. Class: %s, Value: %s",
                    valObj.getClass().toString(), valObj));
        }
        return value;
    }

    private static boolean withoutQuotes(String value) {
        if (value.equals("null")) return false;
        if (value.equals("true")) return false;
        if (value.equals("false")) return false;

//TODO return this fast check
//        boolean onlySimpleChars = true;
//        for (int i = 0; i < value.length(); i++) {
//            char c = value.charAt(i);
//            if (c < '!' || c == '\\' || c == '"' || c == '\'') return false;
//            if (c > '~') {
//                onlySimpleChars = false;
//                break;
//            }
//        }
//        if (onlySimpleChars) return true;
        try {
            Object would = new YadsNodeParser(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)))
                    .parseRawElement();
            if (value.equals(would)) return true;
        } catch (Exception | Error ignore) {}
        return false;
    }

    private YList<String> toStringMap(int width, YadsNode node, String classPrefix) {
        YList<String> result = classPrefix == null ? al() : al(classPrefix);
        tab.inc();
        boolean possibleMerge = toStringBody(node, result);
        tab.dec();
        if (classPrefix != null) result.add(")");
        if (possibleMerge) result = compact(width, result);
        return result;
    }

    private boolean toStringBody(YadsNode node, YList<String> result) {
        YMap<YadsNode, YadsNode> nodes = (YMap<YadsNode, YadsNode>) node.map.get(NAMED_ARGS);
        boolean possibleMerge = true;
        if (node.map.get(ARGS) != null) possibleMerge = addList(node.getNodeList(ARGS), result);
        if (node.map.get(NAMED_ARGS) != null) possibleMerge &= addMap(nodes, result);
        return possibleMerge;
    }

    private boolean addList(YList<YadsNode> nodes, YList<String> result) {
        boolean possibleMerge = true;
        for (YadsNode n : nodes) {
            YList<String> children = toString(tab.toString().length(), n);
            if (children.size() > 1) possibleMerge = false;

            String incer = "".equals(tab.toString()) ? "" : tab.incer;
            result.addAll(children.map(s -> incer + s));
        }
        return possibleMerge;
    }

    private boolean addMap(YMap<YadsNode, YadsNode> nodes, YList<String> result) {
        boolean possibleMerge = true;
        for (Map.Entry<YadsNode, YadsNode> entry : nodes.entrySet()) {
            YList<String> key = toString(0, entry.getKey());
            if (key.size() != 1) throw new RuntimeException("Unexpected key: " + key);
            String keyPrefix = key.first() + "=";
            YList<String> children = toString(keyPrefix.length(), entry.getValue());
            if (children.size() > 1) possibleMerge = false;
            String incer = "".equals(tab.toString()) ? "" : tab.incer;
            for (int i = 0; i < children.size(); i++) {
                if (i == 0) result.add(incer + keyPrefix + children.get(i));
                else result.add(incer + children.get(i));
            }
            //TODO fix for (=== = +++)
        }
        return possibleMerge;
    }

    private static String compact(String s) {
        String oldS = "";
        while (s.length() != oldS.length()) {
            oldS = s;
            s = s.replace("\n", " ");
            s = s.replace("  ", " ");
            //s = s.replaceAll("\\} (.)", "}$1");
            s = s.replaceAll("\\( (.)", "($1");
            s = s.replaceAll("(.) \\)", "$1)");
            //s = s.replaceAll("(.) \\{", "$1{");
        }
        return s;
    }

    private YList<String> compact(int width, YList<String> ss) {
        String result = ss.toString("\n");
        String compacted = compact(result);
        if (width + compacted.length() <= maxWidth) return al(compacted);
        return ss;
    }

    public YadsNodeOutput withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }
}
