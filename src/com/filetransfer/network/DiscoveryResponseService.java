package com.filetransfer.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryResponseService {
    // 定义广播端口和消息
    private static final int DISCOVERY_PORT = 8888;                // 发现服务的端口
    private static final String DISCOVERY_MESSAGE = "DISCOVER_FILE_TRANSFER_SERVICE"; // 发现消息
    private static final String RESPONSE_MESSAGE = "FILE_TRANSFER_SERVICE_HERE";      // 响应消息

    // 监听发现请求的方法
    public void listenForDiscoveryRequests() {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            byte[] buffer = new byte[1024]; // 缓冲区用于接收数据
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                // 接收发现消息
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received discovery message: " + message + " from " + packet.getAddress().getHostAddress());

                // 如果接收到的消息是DISCOVERY_MESSAGE，则发送响应消息
                if (DISCOVERY_MESSAGE.equals(message.trim())) {
                    InetAddress clientAddress = packet.getAddress(); // 获取客户端地址
                    int clientPort = packet.getPort();               // 获取客户端端口
                    byte[] responseData = RESPONSE_MESSAGE.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket); // 发送响应消息
                    System.out.println("Sent response message to " + clientAddress.getHostAddress() + ":" + clientPort);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常
        }
    }

    // 主方法，用于启动DiscoveryResponseService
    public static void main(String[] args) {
        DiscoveryResponseService responseService = new DiscoveryResponseService();
        responseService.listenForDiscoveryRequests(); // 开始监听发现请求
    }
}
