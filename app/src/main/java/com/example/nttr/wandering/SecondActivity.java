package com.example.nttr.wandering;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class SecondActivity extends AppCompatActivity {

    final int FLAG_NONE = 0;
    final int FLAG_GOAL = 1;
    //final int MOVE_APPENDIX = 10;   //171219: iSizeColを元に計算するよう変更したため、未使用
    final String SEPARATOR = "========================";

    private String crlf;            // 改行
//    private String strPromptDebug = "DBG>> ";   // 複数行テキストのプロンプト（行頭文字列） デバッグ用

    private int intStageNo = 0;     // ステージ番号。initStage()を呼ぶ度にインクリメント
    private int[][] aryStageMap;    // ステージのマップ（サイズは動的に変化、initStage()で初期化）
    // デフォルト：FLAG_NONE=0、ゴール：FLAG_GOAL=1

    private int intSizeRow = 0;     // ステージの縦の長さ
    private int intSizeCol = 0;     // ステージの横の長さ

    private int intGoalRow = 0;     // ゴールの位置（縦）
    private int intGoalCol = 0;     // ゴールの位置（横）

    private int intCurRow = 0;      // 現在位置（縦）
    private int intCurCol = 0;      // 現在位置（横）

    final String HIGH_SCORE = "hi-score";

    //private int intMoveMax = 0;     // 残り移動可能数の初期値。未使用
    private int intMoveLeft  = 0;    // 残り移動可能数
    private int intScore     = 0;    // スコア(20億までカウント可能)
    private int intHighScore = 0;   // ハイスコア

    //171227: 端末に永続的にデータを保存するためのクラス
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        crlf = System.getProperty("line.separator");    // 改行の取得

        // ④ SharedPreferencesを取得する
        // MODE_PRIVATEにすると、このアプリからしかアクセスできない
        // ゲームオーバー時にハイスコアが更新されない
        // http://to-developer.com/blog/?p=1139
        sharedPreferences = getSharedPreferences("Wandering", MODE_PRIVATE | MODE_MULTI_PROCESS);

        // フォント変更
        TextView tv;
        tv = (TextView) findViewById(R.id.textScore);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));
        tv = (TextView) findViewById(R.id.textStageInfo);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));
        tv = (TextView) findViewById(R.id.textMoveLeft);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));
        tv = (TextView) findViewById(R.id.textMultiLine);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_Font_Name)));

        // 上ボタン
        ImageButton btnUp = (ImageButton) findViewById(R.id.btnUp);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 区切り行の出力
                addTextLine(SEPARATOR);

                // 操作内容の出力
                addTextLine("（操作）上へ・・・");

                // 移動できない場合
                if (intCurRow<=0) {
                    addTextLine("上端です。これ以上は、上に行けません。");

                // 移動後の処理
                } else {
                    intCurRow--;
                    // 移動可能数のデクリメント
                    intMoveLeft--;
                }
                checkAfterMove();
            }
        });

        // 下ボタン
        ImageButton btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 区切り行の出力
                addTextLine(SEPARATOR);

                // 操作内容の出力
                addTextLine("（操作）下へ・・・");

                // 移動できない場合
                if (intCurRow>=(intSizeRow-1)) {
                    addTextLine("下端です。これ以上は、下に行けません。");

                // 移動後の処理
                } else {
                    intCurRow++;
                    // 移動可能数のデクリメント
                    intMoveLeft--;
                }
                checkAfterMove();
            }
        });

        // 左ボタン
        ImageButton btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 区切り行の出力
                addTextLine(SEPARATOR);

                // 操作内容の出力
                addTextLine("（操作）左へ・・・");

                // 移動できない場合
                if (intCurCol<=0) {
                    addTextLine("左端です。これ以上は、左に行けません。");

                // 移動後の処理
                } else {
                    intCurCol--;
                    // 移動可能数のデクリメント
                    intMoveLeft--;
                }
                checkAfterMove();
            }
        });

        // 右ボタン
        ImageButton btnRight = (ImageButton) findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 区切り行の出力
                addTextLine(SEPARATOR);

                // 操作内容の出力
                addTextLine("（操作）右へ・・・");

                // 移動できない場合
                if (intCurCol>=(intSizeCol-1)) {
                    addTextLine("右端です。これ以上は、右に行けません。");

                // 移動後の処理
                } else {
                    intCurCol++;
                    // 移動可能数のデクリメント
                    intMoveLeft--;
                }
                checkAfterMove();
            }
        });

        // ハイスコアの読込
        loadHighScoreData();

        // ステージ初期化
        initStage();

    }

    // textMultiLine にテキスト追記
    void addTextLine(String strAdd) {
        addTextLineWithPrompt(strAdd, "");
    }

    // textMultiLine にテキスト追記（プロンプト書き換え可能）
    void addTextLineWithPrompt(String strAdd, String strPrompt2) {

        // テキスト追記後(onClickメソッド終了後)、自動スクロール
        // https://groups.google.com/forum/#!search/scrollview$20runnable/androidbrasil-dev/urxfE2XTetM/x_XXhsjjBwAJ
        // テキスト追加時、テキスト追加前の下端が、スクロールの先の下端として認識される。
        // Runnableでスレッド？を作成し、テキスト追加完了後、下端が更新された後にスクロールする仕組みを準備して実行されている
        final ScrollView scrollMultiLine = (ScrollView) findViewById(R.id.scrollMultiLine);
        scrollMultiLine.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollMultiLine.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);

        String strPrompt = ">> ";           // 複数行テキストのプロンプト（行頭文字列）
        TextView textMultiLine = (TextView) findViewById(R.id.textMultiLine);
        String strCurText = (String) textMultiLine.getText();
        String strCurPrompt = strPrompt2;
        // プロンプトを書き換えたい
        if (strCurPrompt.equals("")) {
            strCurPrompt = strPrompt;
        } //if

        // テキストを追加（末尾に改行入り）
        //171218 課題: スクロールしすぎるように見えるかも(ユーザの好みによる？)
        textMultiLine.setText(String.format(Locale.JAPAN, "%s%s%s%s", strCurText, strCurPrompt, strAdd, crlf));
    }

    // スコアの初期化
    // 主にゲームオーバー時に使用
    void clearScore(int intNew) {
        intScore = intNew;
        TextView textScore = (TextView) findViewById(R.id.textScore);
        textScore.setText(String.format(Locale.JAPAN, getString(R.string.format_score), intHighScore, intScore));
    }

    // スコアに加算
    // ステージクリア時の更新で使用
    void addScore(int intAdd) {
        int intNewScore;
        TextView textScore = (TextView) findViewById(R.id.textScore);
        intScore += intAdd;
        textScore.setText(String.format(Locale.JAPAN, getString(R.string.format_score), intHighScore, intScore));
        //171214 課題: 3桁毎のカンマ区切り
    }

    // ステージ初期化
    void initStage() {

        // ステージ番号の更新
        intStageNo++;

        // ステージサイズの決定
        // 横：2,3,3,4,4,5, ...
        // 縦：2,2,3,3,4,4, ...
        intSizeCol = 2 + ( intStageNo / 2);
        intSizeRow = 2 + ( Math.max( 0, ( intStageNo-1) ) / 2);
//        Log.d("initStage","intSizeCol="+intSizeCol+",intSizeRow="+intSizeRow);

        // 配列全体を0で初期化
        aryStageMap = new int[intSizeRow][intSizeCol];
        for(int i=0; i<intSizeRow; i++) {
            for(int j=0; j<intSizeCol; j++) {
                aryStageMap[i][j] = FLAG_NONE;
            } //for
        } //for

        // 乱数を準備
        Random random = new Random();

        // ゴールの決定
        intGoalRow = random.nextInt(intSizeRow);
        intGoalCol = random.nextInt(intSizeCol);
        aryStageMap[intGoalRow][intGoalCol] = FLAG_GOAL;

        // 現在位置の決定
        // ゴールと同じ位置にはしない
        do {
            intCurRow = random.nextInt(intSizeRow);
            intCurCol = random.nextInt(intSizeCol);
        } while ( (intGoalRow==intCurRow) && (intGoalCol==intCurCol) );

        // 移動可能数
        // 縦サイズ＋横サイズ＋乱数(0〜MOVE_APPENDIX-1)
        // 縦サイズ＋横サイズ＋乱数(0〜幅-1)
        intMoveLeft = intSizeRow + intSizeCol + random.nextInt(Math.max(intSizeRow,intSizeCol));

        // UI更新
        TextView textScore = (TextView) findViewById(R.id.textScore);
        textScore.setText(String.format(Locale.JAPAN,getString(R.string.format_score), intHighScore, intScore));
        TextView textMoveLeft = (TextView) findViewById(R.id.textMoveLeft);
        textMoveLeft.setText(Html.fromHtml(String.format(Locale.JAPAN, "移動可能数: <b><big><font color=#FF8822>%d</font></big></b>", intMoveLeft)));
        TextView textStageInfo = (TextView) findViewById(R.id.textStageInfo);
        textStageInfo.setText(Html.fromHtml(String.format(Locale.JAPAN, "ステージ: %d　サイズ: 横<b><big><font color=#FF8822>%d</font></big></b>×縦<b><big><font color=#FF8822>%d</font></big></b>",
                intStageNo, intSizeCol, intSizeRow)));
        TextView textMultiLine = (TextView) findViewById(R.id.textMultiLine);
        textMultiLine.setText("");  // クリア
        addTextLine(SEPARATOR);
        addTextLine("ステージ: "+ intStageNo +" スタート");

        // 移動前にcheckAfterMove()を呼ぶため、移動可能数を1だけ加算
        intMoveLeft++;
        checkAfterMove();
    }

//    // ゴールに到達したか否かのチェック
//    // 戻り値：1→到達,0→未達
//    private int checkGoal() {
//        // ゴールと現在位置が一致していればゴール
//        if ( (intGoalRow==intCurRow) && (intGoalCol==intCurCol) ) {
//            return 1;   // ゴール
//        } //if
//        return 0;
//    }

    private void checkAfterMove() {
        TextView textMoveLeft = (TextView) findViewById(R.id.textMoveLeft);
        textMoveLeft.setText(Html.fromHtml(String.format(Locale.JAPAN, "移動可能数: <b><big><font color=#FF8822>%d</font></big></b>", intMoveLeft)));

        // ゴール判定＆テキスト出力
        int res = getDistanceFromGoal();
        if (res==0) {
            // ゴール時のトースト表示
            Toast.makeText(this, String.format(Locale.JAPAN,"ゴール！（スコア＋%d点）", intMoveLeft),
                    Toast.LENGTH_SHORT).show();

            // ゴール時のアクティビティを開く
            //未

            // スコア更新
            //171214 課題: さらに加算する要素があれば追加すること
            addScore(intMoveLeft);

            // ステージ初期化
            initStage();

            // 中断
            return;
        } //if

        // ゲームオーバー判定
        // 情報表示
        if (intMoveLeft<=0) {
            // ゲームオーバー時のトースト表示
            //Toast.makeText(this, "ゲームオーバー！ステージ1へ戻ります", Toast.LENGTH_SHORT).show();
            //171227: アクティビティを閉じてトップに戻る場合
            Toast.makeText(this, "ゲームオーバー！ステージ1へ戻ります", Toast.LENGTH_SHORT).show();

            // ゲームオーバー時のアクティビティを開く
            //未

            // ハイスコア保存
            saveHighScoreData();

            // プレー中のデータのクリア
            clearScore(0);
            intStageNo = 0;     //初期値

            // このアクティビティを閉じる
            this.finish();

//            // ステージ初期化
//            initStage();

            //　中断
            return;

        } //if

        // 壁チェック
        // 上端
        if ( intCurRow==0 ) {
            if ( intCurCol==0 ) {
                addTextLine("現在、左上の角にいます。");
            } else if ( intCurCol==(intSizeCol-1) ) {
                addTextLine("現在、右上の角にいます。");
            } else {
                addTextLine("現在、上端にいます。");
            }
        // 下端
        } else if ( intCurRow==(intSizeRow-1) ) {
            if ( intCurCol==0 ) {
                addTextLine("現在、左下の角にいます。");
            } else if ( intCurCol==(intSizeCol-1) ) {
                addTextLine("現在、右下の角にいます。");
            } else {
                addTextLine("現在、下端にいます。");
            }
        // 端ではない
        } else {
            if ( intCurCol==0 ) {
                addTextLine("現在、左端にいます。");
            } else if ( intCurCol==(intSizeCol-1) ) {
                addTextLine("現在、右端にいます。");
            }
        }

    }


    // ゴールからの距離（テキスト出力付）
    // 戻り値：0以上の整数
    //        0→ゴール
    private int getDistanceFromGoal() {
        //
        int intDistance = Math.abs(intGoalRow-intCurRow)+Math.abs(intGoalCol-intCurCol);
        int intHintOK = Math.max(intSizeCol, intSizeRow) / 2; // 長い方の端数切り捨て

        // テキスト出力
        // 距離0ならゴール
        if ( intDistance == 0 ) {
            addTextLine("ゴールしました！");

        // 距離が一定数より近ければヒントを出す
        } else if ( intDistance <= intHintOK ) {
            addTextLine("ゴールまであと"+ intDistance +"マスです。");

        } //if

        return intDistance;
    }

    // 整数（ハイスコア）を保存するメソッド
    void saveHighScoreData() {
//        // Setという形でデータを保存する
//        Set<String> data = new HashSet<>();
//        // for文を使って、1つずつデータを入れてく
//        for (int i = 0; i < adapter.getCount(); i++) {
//            data.add(adapter.getItem(i));
//        }
//        // putStringSetを使ってデータを一覧で保存する
//        sharedPreferences.edit().putStringSet("hi-score", data).apply();

        // https://qiita.com/Yuki_Yamada/items/f8ea90a7538234add288
        // スコアがハイスコアを上回っていたら上書き保存
        if ( intScore>intHighScore ) {
            sharedPreferences.edit().putInt(HIGH_SCORE, intScore).apply();
            intHighScore = intScore;
        }

    }

    // 整数（ハイスコア）を取得するメソッド
    void loadHighScoreData() {
//        // Setという形でデータを保存したので、Setで取得する
//        Set<String> data = sharedPreferences.getStringSet("hi-score", new HashSet<String>());
//        // adapterには、addAllという関数で配列や、Setなどのデータの集まりを渡すことができる
//        adapter.addAll(data);

        // 保存されたハイスコアを読込
        intHighScore = sharedPreferences.getInt(HIGH_SCORE, 0);

        // UIを更新
        TextView textScore = (TextView) findViewById(R.id.textScore);
        textScore.setText(String.format(Locale.JAPAN,getString(R.string.format_score), intHighScore, intScore));

    }

}
