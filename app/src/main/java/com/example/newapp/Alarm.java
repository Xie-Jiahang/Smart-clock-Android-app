package com.example.newapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Alarm  extends MainActivity{
    Socket socket = null;//开辟一个socket控件
    private TextView tv1;//item.xml里的TextView：Textviewname
    private ImageView tv2;//item.xml里的TextView：Textviewage
    //    private EditText ed1;
//    private EditText ed2;
    private ListView lv;//activity_main.xml里的ListView
    private LinearLayout ll;
    private BaseAdapter adapter;//要实现的类
    private List<User> userList = new ArrayList<User>();//实体类

    OutputStream outputstream;
    InputStream in;
    boolean isConnected = false;
    Alarm(R){
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listview);
        SharedPreferences sp3=getSharedPreferences("arrangement1",0);
        SharedPreferences sp4=getSharedPreferences("arrangement2",0);
        String a3= sp3.getString("a1", "");//取出前一个的值;
        String a4=sp4.getString("a2", "");//取出前一个的值;

        //模拟数据库
        for (int i = 0; i < 2; i++) {
            User user = new User();//给实体类赋值
            user.setName("闹钟" + (i + 1));
            user.setState(0);//闹钟开/关
            userList.add(user);
        }

        MyAdapter adapter = new MyAdapter(userList);
        lv.setAdapter(adapter);

    }
}
