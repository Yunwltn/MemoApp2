package com.yunwltn98.memoapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yunwltn98.memoapp.adapter.MemoAdapter;
import com.yunwltn98.memoapp.api.MemoApi;
import com.yunwltn98.memoapp.api.NetworkClient;
import com.yunwltn98.memoapp.api.UserApi;
import com.yunwltn98.memoapp.config.Config;
import com.yunwltn98.memoapp.model.Memo;
import com.yunwltn98.memoapp.model.MemoList;
import com.yunwltn98.memoapp.model.Res;
import com.yunwltn98.memoapp.model.UserRes;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Button btnAdd;
    ProgressBar progressBar;
    String accessToken;
    RecyclerView recyclerView;
    MemoAdapter adapter;
    ArrayList<Memo> memoArrayList = new ArrayList<>();
    private ProgressDialog dialog;

    // 페이징 처리를 위한 변수
    int offset = 0;
    int limit = 7;
    int count = 0;
    int deleteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 억세스 토큰이 저장되어 있으면 로그인한 유저이므로 메인 액티비티를 실행하고
        // 그렇지 않으면 회원가입 액티비티를 실행, 메인액티비티는 종료

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        accessToken = sp.getString(Config.ACCESS_TOKEN, "");

        if (accessToken.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 회원가입 / 로그인한 유저면 아래 코드를 실행
        btnAdd = findViewById(R.id.btnAdd);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if (lastPosition +1 == totalCount) {
                    // 네트워크 통해서 데이터를 더 불러온다
                    if (count == limit) {
                        addNetworkData();
                    }
                }
            }
        });

        // 메모 추가
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addNetworkData() {

        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
        MemoApi api = retrofit.create(MemoApi.class);
        Call<MemoList> call = api.getMemoList("Bearer " + accessToken, offset, limit);
        call.enqueue(new Callback<MemoList>() {
            @Override
            public void onResponse(Call<MemoList> call, Response<MemoList> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    // 정상적으로 데이터 받았으니 리사이클러뷰에 표시한다
                    MemoList memoList = response.body();
                    memoArrayList.addAll(memoList.getItems());

                    adapter.notifyDataSetChanged();

                    // 오프셋 코드 처리
                    count = memoList.getCount();
                    offset = offset + count;

                } else {
                    Toast.makeText(MainActivity.this,"서버에 문제가 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<MemoList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getNetworkData();
    }

    private void getNetworkData() {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
        MemoApi api = retrofit.create(MemoApi.class);

        // 오프셋 초기화는 함수 호출하기 전에 입력
        offset = 0;
        count = 0;

        Call<MemoList> call = api.getMemoList("Bearer " + accessToken, offset, limit);
        call.enqueue(new Callback<MemoList>() {
            @Override
            public void onResponse(Call<MemoList> call, Response<MemoList> response) {
                progressBar.setVisibility(View.GONE);

                // getNetworkData 함수는 항상 처음에 데이터를 가져오는 동작이므로 초기화 코드가 필요하다
                memoArrayList.clear();

                if (response.isSuccessful()) {
                    // 정상적으로 데이터 받았으니 리사이클러뷰에 표시한다
                    MemoList memoList = response.body();
                    memoArrayList.addAll(memoList.getItems());

                    adapter = new MemoAdapter(MainActivity.this, memoArrayList);
                    recyclerView.setAdapter(adapter);

                    // 오프셋 처리하는 코드
                    count = memoList.getCount();
                    offset = offset + count;

                } else {
                    Toast.makeText(MainActivity.this,"서버에 문제가 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<MemoList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 액션바에 메뉴가 나오도록 설정
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    // 액션바의 메뉴를 탭했을때 실행하는 함수 오버라이딩해서 사용
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuLogout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("로그아웃");
            builder.setMessage("로그아웃 하시겠습니까?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showProgress("로그아웃중");

                    Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
                    UserApi api = retrofit.create(UserApi.class);
                    Call<UserRes> call = api.logout("Bearer " + accessToken);
                    call.enqueue(new Callback<UserRes>() {
                        @Override
                        public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                            dismissProgress();

                            if (response.isSuccessful()) {
                                // 쉐어드 프리퍼런스에 저장한 토큰을 초기화한다다
                                SharedPreferences sp = getApplication().getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString(Config.ACCESS_TOKEN, "");
                                editor.apply();

                                // 로그아웃하면 앱 죵료 기획 finish();
                                // 로그아웃하면 로그인 화면을 띄우도록 기획
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);

                                finish();

                            } else {

                            }

                        }

                        @Override
                        public void onFailure(Call<UserRes> call, Throwable t) {
                            dismissProgress();
                        }
                    });
                }
            });
            builder.setNegativeButton("NO", null);
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    void showProgress(String message) {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.show();
    }

    // 로직처리가 끝나면 화면에서 사라지는 함수
    void dismissProgress() {
        dialog.dismiss();
    }
    public void deleteMemo(int index) {
        deleteIndex = index;
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
        MemoApi api = retrofit.create(MemoApi.class);
        Memo memo = memoArrayList.get(index);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String accessToken = sp.getString(Config.ACCESS_TOKEN, "");

        Call<Res> call = api.deleteMemo(memo.getId(), "Bearer " + accessToken);
        call.enqueue(new Callback<Res>() {
            @Override
            public void onResponse(Call<Res> call, Response<Res> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                    memoArrayList.remove(deleteIndex);
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(MainActivity.this, "정상동작하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<Res> call, Throwable t) {
                Toast.makeText(MainActivity.this, "정상동작하지 않습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }
}