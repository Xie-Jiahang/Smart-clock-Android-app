package com.example.newapp;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender {
    Socket socket = null;
    String serverIp;
    String message;

    Sender(String message) {
        super();
        //socket=new Socket(IP.getText().toString(),Integer.parseInt(PORT.getText().toString()));//建立好连接之后，就可以进行数据通讯了
        //serverIp = serverAddress;
        this.message = message;
    }

    public void run()
    {
        PrintWriter out;
        try
        {
            // 向服务器端发送消息
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(message);
            // 接收来自服务器端的消息
            //	BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //String msg = br.readLine();
            // 关闭流
            //out.close();
            out.flush();
            //	br.close();
            // 关闭Socket
            //	sock.close();
        } catch (Exception e)
        {
            //LogUtil.e("发送错误:[Sender]run()" + e.getMessage());
        }
    }
}
