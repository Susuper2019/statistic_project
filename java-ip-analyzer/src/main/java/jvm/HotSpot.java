package jvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.ClassFileParser;
import tech.medivh.classpy.classfile.MethodInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class HotSpot {
    private String mainClass;

    private BootstrapClassLoader bootstrapClassLoader;

    public HotSpot(String mainClass, String classPath) {
        this.mainClass = mainClass;
        this.bootstrapClassLoader = new BootstrapClassLoader(Arrays.asList(classPath.split(File.pathSeparator)));
    }

    public void start() throws Exception {
        ClassFile classFile = bootstrapClassLoader.loadClass(mainClass);
        StackFrame stackFrame = new StackFrame(classFile.getMainMethod(), classFile.getConstantPool());
        new Thread("main", stackFrame, bootstrapClassLoader).start();
    }


}
