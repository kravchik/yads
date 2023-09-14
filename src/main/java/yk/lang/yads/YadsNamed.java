package yk.lang.yads;

import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.util.Objects;

/**
 * Created by Yuri at 01.04.22
 */
@Deprecated //use YadsList instead
public class YadsNamed {
    public String name;
    public YList<Object> array;
    public YMap<Object, Object> map;

    public YadsNamed(String name) {
        if (name == null) throw new RuntimeException("Can't be null");
        this.name = name;
    }

    public YadsNamed setArray(YList<Object> array) {
        this.array = array;
        return this;
    }

    public YadsNamed setMap(YMap<Object, Object> map) {
        this.map = map;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YadsNamed yadsNamed = (YadsNamed) o;
        return name.equals(yadsNamed.name) && Objects.equals(array, yadsNamed.array) && Objects.equals(map, yadsNamed.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, array, map);
    }

    @Override
    public String toString() {
        return "YadsNamed{" +
                "name='" + name + '\'' +
                ", array=" + array +
                ", map=" + map +
                '}';
    }
}
