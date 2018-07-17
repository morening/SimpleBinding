package com.morening.android.simplebinding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.morening.android.annotation.OnClick;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MainActivity_SimpleBinding(this, this.getWindow().getDecorView());
    }


    @OnClick(viewId = R.id.click_btn)
    public void onClick(View view){
        Toast.makeText(MainActivity.this, "Clicked "+view.getId(), Toast.LENGTH_LONG).show();
    }
}
