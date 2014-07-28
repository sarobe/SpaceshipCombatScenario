package common.utilities;

public class Pair<A, B> {

    public A first;
    public B second;

    public Pair(A a, B b) {
        this.first = a;
        this.second = b;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }
}
