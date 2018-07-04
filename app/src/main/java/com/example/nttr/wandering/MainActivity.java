package com.example.nttr.wandering;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String HIGH_SCORE = "hi-score";

    private int intHighScore = 0;   // ハイスコア

    //171227: 端末に永続的にデータを保存するためのクラス
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ④ SharedPreferencesを取得する
        // MODE_PRIVATEにすると、このアプリからしかアクセスできない
        // ゲームオーバー時にハイスコアが更新されない
        // http://to-developer.com/blog/?p=1139
        sharedPreferences = getSharedPreferences("Wandering", MODE_PRIVATE | MODE_MULTI_PROCESS);

        // フォント変更
        Button bt;
        bt = (Button) findViewById(R.id.btnStart);
        bt.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));
        TextView tv;
        tv = (TextView) findViewById(R.id.textExplain);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));

        // スタートボタンの定義
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(v);
            }
        });

        // ハイスコアの表示
        loadHighScoreData();

    }

    //171213: Gachaから流用
    // https://qiita.com/Reyurnible/private/4a2d3d203ccecf83259d
    public void click(View view) {
        // 画面の遷移にはIntentというクラスを使用します。
        // Intentは、Android内でActivity同士やアプリ間の通信を行う際の通信内容を記述するクラスです。
        Intent intent = new Intent(this, SecondActivity.class);
        // startActivityで、Intentの内容を発行します。
        startActivity(intent);

        // ハイスコアの更新
        loadHighScoreData();
    }

    //onActivityResult に書くこと

    // 整数（ハイスコア）を取得するメソッド
    void loadHighScoreData() {
//        // Setという形でデータを保存したので、Setで取得する
//        Set<String> data = sharedPreferences.getStringSet("hi-score", new HashSet<String>());
//        // adapterには、addAllという関数で配列や、Setなどのデータの集まりを渡すことができる
//        adapter.addAll(data);

        // 保存されたハイスコアを読込
        intHighScore = sharedPreferences.getInt(HIGH_SCORE, 0);

        // UIを更新
        TextView textScore = (TextView) findViewById(R.id.textScore2);
        textScore.setText(String.format(Locale.JAPAN,getString(R.string.format_high_score), intHighScore));

    }

}

// タイトル非表示
// https://youkey.jimdo.com/android開発に役立つ小ネタ/タイトルの非表示設定/
// →うまくいかない。アクティビティ右上のAppThemeをNoTitleに変えると消える？消えない？

// GridLayout
// https://techacademy.jp/magazine/4446
