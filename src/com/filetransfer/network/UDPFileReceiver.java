package com.filetransfer.network;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPFileReceiver implements Runnable {
    // 定义常量
    private static final int BUFFER_SIZE = 1024; // 缓冲区大小
    private static final int PORT = 8889; // 端口号
    private String saveDirectory; // 保存文件的目录

    // 构造函数，初始化保存目录
    public UDPFileReceiver(String saveDirectory) {
        this.saveDirectory = saveDirectory;
    }

    @Override
    public void run() {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE]; // 缓冲区用于接收数据
            DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);

            // 接收文件元数据
            socket.receive(packet);
            String metadata = new String(packet.getData(), 0, packet.getLength());
            String[] metadataParts = metadata.split(";");
            String fileName = metadataParts[0]; // 文件名
            long fileSize = Long.parseLong(metadataParts[1]); // 文件大小

            // 准备接收文件内容
            File file = new File(saveDirectory, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize) {
                    socket.receive(packet); // 接收文件内容数据包
                    int bytesRead = packet.getLength();
                    fos.write(packet.getData(), 0, bytesRead); // 写入文件
                    fos.flush();
                    totalBytesRead += bytesRead;
                }
                System.out.println("Received file: " + fileName + " (" + totalBytesRead + " bytes)");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常
        }
    }
}
