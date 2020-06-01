package com.xiaoyou.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoyou.bluetooth.Bluetooth.Bluedivice;
import com.xiaoyou.bluetooth.task.BlueAcceptTask;
import com.xiaoyou.bluetooth.task.BlueConnectTask;
import com.xiaoyou.bluetooth.task.BlueReceiveTask;
import com.xiaoyou.bluetooth.util.BluetoothUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;



public class BluetoothsetActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, BlueConnectTask.BlueConnectListener, BlueAcceptTask.BlueAcceptListener {
    private Switch mswitch;
    private ListView mlist;
    private TextView mtvstatue;
    private BluetoothAdapter mbluetooth;
    private static final String TAG="BlueListAdapter";
    private ArrayList<Bluedivice> mdevicelist=new ArrayList<Bluedivice>();
    private String name=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothset);
        mswitch = findViewById(R.id.switch_bluetooth);
        mlist=findViewById(R.id.list);
        mtvstatue=findViewById(R.id.tv_statue);
        mlist.setAdapter(new BlueListAdapter(BluetoothsetActivity.this,mdevicelist));
        if (BluetoothUtil.getBluetoothstatue(this)) {
            mswitch.setChecked(true);
        }
        mswitch.setOnCheckedChangeListener(this);
        mtvstatue.setOnClickListener(this);
        mbluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.tv_statue){
            beginDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "允许本地蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "不允许蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_bluetooth) {
            if (isChecked) {
                beginDiscovery();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, 1);
                // 下面这行代码为服务端需要，客户端不需要
                mhander.postDelayed(mAccept, 1000);
            } else {
                cancelDiscovery();
                BluetoothUtil.setBlueToothStatus(this, false);
                mdevicelist.clear();
                BlueListAdapter adapter = new BlueListAdapter(this, mdevicelist);
                mlist.setAdapter(adapter);
            }
        }
    }
    //这里是新开一个线程(开始广播线程)
    private Runnable mAccept=new Runnable() {
        @Override
        public void run() {
            if (mbluetooth.getState() == BluetoothAdapter.STATE_ON) {
                BlueAcceptTask acceptTask = new BlueAcceptTask(true);
                acceptTask.setBlueAcceptListener(BluetoothsetActivity.this);
                acceptTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mhander.postDelayed(this, 1000);
            }
        }
    };
    private Runnable mRefresh=new Runnable(){
        @Override
        public void run() {
            beginDiscovery();
            mhander.postDelayed(this,2000);
        }
    };

    private void beginDiscovery() {
        if (!mbluetooth.isDiscovering()) {
            mdevicelist.clear();
            BlueListAdapter adapter = new BlueListAdapter(BluetoothsetActivity.this, mdevicelist);
            mlist.setAdapter(adapter);
            mtvstatue.setText("正在搜索蓝牙设备");
            mbluetooth.startDiscovery();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cancelDiscovery();
        Bluedivice item = mdevicelist.get(position);
        BluetoothDevice device = mbluetooth.getRemoteDevice(item.address);
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                Log.d(TAG, "开始配对");
                Boolean result = (Boolean) createBondMethod.invoke(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED &&
                    item.state != BlueListAdapter.CONNECTED) {
                mtvstatue.setText("开始连接");
                BlueConnectTask connectTask = new BlueConnectTask(item.address);
                connectTask.setBlueConnectListener(this);
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mtvstatue.setText("配对异常：" + e.getMessage());
        }
    }
    //向对方发送消息
    public void onInput(String message) {
        Log.d(TAG, "onInput message=" + message);
        Log.d(TAG, "mBlueSocket is " + (mBlueSocket == null ? "null" : "not null"));
        BluetoothUtil.writeOutputStream(mBlueSocket, message);
    }

    private BluetoothSocket mBlueSocket;


    //客户端主动连接
    @Override
    public void onBlueConnect(String address, BluetoothSocket socket) {
        mBlueSocket = socket;
        mtvstatue.setText("连接成功");
        name=address;
        refreshAddress(address);
    }
    //刷新已连接的状态
    private void refreshAddress(String address) {
        for (int i = 0; i < mdevicelist.size(); i++) {
            Bluedivice item = mdevicelist.get(i);
            if (item.address.equals(address)) {
                item.state = BlueListAdapter.CONNECTED;
                mdevicelist.set(i, item);
            }
        }
        BlueListAdapter adapter = new BlueListAdapter(this, mdevicelist);
       mlist.setAdapter(adapter);
    }
    //服务端侦听到连接
    @Override
    public void onBlueAccept(BluetoothSocket socket) {
        Log.d(TAG, "onBlueAccept socket is " + (socket == null ? "null" : "not null"));
        if (socket != null) {
            mBlueSocket = socket;
            BluetoothDevice device = mBlueSocket.getRemoteDevice();
            refreshAddress(device.getAddress());
            BlueReceiveTask receive = new BlueReceiveTask(mBlueSocket, mhander);
            receive.start();
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
            // 获得已经搜索到的蓝牙设备
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Bluedivice item = new Bluedivice(device.getName(), device.getAddress(), device.getBondState() - 10);
                mdevicelist.add(item);
                BlueListAdapter adapter = new BlueListAdapter(BluetoothsetActivity.this, mdevicelist);
                mlist.setAdapter(adapter);
                mlist.setOnItemClickListener(BluetoothsetActivity.this);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mhander.removeCallbacks(mRefresh);
                mtvstatue.setText("蓝牙设备搜索完成");
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    mtvstatue.setText("正在配对" + device.getName());
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mtvstatue.setText("完成配对" + device.getName());
                    mhander.postDelayed(mRefresh, 50);
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    mtvstatue.setText("取消配对" + device.getName());
                }
            }
        }
    }
    /**
     * 重写onRequestPermissionsResult方法
     * 获取动态权限请求的结果,再开启蓝牙
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (BluetoothUtil.getBluetoothstatue(this)) {
                mswitch.setChecked(true);
            }
            mswitch.setOnCheckedChangeListener(this);
            mtvstatue.setOnClickListener(this);
            mbluetooth = BluetoothAdapter.getDefaultAdapter();
            if (mbluetooth== null) {
                Toast.makeText(this, "本机未找到蓝牙功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //接受来自对方的消息
    @SuppressLint("HandlerLeak")
    private Handler mhander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Toast.makeText(BluetoothsetActivity.this,readMessage,Toast.LENGTH_SHORT).show();
            }
        }
    };
    //取消搜索的方法
    private void cancelDiscovery() {
        mhander.removeCallbacks(mRefresh);//
        mtvstatue.setText("取消搜索蓝牙设备");
        if (mbluetooth.isDiscovering()) {
            mbluetooth.cancelDiscovery();
        }
    }
    private BluetoothReceiver blueReceiver;
    @Override
    protected void onStart() {
        super.onStart();
        mhander.postDelayed(mRefresh, 50);
        blueReceiver = new BluetoothReceiver();
        //需要过滤多个动作，则调用IntentFilter对象的addAction添加新动作
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(blueReceiver, foundFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelDiscovery();
        unregisterReceiver(blueReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBlueSocket != null) {
            try {
                mBlueSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SharedPreferences.Editor meditor;
            SharedPreferences msharedPreferences;
            msharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
            meditor=msharedPreferences.edit();
            if(name!=null) {
                meditor.putString("name", name);
            }
            meditor.apply();
            finish();
        }
        return true;
    }

}
