package com.spotify.sdk.android.authentication.sample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArtistListActivity extends AppCompatActivity {

    private String selectedId;
    // private String selectedName;
    public static String selectedName;
    private String mAccessToken;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private ArrayList<String> artistIdList = new ArrayList<>();
    private ArrayList<String> artistNameList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        Intent intent = getIntent();
        selectedId = intent.getStringExtra("id");
        selectedName = intent.getStringExtra("name");
        getSupportActionBar().setTitle("Searching : related to '" + selectedName + "'");

        System.out.println("次のアーティストが選択されました : " + selectedName + " (id = " + selectedId + ")");
        System.out.println("画面遷移しました -> ArtistActivity");

        this.mAccessToken = MainActivity.mAccessToken;

        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_al), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }
    }

    protected void onStart() {
        super.onStart();
        // 関連するアーティストを取得する
        final Request relatedArtistRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/artists/" + selectedId + "/related-artists")
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();
        cancelCall();
        mCall = mOkHttpClient.newCall(relatedArtistRequest);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    final JSONArray artistsArray = jsonObject.getJSONArray("artists");
                    for (int i=0; i<artistsArray.length(); i++) {
                        // 同じインデックスに格納していく
                        artistIdList.add(artistsArray.getJSONObject(i).getString("id"));
                        artistNameList.add(artistsArray.getJSONObject(i).getString("name"));
                    }
                    setArtistsView(artistIdList,artistNameList);
                } catch (JSONException e) {
                    System.out.println("Failed to parse data: " + e);
                }
            }

        });
    }
    private void setArtistsView(ArrayList<String> artistIdList, ArrayList<String> artistNameList) {
        // 画面遷移
        Intent intent = new Intent(ArtistListActivity.this, FirstArtistListActivity.class);
        // 関連アーティストのリストを渡す
        intent.putStringArrayListExtra("artistIdList", artistIdList);
        intent.putStringArrayListExtra("artistNameList", artistNameList);
        intent.putExtra("from","ListActivity");
        startActivity(intent);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}