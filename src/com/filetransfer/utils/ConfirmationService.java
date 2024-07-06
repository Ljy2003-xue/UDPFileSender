package com.filetransfer.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ConfirmationService {
    // 定义常量
    private static final int CONFIRMATION_PORT = 8890; // 确认消息使用的端口
    private static final String CONFIRMATION_MESSAGE = "FILE_RECEIVED"; // 确认消息内容

    // 发送确认消息的方法
    public void sendConfirmation(String targetHost) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) { // 创建DatagramSocket
            byte[] sendData = CONFIRMATION_MESSAGE.getBytes(); // 将确认消息转换为字节数组
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(targetHost), CONFIRMATION_PORT);
            socket.send(sendPacket); // 发送确认消息
        }
    }

    // 接收确认消息的方法
    public boolean receiveConfirmation() {
        try (DatagramSocket socket = new DatagramSocket(CONFIRMATION_PORT)) { // 创建DatagramSocket并绑定到确认端口
            byte[] buffer = new byte[1024]; // 缓冲区用于接收数据
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet); // 接收确认消息
            String message = new String(packet.getData(), 0, packet.getLength()); // 将接收到的数据转换为字符串
            return CONFIRMATION_MESSAGE.equals(message); // 检查接收到的消息是否为确认消息
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return false; // 如果发生异常，则返回false
        }
    }
}
