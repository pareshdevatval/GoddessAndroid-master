package com.krystal.goddesslifestyle.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.room.Room
import com.krystal.goddesslifestyle.R
import com.krystal.goddesslifestyle.base.BaseBindingAdapter
import com.krystal.goddesslifestyle.base.BaseBindingViewHolder
import com.krystal.goddesslifestyle.data.db.AppDatabase
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.databinding.ListRowCartBinding
import com.krystal.goddesslifestyle.utils.AppConstants
import com.krystal.goddesslifestyle.utils.AppUtils

/**
 * Created by Bhargav Thanki on 21 February,2020.
 */
class CartAdapter : BaseBindingAdapter<Cart>() {

    var appDataBase: AppDatabase? = null

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ListRowCartBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding = holder.binding as ListRowCartBinding

        val cartItem = items[position]
        val appDatabase = getAppDatabase(binding.root.context)

        cartItem.productId?.let {
            val product = appDatabase.cartProductDao().getProductById(it)
            binding.tvProduct.text = product?.productTitle
            //binding.tvPrice.text = product?.productPrice
            binding.tvPrice.text = AppUtils.getPriceWithCurrency(binding.root.context, product?.productPrice)
            binding.tvQuantity.text = cartItem.quantity.toString()

            binding.ivProduct.post {
                AppUtils.loadImageThroughGlide(
                    binding.root.context, binding.ivProduct,
                    AppUtils.generateImageUrl(product?.productImage,
                        binding.ivProduct.width, binding.ivProduct.height),
                    R.drawable.ic_placeholder_square
                )
            }
        }

        if(position == itemCount-1) {
            binding.view.visibility = View.GONE
        } else {
            binding.view.visibility = View.VISIBLE
        }
    }

    private fun getAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppConstants.DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    /*private fun changeQuantity(increseDecrease: Int) {
        //var currentQuantity = binding.tvQuantity.text.toString().toInt()
        if(increseDecrease == INCREASE_QUANTITY) {
            currentQuantity += 1
        } else if(increseDecrease == DECREASE_QUANTITY) {
            currentQuantity -= 1
        }
        binding.tvQuantity.text = currentQuantity.toString()
    }*/
}