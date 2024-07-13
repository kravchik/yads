package yk.lang.yads;

import org.junit.Test;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static org.junit.Assert.assertEquals;
import static yk.lang.yads.UtilsForTests.readResource;
import static yk.ycollections.YArrayList.al;

public class TestJavaSerializationCases {

    @Test
    public void testAllCases() {
        boolean errors = false;
        YList<String> result = al();

        YList<Object> oo = Yads.readYadsEntities(readResource("serialization.cases.smoke.yads"));
        for (Object o : oo) {
            if (o instanceof YadsEntity) {

                YadsEntity ye = (YadsEntity) o;
                YadsEntity currentEntity = new YadsEntity(ye.name, al());

                String java = (String) ye.get("java");
                if (java == null) {
                    errors = true;
                    currentEntity.children.add(new YadsEntity.YadsComment(true, "!!! absent 'java' field"));
                }

                String expected = (String) ye.get("expected");
                if (expected == null) {
                    errors = true;
                    currentEntity.children.add(new YadsEntity.YadsComment(true, "!!! absent 'expected' field"));
                }

                //TODO default imports in test case and globally
                Object data = YadsJava.deserialize(expected);

                for (Object child : ye.children) {
                    System.out.println(child);
                    if (child instanceof Tuple) {
                        Tuple t = (Tuple) child;
                        if (expected != null && "java".equals(t.a)) {
                            if (!java.equals(data.toString())) {
                                errors = true;
                                currentEntity.children.add(new Tuple<>("java", data.toString()));
                            } else {
                                currentEntity.children.add(child);
                            }
                        } else if ("expected".equals(t.a)) {
                            currentEntity.children.add(child);
                        } else if (expected != null && "alternative".equals(t.a)) {
                            Object altActual = YadsJava.deserialize((String) t.b);
                            if (!data.equals(altActual)) {
                                errors = true;
                                currentEntity.children.add(new Tuple<>("alternative", "NOT EQUALS"));
                            } else {
                                currentEntity.children.add(new Tuple<>("alternative", t.b));
                            }
                        } else {
                            currentEntity.children.add(t);
                        }
                    } else {
                        currentEntity.children.add(child);
                    }
                }
                System.out.println(currentEntity);
                result.add(Yads.printYadsEntity(currentEntity));
            } else {
                result.add(Yads.printYadsEntities(al(o)));
            }
        }
        //errors = true;
        if (errors) assertEquals(readResource("serialization.cases.smoke.yads"), result.toString("\n\n"));
    }

}
