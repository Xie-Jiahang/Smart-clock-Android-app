package com.example.newapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    sys var
    Socket socket = null;
    InputStream in;
    boolean isConnected = false;

    Button send_time = null;
    TextView sys_time = null;

    private List<User> userList = new ArrayList<User>();//实体类

//    连接页面
    Button enterbut = null;
    EditText IP = null;
    EditText PORT = null;
    EditText upper_temp=null;
    EditText low_temp=null;

//    alarm page
    ListView lv = null;


    //  页面切换
    View content0,content1, connect, temperature;
    List<View> viewlist = new ArrayList<View>();
    private void closeview(){
        for(View v:viewlist){
            v.setVisibility(View.INVISIBLE);
        }
    }
    private void openview(){
        for(View v:viewlist) {
            v.setVisibility(View.VISIBLE);
        }
    }

//    连接
    private void connect(){
        IP = (EditText) findViewById(R.id.editIp);
        IP.setText("192.168.4.1");
        PORT = (EditText) findViewById(R.id.editport);
        PORT.setText("8888");
        enterbut = (Button) findViewById(R.id.conn);//获取ID号
        enterbut.setOnClickListener(new enterclick());
    }


    public class enterclick implements View.OnClickListener//连接!!!
    {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if(!isConnected)//没有连接上
            {
                try {
                    socket=new Socket(IP.getText().toString(),Integer.parseInt(PORT.getText().toString()));
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "服务器连接断开！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            else//连接上，断开连接
            {
                if (socket != null) {
                    try {
                        socket.close();
                        socket = null;
                        isConnected=false;
                        //得到一个消息对象，Message类是有Android操作系统提供

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }


//    闹钟设定
    private void Alarm(){
        lv = (ListView) findViewById(R.id.listview);

        //模拟数据库
        for (int i = 0; i < 2; i++) {
            User user = new User();//给实体类赋值
            if(i<2)
                user.setName("闹钟" + (i + 1));
            else if(i==2)
                user.setName("整点报时");
            else if(i==3)
                user.setName("每日播报");
            else user.setName("开机音乐");
            user.setState(0);//闹钟开/关
            userList.add(user);
        }
        MyAdapter itemAdapter = new MyAdapter(userList);
        lv.setAdapter(itemAdapter);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openview();closeview();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //btn and text
        View send_time = findViewById(R.id.send_time);
        send_time.setOnClickListener(new send_time());
        sys_time = findViewById(R.id.sys_time);
        //设置界面
        content0 = findViewById(R.id.content0);
        content1 = findViewById(R.id.content1);
        connect = findViewById(R.id.connect);
        temperature = findViewById(R.id.temperature);
        viewlist.add(connect);viewlist.add(content0);viewlist.add(content1);
        viewlist.add(temperature);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //connect function
        connect();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class send_time implements View.OnClickListener//一次性发送时间
    {
        @Override
        public void onClick(View arg0){
            long time=System.currentTimeMillis();

            final Calendar mCalendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
            mCalendar.setTimeInMillis(time);
            Calendar calendar=Calendar.getInstance();
            int year=calendar.get(Calendar.YEAR)%100;
            int month=calendar.get(Calendar.MONTH)+1;
            int date=calendar.get(Calendar.DATE);
            int hour=calendar.get(Calendar.HOUR_OF_DAY);
            int min=calendar.get(Calendar.MINUTE);
            int sec=calendar.get(Calendar.SECOND);
            int day=calendar.get(Calendar.DAY_OF_WEEK)-1;
            String message="T";

            if(year<10)
                message=(message+"0"+year);
            else
                message=(message+""+year);

            if(month<10)
                message=(message+"0"+month);
            else
                message=(message+month);

            if(date<10)
                message=(message+"0"+date);
            else
                message=(message+date);

            if(hour<10)
                message=(message+"0"+hour);
            else
                message=(message+hour);

            if(min<10)
                message=(message+"0"+min);
            else
                message=(message+min);

            if(sec<10)
                message=(message+"0"+sec);
            else
                message=(message+sec);

            if(day<10)
                message=(message+"0"+day);
            else
                message=(message+day);

            send_time.setText("校准成功");
            sys_time.setText(message);
            //new Sender(message).start();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            closeview();
            content0.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_gallery) {
            closeview();
            Alarm();
            content1.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_slideshow) {
            closeview();
            temperature.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_manage) {
            closeview();
            connect.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
