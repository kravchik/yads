package yk.lang.yads;

import yk.jcommon.collections.Tuple;
import yk.jcommon.collections.YList;
import yk.jcommon.utils.BadException;

import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.utils.MyMath.max;

@SuppressWarnings("rawtypes")
public class YadsListOutput {
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
            return vv.mapWithIndex((i, v) -> i == 0 ? (kk.first() + " = " + v) : i == vv.size() - 1 ? v : inc + v);
        } else if (o instanceof YadsList) {
            return yadsListToString(startAt, (YadsList) o);
        } else return al(YadsNodeOutput.valueToString(o));
    }

    private YList<String> yadsListToString(int startAt, YadsList yl) {
        String l1 = (yl.name == null ? "" : yl.name) + "(";
        String ln = ")";
        boolean tryCompact = true;
        YList<String> cc = al();
        int commonLength = 0;
        for (Object child : yl.children) {
            if (child instanceof YadsList.YadsComment) {
                YadsList.YadsComment c = (YadsList.YadsComment) child;
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
            int possibleLen = commonLength + l1.length() + ln.length() + max(0, (cc.size() - 1)) + startAt;
            if (possibleLen <= maxWidth) return al(l1 + cc.toString(" ") + ln);
        }
        return al(l1).withAll(cc.map(c -> inc + c)).with(ln);
    }

}
