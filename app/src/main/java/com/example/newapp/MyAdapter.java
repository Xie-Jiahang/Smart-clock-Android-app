package com.example.newapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;

public class MyAdapter extends BaseAdapter {

    Socket socket = null;//开辟一个socket控件
    OutputStream outputstream;
    InputStream in;
    boolean isConnected = false;

    //上下文
    private Context context;
    private EditText ed1;
    private EditText ed2;

    //数据项
    private List<User> data;
    public ArrayList<Boolean> lDropDown;

    public MyAdapter(List<User> data) {
        this.data = data;

        // 初始状态，所有都不显示下拉
        lDropDown = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            lDropDown.add(false);
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {//i与位置对应
        ViewHolder viewHolder = null;
        if (!isConnected)//没有连接上
            new ClientThread().start();//打开连接
        if (context == null)
            context = viewGroup.getContext();
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.unkown, null);//R.layout.item
            viewHolder = new ViewHolder();

            SharedPreferences sp3 = context.getSharedPreferences("arrangement1", 0);
            SharedPreferences sp4 = context.getSharedPreferences("arrangement2", 0);

            String a3 = sp3.getString("a1", "");//取出前一个的值;
            String a4 = sp4.getString("a2", "");//取出前一个的值;

            ed1 = (EditText) view.findViewById(R.id.edittext1);
            ed2 = (EditText) view.findViewById(R.id.edittext2);

            ed1.setText(a3);
            ed2.setText(a4);

            viewHolder.mTv = (TextView) view.findViewById(R.id.Textviewname);
            viewHolder.mBtn = (ImageView) view.findViewById(R.id.Textviewstate);
            view.setTag(viewHolder);

            viewHolder.rlTop = (LinearLayout) view.findViewById(R.id.rl_top_bar);
            viewHolder.llBottom = (LinearLayout) view.findViewById(R.id.ll_app_expand);
//            viewHolder.llBottom_extra1=(LinearLayout)view.findViewById(R.id.ll_app_expand_extra1);
//            viewHolder.llBottom_extra2=(LinearLayout)view.findViewById(R.id.ll_app_expand_extra2);
            viewHolder.llBottom_extra3 = (RelativeLayout) view.findViewById(R.id.ll_app_expand_extra3);
            viewHolder.llBottom_extra4 = (RelativeLayout) view.findViewById(R.id.ll_app_expand_extra4);

            viewHolder.tv = (TextView) view.findViewById(R.id.tv);
            viewHolder.tv1 = (TextView) view.findViewById(R.id.Textviewname);
            viewHolder.epvH = (EasyPickerView) view.findViewById(R.id.epv_h);
            viewHolder.epvM = (EasyPickerView) view.findViewById(R.id.epv_m);
            viewHolder.initHours();
            viewHolder.initMinutes();
        }
        //获取viewHolder实例
        viewHolder = (ViewHolder) view.getTag();
        //设置数据
        viewHolder.mTv.setText(data.get(i).getName());
        viewHolder.mTv.setTag(i);
        //设置监听事件
        //viewHolder.mTv.setOnClickListener(this);

        //设置数据
        viewHolder.mBtn.setImageResource(R.drawable.btnclose);

        SharedPreferences sp = context.getSharedPreferences("alarm_state", 0);
        SharedPreferences.Editor editor = sp.edit();
        int st1 = sp.getInt("" + i, -1);
        data.get(i).state = st1;
        if (st1 == -1) {
            st1 = data.get(i).state;
            editor.putInt("" + i, st1);
            editor.commit();
        }
        if (st1 == 0)
            viewHolder.mBtn.setImageResource(R.drawable.btnclose);
        else
            viewHolder.mBtn.setImageResource(R.drawable.btnopen);

        viewHolder.mBtn.setOnClickListener(new alarm_state());
        viewHolder.mBtn.setTag(i);

        TextWatcher watcher1 = new TextWatcher() {
            SharedPreferences sp3 = context.getSharedPreferences("arrangement1", 0);
            SharedPreferences.Editor editor3 = sp3.edit();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable e1) {
                editor3.putString("a1", "" + e1);
                editor3.commit();
            }
        };
        ed1.addTextChangedListener(watcher1);

        TextWatcher watcher2 = new TextWatcher() {
            SharedPreferences sp4 = context.getSharedPreferences("arrangement2", 0);
            SharedPreferences.Editor editor4 = sp4.edit();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable e2) {
                editor4.putString("a2", "" + e2);
                editor4.commit();
            }
        };
        ed2.addTextChangedListener(watcher2);

        if (lDropDown.get(i) && i != 2 && i != 3 && i != 4) {
            if (data.get(i).state == 1) {
                viewHolder.llBottom.setVisibility(View.VISIBLE);    // 显示下拉内容
                if (i == 0)
                    viewHolder.llBottom_extra3.setVisibility(View.VISIBLE);
                else viewHolder.llBottom_extra4.setVisibility(View.VISIBLE);
            }

        } else {
            viewHolder.llBottom.setVisibility(View.GONE);        // 隐藏下拉内容
//            viewHolder.llBottom_extra1.setVisibility(View.GONE);		// 隐藏下拉内容
//            viewHolder.llBottom_extra2.setVisibility(View.GONE);		// 隐藏下拉内容
            viewHolder.llBottom_extra3.setVisibility(View.GONE);
            viewHolder.llBottom_extra4.setVisibility(View.GONE);
        }
        // 顶部控件组响应点击操作，用于显示/隐藏底部控件组（下拉内容）

        viewHolder.rlTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bFlagTemp = lDropDown.get(i);
                for (int i = 0; i < data.size(); i++) {
                    lDropDown.set(i, false);
                }
                lDropDown.set(i, !bFlagTemp);
                notifyDataSetChanged();
            }
        });
        viewHolder.rlTop.setOnLongClickListener(new View.OnLongClickListener() {
            public String message = "0000000";
            int[] res = {0, 0, 0, 0, 0, 0, 0};
            boolean[] selected = new boolean[]{false, false, false, false, false, false, false};
            String mes = "0000000";
            String str;

            SharedPreferences sp1 = context.getSharedPreferences("alarm_week1", 0);
            SharedPreferences sp2 = context.getSharedPreferences("alarm_week2", 0);
            SharedPreferences sp3 = context.getSharedPreferences("arrangement1", 0);
            SharedPreferences sp4 = context.getSharedPreferences("arrangement2", 0);

            SharedPreferences.Editor editor1 = sp1.edit();
            SharedPreferences.Editor editor2 = sp2.edit();

            @Override
            public boolean onLongClick(View view) {
                if(i==0 || i ==1){
                    if(data.get(i).state==1){
                        if(i==0){
                            mes = sp1.getString("res1", "");//取出前一个的值
                            if (mes.length() == 0)
                                mes = "D0000000";
                            message = "D";
                        } else {
                            mes = sp2.getString("res2", "");//取出前一个的值
                            if (mes.length() == 0)
                                mes = "E0000000";
                            message = "E";
                        }
                        String a3 = sp3.getString("a1", "");//取出前一个的值;
                        String a4 = sp4.getString("a2", "");//取出前一个的值;
                        for (int i = 0; i < selected.length; i++) {
                            if (mes.substring(i + 1, i + 2).equals("0"))
                                selected[i] = false;
                            else selected[i] = true;
                        }
                        final String[] items = new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(i == 0 ? a3 : a4)//position==0?a3:a4
                                .setNegativeButton("退出", null).setPositiveButton("确认", null)
                                .setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {//长按未点击多选框不进入每次点击多选框进入
                                        if (i==0){
                                            message="D";
                                        }
                                        else message="E";
                                        if (isChecked) {
                                            res[which] = 1;
                                            selected[which] = true;
                                        } else {
                                            res[which] = 0;
                                            selected[which] = false;
                                        }
                                        for (int i = 0; i < res.length; i++) {
                                            if (selected[i] == true)
                                                res[i] = 1;
                                            else res[i] = 0;
                                            message += res[i];
                                        }
                                        if (i==0) {
                                            editor1.putString("res1", message);//将前一个的值改为后一个
                                            editor1.commit();//editor.commit();
                                        } else {
                                            editor2.putString("res2", message);//将前一个的值改为后一个
                                            editor2.commit();//editor.commit();
                                        }
                                        if (data.get(i).state == 1) {
//                                        new Sender(message).start();
                                            Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).create();
                        dialog.show();
                    }
                }
                return true;
            }
        });

        return view;
    }

    public class alarm_state implements View.OnClickListener {
        String message = "OFF";
        SharedPreferences sp = context.getSharedPreferences("alarm_state", 0);
        SharedPreferences.Editor editor = sp.edit();

        @Override
        public void onClick(View view) {
            int id = view.getId();
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTv = (TextView) view.findViewById(R.id.Textviewname);
            viewHolder.mBtn = (ImageView) view.findViewById(R.id.Textviewstate);

            switch (id) {
                case R.id.Textviewstate:
                    int i = (Integer) viewHolder.mBtn.getTag();
                    if (data.get(i).state == 0) {
                        data.get(i).state = 1;
                        viewHolder.mBtn.setImageResource(R.drawable.btnopen);
                        if (i == 0)
                            message = "AON";
                        else if (i == 1) message = "BON";
                        else if (i == 2) message = "FON";//整点报时
                        else if (i == 3) message = "GON";//每日播报
                        else message = "KON";//开机音乐
                        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                        new Sender(message).start();

                        editor.putInt("" + i, data.get(i).state);
                        editor.commit();
                    } else {
                        data.get(i).state = 0;
                        viewHolder.mBtn.setImageResource(R.drawable.btnclose);
                        if (i == 0)
                            message = "AOFF";
                        else if (i == 1) message = "BOFF";
                        else if (i == 2) message = "FOFF";
                        else if (i == 3) message = "GOFF";
                        else message = "KOFF";
                        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                        new Sender(message).start();

                        editor.putInt("" + i, data.get(i).state);
                        editor.commit();
                    }
                    break;
            }
        }
    }

    class ViewHolder {
        int hour;
        int minute;
        TextView tv;
        TextView tv1;
        EasyPickerView epvH;
        EasyPickerView epvM;

        TextView mTv;
        ImageView mBtn;
        public LinearLayout rlTop;
        public LinearLayout llBottom;
        //        public LinearLayout llBottom_extra1;
//        public LinearLayout llBottom_extra2;
        public RelativeLayout llBottom_extra3;
        public RelativeLayout llBottom_extra4;
        String message = "0000";

        public void edit_time() {
            message = "";
            if (hour < 10)
                message = "0" + hour;
            else
                message = "" + hour;

            if (minute < 10)
                message = message + "0" + minute;
            else
                message = message + "" + minute;
        }

        private void initHours() {

            final ArrayList<String> hDataList = new ArrayList<>();
            for (int i = 0; i < 24; i++)
                hDataList.add("" + i);

            epvH.setDataList(hDataList);
            epvH.setOnScrollChangedListener(new EasyPickerView.OnScrollChangedListener() {
                @Override
                public void onScrollChanged(int curIndex) {
                    hour = Integer.parseInt(hDataList.get(curIndex));
                    tv.setText(hour + "h" + minute + "m");
                }

                @Override
                public void onScrollFinished(int curIndex) {
                    hour = Integer.parseInt(hDataList.get(curIndex));
                    tv.setText(hour + "h" + minute + "m");
                    edit_time();
                    if (tv1.getText().toString().equals("闹钟1"))
                        message = "A" + message;
                    else message = "B" + message;
                    new Sender(message).start();
                    Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void initMinutes() {

            final ArrayList<String> dataList2 = new ArrayList<>();
            for (int i = 0; i < 60; i++)
                dataList2.add("" + i);

            epvM.setDataList(dataList2);
            epvM.setOnScrollChangedListener(new EasyPickerView.OnScrollChangedListener() {
                @Override
                public void onScrollChanged(int curIndex) {
                    minute = Integer.parseInt(dataList2.get(curIndex));
                    tv.setText(hour + "h" + minute + "m");
                }

                @Override
                public void onScrollFinished(int curIndex) {
                    minute = Integer.parseInt(dataList2.get(curIndex));
                    tv.setText(hour + "h" + minute + "m");
                    edit_time();
                    if (tv1.getText().toString().equals("闹钟1"))
                        message = "A" + message;
                    else message = "B" + message;
                    new Sender(message).start();
                    Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    Handler mHandler = new Handler();  //等待socket连接成功

    public class ClientThread extends Thread {
        public void run() {
            //	 Looper.prepare();
            try {
                socket = new Socket("192.168.4.1", Integer.parseInt("8888"));//建立好连接之后，就可以进行数据通讯了
                isConnected = true;
                in = socket.getInputStream();
                //得到一个消息对象，Message类是有Android操作系统提供
                Message msg = mHandler.obtainMessage();
                //msg.what=1;
                mHandler.sendMessage(msg);
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
        public void run() {
            while (true) {
                if (socket != null) {
                    String result = readFromInputStream(in);
                    try {
                        if (!result.equals("")) {
                            Message msg = new Message();
                            msg.what = 2;
                            Bundle data = new Bundle();
                            data.putString("msg", result);
                            msg.setData(data);
                            mHandler.sendMessage(msg);
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

    class Sender extends Thread {
        String serverIp;
        String message;

        Sender(String message) {
            super();
            //serverIp = serverAddress;
            this.message = message;
        }

        public void run() {
            PrintWriter out;
            try {
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
            } catch (Exception e) {
                //LogUtil.e("发送错误:[Sender]run()" + e.getMessage());
            }
        }
    }


}
