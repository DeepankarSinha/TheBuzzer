package sinha.deepankar.nix.thebuzzer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class InputActivity extends AppCompatActivity {
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        et=(EditText)findViewById(R.id.teamname);
    }

    public void proceed(View view) {
        if(et.getText().toString().isEmpty())
        {
            Toast.makeText(this,"Team name empty",Toast.LENGTH_LONG).show();
        }
        else
        {
            Constant.teamName=et.getText().toString();
            Intent intent= new Intent(this,MainBuzzer.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.pull_in_right,R.anim.pull_out_left);
        }
    }
}
