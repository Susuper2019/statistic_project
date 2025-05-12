package jvm;

public class MainDemo {
    public static void main(String[] args) {
        System.out.println(5);
        System.out.println(max(5, 3));
    }

    public static int max(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }
}
