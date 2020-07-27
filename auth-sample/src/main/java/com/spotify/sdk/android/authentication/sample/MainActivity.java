package com.spotify.sdk.android.authentication.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

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

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "fb88aee284ed49e5ac81f13f0dcf4b15";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;

    private static final String REDIRECT_URI = "spotify-sdk://auth";

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    // private String mAccessToken;
    public static String mAccessToken;
    private String mAccessCode;
    private Call mCall;

    private ArrayList<String> artistIdList = new ArrayList<>();
    private ArrayList<String> artistNameList = new ArrayList<>();

    public static String selectedArtistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Digtter");
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void onGetUserProfileClicked(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        // TODO UIを整える
        // TODO ボタンのラベルを変える

        // 最初のアーティストを取得
        // TODO nameをユーザの入力から取得
        selectedArtistName = "YOASOBI";

        final Request nameRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/search" + "?q=" + selectedArtistName + "&type=artist")
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(nameRequest);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setResponse("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    final JSONObject artistsObject = jsonObject.getJSONObject("artists");
                    final JSONArray itemsArray = artistsObject.getJSONArray("items");
                    final JSONObject artistObject = itemsArray.getJSONObject(0);
                    final String id = artistObject.getString("id");
                    // 関連するアーティストを取得する
                    final Request relatedArtistRequest = new Request.Builder()
                            .url("https://api.spotify.com/v1/artists/" + id + "/related-artists")
                            .addHeader("Authorization","Bearer " + mAccessToken)
                            .build();
                    cancelCall();
                    mCall = mOkHttpClient.newCall(relatedArtistRequest);
                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            setResponse("Failed to fetch data: " + e);
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
                                setResponse("Failed to parse data: " + e);
                            }
                        }

                    });
                } catch (JSONException e) {
                    setResponse("Failed to parse data: " + e);
                }
            }

        });

    }

    private void setArtistsView(ArrayList<String> artistIdList, ArrayList<String> artistNameList) {
        // 画面遷移
        Intent intent = new Intent(MainActivity.this, FirstArtistListActivity.class);
        // 関連アーティストのリストを渡す
        intent.putStringArrayListExtra("artistIdList", artistIdList);
        intent.putStringArrayListExtra("artistNameList", artistNameList);
        intent.putExtra("from","MainActivity");
        startActivity(intent);
    }

    public void onRequestCodeClicked(View view) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void onRequestTokenClicked(View view) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email"})
                .setCampaign("your-campaign-token")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        if (response.getError() != null && !response.getError().isEmpty()) {
            setResponse(response.getError());
        }
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            updateTokenView();
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.getCode();
            updateCodeView();
        }
    }

    private void setResponse(final String text) {
        runOnUiThread(() -> {
            final TextView responseView = findViewById(R.id.response_text_view);
            responseView.setText(text);
        });
    }

    private void updateTokenView() {
        final TextView tokenView = findViewById(R.id.token_text_view);
        tokenView.setText(getString(R.string.token, mAccessToken));
    }

    private void updateCodeView() {
        final TextView codeView = findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, mAccessCode));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }
}
