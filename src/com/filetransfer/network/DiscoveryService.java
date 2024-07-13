package com.filetransfer.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class DiscoveryService {
    // 定义常量
    private static final int DISCOVERY_PORT = 8888; // 发现服务的端口
    private static final int BUFFER_SIZE = 1024; // 缓冲区大小
    private static final String DISCOVERY_MESSAGE = "DISCOVER_FILE_TRANSFER_SERVICE"; // 发现消息
    private static final String RESPONSE_MESSAGE = "FILE_TRANSFER_SERVICE_HERE"; // 响应消息
    private Set<String> discoveredHosts; // 存储发现的主机地址

    // 构造函数，初始化发现的主机集合
    public DiscoveryService() {
        discoveredHosts = new HashSet<>(); // 初始化发现的主机集合
    }

    // 发现主机的方法
    public void discoverHosts() {
        // 使用try-with-resources语句自动关闭资源
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true); // 设置套接字为广播模式
            byte[] sendData = DISCOVERY_MESSAGE.getBytes(); // 将发现消息转换为字节数组
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT); // 创建发送数据报包
            socket.send(sendPacket); // 发送发现消息
            System.out.println("Broadcast message sent to 255.255.255.255:8888"); // 打印广播消息发送信息

            byte[] receiveBuffer = new byte[BUFFER_SIZE]; // 创建缓冲区用于接收数据
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, BUFFER_SIZE); // 创建接收数据报包

            socket.setSoTimeout(3000); // 设置套接字超时时间为3秒
            while (true) {
                try {
                    socket.receive(receivePacket); // 接收响应消息
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength()); // 将接收到的数据转换为字符串
                    System.out.println("Received message: " + message + " from " + receivePacket.getAddress().getHostAddress()); // 打印接收到的消息和发送方地址
                    if (RESPONSE_MESSAGE.equals(message.trim())) { // 判断消息是否匹配
                        String hostAddress = receivePacket.getAddress().getHostAddress(); // 获取响应消息的主机地址
                        if (!discoveredHosts.contains(hostAddress)) { // 如果主机地址不在集合中
                            discoveredHosts.add(hostAddress); // 将主机地址添加到发现的主机集合中
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Discovery timeout or error: " + e.getMessage()); // 打印超时或错误信息
                    break; // 超时或出现错误时停止监听响应消息
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常
        }
    }

    // 获取发现的主机集合的方法
    public Set<String> getDiscoveredHosts() {
        return discoveredHosts; // 返回发现的主机集合
    }

    // 主方法，用于启动DiscoveryService
    public static void main(String[] args) {
        DiscoveryService discoveryService = new DiscoveryService(); // 创建DiscoveryService实例
        discoveryService.discoverHosts(); // 执行发现主机操作
        System.out.println("Discovered hosts: " + discoveryService.getDiscoveredHosts()); // 输出发现的主机
    }
}
