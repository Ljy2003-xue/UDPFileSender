package com.filetransfer.network;

import com.filetransfer.utils.ConfirmationService;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPFileSender {
    // 定义常量
    private static final int BUFFER_SIZE = 1024; // 缓冲区大小
    private static final int PORT = 8889; // 端口号

    // 发送文件的方法
    public void sendFile(String targetHost, File file) throws Exception {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket(); // 创建DatagramSocket对象
             FileInputStream fis = new FileInputStream(file)) { // 创建文件输入流

            InetAddress targetAddress = InetAddress.getByName(targetHost); // 获取目标主机地址

            // 发送文件元数据（文件名和文件大小）
            String fileName = file.getName(); // 获取文件名
            long fileSize = file.length(); // 获取文件大小
            String metadata = fileName + ";" + fileSize; // 创建元数据字符串
            byte[] metadataBytes = metadata.getBytes(); // 将元数据转换为字节数组
            DatagramPacket metadataPacket = new DatagramPacket(metadataBytes, metadataBytes.length, targetAddress, PORT); // 创建元数据包
            socket.send(metadataPacket); // 发送元数据包

            // 发送文件内容
            byte[] buffer = new byte[BUFFER_SIZE]; // 缓冲区用于读取文件内容
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) { // 循环读取文件内容到缓冲区
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, targetAddress, PORT); // 创建数据包
                socket.send(packet); // 发送数据包
            }
            // 接收确认消息
            ConfirmationService confirmationService = new ConfirmationService();
            boolean confirmationReceived = confirmationService.receiveConfirmation();
            if (!confirmationReceived) {
                throw new Exception("File transfer failed: no confirmation received.");
            }
        }

    }
}
