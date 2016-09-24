package sinha.deepankar.nix.thebuzzer;

import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LaunchScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        getSupportActionBar().hide();

    }


    public void host(View view) {
        Intent intent= new Intent(this,HostActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pull_in_left,R.anim.pull_out_right);
    }

    public void join(View view) {
        Intent intent= new Intent(this,InputActivity.class);
        startActivity(intent);
        WifiManager wifi = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        overridePendingTransition(R.anim.pull_in_right,R.anim.pull_out_left);
    }
}
