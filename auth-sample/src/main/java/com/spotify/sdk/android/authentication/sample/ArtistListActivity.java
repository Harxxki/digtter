package com.spotify.sdk.android.authentication.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ArtistListActivity extends AppCompatActivity {

    private String id;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        // TODO 関連するアーティストを取得する
        // TODO アーティストのリストを表示する
        // TODO Artist Data を再帰的に呼び出す

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");

        System.out.println(id + " : " + name);
    }
}