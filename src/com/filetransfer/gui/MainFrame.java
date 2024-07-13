package com.filetransfer.gui;

import com.filetransfer.network.DiscoveryService;
import com.filetransfer.network.DiscoveryResponseService;
import com.filetransfer.network.UDPFileReceiver;
import com.filetransfer.report.TransferReport;
import com.filetransfer.utils.RetryMechanism;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFrame extends JFrame {
    // 定义选择文件按钮
    private JButton selectFileButton;
    // 定义标签用于显示选择的文件名
    private JLabel selectedFileLabel;
    // 定义发现主机按钮
    private JButton discoverHostsButton;
    // 定义列表用于显示发现的目标主机
    private JList<String> targetList;
    // 定义发送文件按钮
    private JButton sendFileButton;
    // 定义文本区域用于显示传输结果
    private JTextArea resultArea;
    // 定义变量保存已选择的文件
    private File selectedFile;
    // 定义目标主机列表的模型
    private DefaultListModel<String> targetListModel;
    // 定义文件保存目录
    private String saveDirectory = "D:/";

    public MainFrame() {
        // 设置窗口标题
        setTitle("UDP File Transfer");
        // 设置窗口大小
        setSize(600, 400);
        // 设置窗口关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口布局管理器
        setLayout(new BorderLayout());

        // 创建顶部面板
        JPanel topPanel = new JPanel();
        // 设置顶部面板布局管理器
        topPanel.setLayout(new FlowLayout());

        // 初始化选择文件按钮
        selectFileButton = new JButton("Select File");
        // 将选择文件按钮添加到顶部面板
        topPanel.add(selectFileButton);

        // 初始化文件名标签
        selectedFileLabel = new JLabel("No file selected");
        // 将文件名标签添加到顶部面板
        topPanel.add(selectedFileLabel);

        // 初始化发现主机按钮
        discoverHostsButton = new JButton("Discover Hosts");
        // 将发现主机按钮添加到顶部面板
        topPanel.add(discoverHostsButton);

        // 将顶部面板添加到窗口的北部（顶部）
        add(topPanel, BorderLayout.NORTH);

        // 初始化目标主机列表的模型
        targetListModel = new DefaultListModel<>();
        // 初始化目标主机列表
        targetList = new JList<>(targetListModel);
        // 将目标主机列表放入滚动面板
        JScrollPane scrollPane = new JScrollPane(targetList);
        // 设置滚动面板的首选大小
        scrollPane.setPreferredSize(new Dimension(200, 300));
        // 将滚动面板添加到窗口的西部（左侧）
        add(scrollPane, BorderLayout.WEST);

        // 初始化结果显示文本区域
        resultArea = new JTextArea();
        // 设置结果显示文本区域为只读
        resultArea.setEditable(false);
        // 将结果显示文本区域放入滚动面板
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        // 将滚动面板添加到窗口的中部
        add(resultScrollPane, BorderLayout.CENTER);

        // 初始化发送文件按钮
        sendFileButton = new JButton("Send File");
        // 将发送文件按钮添加到窗口的南部（底部）
        add(sendFileButton, BorderLayout.SOUTH);

        // 为选择文件按钮添加动作监听器
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建文件选择对话框
                JFileChooser fileChooser = new JFileChooser();
                // 显示文件选择对话框
                int returnValue = fileChooser.showOpenDialog(null);
                // 如果用户选择了文件
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    // 获取选择的文件
                    selectedFile = fileChooser.getSelectedFile();
                    // 更新文件名标签显示
                    selectedFileLabel.setText("Selected File: " + selectedFile.getName());
                }
            }
        });

        // 为发现主机按钮添加动作监听器
        discoverHostsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空目标主机列表模型
                targetListModel.clear();
                // 创建发现服务
                DiscoveryService discoveryService = new DiscoveryService();
                // 执行发现主机操作
                discoveryService.discoverHosts();
                // 获取发现的主机
                Set<String> discoveredHosts = discoveryService.getDiscoveredHosts();
                // 将发现的主机添加到目标主机列表模型
                for (String host : discoveredHosts) {
                    targetListModel.addElement(host);
                }
                // 如果没有发现任何主机，显示消息提示
                if (discoveredHosts.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hosts discovered.");
                }
            }
        });

        // 为发送文件按钮添加动作监听器
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 如果已选择文件
                if (selectedFile != null) {
                    // 获取选择的目标主机列表
                    List<String> targets = targetList.getSelectedValuesList();
                    // 如果选择了至少一个目标主机
                    if (!targets.isEmpty()) {
                        // 创建线程池
                        //ExecutorService executorService = Executors.newCachedThreadPool();
                        // 创建传输报告对象
                        TransferReport transferReport = new TransferReport();
                        // 遍历目标主机列表
                        for (String target : targets) {
                            // 创建重试机制对象
                            RetryMechanism retryMechanism = new RetryMechanism();
                            // 尝试发送文件并获取结果
                            boolean success = retryMechanism.sendWithRetry(target, selectedFile);
                            // 根据发送结果更新传输报告
                            if (success) {
                                transferReport.addReport(target, "Success");
                            } else {
                                transferReport.addReport(target, "Failed");
                            }
                        }
                        // 打印传输报告
                        transferReport.printReport();
                        // 清空结果显示区域
                        resultArea.setText("");
                        // 将传输报告内容显示在结果区域
                        transferReport.getReport().forEach((host, result) -> resultArea.append("Host: " + host + ", Result: " + result + "\n"));
                    } else {
                        // 如果没有选择目标主机，显示消息提示
                        JOptionPane.showMessageDialog(null, "Please select at least one target host.");
                    }
                } else {
                    // 如果没有选择文件，显示消息提示
                    JOptionPane.showMessageDialog(null, "Please select a file to send.");
                }
            }
        });

        // 启动DiscoveryResponseService线程，监听发现请求
        new Thread(() -> {
            DiscoveryResponseService responseService = new DiscoveryResponseService();
            responseService.listenForDiscoveryRequests();
        }).start();

        // 启动UDPFileReceiver线程，接收文件
        new Thread(() -> {
            UDPFileReceiver fileReceiver = new UDPFileReceiver(saveDirectory);
            fileReceiver.run();
        }).start();
    }

    // 主方法，启动应用程序
    public static void main(String[] args) {
        // 使用SwingUtilities.invokeLater确保在事件调度线程上创建和显示GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 创建并显示主窗口
                new MainFrame().setVisible(true);
            }
        });
    }
}
