package com.filetransfer.transfer;

import com.filetransfer.network.UDPFileReceiver;

public class FileReceiverThread implements Runnable {
    private String savePath; // 保存文件的路径

    // 构造函数，初始化保存路径
    public FileReceiverThread(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public void run() {
        // 创建UDPFileReceiver对象并运行
        UDPFileReceiver receiver = new UDPFileReceiver(savePath);
        receiver.run();
    }
}
