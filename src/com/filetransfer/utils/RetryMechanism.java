package com.filetransfer.utils;

import com.filetransfer.transfer.FileSenderThread;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RetryMechanism {
    // 定义常量
    private static final int MAX_RETRIES = 3; // 最大重试次数
    private ExecutorService executorService; // 线程池

    // 构造函数，初始化线程池
    public RetryMechanism() {
        executorService = Executors.newCachedThreadPool(); // 使用缓存线程池
    }

    // 带重试机制的发送文件方法
    public boolean sendWithRetry(String targetHost, File file) {
        int attempts = 0; // 尝试次数初始化为0
        while (attempts < MAX_RETRIES) { // 当尝试次数小于最大重试次数时
            try {
                FileSenderThread senderThread = new FileSenderThread(targetHost, file); // 创建文件发送线程
                Future<Boolean> result = executorService.submit(senderThread); // 提交任务到线程池并获取结果
                if (result.get()) { // 如果发送成功
                    return true; // 返回成功
                }
            } catch (Exception e) {
                e.printStackTrace(); // 打印异常信息
            }
            attempts++; // 增加尝试次数
        }
        return false; // 如果所有尝试均失败，则返回失败
    }
}
