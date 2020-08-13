package com.example.zeeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class FashionAdapter extends PagerAdapter {
    private List<Model> models;
    private List<Upload> mUploads;
    private LayoutInflater layoutInflater;
    private Context context;
    private String[] imageUrls;

    public FashionAdapter(List<Model> models, Context context) {
        this.models = models;
        this.context = context;
    }


    @Override
    public int getCount() {
        return models.size();
        //return mUploads.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item, container, false);

        ImageView imageView;
        TextView title, description;
        imageView = view.findViewById(R.id.fashion_imageView);

        imageView.setImageResource(models.get(position).getImage());


        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
