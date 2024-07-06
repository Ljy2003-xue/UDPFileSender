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
    // GUI组件定义
    private JButton selectFileButton;          // 选择文件按钮
    private JLabel selectedFileLabel;          // 显示选择文件名称的标签
    private JButton discoverHostsButton;       // 发现主机按钮
    private JList<String> targetList;          // 显示发现的目标主机列表
    private JButton sendFileButton;            // 发送文件按钮
    private JTextArea resultArea;              // 显示传输结果的文本区域
    private File selectedFile;                 // 已选择的文件
    private DefaultListModel<String> targetListModel;  // 目标主机列表的模型
    private String saveDirectory = "D:/";   // 指定文件保存目录

    public MainFrame() {
        // 设置窗口标题和大小
        setTitle("UDP File Transfer");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 顶部面板布局
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        selectFileButton = new JButton("Select File");
        topPanel.add(selectFileButton);

        selectedFileLabel = new JLabel("No file selected");
        topPanel.add(selectedFileLabel);

        discoverHostsButton = new JButton("Discover Hosts");
        topPanel.add(discoverHostsButton);

        add(topPanel, BorderLayout.NORTH);

        // 左侧主机列表布局
        targetListModel = new DefaultListModel<>();
        targetList = new JList<>(targetListModel);
        JScrollPane scrollPane = new JScrollPane(targetList);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        add(scrollPane, BorderLayout.WEST);

        // 中间结果显示区域布局
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        add(resultScrollPane, BorderLayout.CENTER);

        // 底部发送文件按钮布局
        sendFileButton = new JButton("Send File");
        add(sendFileButton, BorderLayout.SOUTH);

        // 选择文件按钮的监听器
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    selectedFileLabel.setText("Selected File: " + selectedFile.getName());
                }
            }
        });

        // 发现主机按钮的监听器
        discoverHostsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetListModel.clear();
                DiscoveryService discoveryService = new DiscoveryService();
                discoveryService.discoverHosts();
                Set<String> discoveredHosts = discoveryService.getDiscoveredHosts();
                for (String host : discoveredHosts) {
                    targetListModel.addElement(host);
                }
                if (discoveredHosts.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hosts discovered.");
                }
            }
        });

        // 发送文件按钮的监听器
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    List<String> targets = targetList.getSelectedValuesList();
                    if (!targets.isEmpty()) {
                        ExecutorService executorService = Executors.newCachedThreadPool();
                        TransferReport transferReport = new TransferReport();
                        for (String target : targets) {
                            RetryMechanism retryMechanism = new RetryMechanism();
                            boolean success = retryMechanism.sendWithRetry(target, selectedFile);
                            if (success) {
                                transferReport.addReport(target, "Success");
                            } else {
                                transferReport.addReport(target, "Failed");
                            }
                        }
                        transferReport.printReport();
                        resultArea.setText("");
                        transferReport.getReport().forEach((host, result) -> resultArea.append("Host: " + host + ", Result: " + result + "\n"));
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select at least one target host.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a file to send.");
                }
            }
        });

        // 启动DiscoveryResponseService线程
        new Thread(() -> {
            DiscoveryResponseService responseService = new DiscoveryResponseService();
            responseService.listenForDiscoveryRequests();
        }).start();

        // 启动UDPFileReceiver线程
        new Thread(() -> {
            UDPFileReceiver fileReceiver = new UDPFileReceiver(saveDirectory);
            fileReceiver.run();
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
