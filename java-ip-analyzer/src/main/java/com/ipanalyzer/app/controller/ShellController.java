package com.ipanalyzer.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/shell")
@RequiredArgsConstructor
@Slf4j
public class ShellController {
    @GetMapping
    public String run2(@RequestParam(value = "name") String name,
                       @RequestParam(value = "src_ip", required = false) String srcIp,
                       @RequestParam(value = "dst_ip", required = false) String dstIp) {
        try {
            // Conda 环境中的 Python 路径
            String pythonPath = "/opt/TopDSP/service/pytest/demo_env/bin/python";
            // Python 脚本路径
            String scriptPath = "/opt/TopDSP/service/pytest/" + name + ".py";

            // 启动 Python 进程
            // 构造命令列表
            List<String> command = new ArrayList<>();
            command.add(pythonPath);
            command.add(scriptPath);

            // 根据是否提供参数，动态添加
            if (srcIp != null && !srcIp.isEmpty()) {
                command.add("--src_ip");
                command.add(srcIp);
            }
            if (dstIp != null && !dstIp.isEmpty()) {
                command.add("--dst_ip");
                command.add(dstIp);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // 读取 Python 输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 直接输出 Python 返回值
                stringBuffer.append(line);
            }

            // 等待 Python 进程完成
            process.waitFor();
            return stringBuffer.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
