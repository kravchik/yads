package yk.lang.yads;

import yk.jcommon.collections.YList;

public class YadsList {
    //can be null
    public String name;
    //Tuple<String, Object> - field
    //Tuple<null, Object> - child
    //Object - child
    //YadsComment - comment
    public YList<Object> children;

    public YadsList(String name, YList<Object> children) {
        this.name = name;
        this.children = children;
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
        return "YadsList{" + (name == null ? "" : "name='" + name + "', ") + "children=" + children + '}';
    }
}
