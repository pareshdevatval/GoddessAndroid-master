package com.krystal.goddesslifestyle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.krystal.goddesslifestyle.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding

    var isCollapsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test)

        binding.button.setOnClickListener {
            if(!isCollapsed) {
                isCollapsed = true
                collapseView()
            } else {
                isCollapsed = false
                expandView()
            }
        }
    }

    fun collapseView() {
        binding.ll1.visibility = View.GONE
        binding.ll2.visibility = View.GONE
        binding.ll4.visibility = View.GONE
        binding.ll5.visibility = View.GONE
    }

    fun expandView() {
        binding.ll1.visibility = View.VISIBLE
        binding.ll2.visibility = View.VISIBLE
        binding.ll4.visibility = View.VISIBLE
        binding.ll5.visibility = View.VISIBLE
    }
}
