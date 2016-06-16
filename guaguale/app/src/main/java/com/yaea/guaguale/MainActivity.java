package com.yaea.guaguale;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private GuaGuaLe ggl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ggl = (GuaGuaLe) findViewById(R.id.ggl);
        ggl.setListener(new GuaGuaLe.CompleteListener() {
            @Override
            public void complete() {
                Log.d("TAG", "complete");
            }
        });
    }
}
