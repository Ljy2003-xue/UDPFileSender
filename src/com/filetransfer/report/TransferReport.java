package com.filetransfer.report;

import java.util.HashMap;
import java.util.Map;

public class TransferReport {
    // 定义一个Map，用于存储传输报告，键为目标主机，值为传输结果
    private Map<String, String> report;

    // 构造函数，初始化report Map
    public TransferReport() {
        report = new HashMap<>(); // 初始化report为一个空的HashMap
    }

    // 添加传输报告的方法
    public void addReport(String targetHost, String result) {
        report.put(targetHost, result); // 将目标主机和传输结果添加到report Map中
    }

    // 获取传输报告的方法
    public Map<String, String> getReport() {
        return report; // 返回report Map
    }

    // 打印传输报告的方法
    public void printReport() {
        // 遍历report Map，并打印每个目标主机的传输结果
        report.forEach((host, result) -> System.out.println("Host: " + host + ", Result: " + result));
    }
}
