package yk.lang.yads;

import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.util.Objects;

import static yk.ycollections.Tuple.tuple;

//TODO rename
//  Node
//  AstNode ?
//  Entity
//  Something, Smthng
//  Item
//  Thing, Thingy
//  Piece
public class YadsEntity {
    //can be null
    public String name;
    //TODO Caret caret;
    //TODO YList<Caret> childrenCarets;
    /**
     *
     * <pre>{@code
     * Tuple<String, Object> - field
     * Object - child
     * YadsComment - comment
     * }
     */
    public YList children;

    public YadsEntity(String name, YList children) {
        this.name = name;
        this.children = children;
    }

    public boolean containsKey(Object k) {
        return children.isAny(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
    }

    public Object get(Object k) {
        Object result = children.find(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? null : ((Tuple)result).b;
    }

    public Object getOr(Object k, Object or) {
        Object result = children.find(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? or : ((Tuple)result).b;
    }

    public YadsEntity withReplace(String key, Object value) {
        if (key == null) throw new RuntimeException("key is null");
        return new YadsEntity(name,
            children.map(c -> c instanceof Tuple && key.equals(((Tuple) c).a) ? tuple(key, value) : c));
    }

    public static class YadsComment {
        public boolean isOneLine;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            YadsComment that = (YadsComment) o;
            return isOneLine == that.isOneLine && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isOneLine, text);
        }
    }

    @Override
    public String toString() {
        return "YadsEntity{" + (name == null ? "" : "name='" + name + "', ") + "children=" + children + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YadsEntity that = (YadsEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, children);
    }
}
