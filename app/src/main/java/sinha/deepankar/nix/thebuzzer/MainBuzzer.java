package sinha.deepankar.nix.thebuzzer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
public class MainBuzzer extends AppCompatActivity {
    private Socket socket;
    private RelativeLayout rl;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String tname;
    boolean ready=false;
    Handler handler;
    MediaPlayer mp;
    AssetFileDescriptor afd=null;
    boolean back_button_pressed=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_buzzer);
        getSupportActionBar().hide();
        rl=(RelativeLayout)findViewById(R.id.background);
        rl.setBackgroundColor(Color.parseColor("#4052b5"));
        tname=Constant.teamName;
        handler=new Handler();
        new Thread(new ClientThread()).start();

        //initializing mediaplayer

        mp=new MediaPlayer();
        try {
            afd=getAssets().openFd("buzz.mp3");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    Thread commThread=new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                long it=0,ft=0;
                dis = new DataInputStream(socket.getInputStream());
                while(true) {

                    //Log.i("ccc","comthread");

                    String str = null;

                    str = dis.readUTF();
                    if(!str.equals("ping"))
                    Log.i("ccc","comthread "+str);
                    //On message delivered successfully
                    if (str.equals("recv")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                rl.setBackgroundColor(Color.parseColor("#FFD33030"));
                                mp.start();

                            }
                        });
                        ready = false;
                        //wait for 5 sec
                        it=System.currentTimeMillis();
                        do{
                            ft=System.currentTimeMillis();
                        }while((ft-it)<3000);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                rl.setBackgroundColor(Color.parseColor("#4052b5"));
                            }
                        });

                    }
                    //On reset request
                    if (str.equals("reset")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                rl.setBackgroundColor(Color.parseColor("#FF398F3D"));
                                try{
                                    mp.reset();
                                    mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    mp.prepare();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        ready = true;
                    }



                    if(getIpAddress()==null ||getIpAddress().isEmpty()) {
                        Toast.makeText(MainBuzzer.this,"Lost connection to host",Toast.LENGTH_LONG).show();
                        finish();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void onBackPressed() {
        if(back_button_pressed) {
            super.onBackPressed();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
        back_button_pressed=true;
        Toast.makeText(this,"Press back again to exit",Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                back_button_pressed = false;
            }
        }, 2000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN && ready)
        try {
            dos=new DataOutputStream(socket.getOutputStream());
            //dis=new DataInputStream(socket.getInputStream());
            String s=tname+"|||"+Long.toString(System.currentTimeMillis());
            dos.writeUTF(s);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public String getIpAddress()//default getway
    {
        WifiManager wm=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d=wm.getDhcpInfo();
        int ip=d.gateway;
        return ((ip & 0xff)+"."+((ip>>>=8)&0xff)+"."+((ip>>>=8)&0xff)+"."+((ip>>>=8)&0xff));//stackoverflow solution
    }
   class ClientThread implements Runnable{

       @Override
       public void run() {
           Looper.prepare();

           try {
               InetAddress serverAddr=InetAddress.getByName(getIpAddress());
               socket=new Socket(serverAddr,7894);
               commThread.start();
           } catch (IOException e) {
               Toast.makeText(MainBuzzer.this,"Connect to host",Toast.LENGTH_LONG).show();
               Log.i("xxxx","err "+e);
               finish();
           }
       }
   }
}
