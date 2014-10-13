package truffler.simple.node;

import truffler.simple.env.Environment;

public class NumberNode extends Node {
    private final long num;

    public NumberNode(long num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return Long.toString(this.num);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NumberNode &&
            this.num == ((NumberNode) other).num;
    }

    @Override
    public Object eval(Environment env) {
        return new Long(this.num);
    }
}