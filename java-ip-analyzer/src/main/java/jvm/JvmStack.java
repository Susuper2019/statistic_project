package jvm;

import java.util.ArrayDeque;
import java.util.Deque;

public class JvmStack {
    Deque<StackFrame> stackFrames = new ArrayDeque<>();

    public boolean isEmpty(){
        return stackFrames.isEmpty();
    }

    public StackFrame peek(){
        return stackFrames.peek();
    }

    public StackFrame pop(){
        return stackFrames.pop();
    }

    public void push(StackFrame frame){
        stackFrames.push(frame);
    }
}
