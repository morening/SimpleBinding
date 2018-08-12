package com.morening.android.simplebinding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.morening.annotation.BindString;
import com.morening.annotation.BindView;
import com.morening.annotation.OnClick;
import com.morening.simplebinding.SimpleBinding;
import com.morening.simplebinding.Unbinder;

public class MainActivity extends Activity {

    private Unbinder unbinder = null;

    @BindView(id = R.id.result_tv)
    TextView resultTv;

    @BindString(value = "Hello SimpleBinding~")
    String result;

    @BindString(resId = R.string.app_name)
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = SimpleBinding.bind(this);
    }


    @OnClick(id = {R.id.click_btn0, R.id.click_btn1, R.id.click_btn2})
    public void showToast(View view, Object obj){
        Toast.makeText(MainActivity.this, "Clicked "+view.getId(), Toast.LENGTH_LONG).show();
    }

    @OnClick(id = R.id.result_tv)
    public void changeText(View view){
        resultTv.setText(result);
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
