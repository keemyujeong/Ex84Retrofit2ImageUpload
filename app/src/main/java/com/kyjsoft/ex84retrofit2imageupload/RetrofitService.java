package com.kyjsoft.ex84retrofit2imageupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitService {

    @Multipart // @Part -> 이거쓸 때 꼭 필요한 어노테이션
    @POST("04Retrofit/fileUpload.php")
    Call<String> sendImage(@Part MultipartBody.Part filepart); // 파일 감싸는 택배상자로 파일 싸서 보내야함.
}
