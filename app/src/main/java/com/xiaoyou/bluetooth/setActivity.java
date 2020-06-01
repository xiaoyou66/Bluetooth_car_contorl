package com.xiaoyou.bluetooth;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class setActivity extends AppCompatActivity {
    private EditText mets,metf,metb,metl,metr,mets2;
    private Button msave;
    private SharedPreferences.Editor meditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        mets=findViewById(R.id.et_s);
        metf=findViewById(R.id.et_f);
        metb=findViewById(R.id.et_b);
        metl=findViewById(R.id.et_l);
        metr=findViewById(R.id.et_r);
        mets2=findViewById(R.id.et_s2);
        msave=findViewById(R.id.btn_save);
        //实例化创建对象
        SharedPreferences msharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        meditor= msharedPreferences.edit();
        metb.setText(msharedPreferences.getString("back","1"));
        metf.setText(msharedPreferences.getString("front","0"));
        metl.setText(msharedPreferences.getString("left","2"));
        metr.setText(msharedPreferences.getString("right","3"));
        mets.setText(msharedPreferences.getString("stop","4"));
        mets2.setText(msharedPreferences.getString("stop2","5"));
        msave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meditor.putString("back",metb.getText().toString());
                meditor.putString("front",metf.getText().toString());
                meditor.putString("left",metl.getText().toString());
                meditor.putString("right",metr.getText().toString());
                meditor.putString("stop",mets.getText().toString());
                meditor.putString("stop2",mets2.getText().toString());
                meditor.apply();
                Toast.makeText(setActivity.this,"保存成功！",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
