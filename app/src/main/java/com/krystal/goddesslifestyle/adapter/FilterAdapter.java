package com.krystal.goddesslifestyle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.krystal.goddesslifestyle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhargav Thanki on 24 March,2020.
 */
public class FilterAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> subCategories = new ArrayList<>();

    int selectedPosition = 0;

    public FilterAdapter(@NonNull Context context,  ArrayList<String> list) {
        super(context, 0 , list);
        mContext = context;
        subCategories = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.filter_drop_down,parent,false);

        String subCategory = subCategories.get(position);

        LinearLayout root = listItem.findViewById(R.id.root);
        View separator = listItem.findViewById(R.id.separator);

        TextView textView = listItem.findViewById(R.id.textView);
        ImageView iv_tick = listItem.findViewById(R.id.iv_tick);
        textView.setText(""+subCategory);

        float scale = mContext.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (10*scale + 0.5f);

        if(position == 0) {
            root.setPadding(0, dpAsPixels, 0, 0);
        } else {
            root.setPadding(0, 0, 0, 0);
        }

        if(position == selectedPosition) {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.pink));
            iv_tick.setVisibility(View.VISIBLE);

        } else {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.color_black_edittext));
            iv_tick.setVisibility(View.INVISIBLE);
        }

        if(position == getCount()-1) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }

        return listItem;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public int getCount() {
        return subCategories.size();
    }
}
