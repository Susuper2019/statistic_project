package jvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.*;
import tech.medivh.classpy.classfile.constant.ConstantMethodrefInfo;
import tech.medivh.classpy.classfile.constant.ConstantPool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class Thread {
    private final String threadName;
    private final JvmStack stack;
    private final PcRegister pcRegister;
    final BootstrapClassLoader bootstrapClassLoader;

    public Thread(String threadName, StackFrame frame, BootstrapClassLoader classLoader) {
        this.threadName = threadName;
        this.stack = new JvmStack();
        this.stack.push(frame);
        this.pcRegister = new PcRegister(this.stack); //让寄存器里面也有一个栈，不合适
        this.bootstrapClassLoader = classLoader;
    }

    public void start() throws Exception {
        for (Instruction instruction : pcRegister) {
//            System.out.println(instruction);
            ConstantPool constantPool = stack.peek().constantPool; //由于本身有stack，切寄存器中也有，同时往外提出
            switch (instruction.getOpcode()) {
                case getstatic -> {
                    GetStatic getStatic = (GetStatic) instruction;
                    String className = getStatic.getClassName(constantPool);
                    String fieldName = getStatic.getFieldName(constantPool);
                    Object staticField;
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Field declaredField = aClass.getDeclaredField(fieldName);
                        staticField = declaredField.get(null);
                        stack.peek().pushObjectToOperatorStack(staticField);
                    }
                }
                case iconst_1 -> stack.peek().pushObjectToOperatorStack(1);
                case iconst_2 -> stack.peek().pushObjectToOperatorStack(2);
                case iconst_3 -> stack.peek().pushObjectToOperatorStack(3);
                case iconst_4 -> stack.peek().pushObjectToOperatorStack(4);
                case iconst_5 -> stack.peek().pushObjectToOperatorStack(5);
                case iload_0 -> stack.peek().pushObjectToOperatorStack(stack.peek().localVariable[0]);
                case iload_1 -> stack.peek().pushObjectToOperatorStack(stack.peek().localVariable[1]);
                case if_icmple -> {
                    int b = (int) stack.peek().opertorStack.pop();
                    int a = (int) stack.peek().opertorStack.pop();
                    if (b <= a) {
                        Branch branch = (Branch) instruction;
                        int jumpTo = branch.getJumpTo();
                        stack.peek().jumpTo(jumpTo);
//                        stack.peek().pushObjectToOperatorStack(a);
//                    } else {
//                        stack.peek().pushObjectToOperatorStack(b);
                    }
                }
                case ireturn -> {
                    int result = (int)stack.peek().opertorStack.pop(); //当前栈上的操作数
                    stack.pop(); //移除当前栈
                    stack.peek().pushObjectToOperatorStack(result); //下一个栈的操作数加上上一个栈的返回值
                }
                case invokevirtual -> {
                    InvokeVirtual invokeVirtual = (InvokeVirtual) instruction;
                    ConstantMethodrefInfo methodInfo = invokeVirtual.getMethodInfo(constantPool);
                    String className = methodInfo.className(constantPool);
                    String methodName = methodInfo.methodName(constantPool);
                    List<String> params = methodInfo.paramClassName(constantPool);
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Method declaredMethod = aClass.getDeclaredMethod(methodName, params.stream().map(this::nameToClass).toArray(Class[]::new));
                        Object[] args = new Object[params.size()];
                        // print(1)
                        // 1
                        for (int index = args.length - 1; index >= 0; index--) {
                            args[index] = stack.peek().opertorStack.pop();
                        }
                        Object result = declaredMethod.invoke(stack.peek().opertorStack.pop(), args);
                        if (!methodInfo.isVoid(constantPool)) {
                            stack.peek().pushObjectToOperatorStack(result);
                        }
                        break;
                    }
                    ClassFile classFile = bootstrapClassLoader.loadClass(className);

                    MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0); //取0会影响重载函数
                    Object[] args = new Object[params.size() + 1];
                    // cat.say(1)
                    // cat 1
                    for (int index = args.length - 1; index >= 0; index--) {
                        args[index] = stack.peek().opertorStack.pop();
                    }
                    StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
                    stack.push(stackFrame);
                }
                case _return -> {
                    stack.pop();
                }
                case invokestatic -> {
                    InvokeStatic invokeStatic = (InvokeStatic) instruction;
                    ConstantMethodrefInfo methodInfo = invokeStatic.getMethodInfo(constantPool);
                    String className = methodInfo.className(constantPool);
                    String methodName = methodInfo.methodName(constantPool);
                    List<String> params = methodInfo.paramClassName(constantPool);
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Method declaredMethod = aClass.getDeclaredMethod(methodName, params.stream().map(this::nameToClass).toArray(Class[]::new));
                        Object[] args = new Object[params.size()];
                        // print(1)
                        // 1
                        for (int index = args.length - 1; index >= 0; index--) {
                            args[index] = stack.peek().opertorStack.pop();
                        }
                        Object result = declaredMethod.invoke(null, args);
                        if (!methodInfo.isVoid(constantPool)) {
                            stack.peek().pushObjectToOperatorStack(result);
                        }
                        break;
                    }
                    ClassFile classFile = bootstrapClassLoader.loadClass(className);

                    MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0); //取0会影响重载函数
                    Object[] args = new Object[params.size()];
                    // cat.say(1)
                    // cat 1
                    for (int index = args.length - 1; index >= 0; index--) {
                        args[index] = stack.peek().opertorStack.pop();
                    }
                    StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
                    stack.push(stackFrame);
                }
                default -> {
                    System.out.println(instruction + "还没实现");
                }
            }
        }
    }

    private Class<?> nameToClass(String className) {
        if (className.equals("int")) {
            return int.class;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
