package yk.lang.yads;

public class TestClassNumbers {
    public int i;
    public Integer I;
    public float f;
    public Float F;
    public long l;
    public Long L;
    public double d;
    public Double D;

    public TestClassNumbers setI(int i) {
        this.i = i;
        return this;
    }

    public TestClassNumbers setI(Integer i) {
        I = i;
        return this;
    }

    public TestClassNumbers setF(float f) {
        this.f = f;
        return this;
    }

    public TestClassNumbers setF(Float f) {
        F = f;
        return this;
    }

    public TestClassNumbers setL(long l) {
        this.l = l;
        return this;
    }

    public TestClassNumbers setL(Long l) {
        L = l;
        return this;
    }

    public TestClassNumbers setD(double d) {
        this.d = d;
        return this;
    }

    public TestClassNumbers setD(Double d) {
        D = d;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestClassNumbers that = (TestClassNumbers) o;

        if (i != that.i) {
            return false;
        }
        if (Float.compare(that.f, f) != 0) {
            return false;
        }
        if (l != that.l) {
            return false;
        }
        if (Double.compare(that.d, d) != 0) {
            return false;
        }
        if (I != null ? !I.equals(that.I) : that.I != null) {
            return false;
        }
        if (F != null ? !F.equals(that.F) : that.F != null) {
            return false;
        }
        if (L != null ? !L.equals(that.L) : that.L != null) {
            return false;
        }
        return D != null ? D.equals(that.D) : that.D == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = i;
        result = 31 * result + (I != null ? I.hashCode() : 0);
        result = 31 * result + (f != +0.0f ? Float.floatToIntBits(f) : 0);
        result = 31 * result + (F != null ? F.hashCode() : 0);
        result = 31 * result + (int) (l ^ (l >>> 32));
        result = 31 * result + (L != null ? L.hashCode() : 0);
        temp = Double.doubleToLongBits(d);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (D != null ? D.hashCode() : 0);
        return result;
    }
}
