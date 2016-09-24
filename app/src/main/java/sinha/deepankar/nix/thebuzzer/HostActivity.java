package sinha.deepankar.nix.thebuzzer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class HostActivity extends AppCompatActivity {
    private Button button;
    android.os.Handler handler;
    static String msg="";
    List<String> buzzlist=new ArrayList<>();
    List<Long> buzztime=new ArrayList<>();
    ListView lv;
    ServerSocket serverSockets;
    int device_count=0, delivery_count=0;
    boolean back_button_pressed=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        button = (Button) findViewById(R.id.button);
        lv=(ListView)findViewById(R.id.listView);
        button.setText("Activate");
        handler=new android.os.Handler();
        toggleHotspot(true);
        new Thread(new ServerThread()).start();
    }

    @Override
    public void onBackPressed() {
        if(back_button_pressed) {
            super.onBackPressed();
            msg = "close";
            toggleHotspot(false);
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
        back_button_pressed=true;
        Toast.makeText(this,"Press back again to exit",Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                back_button_pressed=false;
            }
        },2000);

    }

    public void toggleHotspot(boolean state) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating HotSpot");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.show();

        WifiManager wifi = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        WifiConfiguration wc = null;
        try {
            if (isAPOn()) {
                wifi.setWifiEnabled(false);
            }
            Method method = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);

            method.invoke(wifi, wc, state);

        } catch (Exception e) {
            Toast.makeText(this,"Can't create hotspot",Toast.LENGTH_LONG).show();
            Log.i("hs",e+"");
        }
        pd.dismiss();
    }

    @Override
    protected void onDestroy() {
        msg="close";
        try {
            serverSockets.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WifiManager wifi = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        try {
            if (isAPOn()) {
                wifi.setWifiEnabled(false);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    public boolean isAPOn() throws NoSuchMethodException {
        WifiManager wifi = (WifiManager) this.getSystemService(this.WIFI_SERVICE);

        Method method = null;
        try {
            method = wifi.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifi);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;

    }


    public void buttonClicked(View view) {
        msg="reset";
        lv.setAdapter(null);
        buzzlist.clear();
    }


    public void setList(String s)
    {
        String teamname=s.substring(0, s.indexOf("|||"));
//        long time=Long.parseLong(s.substring(s.indexOf("|||") + 3, s.length()));
//        int i=0;
//        for(Long t : buzztime)
//        {
//            if(t>time)
//            {
//                break;
//            }
//            i++;
//        }
        buzzlist.add(teamname);
        //buzztime.add(i,time);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,buzzlist);
        lv.setAdapter(adapter);
    }




    class ServerThread implements Runnable {

        @Override
        public void run() {
            serverSockets = null;
            Socket socket=null;

            try {
                serverSockets=new ServerSocket(7894);
            } catch (IOException e) {
                e.printStackTrace();

            }
            while(true)
            {
                Log.i("hs","here");
                try {
                    socket=serverSockets.accept();
                    device_count++;
                    Log.i("hs",device_count+" connected");
                    new Thread(new CommunicationThread(socket)).start();
                    Log.i("hs","accepted");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("hs", e+"");
                }
            }

        }
    }
    class CommunicationThread implements Runnable
    {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        public CommunicationThread(Socket socket){
            this.socket=socket;

            try {
                dis=new DataInputStream(this.socket.getInputStream());
                dos=new DataOutputStream(this.socket.getOutputStream());
                commThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Thread commThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {

                        if (msg.equals("reset")) {
                            Log.i("nn", msg);
                            dos.writeUTF(msg);
                            delivery_count++;
                            //delay();
                            if(delivery_count>=device_count) {
                                msg = "nomsg";
                                delivery_count=0;
                            }
                        }
                        //dos.writeUTF("ping");
//                        if (msg.equals("close")) {
//                            socket.close();
//                            Log.i("ccc", "close");
//                            msg="nomsg";
//                        }
                    }
                }catch(IOException e){
                        device_count=(device_count--)<=0?0:device_count--;
                        Log.i("hs",device_count+" connected");
                    }
            }
        });

//        public void delay(){
//            long it=System.currentTimeMillis();
//            long ft=it;
//            Log.i("delay","waiting");
//            while((ft-it)<2000){
//                ft=System.currentTimeMillis();
//
//            }
//            Log.i("delay","done");
//        }


        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    String str=dis.readUTF();
                    dos.writeUTF("recv");
                    handler.post(new updateUIThread(str));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class updateUIThread implements Runnable {
        private String str;
        public updateUIThread(String str) {
            this.str=str;
        }

        @Override
        public void run() {
            //Toast.makeText(HostActivity.this,str,Toast.LENGTH_LONG).show();
            setList(str);
        }
    }
}