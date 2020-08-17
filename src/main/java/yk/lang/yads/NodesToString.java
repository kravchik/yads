package yk.lang.yads;

import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.jcommon.utils.Tab;
import yk.yast.common.YastNode;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static yk.jcommon.collections.YArrayList.al;
import static yk.lang.yads.YadsShorts.*;
import static yk.yast.common.YadsWords.*;

public class NodesToString {
    private int maxWidth = 100;
    private Tab tab = new Tab("  ");

    public String toString(YList<YastNode> nodes) {
        return nodes.map(n -> toString(0, n).toString("\n")).toString("\n");
    }

    public String toStringBody(YastNode nodes) {
        YList<String> result = al();
        YList<YastNode> args = nodes.getNodeList(ARGS);
        YList<YastNode> imports = args.filter(n -> n.isType(IMPORT));
        YList<YastNode> other = args.filter(n -> !n.isType(IMPORT));
        nodes = nodes.with(ARGS, other);
        boolean possibleCompact = toStringBody(nodes, result);
        if (possibleCompact) result = compact(0, result);
        String si = addListAndCompact(imports);
        return (si.equals("") ? "" : (si + "\n")) + result.toString("\n");
    }

    private String addListAndCompact(YList<YastNode> nn) {
        YList<String> result = al();
        if (addList(nn, result)) result = compact(0, result);
        return result.toString("\n");
    }

    public YList<String> toString(int width, YastNode node) {
        if (node.isType(IMPORT)) {
            return al(tab + "import " + node.getString(VALUE));
        }

        if (node.isType(CONST)) {
            Object valObj = node.map.get(VALUE);
            String value;
            if (valObj == null) {
                value = "null";
            } else if (valObj instanceof String || valObj instanceof Character) {
                value = valObj.toString();
                boolean woQuotes = withoutQuotes(value);
                if (!woQuotes) {
                    if (value.contains("'")) value = "\"" + ESCAPE_YADS_DOUBLE_QUOTES.translate(value) + "\"";
                    else value = "'" + ESCAPE_YADS_SINGLE_QUOTES.translate(value) + "'";
                }

            } else if (valObj instanceof Number) {
                if (valObj instanceof Byte) {
                    int i = ((Byte) valObj).intValue();
                    if (i < 0) i = i + 256;
                    valObj = i;
                }
                String space = ((Number) valObj).intValue() < 0 ? " " : "";
                if (valObj instanceof Float) {
                    String s = valObj.toString();
                    if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                    value = String.format(space + "%sf", s);
                } else if (valObj instanceof Double) {
                    String s = valObj.toString();
                    if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                    value = String.format(space + "%sd", s);
                } else if (valObj instanceof Long) {
                    value = space + valObj.toString() + "l";
                } else if (valObj instanceof Integer) {
                    value = space + valObj.toString();
                } else if (valObj instanceof Short) {
                    value = space + valObj.toString();
                } else {
                    throw new RuntimeException("Should never reach here");
                }
            } else if (valObj instanceof Boolean) {
                value = valObj.toString();
            } else {
                throw new RuntimeException(String.format("Not implemented const type. Class: %s, Value: %s",
                        valObj.getClass().toString(),
                        valObj.toString()));
            }
            return al(value);
        }

        if (node.isType(YADS_ARRAY)) {
            YList<YastNode> nodes = node.getNodeList(ARGS);
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
            YMap<YastNode, YastNode> nodes = (YMap) node.map.get(NAMED_ARGS);
            if (nodes.isEmpty()) return al("(=)");
            return toStringMap(width, node, "(");
        }

        if (node.isType(YADS_NAMED)) {
            return toStringMap(width, node, node.getString(NAME) + "(");
        }

        throw new RuntimeException("Not implemented for " + node);
    }

    private static boolean withoutQuotes(String value) {
        if (value.equals("null")) return false;
        if (value.equals("true")) return false;
        if (value.equals("false")) return false;

        boolean onlySimpleChars = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '!' || c == '\\' || c == '"' || c == '\'') return false;
            if (c > '~') {
                onlySimpleChars = false;
                break;
            }
        }
        if (onlySimpleChars) return true;
        try {
            Object would = new YadsParser(new ByteArrayInputStream(value.getBytes("UTF-8"))).parseRawElement();
            if (value.equals(would)) return true;
        } catch (Exception | Error ignore) {}
        return false;
    }

    private YList<String> toStringMap(int width, YastNode node, String classPrefix) {
        YList<String> result = classPrefix == null ? al() : al(classPrefix);
        tab.inc();
        boolean possibleMerge = toStringBody(node, result);
        tab.dec();
        if (classPrefix != null) result.add(")");
        if (possibleMerge) result = compact(width, result);
        return result;
    }

    private boolean toStringBody(YastNode node, YList<String> result) {
        YMap<YastNode, YastNode> nodes = (YMap<YastNode, YastNode>) node.map.get(NAMED_ARGS);
        boolean possibleMerge = true;
        if (node.map.get(ARGS) != null) possibleMerge = addList(node.getNodeList(ARGS), result);
        if (node.map.get(NAMED_ARGS) != null) possibleMerge &= addMap(nodes, result);
        return possibleMerge;
    }

    private boolean addList(YList<YastNode> nodes, YList<String> result) {
        boolean possibleMerge = true;
        for (YastNode n : nodes) {
            YList<String> children = toString(tab.toString().length(), n);
            if (children.size() > 1) possibleMerge = false;

            String incer = "".equals(tab.toString()) ? "" : tab.incer;
            result.addAll(children.map(s -> incer + s));
        }
        return possibleMerge;
    }

    private boolean addMap(YMap<YastNode, YastNode> nodes, YList<String> result) {
        boolean possibleMerge = true;
        for (Map.Entry<YastNode, YastNode> entry : nodes.entrySet()) {
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

    public NodesToString withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }
}
