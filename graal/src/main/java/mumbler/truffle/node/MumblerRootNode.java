package mumbler.truffle.node;

import java.util.Arrays;

import mumbler.truffle.node.special.DefineNodeGen;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RootNode;

public class MumblerRootNode extends RootNode {
    @Children private final MumblerNode[] bodyNodes;

    public MumblerRootNode(MumblerNode[] bodyNodes,
            FrameDescriptor frameDescriptor) {
        super(null, frameDescriptor);
        this.bodyNodes = bodyNodes;
    }

    @Override
    @ExplodeLoop
    public Object execute(VirtualFrame virtualFrame) {
        int last = this.bodyNodes.length - 1;
        CompilerAsserts.compilationConstant(last);
        for (int i=0; i<last; i++) {
            this.bodyNodes[i].execute(virtualFrame);
        }
        return this.bodyNodes[last].execute(virtualFrame);
    }

    public static MumblerRootNode create(FrameSlot[] argumentNames,
            MumblerNode[] bodyNodes, FrameDescriptor frameDescriptor) {
        MumblerNode[] allNodes = new MumblerNode[argumentNames.length
                                                 + bodyNodes.length];
        for (int i=0; i<argumentNames.length; i++) {
            allNodes[i] = DefineNodeGen.create(
                    new ReadArgumentNode(i), argumentNames[i]);
        }
        System.arraycopy(bodyNodes, 0, allNodes,
                argumentNames.length, bodyNodes.length);
        return new MumblerRootNode(allNodes, frameDescriptor);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bodyNodes);
    }
}
