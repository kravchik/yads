package yk.lang.yads;

import yk.ycollections.Tuple;
import yk.ycollections.YList;

public class YadsThingy {
    //can be null
    public String name;
    //Tuple<String, Object> - field
    //Tuple<null, Object> - child
    //Object - child
    //YadsComment - comment
    public YList<Object> children;

    public YadsThingy(String name, YList<Object> children) {
        this.name = name;
        this.children = children;
    }

    public boolean containsKey(Object k) {
        return children.isAny(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
    }

    public Object get(Object k) {
        Object result = children.first(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? null : ((Tuple)result).b;
    }

    public Object getOr(Object k, Object or) {
        Object result = children.first(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? or : ((Tuple)result).b;
    }

    public static class YadsComment {
        public boolean isOneLine = true;
        public String text;

        public YadsComment(boolean isOneLine, String text) {
            this.isOneLine = isOneLine;
            this.text = text;
        }

        @Override
        public String toString() {
            return "YadsComment{" +
                    "isOneLine=" + isOneLine +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
    //do we need this ?
    public static class YadsCommentedData {
        public Object obj;
    }

    @Override
    public String toString() {
        return "YadsThingy{" + (name == null ? "" : "name='" + name + "', ") + "children=" + children + '}';
    }
}
