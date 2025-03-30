import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;

public class RunPython {

    public static void main(String[] args) {
        try {
            // Conda 环境中的 Python 路径
            String pythonPath = "/opt/TopDSP/environment/python/envs/demo_env/bin/python";
            // Python 脚本路径
            String scriptPath = "/path/to/your/script.py";

            // 执行 Python 脚本
            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath);

            // 启动进程
            Process process = processBuilder.start();

            // 读取 Python 脚本的标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 读取 Python 脚本的错误输出（如果有）
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            // 等待进程执行完成
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
