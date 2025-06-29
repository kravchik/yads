package yk.lang.yads;

import yk.lang.yads.utils.Caret;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

public class YadsCst {
    public final String type;
    public final Caret caret;
    public final YList<YadsCst> children;
    public final YMap<String, YadsCst> childByField;

    public Object value;

    public YadsCst(String type, Caret caret, Object value, YList<YadsCst> children, YMap<String, YadsCst> childByField) {
        this.type = type;
        this.caret = caret;
        this.children = children != null ? children : al();
        this.childByField = childByField == null ? hm() : childByField;
        this.value = value;
    }

    public YadsCst(String type, Caret caret, YList<YadsCst> children) {
        this(type, caret, null, children, null);
    }

    public YadsCst(String type, Caret caret, Object value) {
        this(type, caret, value, null, null);
    }


    public YadsCst(String type, Caret caret) {
        this(type, caret, null, null, null);
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return type;
        }
        return type + "(" + children + ")";
    }
}