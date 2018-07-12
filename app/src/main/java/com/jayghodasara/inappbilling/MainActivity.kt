package com.jayghodasara.inappbilling

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import kotlinx.android.synthetic.main.activity_main.*
import android.util.DisplayMetrics
import java.util.*


class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {

    lateinit var billingClient: BillingClient
    var hashMap: HashMap<String, String> = HashMap()
    var hashMap2: HashMap<String, String> = HashMap()

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {


                billingClient.consumeAsync(purchase.purchaseToken) { responseCode, purchaseToken -> Toast.makeText(this, "Consumed", Toast.LENGTH_LONG).show() }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        } else {

        }
    }

    var list: ArrayList<String> = ArrayList()


    fun queryskudetails() {

        billingClient = BillingClient.newBuilder(this).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.i("Disconnected", "billing client")
            }

            override fun onBillingSetupFinished(responseCode: Int) {

                billingClient.let { billingClient ->

                    val skulist = ArrayList<String>()
                    skulist.add("subscribe")

                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skulist).setType(BillingClient.SkuType.SUBS)
                    billingClient.querySkuDetailsAsync(params.build(), { responseCode, skuDetailsList ->

                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            for (skuDetails in skuDetailsList) {
                                val sku = skuDetails.sku
                                val price = skuDetails.price
                                Log.i("skudetails", sku)
                                Log.i("skuprice", price)
                                hashMap[sku] = price


                            }
                        }

                    })
                }


            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jaLocale = Locale("ja")
        Locale.setDefault(jaLocale)

        val configuration = this.getResources().getConfiguration()
        val displayMetrics = this.getResources().getDisplayMetrics()
        configuration.locale = jaLocale

        this.getResources().updateConfiguration(configuration, displayMetrics)


        MobileAds.initialize(this, "ca-app-pub-3808194518393666~2116890476")

        var interstitialAd:InterstitialAd=InterstitialAd(this).apply {
            adUnitId="ca-app-pub-3808194518393666/1374229415"
            adListener=(object : AdListener(){

                override fun onAdClosed() {
                    Toast.makeText(applicationContext,"Finished",Toast.LENGTH_LONG).show()
                }
            })
        }
        interstitialAd.loadAd(AdRequest.Builder().build())

        ad.setOnClickListener(View.OnClickListener {

            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            } else {
                Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()

            }
        })


        var mRewardedVideoAd:RewardedVideoAd= MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.userId="ca-app-pub-3808194518393666/8351960489"
        mRewardedVideoAd.loadAd("ca-app-pub-3808194518393666/8351960489",AdRequest.Builder().build())

        video.setOnClickListener(View.OnClickListener {
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }else {
                Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()

            }
        })

       val adRequest:AdRequest= AdRequest.Builder().build()
        adView.loadAd(adRequest)


       adView.adListener= object :AdListener(){

           override fun onAdLoaded() {
               Toast.makeText(applicationContext,"Loaded",Toast.LENGTH_LONG).show()
           }

           override fun onAdClicked() {
               Toast.makeText(applicationContext,"clicked",Toast.LENGTH_LONG).show()
           }

           override fun onAdOpened() {
               Toast.makeText(applicationContext,"Opened",Toast.LENGTH_LONG).show()
           }
       }

        list.add("Books")
        list.add("Pens")
        list.add("Keychains")
        list.add("Mobiles")
        queryskudetails()
        subscribe.setOnClickListener(View.OnClickListener {


            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("subscribe")
                    .setType(BillingClient.SkuType.SUBS) // SkuType.SUB for subscription

                    .build()
            val responseCode = billingClient.launchBillingFlow(this, flowParams)
        })

        var layman: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        var adapter = myadapter(this, list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layman


    }
}
