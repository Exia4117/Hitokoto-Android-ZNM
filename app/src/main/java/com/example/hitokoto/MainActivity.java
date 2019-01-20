package com.example.hitokoto;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hitokoto.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity implements View.OnClickListener {
    private ImageView mUpdateBtn;
    private ImageView mShareBtn;
    private TextView title;
    protected String Con;
    protected String Fro;
    private TextView Content;
    private TextView from;
    private final int SUCCESS = 1;
    private final int FAILURE = 0;
    private final int ERRORCODE = 2;


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    /**
                     * 获取信息成功后，对该信息进行JSON解析，得到所需要的信息，然后在textView上展示出来。
                     */
                    JSONAnalysis(msg.obj.toString());
                    Toast.makeText(MainActivity.this, "获取数据成功", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case FAILURE:
                    Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case ERRORCODE:
                    Toast.makeText(MainActivity.this, "获取的CODE码不为200！",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mShareBtn = (ImageView)findViewById(R.id.title_share);
        mShareBtn.setOnClickListener(this);
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
        init();
        queryHitokoto();

    }

    private void init() {
        title = (TextView) findViewById(R.id.title_name);
        Content = (TextView) findViewById(R.id.Content);
        from = (TextView) findViewById(R.id.from);
        Content.setText("Hello World");
        from.setText("-Mobile Platform Development Course");
    }

    private void queryHitokoto() {
        final String address = "https://v1.hitokoto.cn/";
        Log.d("url", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                String Content = null;
                String from = null;
                String id = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("Content", str);
                    }
                    String responseStr = response.toString();
                    Log.d("Content", responseStr);
                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = SUCCESS;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


    protected void JSONAnalysis(String string) {
        try {
            //这里的text就是上边获取到的数据，一个String.
            JSONObject jsonObject = new JSONObject(string);
            // getJSONArray中的参数就是你想要分析的JSON的键
            String hito,fro;
            hito = jsonObject.getString("hitokoto");
            fro = jsonObject.getString("from");

            Log.v("out---------------->", hito);

            Content.setText("『" +hito +"』");
            from.setText("-「" + fro + "」");
            Con = hito;
            Fro = fro;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("hi", "网络OK");
                queryHitokoto();
            } else {
                Log.d("hi", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
        else if (view.getId() == R.id.title_share){
            Context context = this;
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            //创建ClipData对象
            ClipData clipData = ClipData.newPlainText("simple text copy", Con);
            //添加ClipData对象到剪切板中
            clipboardManager.setPrimaryClip(clipData);

        }
    }
}
