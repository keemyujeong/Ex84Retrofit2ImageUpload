package com.kyjsoft.ex84retrofit2imageupload;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kyjsoft.ex84retrofit2imageupload.databinding.ActivityMainBinding;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSelect.setOnClickListener(view -> clickSelect());
        binding.btnSend.setOnClickListener(view -> clickSend());

    }

    void clickSelect(){
        // 사진 앱 or 갤러리 앱을 통해 사진을 선택하도록
        Intent intent = new Intent(Intent.ACTION_PICK); // 사진 앱 부를 때 쓰는 묵시적 인텐트
        intent.setType("image/*");

        resultLauncher.launch(intent);


    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() != RESULT_CANCELED) {
                Intent intent = result.getData();
                Uri uri = intent.getData(); // 인텐트가 가지고온 uri
                Glide.with(MainActivity.this).load(uri).into(binding.iv);
                // (retrofit에서는 uri인식못함) 서버에 업로드하려면 선택된 사진의 실제 경로가 필요함. 근데 uri는 실제 주소가 아니라 콘텐츠주소(DB주소)임
                // uri 주소는 어떻게 생겼는지 확인 -> 경로가 실제 폴더이름이 아님
                // uri를 절대주소(실제주소)로 변경해주는 기능을 통해 변환.(직접 만들어야함.)
                imgPath = getRealPathFromUri(uri);
//                new AlertDialog.Builder(MainActivity.this).setMessage(imgPath).create().show();


            }
        }
    });


    // 선택된 이미지의 절대주소를 저장할 String 변수
    String imgPath = null;

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드 (데이터베이스안에 있는 이미지 절대주소의 값을 가져오는 코드임..resultset이 cursor임,)
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA}; // 데이터베이스 안에 필요한 칸 (select)
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null); // 커서 만들어주는 거임 -> uri를 이미 파라미터로 준거라 where는 하나를 이미 가리리고 있음. 그래서 null임.
        Cursor cursor= loader.loadInBackground(); // 메인스레드로 하지마라
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); // 나중에 이름가지고 인덱스번호를 내놔 나중에 데이터 베이스 칸을 더 추가할 수도 있으니까
        cursor.moveToFirst();
        String result= cursor.getString(column_index); // data한 개의 resultset이니까 column_index은 0이겠지
        cursor.close();
        return result;
    }



    void clickSend(){
        if(imgPath == null) return;
        // Retrofit을 이용해서 서버로 사진 업로드


        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl("http://kyjsoft.dothome.co.kr/").addConverterFactory(ScalarsConverterFactory.create());
        Retrofit retrofit = builder.build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        // 파일 전송할 때 MultipartBody.Part 택배상자 포장
        File file = new File(imgPath);
        // 파일을 비닐로 감싸
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file); // 포장지에다가 이미지를 싸는 타입이라고 알려주는 거야.
        // 택배상자(MultipartBody.Part)에 파일을 담은 비닐팩(requestBody)을 넣기
        MultipartBody.Part part = MultipartBody.Part.createFormData("img", file.getName(), requestBody); // 택배상자 구분하려고 식별자, 이미지 이름주는 거임(실제 이름이랑 달라도 됨. 받는 쪽에서는 몰라), 비닐팩

        Call<String> call = retrofitService.sendImage(part);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String s = response.body();
                new AlertDialog.Builder(MainActivity.this).setMessage(s).create().show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                new AlertDialog.Builder(MainActivity.this).setMessage(t.getMessage()).create().show();

            }
        });







    }


}