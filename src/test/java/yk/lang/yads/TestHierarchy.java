package yk.lang.yads;

import yk.jcommon.collections.YMap;

import static yk.jcommon.collections.YHashMap.hm;

/**
 * Created by Yuri Kravchik on 19.04.2020
 */
public class TestHierarchy {
    public YMap<String, Object> map;

    private TestHierarchy() {
    }

    public TestHierarchy(String k, Object v, Object... kv) {
        map = hm(k, v, kv);
    }

    public TestHierarchy(long id, YMap<String, Object> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "{" + map.toString(" ") + "}";
    }
}
