package yk.lang.yads;

import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.yast.common.YastNode;

import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YHashMap.hm;
import static yk.lang.yads.YadsShorts.*;
import static yk.yast.common.YadsWords.*;

public class ResolveMap {
    private static final String DELIMITER = "=";

    public YastNode resolve(YastNode node) {
        if (!node.isType(YADS_ARRAY) && !node.isType(YADS_RAW_CLASS)) return node;
        YList<YastNode> values = (YList) node.map.get(ARGS);

        YList<YastNode> args = al();
        YMap<YastNode, YastNode> namedArgs = hm();
        YastNode left = null;

        if (values.size() == 1 && DELIMITER.equals(values.get(0).map.get(VALUE))) {
            return node.with(NODE_TYPE, YADS_MAP, ARGS, null, NAMED_ARGS, hm());
        }

        for (int i = 0; i < values.size(); i++) {
            YastNode arg = values.get(i);
            //TODO assert starts with delimiter
            //TODO assert ends with delimiter
            //TODO assert several delimiters in a row
            //TODO errors with Caret
            if (DELIMITER.equals(arg.map.get(VALUE))) {
                if (left != null) {
                    YastNode value = values.get(++i);
                    namedArgs.put(left, value);
                    left = null;
                    args.remove(args.size() - 1);//remove, it was a key
                }
            }
            else {
                left = arg;
                args.add(left);
            }
        }

        if (node.isType(YADS_RAW_CLASS)) {//it was a named class
            return node.with(NODE_TYPE, YADS_NAMED, ARGS, args, NAMED_ARGS, namedArgs);
        } else {//YADS_ARRAY (unnamed class)
            if (namedArgs.isEmpty()) {//array
                return node.with(NODE_TYPE, YADS_ARRAY, ARGS, args);
            } else if (args.isEmpty()) {//map
                return node.with(NODE_TYPE, YADS_MAP, ARGS, null, NAMED_ARGS, namedArgs);
            } else {//unnamed class
                return node.with(NODE_TYPE, YADS_UNNAMED, ARGS, args, NAMED_ARGS, namedArgs);
            }
        }
    }
}
