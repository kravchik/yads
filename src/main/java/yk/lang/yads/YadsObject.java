package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.util.List;

import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 30/08/16
 * Time: 17:54
 *
 * Would be great to just use Map and extension methods...
 */
public class YadsObject {
    public YMap<String, Object> map;

    public static YadsObject node(String type, String k, Object v, Object... kv) {
        if ((kv.length % 2) != 0) BadException.die("Expected even count");
        YadsObject result = new YadsObject();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        if (v != null) result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public static YadsObject node(String type) {
        YadsObject result = new YadsObject();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        return result;
    }

    public boolean isType(String t) {
        return t.equals(map.get(NODE_TYPE));
    }

    private YadsObject() {
    }

    public YadsObject(String k, Object v, Object... kv) {
        map = hm(k, v, kv);
    }

    public YadsObject(YMap<String, Object> map) {
        this.map = map;
    }

    public YadsObject with(String k, Object v, Object... kv) {
        return new YadsObject(map.with(k, v, kv).filter((kk, vv) -> vv != null));
    }

    //TODO remove key if value is null?
    public YadsObject put(String k, Object v, Object... kv) {
        if (v != null) map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) {
            Object value = kv[i + 1];
            if (value != null) map.put((String) kv[i], value);
        }
        return this;
    }

    public YadsObject without(String... kk) {
        return new YadsObject(map.without(al(kk)));
    }

    public YadsObject withRearrange(String k, Object v, Object... kv) {
        YadsObject result = new YadsObject(map);
        result.map.remove(k);
        result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) result.map.remove(kv[i]);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public YadsObject with(YMap other) {
        return new YadsObject(map.with(other).filter((kk, vv) -> vv != null));
    }

    @Override
    public String toString() {
        return "{" + map.toString(" ") + "}";
    }

    public Long getLong(String name) {
        return (Long)map.get(name);
    }

    public String getType() {
        return (String) map.get(NODE_TYPE);
    }

    public String getString(String name) {
        Object result = map.get(name);
        if (result == null || !(result instanceof String)) {
            throw BadException.die("can't get string " + name + " from " + this);
        }
        return (String) result;
    }

    public String getString(String... nn) {
        return (String) getLast(nn);
    }

    public YadsObject getNode(String name) {
        return (YadsObject) map.get(name);
    }

    public YadsObject getNode(String... nn) {
        return (YadsObject) getLast(nn);
    }

    private Object getLast(String... nn) {
        YadsObject result = this;
        for (int i = 0; i < nn.length - 1; i++) {
            String n = nn[i];
            Object cur = result.map.get(n);
            if (!(cur instanceof YadsObject)) return null;
            result = (YadsObject) cur;
        }
        return result.map.get(nn[nn.length - 1]);
    }

    public YList<YadsObject> getNodeList(String... namesPath) {
        return (YList<YadsObject>) getLast(namesPath);
    }

    public boolean getBoolean(String key) {
        Boolean result = (Boolean) map.get(key);
        return result != null && result;
    }

    public boolean getBoolean(String... kk) {
        Boolean result = (Boolean) getLast(kk);
        return result != null && result;
    }

    public float getFloat(String key) {
        Object result = map.get(key);
        if (result == null) throw BadException.die("no (float) value for key: " + key);
        return (Float) result;
    }
    public int getInt(String name) {
        return (Integer)map.get(name);
    }

    public static YadsObject ref(String name) {
        return node(REF, NAME, name);
    }
    public static YadsObject dot(Object left, String right) {
        if (right == null) {
            BadException.shouldNeverReachHere();
        }
        return node(DOT, LEFT, left, NAME, right);
    }

    public boolean has(String key) {
        return !isAbsent(key);
    }

    public boolean hasNotEmptyList(String key) {
        Object value = map.get(key);
        return value != null && !((List)value).isEmpty();
    }

    public boolean isAbsent(String key) {
        return map.get(key) == null;
    }

    public Object get(String key) {
        return map.get(key);
    }
}
