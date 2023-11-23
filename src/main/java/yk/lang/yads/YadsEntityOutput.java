package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.lang.yads.utils.YadsUtils;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static yk.ycollections.YArrayList.al;

@SuppressWarnings("rawtypes")
public class YadsEntityOutput {
    //to do: property closeParensSameLine
    //to do: property firstElementSameLine (if unnamed)
    //to do: 'floating' maxWidth
    public int maxWidth = 100;
    public String tab = "  ";

    public static String valueToString(Object valObj) {
        String value;
        if (valObj == null) {
            value = "null";
        } else if (valObj instanceof String || valObj instanceof Character) {
            String value1;
            value1 = valObj.toString();
            boolean woQuotes = withoutQuotes(value1);
            if (!woQuotes) {
                if (value1.contains("'")) value1 = "\"" + YadsUtils.escapeDoubleQuotes(value1) + "\"";
                else value1 = "'" + YadsUtils.escapeSingleQuotes(value1) + "'";
            }
            value = value1;
        } else if (valObj instanceof Number) {
            Number valObj1 = (Number) valObj;
            String value1;
            if (valObj1 instanceof Byte) {
                int i = valObj1.intValue();
                if (i < 0) i = i + 256;
                valObj1 = i;
            }
            //TODO fix for other types of number, tests
            if (valObj1 instanceof Float) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format("%sf", s);
            } else if (valObj1 instanceof Double) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format("%sd", s);
            } else if (valObj1 instanceof Long) {
                value1 = valObj1 + "l";
            } else if (valObj1 instanceof Integer) {
                value1 = "" + valObj1;
            } else if (valObj1 instanceof Short) {
                value1 = "" + valObj1;
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

    public static boolean withoutQuotes(String value) {
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
            Object would = new YadsObjectParser(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)))
                    .parseRawElement();
            if (value.equals(would)) return true;
        } catch (Exception | Error ignore) {}
        return false;
    }

    public String print(Object o) {
        return print(0, o).toString("\n");
    }

    public String printBody(YList<Object> objects) {
        return print(objects, 0, null, null, false).toString("\n");
    }

    private YList<String> print(int startAt, Object o) {
        if (o instanceof Tuple) {
            Tuple t = (Tuple) o;
            YList<String> result = print(startAt, t.a);
            if (result.isEmpty()) BadException.shouldNeverReachHere();
            YList<String> vv = print(startAt + tab.length(), t.b);
            if (vv.isEmpty()) BadException.shouldNeverReachHere();
            result.set(result.size() - 1, result.last() + " = " + vv.first());
            for (int i = 1; i < vv.size(); i++) result.add(vv.get(i));
            return result;
        } else if (o instanceof YadsEntity) {
            YadsEntity ye = (YadsEntity) o;
            return print(ye.children, startAt, (ye.name == null ? "" : ye.name) + "(", ")", true);
        } else return al(valueToString(o));
    }

    private YList<String> print(YList<Object> objects, int startAt, String l1, String ln, boolean addTabs) {
        if ((l1 == null) != (ln == null)) BadException.die("Both prefix and suffix should either null, or not");
        boolean tryCompact = true;
        YList<String> cc = al();
        int commonLength = 0;
        for (Object child : objects) {
            if (child instanceof YadsEntity.YadsComment) {
                YadsEntity.YadsComment c = (YadsEntity.YadsComment) child;
                if (c.isOneLine) {
                    tryCompact = false;
                    cc.add("//" + c.text);
                } else {//TODO format tabulation ?
                    cc.add("/*" + c.text + "*/");
                }
                continue;
            }
            YList<String> childStrings = print(startAt + tab.length(), child);
            cc.addAll(childStrings);
            if (childStrings.isEmpty()) BadException.shouldNeverReachHere();
            if (childStrings.size() > 1) tryCompact = false;
            else commonLength += childStrings.first().length();
        }
        if (tryCompact) {
            int estimatedLen = commonLength + Math.max(0, (cc.size() - 1));
            if (l1 != null) estimatedLen += l1.length() + ln.length();
            if (estimatedLen + startAt <= maxWidth) {

                String result = l1 == null ? cc.toString(" ") : (l1 + cc.toString(" ") + ln);
                if (result.length() != estimatedLen) {
                    throw new RuntimeException(String.format(
                            "Expected same length, but estimatedLen is %s, actual: %s, in text '%s'", estimatedLen, result.length(), result));
                }
                return al(result);
            }
        }
        if (l1 != null) {
            if (addTabs) return al(l1).withAll(cc.map(c -> tab + c)).with(ln);
            return al(l1).withAll(cc).with(ln);
        } else {
            if (addTabs) return cc.map(c -> tab + c);
            return cc;
        }
    }

    public YadsEntityOutput setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public YadsEntityOutput setTab(String tab) {
        this.tab = tab;
        return this;
    }
}
