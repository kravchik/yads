package yk.lang.yads;

import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.util.List;
import java.util.Map;

import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

public class YadsNodeResolver {
    public static final String DELIMITER = "=";

    public Object resolve(Object o) {
        if (o instanceof List) {
            YList result = al();
            for (Object o1 : (List)o) result.add(resolve(o1));
            return result;
        }
        if (o instanceof YadsNode) return resolve((YadsNode)o);
        //if (o instanceof String) {
        //    Optional<Object> optional = resolveConsts.resolve((String) o);
        //    if (optional.isPresent()) return optional.get();
        //}
        return o;
    }

    public YadsNode resolve(YadsNode input) {
        YMap newMap = hm();
        for (Map.Entry<String, Object> entry : input.map.entrySet()) {
            newMap.put(resolve(entry.getKey()), resolve(entry.getValue()));
        }
        //return input.with(newMap);
        return resolveImpl(input.with(newMap));
    }

    private static YadsNode resolveImpl(YadsNode node) {
        if (!node.isType(YADS_ARRAY) && !node.isType(YADS_RAW_CLASS)) return node;
        YList<YadsNode> values = (YList) node.map.get(ARGS);

        YList<YadsNode> args = al();
        YMap<YadsNode, YadsNode> namedArgs = hm();
        YadsNode left = null;

        if (values.size() == 1 && DELIMITER.equals(values.get(0).map.get(VALUE))) {
            return node.with(NODE_TYPE, YADS_MAP, ARGS, null, NAMED_ARGS, hm());
        }

        for (int i = 0; i < values.size(); i++) {
            YadsNode arg = values.get(i);
            //TODO assert starts with delimiter
            //TODO assert ends with delimiter
            //TODO assert several delimiters in a row
            //TODO errors with Caret
            if (DELIMITER.equals(arg.map.get(VALUE))) {
                if (left != null) {
                    YadsNode value = values.get(++i);
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
