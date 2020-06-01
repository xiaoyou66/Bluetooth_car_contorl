package com.xiaoyou.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoyou.bluetooth.Bluetooth.Bluedivice;
import com.xiaoyou.bluetooth.task.BlueAcceptTask;
import com.xiaoyou.bluetooth.task.BlueConnectTask;
import com.xiaoyou.bluetooth.task.BlueReceiveTask;
import com.xiaoyou.bluetooth.util.BluetoothUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import static android.graphics.Color.parseColor;
import static com.xiaoyou.bluetooth.function.commom.setStatusBarColor;

public class MainActivity extends AppCompatActivity implements BlueConnectTask.BlueConnectListener {
    private BluetoothAdapter mbluetooth;
    private ImageButton mbtnset,mbtnblue,mbtnup,mbtndown,mbtnleft,mbtnright;
    private TextView mbulemessage;
    public String name="";
    public String[] data={"0","1","2","3","4","5"};
    private static final String TAG="BlueListAdapter";
    private BluetoothSocket mBlueSocket;
    private boolean stand=false;
    @SuppressLint({"ClickableViewAccessibility", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取控件
        mbtnblue=findViewById(R.id.btn_bluetooth);
        mbtnset=findViewById(R.id.btn_set);
        mbulemessage=findViewById(R.id.tv_message);
        mbtnup=findViewById(R.id.btn_up);
        mbtndown=findViewById(R.id.btn_down);
        mbtnleft=findViewById(R.id.btn_left);
        mbtnright=findViewById(R.id.btn_right);
        Onclick onclick=new Onclick();
        mbtnup.setOnTouchListener(onclick);
        mbtndown.setOnTouchListener(onclick);
        mbtnleft.setOnTouchListener(onclick);
        mbtnright.setOnTouchListener(onclick);
        //设置监听事件
        mbtnset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect();
              Intent intent=new Intent(MainActivity.this,setActivity.class);
              startActivity(intent);
            }
        });
        mbtnblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect();
                Intent intent=new Intent(MainActivity.this,BluetoothsetActivity.class);
                startActivity(intent);
            }
        });
        mbulemessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始连接已有设备
                if(name!=null && stand ){
                        Connect();
                }


            }
        });
        //设置状态栏颜色
        setStatusBarColor(this,parseColor("#44cef6"));
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mbluetooth= BluetoothAdapter.getDefaultAdapter();//这里是启用一个默认的蓝牙适配器
        //1.先动态申请权限
       bluetoothpermissions();
        if(mbluetooth==null){
            Toast.makeText(this,"本机未找到蓝牙功能!",Toast.LENGTH_SHORT).show();
            finish();
        }
        mbluetooth= BluetoothAdapter.getDefaultAdapter();
    }
    private void bluetoothpermissions(){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            //动态申请权限成功（上面是判断你这个是不是已经申请过了，没有就再申请一次）
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1);

        }
    }
class Onclick implements View.OnTouchListener{
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.btn_up:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    onInput(data[0]);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    onInput(data[4]);
                }
                break;
            case R.id.btn_down:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    onInput(data[1]);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    onInput(data[4]);
                }
                break;
            case R.id.btn_left:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    onInput(data[2]);
                }else if(event.getAction()==MotionEvent.ACTION_UP) {
                    onInput(data[5]);
                }
                break;
            case R.id.btn_right:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    onInput(data[3]);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    onInput(data[5]);
                }
                break;
        }
        return false;
    }
}


    @Override
    protected void onResume() {
        super.onResume();
        //2.获取蓝牙状态
        if(!BluetoothUtil.getBluetoothstatue(this)){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("提示").setMessage("蓝牙未开启，是否需要打开？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent=new Intent(MainActivity.this,BluetoothsetActivity.class);
                    startActivity(intent);
                }
            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
        stand=true;
        SharedPreferences msharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        name=msharedPreferences.getString("name","");
        if(name.equals("")){
            mbulemessage.setText("你还没有蓝牙连接记录，请先连接蓝牙！");
        }else{
            mbulemessage.setText("正在连接上一次蓝牙记录....");
        }
        data[0]=msharedPreferences.getString("front","0");
        data[1]=msharedPreferences.getString("back","1");
        data[2]=msharedPreferences.getString("left","2");
        data[3]=msharedPreferences.getString("right","3");
        data[4]=msharedPreferences.getString("stop","4");
        data[5]=msharedPreferences.getString("stop2","5");
        if(!name.equals("") && stand ){
            Connect();
        }
    }

    private void Connect(){
        mbluetooth= BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mbluetooth.getRemoteDevice(name);
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                Boolean result = (Boolean) createBondMethod.invoke(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                mbulemessage.setText("正在连接蓝牙...");
                BlueConnectTask connectTask = new BlueConnectTask(name);
                connectTask.setBlueConnectListener(this);
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mbulemessage.setText("配对异常：" + e.getMessage());
        }
    }
    private void disConnect(){
        if (mBlueSocket != null) {
            try {
                mBlueSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //客户端主动连接
    @Override
    public void onBlueConnect(String address, BluetoothSocket socket) {
        mBlueSocket = socket;
        mbulemessage.setText("连接成功!");
    }
    //向对方发送消息
    public void onInput(String message) {
        Log.d(TAG, "onInput message=" + message);
        Log.d(TAG, "mBlueSocket is " + (mBlueSocket == null ? "null" : "not null"));
        BluetoothUtil.writeOutputStream(mBlueSocket, message);
    }
//    //服务端侦听到连接
//    @Override
//    public void onBlueAccept(BluetoothSocket socket) {
//        Log.d(TAG, "onBlueAccept socket is " + (socket == null ? "null" : "not null"));
//        if (socket != null) {
//            mBlueSocket = socket;
//            BlueReceiveTask receive = new BlueReceiveTask(mBlueSocket, handler);
//            receive.start();
//        }
//    }
    //接受来自对方的消息

//    @SuppressLint("HandlerLeak")
//    private Handler handler=new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == 0) {
//                byte[] readBuf = (byte[]) msg.obj;
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                mbulemessage.setText("接受到消息:"+readMessage);
//                Toast.makeText(MainActivity.this,readMessage,Toast.LENGTH_SHORT).show();
//            }
//        }
//    };


}
