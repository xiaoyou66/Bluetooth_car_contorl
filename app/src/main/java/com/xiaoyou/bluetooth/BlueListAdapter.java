package com.xiaoyou.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoyou.bluetooth.Bluetooth.Bluedivice;

import java.util.ArrayList;

public class BlueListAdapter extends BaseAdapter {
    private static final String TAG="BlueListAdapter";
    private Context mcontext;
    private LayoutInflater mlayout;
    private ArrayList<Bluedivice> mbluelist;
    private String[] mstatearray={"未绑定","绑定中","已绑定","已连接"};
    public static int CONNECTED=3;
    public BlueListAdapter(Context context,ArrayList<Bluedivice> blue_list){
        this.mcontext=context;
        mbluelist=blue_list;
        mlayout=LayoutInflater.from(context);
    }//这个是把蓝牙的内容和蓝牙列表传进去
    @Override
    public int getCount() {
        return mbluelist.size();
    }

    @Override
    public Object getItem(int position) {
        return mbluelist.get(position);//获取你点击的位置
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    static class viewhoder{
        public TextView lvblue,lvmac,lvstatue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewhoder holder=null;
        if(convertView==null){//这里是实例化一个类，把类和控件联系起来
            holder=new viewhoder();
            convertView=mlayout.inflate(R.layout.mylistview,null);
            holder.lvblue=convertView.findViewById(R.id.tv_blue);
            holder.lvmac=convertView.findViewById(R.id.tv_mac);
            holder.lvstatue=convertView.findViewById(R.id.tv_statue);
            convertView.setTag(holder);
        }else{
            holder= (viewhoder) convertView.getTag();//强制类型转换
        }
        final Bluedivice device =mbluelist.get(position);
        //给控件赋值
        holder.lvblue.setText(device.name);
        holder.lvmac.setText(device.address);
        holder.lvstatue.setText(mstatearray[device.state]);
        return convertView;
    }

    private class LENGTH_SHORT0 {
    }
}
