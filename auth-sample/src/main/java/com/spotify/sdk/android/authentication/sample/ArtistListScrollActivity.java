package com.spotify.sdk.android.authentication.sample;

import android.R.id;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArtistListScrollActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<String> artistIdList = new ArrayList<>();
    ArrayList<String> artistNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_list_artist);

        Intent intent = getIntent();
        artistIdList = intent.getStringArrayListExtra("artistIdList");
        artistNameList = intent.getStringArrayListExtra("artistNameList");

        // Adapter生成
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,
                getListData(artistIdList, artistNameList), // 使用するデータ
                android.R.layout.simple_list_item_2, // 使用するレイアウト
                new String[]{"name", "id"}, // どの項目を
                new int[]{id.text1, id.text2} // どのidの項目に入れるか
        );

        // ListViewを取得
        ListView listView = (ListView) findViewById(R.id.artist_list);
        listView.setAdapter(simpleAdapter);

        System.out.println("画面遷移しました");
        System.out.println(getListData(artistIdList, artistNameList));

        // TODO:リスト項目をクリックした時の処理
        listView.setOnItemClickListener(this);
    }

    private List<HashMap<String,String>> getListData(ArrayList<String> artistIdList, ArrayList<String> artistNameList) {
        List<HashMap<String, String>> listData = new ArrayList<>();
        for (int i = 0; i < artistIdList.size(); i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("id", artistIdList.get(i));
            map.put("name", artistNameList.get(i));
            listData.add(map);
        }
        return listData;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        // clickされたpositionのtextとphotoのID
        String selectedId = artistIdList.get(position);
        String selectedName = artistNameList.get(position);
        // インテントにセット
        intent.putExtra("id", selectedId);
        intent.putExtra("name", artistNameList);
        // Activity をスイッチする
        startActivity(intent);
    }
}
