package yk.lang.yads.utils;

import yk.lang.yads.JavaCharStream;

public class Caret {

    public int beginLine;
    public int beginColumn;
    public int endLine;
    public int endColumn;

    public static Caret begin(int beginLine, int beginColumn) {
        return new Caret(beginLine, beginColumn, 0, 0);
    }

    public Caret(int beginLine, int beginColumn, int endLine, int endColumn) {
        this.beginLine = beginLine == 0 ? 1 : beginLine;
        this.beginColumn = beginColumn == 0 ? 1 : beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public static Caret create(JavaCharStream stream) {
        return new Caret(stream.getBeginLine(), stream.getBeginColumn(), stream.getEndLine(), stream.getEndColumn());
    }

    public static Caret begin(JavaCharStream stream) {
        return new Caret(stream.getBeginLine(), stream.getBeginColumn(), 0, 0);
    }

    public Caret setEnd(JavaCharStream stream) {
        endLine = stream.getEndLine();
        endColumn = stream.getEndColumn();
        return this;
    }

    public String toStringInside() {
        return String.format("%s:%s .. %s:%s", beginLine, beginColumn, endLine, endColumn);
    }

    public String toStringBegin() {
        return String.format("%s:%s", beginLine, beginColumn);
    }

    @Override
    public String toString() {
        return "Caret{" +
                "beginLine=" + beginLine +
                ", beginColumn=" + beginColumn +
                ", endLine=" + endLine +
                ", endColumn=" + endColumn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Caret)) {
            return false;
        }

        Caret caret = (Caret) o;

        if (beginLine != caret.beginLine) {
            return false;
        }
        if (beginColumn != caret.beginColumn) {
            return false;
        }
        if (endLine != caret.endLine) {
            return false;
        }
        return endColumn == caret.endColumn;
    }

    @Override
    public int hashCode() {
        int result = beginLine;
        result = 31 * result + beginColumn;
        result = 31 * result + endLine;
        result = 31 * result + endColumn;
        return result;
    }
}
