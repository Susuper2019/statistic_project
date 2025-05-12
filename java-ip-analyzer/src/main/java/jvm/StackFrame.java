package jvm;

import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.Instruction;
import tech.medivh.classpy.classfile.constant.ConstantPool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class StackFrame {
    MethodInfo methodInfo;
    Object[] localVariable; //局部变量表
    Deque<Object> opertorStack; //操作数栈
    List<Instruction> codes; //指令
    ConstantPool constantPool;
    int currentIndex;


    public StackFrame(MethodInfo methodInfo, ConstantPool constantPool, Object... args) {
        this.methodInfo = methodInfo;
        localVariable = new Object[methodInfo.getMaxLocals()];
        this.opertorStack = new ArrayDeque<>();
        this.codes = methodInfo.getCodes();
        this.constantPool = constantPool;
        System.arraycopy(args, 0, localVariable, 0, args.length);
    }

    public Instruction getNextInstruction() {
        return codes.get(currentIndex++);
    }

    public void pushObjectToOperatorStack(Object object) {
        this.opertorStack.push(object);
    }

    public void jumpTo(int jumpTo) {
        for (int i = 0; i < codes.size(); i++) {
            Instruction instruction = codes.get(i);
            if (instruction.getPc() == jumpTo) {
                this.currentIndex = i;
                return;
            }
        }
    }

}
