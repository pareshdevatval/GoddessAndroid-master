package com.krystal.goddesslifestyle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.android.billingclient.api.SkuDetails;
import com.krystal.goddesslifestyle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhargav Thanki on 24 March,2020.
 */
public class SubscriptionsAdapter extends ArrayAdapter<SkuDetails> {

    private Context mContext;
    private List<SkuDetails> skus = new ArrayList<>();

    int selectedPosition = 0;

    public SubscriptionsAdapter(@NonNull Context context, ArrayList<SkuDetails> list/*, int selectedPosition*/) {
        super(context, 0 , list);
        mContext = context;
        skus = list;
        //this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.subscription_price_drop_down,parent,false);

        SkuDetails sku = skus.get(position);


        TextView tvAmount = listItem.findViewById(R.id.tvAmount);
        TextView tvDesc = listItem.findViewById(R.id.tvDesc);

        tvAmount.setText(sku.getPrice());
        if(sku.getSubscriptionPeriod().equalsIgnoreCase("P1M")) {
            tvDesc.setText("per month");
        } else if(sku.getSubscriptionPeriod().equalsIgnoreCase("P1Y")) {
            tvDesc.setText("per year");
        }
        //tvDesc.setText(plan.getDesc());

        return listItem;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public int getCount() {
        return skus.size();
    }
}
