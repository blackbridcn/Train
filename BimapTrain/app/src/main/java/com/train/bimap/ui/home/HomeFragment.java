package com.train.bimap.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.train.bimap.R;

import org.utils.BitmapUtils;

import java.io.IOException;
import java.io.InputStream;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Bitmap bitmap;
    ImageView holderBitmap;
    EditText quInput, layoutInput;
    TextView textSize;

    Button quBtn, layoutBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView result = root.findViewById(R.id.bitmap_resule);
        textSize = root.findViewById(R.id.text_size);
        quBtn = root.findViewById(R.id.text_qu);

        quInput = root.findViewById(R.id.qu_value);

        layoutBtn = root.findViewById(R.id.text_layout);
        layoutInput = root.findViewById(R.id.layout_value);

        holderBitmap = root.findViewById(R.id.bitmap_holder);

        Bitmap orgin = BitmapUtils.loadAssetsBitmap(getContext(), "860.jpg");
        int mSize = BitmapUtils.calcBitmapMemorySize(orgin);
        Log.i("TAG", "Assets Bitmap Size :" + mSize);
        textSize.setText("SRC MSIZE :"+mSize+" width："+orgin.getWidth()+" height:"+orgin.getHeight());
        holderBitmap.setImageBitmap(orgin);

        quBtn.setOnClickListener(v -> {
             Bitmap compressBitmap=BitmapUtils.qualityCompressBitmap(orgin,20);
            result.setImageBitmap(compressBitmap);
            int mSizes = BitmapUtils.calcBitmapMemorySize(compressBitmap);
            textSize.setText("SRC MSIZE :"+mSizes+" width："+compressBitmap.getWidth()+" height:"+compressBitmap.getHeight());
        });

        root.findViewById(R.id.scale).setOnClickListener(v -> {
            Bitmap mScale = BitmapUtils.scaleBitmap(orgin, 0.6f);
            result.setImageBitmap(mScale);
            int mSizes = BitmapUtils.calcBitmapMemorySize(mScale);
            textSize.setText("SRC MSIZE :"+mSizes+" width："+mScale.getWidth()+" height:"+mScale.getHeight());
        });

       root.findViewById(R.id.rotate).setOnClickListener(v -> {
           Bitmap mScale = BitmapUtils.rotateBitmap(orgin, 45.0f);
           result.setImageBitmap(mScale);
           int mSizes = BitmapUtils.calcBitmapMemorySize(mScale);
           textSize.setText("SRC MSIZE :"+mSizes+" width："+mScale.getWidth()+" height:"+mScale.getHeight());
       });

        root.findViewById(R.id.translate).setOnClickListener(v -> {
            Matrix matrix = new Matrix();
            matrix.setTranslate(orgin.getWidth() / 2, orgin.getHeight() / 2);// 向左下平移
            Bitmap mScale = Bitmap.createBitmap(orgin, 0, 0, orgin.getWidth(),
                    orgin.getHeight(), matrix, true);
            result.setImageBitmap(mScale);
            int mSizes = BitmapUtils.calcBitmapMemorySize(mScale);
            textSize.setText("SRC MSIZE :"+mSizes+" width："+mScale.getWidth()+" height:"+mScale.getHeight());
        });

       root.findViewById(R.id.skew).setOnClickListener(v -> {
          Bitmap mScale = BitmapUtils.skewBitmap(orgin, -0.6f,-0.3f);
          result.setImageBitmap(mScale);
          int mSizes = BitmapUtils.calcBitmapMemorySize(mScale);
          textSize.setText("SRC MSIZE :"+mSizes+" width："+mScale.getWidth()+" height:"+mScale.getHeight());
        });
         root.findViewById(R.id.sc).setOnClickListener(v -> {
             Matrix matrix = new Matrix();
             float[] src = new float[] { 0, 0, // 左上
                     orgin.getWidth(), 0,// 右上
                     orgin.getWidth(), orgin.getHeight(),// 右下
                     0, orgin.getHeight() };// 左下
             float[] dst = new float[] { 0, 0,
                     orgin.getWidth(), 30,
                     orgin.getWidth(), orgin.getHeight() - 30,
                     0,orgin.getHeight() };
             matrix.setPolyToPoly(src, 0, dst, 0, src.length/2);
             Bitmap mScale = Bitmap.createBitmap(orgin, 0, 0, orgin.getWidth(),
                     orgin.getHeight(), matrix, true);
             result.setImageBitmap(mScale);
             int mSizes = BitmapUtils.calcBitmapMemorySize(mScale);
             textSize.setText("SRC MSIZE :"+mSizes+" width："+mScale.getWidth()+" height:"+mScale.getHeight());
         });


        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        Log.i("TAG", "DisplayMetrics dpi :" + displayMetrics.densityDpi);
        Log.i("TAG", "DisplayMetrics :" + displayMetrics.toString());

        return root;
    }


    public void quality(View view) {
        // String size = input.getText().toString();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        //624 layout: 438X365
        //156 layout: 219X183

        Bitmap result = BitmapUtils.loadResBitmap(getContext(), R.mipmap.comm, options);
        holderBitmap.setImageBitmap(result);

        int size = BitmapUtils.calcBitmapMemorySize(result);
        textSize.setText(" KB :" + size / 1024);
        Log.i("TAG", "Bitmap Memory inSampleSize  :" + size / 1024 + " layout: " + result.getWidth() + "X" + result.getHeight());
    }
}