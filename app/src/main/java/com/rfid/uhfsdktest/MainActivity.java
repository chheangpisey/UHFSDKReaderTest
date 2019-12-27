package com.rfid.uhfsdktest;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;
    ArrayList<MyListData> myListData;
    MyListData[] myListData1;
    RecyclerView recyclerView;
    TextView rfSize;
    Button btn;
    MyListAdapter mAdapter;
    Handler mHandler;

    public boolean findDTByName(String name) {
        for(MyListData mld : myListData) {
            if(mld.getDescription().trim().toLowerCase().equals(name.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    RXObserver rxObserver = new RXObserver(){
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            Log.d("TAG", tag.strEPC);
            MyListData  mld = new MyListData(tag.strEPC);
            //findDTByName(tag.strCRC);
            if(!findDTByName(tag.strEPC)){
                myListData.add(new MyListData(tag.strEPC));
            }
        }
        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            Log.d("TAGEnd", myListData.size()+"");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn.performClick();
                }
            }, 1000);


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        myListData = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        rfSize = (TextView) findViewById(R.id.countV);
        btn = (Button) findViewById(R.id.btn_d);

        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Log.d("TAGMYPerform", "Yes");
                rfSize.setText("Total RFID: "+ myListData.size());
                recyclerView.setHasFixedSize(true);
                // use a linear layout manager
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                mAdapter = new MyListAdapter(myListData);
                recyclerView.setAdapter(mAdapter);
            }
        });

        if (connector.connectCom("dev/ttyS4",115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
            } catch (Exception e) {
                Log.d("ExceptionError:", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReader != null) {
            mReader.unRegisterObserver(rxObserver);
        }
        if (connector != null) {
            connector.disConnect();
        }

        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }
}
