package com.krystal.goddesslifestyle.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.room.Room
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.response.Product
import com.krystal.goddesslifestyle.data.response.VideoCategoriesResponse
import com.krystal.goddesslifestyle.databinding.ListRowShopBinding
import com.krystal.goddesslifestyle.databinding.ListRowVideoListingBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class ShopAdapter : BaseBindingAdapter<Product>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ListRowShopBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ListRowShopBinding
        val product = items[position]
        binding.tvName.text = product.productTitle
        binding.tvPrice.text = "$ "+product.productPrice

        binding.imageView.post {
            //Log.e("VID_CATG_URL", AppUtils.generateImageUrl(videoCategory.image, binding.imageView.width, 0))
            AppUtils.loadImageThroughGlide(binding.root.context, binding.imageView,
                AppUtils.generateImageUrl(product.productImage, binding.imageView.width, 0),
                R.drawable.ic_placeholder_square)
        }

        if(isItemAlreadyInCart(binding.root.context, product)) {
            binding.ivCart.setImageResource(R.drawable.ic_cart_yellow_bg)
        } else {
            binding.ivCart.setImageResource(R.drawable.ic_cart_white_bg)
        }

        if(product.productIsNew) {
            binding.ivNewTag.visibility = View.VISIBLE
        } else {
            binding.ivNewTag.visibility = View.GONE
        }
    }

    private fun getAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppConstants.DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    fun isItemAlreadyInCart(context: Context, product: Product): Boolean {

        val appDatabase = getAppDatabase(context)

        val cartItems = appDatabase.cartDao().getCartData()
        for(cartItem in cartItems) {
            if(cartItem.productId!! == product.productId) {
                return true
            }
        }
        return false;
    }

    fun filter(string: String) {
        if (string.isEmpty()) {
            items = allItems
            notifyDataSetChanged()
        } else {
            items =
                ArrayList(allItems.filter { it.productTitle?.toLowerCase()!!.contains(string.toLowerCase()) })
            notifyDataSetChanged()
        }

    }
}