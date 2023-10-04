package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static yk.ycollections.YArrayList.al;

@SuppressWarnings("rawtypes")
public class YadsThingyOutput {
    public int maxWidth = 100;
    public String inc = "  ";

    //TODO closeParensSameLine
    //TODO firstElementSameLine (if unnamed)

    public String yadsListToString(Object o) {
        return yadsListToString(0, o).toString("\n");
    }

    private YList<String> yadsListToString(int startAt, Object o) {
        if (o instanceof Tuple) {
            Tuple t = (Tuple) o;
            YList<String> kk = yadsListToString(startAt, t.a);
            if (kk.size() != 1) BadException.notImplemented();
            YList<String> vv = yadsListToString(startAt + inc.length(), t.b);
            if (vv.isEmpty()) BadException.shouldNeverReachHere();
            return vv.mapWithIndex((i, v) -> i == 0 ? (kk.first() + " = " + v) : v);
        } else if (o instanceof YadsThingy) {
            return yadsListToString(startAt, (YadsThingy) o);
        } else return al(YadsObjectOutput.valueToString(o));
    }

    private YList<String> yadsListToString(int startAt, YadsThingy yl) {
        String l1 = (yl.name == null ? "" : yl.name) + "(";
        String ln = ")";
        boolean tryCompact = true;
        YList<String> cc = al();
        int commonLength = 0;
        for (Object child : yl.children) {
            if (child instanceof YadsThingy.YadsComment) {
                YadsThingy.YadsComment c = (YadsThingy.YadsComment) child;
                tryCompact = false;
                if (c.isOneLine) {
                    cc.add("//" + c.text);
                } else {//TODO format tabulation ?
                    cc.add("/*" + c.text + "*/");
                }
                continue;
            }
            YList<String> childStrings = yadsListToString(startAt + inc.length(), child);
            cc.addAll(childStrings);
            if (childStrings.isEmpty()) BadException.shouldNeverReachHere();
            if (childStrings.size() > 1) tryCompact = false;
            else commonLength += childStrings.first().length();
        }
        if (tryCompact) {
            int possibleLen = commonLength + l1.length() + ln.length() + Math.max(0, (cc.size() - 1)) + startAt;
            if (possibleLen <= maxWidth) return al(l1 + cc.toString(" ") + ln);
        }
        return al(l1).withAll(cc.map(c -> inc + c)).with(ln);
    }

}
