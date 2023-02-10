package com.yunwltn98.memoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.yunwltn98.memoapp.api.MemoApi;
import com.yunwltn98.memoapp.api.NetworkClient;
import com.yunwltn98.memoapp.api.UserApi;
import com.yunwltn98.memoapp.config.Config;
import com.yunwltn98.memoapp.model.Memo;
import com.yunwltn98.memoapp.model.MemoList;
import com.yunwltn98.memoapp.model.Res;
import com.yunwltn98.memoapp.model.User;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddActivity extends AppCompatActivity {

    EditText editTitle;
    EditText editContent;
    Button btnDate;
    Button btnTime;
    Button btnSave;
    String Date = "";
    String Time = "";
    private ProgressDialog dialog;
    Memo memo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        editTitle = findViewById(R.id.editTitle);
        editContent = findViewById(R.id.editContent);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 오늘 날짜로 셋팅
                Calendar current = Calendar.getInstance();
                int y = current.get(Calendar.YEAR);
                int m = current.get(Calendar.MONTH);
                int d = current.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 월은 0이 1월이기때문에 +1해서 사용해준다
                        int m = month + 1;
                        String strM;
                        if (m < 10) {
                            strM = "0"+m;
                        } else {
                            strM = ""+m;
                        }
                        String strD;
                        if (dayOfMonth < 10) {
                            strD = "0"+dayOfMonth;
                        } else {
                            strD = ""+dayOfMonth;
                        }

                        Date = year + "-" + strM + "-" + strD;
                        btnDate.setText(Date);
                    }
                }, y, m, d); // 셋팅할 날짜 입력
                datePickerDialog.show();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar current = Calendar.getInstance();
                int h = current.get(Calendar.HOUR_OF_DAY);
                int m = current.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        String strH;
                        if (hourOfDay < 10) {
                            strH = "0"+hourOfDay;
                        } else {
                            strH = ""+hourOfDay;
                        }
                        String strM;
                        if (minute < 10) {
                            strM = "0"+minute;
                        } else {
                            strM = ""+minute;
                        }

                        Time = strH + ":" + strM;
                        btnTime.setText(Time);
                    }
                }, h, m, true); // 12시간 AM/PM은 false 24시로 보여주기는 true
                timePickerDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = editTitle.getText().toString().trim();
                String datetime = Date + " " + Time;
                String content = editContent.getText().toString().trim();

                if (Date.isEmpty() || Time.isEmpty()) {
                    Toast.makeText(AddActivity.this, "날짜와 시간을 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(AddActivity.this, "타이틀과 내용은 필수입니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 포스팅 API 호출
                showProgress("메모 생성중..");

                Retrofit retrofit = NetworkClient.getRetrofitClient(AddActivity.this);
                MemoApi api = retrofit.create(MemoApi.class);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                String accessToken = sp.getString(Config.ACCESS_TOKEN, "");

                memo = new Memo(title, datetime, content);
                Call<Res> call = api.addMemo("Bearer " + accessToken, memo);
                call.enqueue(new Callback<Res>() {
                    @Override
                    public void onResponse(Call<Res> call, Response<Res> response) {
                        dismissProgress();
                        if (response.isSuccessful()) {
                            Toast.makeText(AddActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AddActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<Res> call, Throwable t) {
                        dismissProgress();
                        Toast.makeText(AddActivity.this, "저장불가", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }
    // 네트워크 로직처리시에 화면에 보여주는 함수
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
}