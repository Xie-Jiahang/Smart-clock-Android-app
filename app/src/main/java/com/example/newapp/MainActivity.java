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
import android.view.KeyEvent;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
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


import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AnimatedCircleLoadingView animatedCircleLoadingView;

    boolean isConnected = false;

    Button send_time = null;
    Button set_temp=null;
    Button send_light=null;
    FloatingActionButton alarm_music = null;
//    TextView sys_time = null;
    SeekBar edit_volume = null;
    SeekBar edit_lcd = null;
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
    //连接
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

    //time页面
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

//            sys_time.setText(message);
            new Sender(message).start();
        }
    }

    //闹钟设定
    private void Alarm() {

        if (userList.size()==0) {
            //模拟数据库
            for (int i = 0; i < 6; i++) {
                User user = new User();//给实体类赋值
                if(i<2)
                    user.setName("闹钟" + (i + 1));
                else if (i==2)
                    user.setName("睡眠模式");
                else if(i==3)
                    user.setName("整点报时");
                else if(i==4)
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
        send_light=findViewById(R.id.send_light);
        send_light.setOnClickListener(new light_setting());

//        sys_time = findViewById(R.id.sys_time);

        alarm_music = (FloatingActionButton) findViewById(R.id.alarm_music);
        alarm_music.setOnClickListener(new alarm_setting());

        upper_temp=(EditText)findViewById(R.id.ed1);
        low_temp=(EditText)findViewById(R.id.ed2);
        SharedPreferences sp1= getSharedPreferences("upper_temp", 0);
        SharedPreferences sp2= getSharedPreferences("low_temp", 0);
        upper_temp.setText(sp1.getString("tmp1",""));
        low_temp.setText(sp2.getString("tmp2",""));
        set_temp=findViewById(R.id.edit_temp);
        set_temp.setOnClickListener(new temp_setting());

        edit_volume= (SeekBar) findViewById(R.id.editvolume);
        SharedPreferences sp= getSharedPreferences("volume", 0);
        edit_volume.setProgress(sp.getInt("v",0));
        edit_volume.setOnSeekBarChangeListener(new volume_setting());

        edit_lcd=(SeekBar)findViewById(R.id.edit_lcd);
        sp= getSharedPreferences("lcd", 0);
        edit_lcd.setProgress(sp.getInt("l",0));
        edit_lcd.setOnSeekBarChangeListener(new lcd_setting());

        //设置界面
        content0 = findViewById(R.id.content0);
        content1 = findViewById(R.id.content1);
        connect = findViewById(R.id.connect);
        temperature = findViewById(R.id.temperature);
        viewlist.add(connect);viewlist.add(content0);viewlist.add(content1);
        viewlist.add(temperature);viewlist.add(findViewById(R.id.developer));

        lv = (ListView) findViewById(R.id.listview);
        ll = (LinearLayout)findViewById(R.id.ll_app_expand);
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
                }
            }
        };

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

    public class temp_setting implements View.OnClickListener {
        String message="P";
        String str1;
        String str2;
        SharedPreferences sp1= getSharedPreferences("upper_temp", 0);
        SharedPreferences sp2= getSharedPreferences("low_temp", 0);
        SharedPreferences.Editor editor1 = sp1.edit();
        SharedPreferences.Editor editor2 = sp2.edit();

        public void edit_temp(){
            message="P";
            if (str1.length()==1)
                message = message+"0" + str1;
            else
                message = message+"" + str1;

            if (str2.length()==1)
                message = message+"0" + str2;
            else
                message = message+""+ str2;
            if(str1.equals("")||str2.equals(""))
                message="PERROR";
            if(str1.equals("")==false&&str2.equals("")==false){
                if(Integer.valueOf(str1)<=Integer.valueOf(str2))
                    message="PERROR";
            }
        }

        @Override
        public void onClick(View arg0) {
            str1=upper_temp.getText().toString();
            if(str1.equals(""))
                message="PERROR";
            else{
                editor1.putString("tmp1",str1);//将前一个的值改为后一个
                editor1.apply();//editor.commit();
            }

            str2=low_temp.getText().toString();
            if(str2.equals(""))
                message="PERROR";
            else{
                editor2.putString("tmp2",str2);//将前一个的值改为后一个
                editor2.apply();//editor.commit();
            }

            edit_temp();
            new Sender(message).start();
            Toast.makeText(MainActivity.this,""+message,Toast.LENGTH_SHORT).show();
        }
    }

    public class light_setting implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            Toast.makeText(MainActivity.this,"CB",Toast.LENGTH_SHORT).show();
            new Sender("CB").start();
        }
    }

    public class volume_setting implements SeekBar.OnSeekBarChangeListener {
        SharedPreferences sp = getSharedPreferences("volume", 0);
        SharedPreferences.Editor editor = sp.edit();

        @Override
        public void onStartTrackingTouch(SeekBar seekBar){
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar){
        }

        @Override
        public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser){
            // TODO Auto-generated method stub
            String message="V";
            editor.putInt("v",progress);
            editor.commit();
            message=message+progress;
            Toast.makeText(MainActivity.this,""+message,Toast.LENGTH_SHORT).show();
            new Sender(message).start();
        }
    }

//    public class send_light implements View.OnClickListener{
//        @Override
//        public void onClick(View view) {
//
//        }
//    }

    public class lcd_setting implements SeekBar.OnSeekBarChangeListener {
        SharedPreferences sp = getSharedPreferences("lcd", 0);
        SharedPreferences.Editor editor = sp.edit();

        @Override
        public void onStartTrackingTouch(SeekBar seekBar){
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar){
        }

        @Override
        public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser){
            // TODO Auto-generated method stub
            String message="l";
            String toast="";
            editor.putInt("l",progress);
            editor.commit();
            message=message+progress;
            switch (progress){
                case 0:toast="关闭";break;
                case 1:toast="智能";break;
                case 2:toast="开启";break;
            }
            Toast.makeText(MainActivity.this,""+message+toast,Toast.LENGTH_SHORT).show();
            new Sender(message).start();
        }

    }

    public class alarm_setting implements View.OnClickListener {
        public String message="M";
        String mes;
        int checkedItem=0;
        SharedPreferences sp = getSharedPreferences("alarm_music", 0);
        SharedPreferences.Editor editor = sp.edit();

        @Override
        public void onClick(View v) {
            mes = sp.getString("alarm_music","");//取出前一个的值
            if(mes.length()==0)
                mes="M1";
            checkedItem=Integer.valueOf(mes.substring(1,2))-1;//0表示选中第一个项目

            AlertDialog.Builder localBuilder = new AlertDialog.Builder(MainActivity.this);
            final String[] arrayOfString = {"Music 1", "Music 2","Music 3"};
            localBuilder.setTitle("闹钟音乐选择");
            localBuilder.setSingleChoiceItems(arrayOfString,checkedItem, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                    message="M";
                    switch (paramAnonymousInt)
                    {
                        case 0:
                            message="M1";
                            break;
                        case 1:
                            message="M2";
                            break;
                        case 2:
                            message="M3";
                            break;
                    }
                    editor.putString("alarm_music",message);//将前一个的值改为后一个
                    editor.commit();
                    //paramAnonymousDialogInterface.dismiss();
                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(message.equals("M")){
                        message=mes;
                    }
                    new Sender(message).start();
                    Toast.makeText(MainActivity.this,message, Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("取消",null).show();
        }
    }

    private void startPercentMockThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);//1500
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(10);//65
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
                    Thread.sleep(1000);//1500
                    Thread.sleep(50);//65
                    changePercent(85);
                    for (int i = 94; i <= 100; i=i+2) {
                        Thread.sleep(50);//65
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void reload(){
        CloseView();
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
            getSupportActionBar().setTitle("Personalized Settings");
        } else if (id == R.id.nav_manage) {
            reload();
            connect.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Connection");
        } else if (id == R.id.nav_share) {
            CloseView();
            findViewById(R.id.developer).setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Developer");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            conLogout();
        }
        return true;
    }

    public void conLogout()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认退出吗？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                LoginActivity();
            }
        }).setNegativeButton("否", null).create().show();;
    }

    private void LoginActivity()
    {
        finish();
    }

}
