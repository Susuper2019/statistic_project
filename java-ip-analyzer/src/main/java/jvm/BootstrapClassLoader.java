package jvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.ClassFileParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class BootstrapClassLoader {
    private final List<String> classPath;

    BootstrapClassLoader(List<String> classPath){
        this.classPath = classPath;
    }


    public ClassFile loadClass(String qfcn) throws ClassNotFoundException {
        return classPath.stream()
                .map(path->tryLoad(path,qfcn))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(()->new ClassNotFoundException(qfcn+"找不到了"));
    }


    private ClassFile tryLoad(String path, String mainClass) {
        File file = new File(path, mainClass.replace(".", File.separator) + ".class");
        if (!file.exists()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new ClassFileParser().parse(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
