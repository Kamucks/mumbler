package mumbler.truffle.node.builtin.arithmetic;

import mumbler.truffle.node.builtin.BuiltinNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateNodeFactory
public abstract class SubBuiltinNode extends BuiltinNode {
    @Specialization
    protected long minus(long value0, long value1) {
        return value0 - value1;
    }
}
