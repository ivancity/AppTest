package com.design.ivan.apptest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.design.ivan.apptest.appsync.GetImgTask;
import com.design.ivan.apptest.appsync.GetProductTask;
import com.squareup.picasso.Picasso;

/**
 * Created by ivanm on 10/12/15.
 */
public class FragmentProduct extends Fragment
        implements GetImgTask.onFinishGetImgTask,
                    GetProductTask.onFinishGetProduct,
                    View.OnClickListener{

    ImageView imgProdFrag;
    Button btnProdFrag;
    FrameLayout frameProductLayout;
    boolean stopProductUpdate = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        frameProductLayout = (FrameLayout)view.findViewById(R.id.frame_product_layout);
        imgProdFrag = (ImageView)view.findViewById(R.id.img_product_frag);
        imgProdFrag.setOnClickListener(this);
        btnProdFrag = (Button)view.findViewById(R.id.btn_product_frag);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getImgFromDb();
    }

    private void getImgFromDb(){
        //AsyncTask to get image from DB.
        GetImgTask imgTask = new GetImgTask(getActivity());
        imgTask.setFinishCallback(this);
        imgTask.execute(getActivity().getString(R.string.product_url));
    }

    private void updateProductList(){
        //updates product list from server
        GetProductTask productTask = new GetProductTask(getActivity());
        productTask.setOnFinishGetProductCallback(this);
        productTask.execute(getActivity().getString(R.string.product_url));

    }

    @Override
    public void callBackFromGetImgTask(String imgUrl) {
        //it checks if it failes to find a valid imgUrl
        if(imgUrl != null){
            //load image from URL found in db
            Picasso.with(getActivity())
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_photo_size_select_actual_black_36dp)
                    .error(R.drawable.ic_error_black_36dp)
                    .into(imgProdFrag);
        } else {
            //Not found URL in db. Attempt to sync Product list with server
            Picasso.with(getActivity())
                    .load(R.drawable.ic_error_black_36dp)
                    .into(imgProdFrag);
            //to avoid any loops avoid syncing from the server more than once. Otherwise simply
            //fail with a image error holder set above.

            if(!stopProductUpdate)
                updateProductList();
        }
    }

    @Override
    public void callBackFromGetProductTask() {
        //attempt to update the Product List if the DB is empty. Make sure to set to true
        stopProductUpdate = true;
        getImgFromDb();
    }

    @Override
    public void onClick(View v) {
        //when image is clicked the color background change happens here
        frameProductLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
    }
}
