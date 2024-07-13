package com.filetransfer.network;

import com.filetransfer.utils.ConfirmationService;
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
        this.saveDirectory = saveDirectory; // 初始化保存目录
    }

    @Override
    public void run() {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket(PORT)) { // 创建DatagramSocket并绑定到指定端口
            byte[] buffer = new byte[BUFFER_SIZE]; // 缓冲区用于接收数据
            DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE); // 数据报包，用于接收数据

            // 接收文件元数据
            socket.receive(packet); // 阻塞等待接收数据报包
            String metadata = new String(packet.getData(), 0, packet.getLength()); // 将接收到的数据转换为字符串
            String[] metadataParts = metadata.split(";"); // 分割元数据字符串，获取文件名和文件大小
            String fileName = metadataParts[0]; // 文件名
            long fileSize = Long.parseLong(metadataParts[1]); // 文件大小

            // 准备接收文件内容
            File file = new File(saveDirectory, fileName); // 创建保存文件的File对象
            try (FileOutputStream fos = new FileOutputStream(file)) { // 创建文件输出流
                long totalBytesRead = 0; // 记录已接收的字节数
                while (totalBytesRead < fileSize) { // 当接收的字节数小于文件大小时，继续接收
                    socket.receive(packet); // 接收文件内容数据包
                    int bytesRead = packet.getLength(); // 获取接收到的数据长度
                    fos.write(packet.getData(), 0, bytesRead); // 将接收到的数据写入文件
                    fos.flush(); // 刷新输出流
                    totalBytesRead += bytesRead; // 更新已接收的字节数
                }
                System.out.println("Received file: " + fileName + " (" + totalBytesRead + " bytes)"); // 打印接收文件的信息
                // 发送确认消息
                ConfirmationService confirmationService = new ConfirmationService();
                confirmationService.sendConfirmation(packet.getAddress().getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常
        }
    }
}
