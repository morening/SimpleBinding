package com.morening.android.simplebinding;

import android.app.Activity;
import android.os.Bundle;

import com.morening.annotation.OnClick;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @OnClick(id = R.id.back_btn)
    public void backToMain(){
        finish();
    }
}
