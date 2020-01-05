package yk.lang.yads;

import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.yast.common.YastNode;

import java.util.List;
import java.util.Map;

import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YHashMap.hm;

public class YadsResolver {
    //TODO custom const resolver
    //TODO custom operator resolver
    //private ResolveConsts resolveConsts = new ResolveConsts();
    private ResolveMap resolveMap = new ResolveMap();

    public Object resolve(Object o) {
        if (o instanceof List) {
            YList result = al();
            for (Object o1 : (List)o) result.add(resolve(o1));
            return result;
        }
        if (o instanceof YastNode) return resolve((YastNode)o);
        //if (o instanceof String) {
        //    Optional<Object> optional = resolveConsts.resolve((String) o);
        //    if (optional.isPresent()) return optional.get();
        //}
        return o;
    }

    public YastNode resolve(YastNode input) {
        YMap newMap = hm();
        for (Map.Entry<String, Object> entry : input.map.entrySet()) {
            newMap.put(resolve(entry.getKey()), resolve(entry.getValue()));
        }
        //return input.with(newMap);
        return resolveMap.resolve(input.with(newMap));
    }
}
