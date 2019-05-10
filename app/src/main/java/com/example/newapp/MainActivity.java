package com.example.newapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.sy.timepick.EasyPickerView;
import com.sy.timepick.TimePickDialog;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    sys var
    private AnimatedCircleLoadingView animatedCircleLoadingView;

    boolean isConnected = false;

    Button send_time = null;
    TextView sys_time = null;
    LinearLayout bk;

    //Socket
    private Handler mMainHandler;//Handler线程  获取消息
    private Socket socket;//Socket 变量
    private ExecutorService mThreadPool;//线程池
    InputStream is;//输入流
    InputStreamReader isr ;
    BufferedReader br ;// 输入流读取器对象
    String response;// 接收服务器发送过来的消息
    OutputStream outputStream;// 输出流对象


    private List<User> userList = new ArrayList<User>();//实体类

    private Toolbar toolbar;
//  time page
    TextView receive_time = null;

//    连接页面
    Button enterbut = null;
    EditText IP = null;
    EditText PORT = null;
    EditText upper_temp=null;
    EditText low_temp=null;

//    alarm page
    ListView lv = null;
    private LinearLayout ll=null;



    //  页面切换
    View content0,content1, connect, temperature;
    List<View> viewlist = new ArrayList<View>();
    private void CloseView(){
        for(View v:viewlist){
            v.setVisibility(View.INVISIBLE);
        }
    }
    private void OpenView(){
        for(View v:viewlist) {
            v.setVisibility(View.VISIBLE);
        }
    }

//    连接线程
    //开辟一个线程 ,线程不允许更新UI  socket连接使用
    public class ClientThread extends Thread {
        public void run() {
            try {
                socket = new Socket(IP.getText().toString(), Integer.parseInt(PORT.getText().toString()));//建立好连接之后，就可以进行数据通讯了
                isConnected = true;
                is = socket.getInputStream();
                //得到一个消息对象，Message类是有Android操作系统提供
                Message msg = mMainHandler.obtainMessage();
                msg.what = 1;
                mMainHandler.sendMessage(msg);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//连接服务器
        }
    }
    public class InputThread extends Thread {
        public void run()
        {
            while(true)
            {
                if(socket!=null)
                {
                    String result = readFromInputStream(is);
                    try {
                        if (!result.equals("")) {
                            Message msg = new Message();
                            msg.what = 2;
                            Bundle data = new Bundle();
                            data.putString("msg", result);
                            msg.setData(data);
                            mMainHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        //Log.e(tag, "--->>read failure!" + e.toString());
                    }
                    try {
                        //设置当前显示睡眠1秒
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public String readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return new String(inDatas, "gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /* 客户端发送数据 */
    class Sender extends Thread {
        String serverIp;
        String message;

        Sender(String message) {
            super();
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
//    连接
    private void connect(){
        IP = (EditText) findViewById(R.id.editIp);
        IP.setText("192.168.4.1");
        PORT = (EditText) findViewById(R.id.editport);
        PORT.setText("8888");
        enterbut = (Button) findViewById(R.id.conn);//获取ID号
        enterbut.setOnClickListener(new enterclick());
    }
    public class enterclick implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if(!isConnected) {//没有连接上
                new ClientThread().start();//打开连接
            }
            else  {//连接上，断开连接
                if (socket != null) {
                    try {
                        socket.close();
                        socket = null;
                        isConnected=false;
                        Message msg = mMainHandler.obtainMessage();
                        msg.what=0;//信息标识号
                        mMainHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


//    time页面
    public class send_time implements View.OnClickListener {
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
            new Sender(message).start();
        }
    }

//    闹钟设定
    private void Alarm() {

        if (userList.size()==0) {
            //模拟数据库
            for (int i = 0; i < 4; i++) {
                User user = new User();//给实体类赋值
                if (i < 2)
                    user.setName("闹钟" + (i + 1));
                else if (i == 2)
                    user.setName("整点报时");
                else if (i == 3)
                    user.setName("每日播报");
                else user.setName("开机音乐");
                user.setState(0);//闹钟开/关
                userList.add(user);
            }
        }
        MyAdapter itemAdapter = new MyAdapter(userList);
        lv.setAdapter(itemAdapter);
    }

    private void init(){
        bk = findViewById(R.id.back);
        OpenView();CloseView();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //btn and text
        send_time = findViewById(R.id.send_time);
        send_time.setOnClickListener(new send_time());
        sys_time = findViewById(R.id.sys_time);

        //设置界面
        content0 = findViewById(R.id.content0);
        content1 = findViewById(R.id.content1);
        connect = findViewById(R.id.connect);
        temperature = findViewById(R.id.temperature);
        viewlist.add(connect);viewlist.add(content0);viewlist.add(content1);
        viewlist.add(temperature);viewlist.add(findViewById(R.id.developer));
        viewlist.add(findViewById(R.id.lis));

        lv = (ListView) findViewById(R.id.listview);
        ll = (LinearLayout)findViewById(R.id.ll_app_expand);
        receive_time = (TextView) findViewById(R.id.receive_time);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        //Loading page
        animatedCircleLoadingView = (AnimatedCircleLoadingView) findViewById(R.id.circle_loading_view);
        animatedCircleLoadingView.startDeterminate();
        startPercentMockThread();


        //Thread pool
        mThreadPool = Executors.newCachedThreadPool();
        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String a, receive;
                switch (msg.what) {
                    case 0://TCP断开连接
                        enterbut.setText("连接");
                        Toast.makeText(MainActivity.this, "服务器连接断开！", Toast.LENGTH_SHORT).show();
                        break;
                    case 1://表明TCP连接成功，可以进行数据交互了
                        enterbut.setText("断开");
                        Toast.makeText(MainActivity.this, "服务器连接成功！", Toast.LENGTH_SHORT).show();
                        new InputThread().start();//开启接收线程
                        break;
                    case 2://有数据进来
                        String result = msg.getData().get("msg").toString();
                        a = result.substring(0, 1);
                        if (a.equals("S")) {
                            receive = result.substring(1, 3);
                            receive_time.setText(receive);
                            Toast.makeText(MainActivity.this, "接收成功", Toast.LENGTH_SHORT).show();
                        }
                        if (a.equals("V")) {
                            receive = result.substring(1, 3);
                            receive_time.setText(receive);
                            Toast.makeText(MainActivity.this, "接收成功", Toast.LENGTH_SHORT).show();
                        }
                        if (a.equals("D")) {
                            receive = result.substring(1, 8);
                            receive_time.setText(receive);
                            Toast.makeText(MainActivity.this, "接收成功", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };


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
    private void startPercentMockThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(65);
                        changePercent(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }
    private void startPercentMockThread(final int speed) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    Thread.sleep(65);
                    changePercent(85);
                    for (int i = 94; i <= 100; i=i+2) {
                        Thread.sleep(65);
                        changePercent(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }
    private void changePercent(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animatedCircleLoadingView.setPercent(percent);
                if(percent==100) {
                    try {
                        Thread.sleep(10);

                        bk.setVisibility(View.GONE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void resetLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animatedCircleLoadingView.resetLoading();
            }
        });
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



    private void reload(){
        CloseView();
        bk.setVisibility(View.VISIBLE);
        resetLoading();
        startPercentMockThread(25);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            reload();
            content0.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Time");
        } else if (id == R.id.nav_gallery) {
            reload();
            Alarm();
            content1.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Alarm");
        } else if (id == R.id.nav_slideshow) {
            reload();
            temperature.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Temperature");
        } else if (id == R.id.nav_manage) {
            reload();
            connect.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Connection");
        } else if (id == R.id.nav_share) {
            CloseView();
            findViewById(R.id.developer).setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Developer");
        } else if (id == R.id.nav_send) {
            CloseView();
            findViewById(R.id.lis).setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Licence");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
