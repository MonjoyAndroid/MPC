package com.microblocklabs.mpc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.microblocklabs.mpc.R
import com.microblocklabs.mpc.adapter.OnBoardingItemsAdapter
import com.microblocklabs.mpc.model.OnBoardingItem

class OnboardActivity : BaseActivity() {

    private lateinit var onboardingItemAdapter : OnBoardingItemsAdapter
    private lateinit var indicatorContainer: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        setOnBoardingItems()
        setIndicator()
        setCurrentIndicator(0)
    }


    private fun setOnBoardingItems(){
        onboardingItemAdapter = OnBoardingItemsAdapter(
            listOf(
                OnBoardingItem(
                    onBoardingImage = R.drawable.lock,
                    onBoardingTitle = resources.getString(R.string.first_slide_heading),
                    onBoardingDesc = resources.getString(R.string.first_slide_desc)
                ),
                OnBoardingItem(
                    onBoardingImage = R.drawable.safe,
                    onBoardingTitle = resources.getString(R.string.second_slide_heading),
                    onBoardingDesc = resources.getString(R.string.second_slide_desc)
                ),
//                OnBoardingItem(
//                    onBoardingImage = R.drawable.lock,
//                    onBoardingTitle = resources.getString(R.string.first_slide_heading),
//                    onBoardingDesc = resources.getString(R.string.first_slide_desc)
//                ),
            )
        )
        val onBoardingViewPager = findViewById<ViewPager2>(R.id.onBoardingViewPager)
        onBoardingViewPager.adapter = onboardingItemAdapter

        onBoardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                val pageCount = onboardingItemAdapter.itemCount
                if(position == pageCount-1){
                    findViewById<AppCompatTextView>(R.id.textSkip).visibility = View.GONE
                }else{
                    findViewById<AppCompatTextView>(R.id.textSkip).visibility = View.VISIBLE
                }
            }
        })
        (onBoardingViewPager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        findViewById<MaterialButton>(R.id.buttonNext).setOnClickListener {
            if(onBoardingViewPager.currentItem +1 < onboardingItemAdapter.itemCount){
                onBoardingViewPager.currentItem += 1
            }else{
                navigateToWalletSetup()
            }
        }
        findViewById<TextView>(R.id.textSkip).setOnClickListener {
            navigateToWalletSetup()
        }
    }

    private fun navigateToWalletSetup(){
        startActivity(Intent(applicationContext, WalletSetupActivity::class.java))
//        finish()
    }

    private fun navigateToHomeScreen(){
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }


    private fun setIndicator(){
        indicatorContainer = findViewById(R.id.indicatorContainer)
        val indicators = arrayOfNulls<ImageView>(onboardingItemAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8,0,8,0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_bg
                    )
                )
                it.layoutParams = layoutParams
                indicatorContainer.addView(it)
            }
        }
    }

    private fun setCurrentIndicator(position: Int){
        val childCount = indicatorContainer.childCount
        for (i in 0 until childCount){
            val imageView = indicatorContainer.getChildAt(i) as ImageView
            if(i == position){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active_bg
                    )
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_bg
                    )
                )
            }
        }
    }
}