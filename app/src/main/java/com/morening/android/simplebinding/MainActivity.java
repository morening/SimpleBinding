package com.morening.android.simplebinding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.morening.android.annotation.BindView;
import com.morening.android.annotation.OnClick;

public class MainActivity extends Activity {

    @BindView(id = R.id.result_tv)
    TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleBinding.bind(this);
    }


    @OnClick(id = R.id.click_btn)
    public void onClick(View view){
        Toast.makeText(MainActivity.this, "Clicked "+view.getId(), Toast.LENGTH_LONG).show();
    }
}
