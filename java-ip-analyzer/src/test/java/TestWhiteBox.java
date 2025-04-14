import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordingFile;
import jdk.test.whitebox.WhiteBox;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TestWhiteBox {
    private static final int BYTE_ARRAY_HEADER = 16; //对象头的大小
    private static final int OBJECT_SIZE = 1024;// 1KB

    public static byte[] tmp;
    private static final String BYTE_ARRAY_CLASS_NAME = byte[].class.getName();

    /**
     * -Xbootclasspath/a:D:/store/py/statistic_projetct/java-ip-analyzer/lib/whitebox-1.0-SNAPSHOT.jar
     * -XX:+UnlockDiagnosticVMOptions
     * -XX:+WhiteBoxAPI
     * -Xlog:gc
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
//        WhiteBox whiteBox = WhiteBox.getWhiteBox();
//        Long maxHeapSize = whiteBox.getUintxVMFlag("ReservedCodeCacheSize");
//        System.out.println(maxHeapSize);
//        whiteBox.printHeapSizes();
//        whiteBox.fullGC();
//        Thread.currentThread().join();
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        Recording recording = new Recording();
        recording.enable("jdk.ObjectAllocationInNewTLAB");
        recording.start();
        whiteBox.fullGC();
        //分配1kb 512个
        for (int i = 0; i < 512; i++) {
            tmp = new byte[OBJECT_SIZE - BYTE_ARRAY_HEADER];
        }
        whiteBox.fullGC();
        //分配100kb 200个
        for (int i = 0; i < 200; i++) {
            tmp = new byte[OBJECT_SIZE * 100 - BYTE_ARRAY_HEADER];
        }
        whiteBox.fullGC();
        Path path = new File(new File(".").getAbsolutePath(), "recording-" + recording.getId() + "-pid" + ProcessHandle.current().pid() + ".jfr").toPath();
        recording.dump(path);
        int count1Kb = 0;
        int count100Kb = 0;
        for (RecordedEvent event : RecordingFile.readAllEvents(path)) {
            String className = event.getString("objectClass.name");
            if (className.equalsIgnoreCase(BYTE_ARRAY_CLASS_NAME)) {
                RecordedFrame recordedFrame = event.getStackTrace().getFrames().get(0);
                if (recordedFrame.isJavaFrame() && "main".equalsIgnoreCase(recordedFrame.getMethod().getName())) {
                    long allocationSize = event.getLong("allocationSize");
                    if ("jdk.ObjectAllocationInNewTLAB".equals(event.getEventType().getName())) {
                        if (allocationSize == OBJECT_SIZE) {
                            count1Kb++;
                        } else if (allocationSize == OBJECT_SIZE * 100) {
                            count100Kb++;
                        }
                    }else{
                        System.out.println("allocationSize: " + allocationSize);
                    }
                }
                System.out.println(event);
            }
        }
        System.out.println("1kb count: " + count1Kb);
        System.out.println("100kb count: " + count100Kb);
    }
}
