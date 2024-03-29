package com.yunwltn98.memoapp.api;

import com.yunwltn98.memoapp.model.Memo;
import com.yunwltn98.memoapp.model.MemoList;
import com.yunwltn98.memoapp.model.Res;
import com.yunwltn98.memoapp.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MemoApi {

    // 내 메모 가져오는 API
    @GET("/memo")
    Call<MemoList> getMemoList(@Header("Authorization") String token, @Query("offset") int offset,
                               @Query("limit") int limit);

    // 메모 생성하는 API
    @POST("/memo")
    Call<Res> addMemo(@Header("Authorization") String token, @Body Memo memo);

    // 메모 수정 API
    @PUT("/memo/{memoId}")
    Call<Res> updateMemo(@Path("memoId") int memoId, @Header("Authorization") String token, @Body Memo memo);

    // 메모 삭제하는 API
    @DELETE("/memo/{memoId}")
    Call<Res> deleteMemo(@Path("memoId") int memoId, @Header("Authorization") String token);

}
