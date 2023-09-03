package yk.lang.yads;

import org.junit.Test;
import yk.jcommon.collections.Tuple;
import yk.jcommon.utils.BadException;
import yk.jcommon.utils.IO;

import static junit.framework.TestCase.assertEquals;

public class TestYadsListOutput {
    @Test
    public void test1() {
        YadsList yl = Yads.parseYadsListBody(IO.readResource("formatting.cases.yads"));
        int maxWidth = 100;
        for (Object o : yl.children) {
            if (o instanceof Tuple) {
                if (((Tuple<?, ?>) o).a.equals("maxWidth")) {
                    maxWidth = ((Number)((Tuple<?, ?>) o).b).intValue();
                } else BadException.notImplemented("o");
            } else if (o instanceof String) {
                String s = (String) o;
                assertEquals(s, Yads.printYadsList(Yads.parseYadsList(s), maxWidth));
            }
        }
    }
}
