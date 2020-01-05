package yk.lang.yads;

public class TestClass25 extends TestClass2 {
    private String specific25;

    public TestClass25() {
    }

    public TestClass25(float a) {
        super(a);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        TestClass25 that = (TestClass25) o;

        return specific25 != null ? specific25.equals(that.specific25) : that.specific25 == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (specific25 != null ? specific25.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestClass25{} " + super.toString();
    }
}
