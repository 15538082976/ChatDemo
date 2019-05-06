package com.test.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.HashMap;

public class ClientManager {
    private static HashMap<String, Socket> clientList = new HashMap<>();
    private static ServerThread serverThread = null;

    private static class ServerThread implements Runnable {
        private int port = 10016;
        private boolean isExit = false;
        private ServerSocket server;

        public ServerThread() {
            try {
                server = new ServerSocket(port);
                System.out.println("启动服务成功" + "port:" + port);
            } catch (IOException e) {
                System.out.println("启动server失败，错误原因：" + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while (!isExit) {
                    // 进入等待环节
                    System.out.println("等待手机的连接... ... ");
                    final Socket socket = server.accept();
                    final String address = socket.getRemoteSocketAddress().toString();
                    System.out.println("连接成功，连接的手机为：" + address);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    clientList.put(address, socket);
                                }
                                InputStream inputStream = socket.getInputStream();
                                byte[] buffer = new byte[1024];//注意发送的数据一次不能超过1024字节
                                int len;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    String text = new String(buffer, 0, len);
                                    System.out.println("收到的数据为：" + text);
                                    // 在这里群发消息
                                    sendMsgAll(text);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void Stop() {
            isExit = true;
            if (server != null) {
                try {
                    server.close();
                    System.out.println("已关闭server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ServerThread startServer() {
        System.out.println("开启服务");
        if (serverThread != null) {
            showDown();
        }
        serverThread = new ServerThread();
        new Thread(serverThread).start();
        System.out.println("开启服务成功");
        return serverThread;
    }

    public static void showDown() {
        for (Socket socket : clientList.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientList.clear();
        serverThread.Stop();
    }

    public static boolean sendMsgAll(String msg) {
        try {
            for (Socket socket : clientList.values()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(msg.getBytes("utf-8"));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}


