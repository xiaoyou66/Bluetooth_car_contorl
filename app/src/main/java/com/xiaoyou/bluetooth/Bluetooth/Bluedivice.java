package com.xiaoyou.bluetooth.Bluetooth;

public class Bluedivice {
    public String name;
    public String address;
    public int state;

    public Bluedivice(String name, String address, int i) {
        this.name = name;
        this.address = address;
        this.state = i;
    }


    public void  BlueDevice() {
        name = "";
        address = "";
        state = 0;
    }

//    public void BlueDevice(String name, String address, int state) {
//        this.name = name;
//        this.address = address;
//        this.state = state;
//    }
}
