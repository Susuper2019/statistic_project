package jvm;

public class Main {
    public static void main(String[] args) throws Exception {
        HotSpot hotSpot = new HotSpot("MainDemo","D:\\store\\py\\statistic_projetct\\java-ip-analyzer\\target\\classes\\jvm;D:\\store\\py\\statistic_projetct\\java-ip-analyzer\\target\\classes");
        hotSpot.start();
    }
}
