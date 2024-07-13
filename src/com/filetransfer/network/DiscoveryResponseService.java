package com.filetransfer.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryResponseService {
    // 定义广播端口和消息
    private static final int DISCOVERY_PORT = 8888; // 发现服务的端口
    private static final String DISCOVERY_MESSAGE = "DISCOVER_FILE_TRANSFER_SERVICE"; // 发现消息
    private static final String RESPONSE_MESSAGE = "FILE_TRANSFER_SERVICE_HERE"; // 响应消息

    // 监听发现请求的方法
    public void listenForDiscoveryRequests() {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            byte[] buffer = new byte[1024]; // 缓冲区用于接收数据
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // 数据报包，用于接收数据

            while (true) { // 无限循环，持续监听
                // 接收发现消息
                socket.receive(packet); // 阻塞等待接收数据报包
                String message = new String(packet.getData(), 0, packet.getLength()); // 将接收到的数据转换为字符串
                System.out.println("Received discovery message: " + message + " from " + packet.getAddress().getHostAddress()); // 打印接收到的消息和发送方地址

                // 如果接收到的消息是DISCOVERY_MESSAGE，则发送响应消息
                if (DISCOVERY_MESSAGE.equals(message.trim())) { // 判断消息是否匹配
                    InetAddress clientAddress = packet.getAddress(); // 获取客户端地址
                    int clientPort = packet.getPort(); // 获取客户端端口
                    byte[] responseData = RESPONSE_MESSAGE.getBytes(); // 将响应消息转换为字节数组
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort); // 创建响应数据报包
                    socket.send(responsePacket); // 发送响应消息
                    System.out.println("Sent response message to " + clientAddress.getHostAddress() + ":" + clientPort); // 打印发送的响应消息和目标地址
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常
        }
    }

    // 主方法，用于启动DiscoveryResponseService
    public static void main(String[] args) {
        DiscoveryResponseService responseService = new DiscoveryResponseService(); // 创建DiscoveryResponseService实例
        responseService.listenForDiscoveryRequests(); // 开始监听发现请求
    }
}
