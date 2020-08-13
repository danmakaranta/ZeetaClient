package com.example.zeeta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zeeta.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> secondImageUrls;
    private LayoutInflater layoutInflater;


    public ViewPagerAdapter(Context context, ArrayList<String> secondImageUrlsmageUrls) {
        this.context = context;
        this.secondImageUrls = secondImageUrlsmageUrls;
    }

    @Override
    public int getCount() {
        return secondImageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.fashion_items, container, false);

        ImageView imageView;
        TextView title, description;
        imageView = view.findViewById(R.id.fashion_imageView);

        Picasso.get()
                .load(secondImageUrls.get(position))
                .placeholder(R.drawable.zeetaicon)
                .fit()
                .into(imageView);

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}