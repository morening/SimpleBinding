# SimpleBinding

Just a simple binding for field and method

[![](https://jitpack.io/v/morening/SimpleBinding.svg)](https://jitpack.io/#morening/SimpleBinding)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)

## How to
1. Add repo and denpendencies in the module's build.gradle like below
```
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    ...
    implementation 'com.github.morening.SimpleBinding:simplebinding:0.0.3'
    implementation 'com.github.morening.SimpleBinding:annotation:0.0.3'
    annotationProcessor 'com.github.morening.SimpleBinding:processor:0.0.3'
}
```

2. Bind/unbind your activity
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    unbinder = SimpleBinding.bind(this);
}

@Override
protected void onDestroy() {
    unbinder.unbind();
    ...
}
```

3. Add annotation with what you want to inject
```java
@BindView(id = R.id.result_tv)
TextView resultTv;

@BindString(resId = R.string.app_name)
String message;

@OnClick(id = {R.id.click_btn0, R.id.click_btn1, R.id.click_btn2})
public void showToast(View view, Object obj){
    Toast.makeText(MainActivity.this, "Clicked "+view.getId(), Toast.LENGTH_LONG).show();
}
```
