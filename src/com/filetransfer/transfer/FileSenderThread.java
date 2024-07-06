package com.filetransfer.transfer;

import com.filetransfer.network.UDPFileSender;

import java.io.File;
import java.util.concurrent.Callable;

public class FileSenderThread implements Callable<Boolean> {
    private String targetHost; // 目标主机地址
    private File file; // 要发送的文件
    private UDPFileSender sender; // 发送文件的工具类

    // 构造函数，初始化目标主机地址和文件
    public FileSenderThread(String targetHost, File file) {
        this.targetHost = targetHost;
        this.file = file;
        this.sender = new UDPFileSender(); // 初始化UDPFileSender对象
    }

    @Override
    public Boolean call() {
        try {
            sender.sendFile(targetHost, file); // 发送文件
            return true; // 返回成功
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return false; // 返回失败
        }
    }
}
