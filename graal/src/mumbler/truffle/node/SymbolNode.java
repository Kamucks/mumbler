package mumbler.truffle.node;

import mumbler.truffle.MumblerException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeField(name = "slot", type = FrameSlot.class)
public abstract class SymbolNode extends MumblerNode {

    public abstract FrameSlot getSlot();

    @CompilationFinal
    FrameSlot resolvedSlot;
    @CompilationFinal
    int lookupDepth;

    public static interface FrameGet<T> {
        public T get(Frame frame, FrameSlot slot) throws FrameSlotTypeException;
    }

    private <T> T readUpStack(FrameGet<T> getter, VirtualFrame virtualFrame)
            throws FrameSlotTypeException {
        FrameSlot slot = this.getSlot();
        Object identifier = slot.getIdentifier();
        T value = getter.get(virtualFrame, slot);
        if (value != null) {
            return value;
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        MaterializedFrame frame = getLexicalScope(virtualFrame);
        while (value == null) {
            FrameDescriptor desc = frame.getFrameDescriptor();
            slot = desc.findFrameSlot(identifier);
            if (slot != null) {
                value = getter.get(frame, slot);
            }

            if (value != null) {
                continue;
            } else {
                frame = getLexicalScope(frame);
                if (frame == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw new MumblerException("Unknown variable: "
                            + this.getSlot().getIdentifier());
                }
            }
        }
        this.replace(LexicalReadNodeGen.create(frame, slot));
        return value;
    }

    @Specialization(rewriteOn = FrameSlotTypeException.class)
    protected long readLong(VirtualFrame virtualFrame)
            throws FrameSlotTypeException {
        return this.readUpStack(Frame::getLong, virtualFrame);
    }

    @Specialization(rewriteOn = FrameSlotTypeException.class)
    protected boolean readBoolean(VirtualFrame virtualFrame)
            throws FrameSlotTypeException {
        return this.readUpStack(Frame::getBoolean, virtualFrame);
    }

    @Specialization(rewriteOn = FrameSlotTypeException.class)
    protected Object readObject(VirtualFrame virtualFrame)
            throws FrameSlotTypeException {
        return this.readUpStack(Frame::getObject, virtualFrame);
    }

    @Specialization(contains = { "readLong", "readBoolean", "readObject" })
    protected Object read(VirtualFrame virtualFrame) {
        try {
            return this.readUpStack(Frame::getValue, virtualFrame);
        } catch (FrameSlotTypeException e) {
            // FrameSlotTypeException not thrown
        }
        return null;
    }

    @Override
    public String toString() {
        return "'" + this.getSlot().getIdentifier();
    }
}
