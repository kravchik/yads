package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static yk.lang.yads.YadsObjectResolver.DELIMITER;
import static yk.lang.yads.utils.BadException.die;
import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
//YadsObject -> YadsEntity
//TODO parser to generate YadsEntity
public class YadsEntityResolver {
    public static Object toYadsList(YadsObject node) {
        if (node.isType(CONST)) return node.get(VALUE);
        if (node.isType(COMMENT_SINGLE_LINE)) return new YadsEntity.YadsComment(true, node.getString(VALUE).substring(2));
        if (node.isType(COMMENT_MULTI_LINE)) {
            String v = node.getString(VALUE);
            return new YadsEntity.YadsComment(false, v.substring(2, v.length() - 2));
        }
        if (!node.isType(YADS_RAW_CLASS) && !node.isType(YADS_ARRAY)) BadException.shouldNeverReachHere();
        YList<YadsObject> args = (YList<YadsObject>) node.map.get(ARGS);
        YList<Object> result = toYadsList(args);
        return new YadsEntity((String) node.get(NAME), result);
    }

    public static YList<Object> toYadsList(YList<YadsObject> args) {
        Object left = null;
        YadsObject leftNode = null;
        YList<Object> result = al();
        for (int i = 0; i < args.size(); i++) {
            YadsObject arg = args.get(i);
            if (isDelimiter(arg)) {
                if (leftNode == null) die("Expected key before %s at %s", DELIMITER, arg.get(CARET));
                if (leftNode.isType(COMMENT_SINGLE_LINE) || leftNode.isType(COMMENT_MULTI_LINE)) {
                    die("Comment instead of key at %s", leftNode.get(CARET));
                }
                i++;
                if (i >= args.size()) die("Expected value after %s at %s", DELIMITER, arg.get(CARET));
                YadsObject right = args.get(i);
                if (right.isType(COMMENT_SINGLE_LINE) || right.isType(COMMENT_MULTI_LINE)) die("Comment instead of value at %s", right.get(CARET));
                if (isDelimiter(right)) die("Expected value at %s", right.get(CARET));
                result.remove(result.size() - 1);//remove, it was a key
                result.add(new Tuple<>(left, toYadsList(right)));
                leftNode = null;
            } else {
                leftNode = arg;
                left = toYadsList(arg);
                result.add(left);
            }
        }
        return result;
    }

    private static boolean isDelimiter(YadsObject node) {
        return node.isType(CONST) && "Operator".equals(node.getString(TYPE)) && DELIMITER.equals(node.getString(VALUE));
    }
}
