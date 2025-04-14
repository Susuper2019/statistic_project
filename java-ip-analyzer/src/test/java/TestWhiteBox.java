import jdk.test.whitebox.WhiteBox;

public class TestWhiteBox {
    /**
     * -Xbootclasspath/a:D:/store/py/statistic_projetct/java-ip-analyzer/lib/whitebox-1.0-SNAPSHOT.jar
     * -XX:+UnlockDiagnosticVMOptions
     * -XX:+WhiteBoxAPI
     * -Xlog:gc
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        Long maxHeapSize = whiteBox.getUintxVMFlag("ReservedCodeCacheSize");
        System.out.println(maxHeapSize);
        whiteBox.printHeapSizes();
        whiteBox.fullGC();
        Thread.currentThread().join();
    }
}
