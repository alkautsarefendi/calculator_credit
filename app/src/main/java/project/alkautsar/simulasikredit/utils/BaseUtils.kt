package project.alkautsar.simulasikredit.utils

import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import project.alkautsar.simulasikredit.BuildConfig
import project.alkautsar.simulasikredit.R

open class BaseUtils : AppCompatActivity() {
    private var adView: AdManagerAdView? =null
    private var mInterstitialAd: InterstitialAd? = null
    var id: String? = null
    private var isLoad = false


    fun setupInterstitial() {

        val unitIdInterstitial: String
        if(BuildConfig.DEBUG){
            unitIdInterstitial = "ca-app-pub-3940256099942544/1033173712" //debug version
        }else{
            unitIdInterstitial = "ca-app-pub-7025020357054894/6795433746" // prod Version
        }
        CoroutineScope(Dispatchers.Main).launch {

            try {
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(this@BaseUtils, unitIdInterstitial, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            isLoad = true
                            Log.d("success","AdMob Inters Loaded Success")

                            // Set the FullScreenContentCallback
                            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    // Handle the ad dismissed event
                                    Log.d("success","AdMob Inters Ad Dismissed")
                                    // Load a new interstitial ad
                                    mInterstitialAd = null
                                    setupInterstitial()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Handle the ad failed to show event
                                    Log.d("success","AdMob Inters Ad Failed to Show: ${adError.message}")

                                }

                                override fun onAdShowedFullScreenContent() {
                                    // Handle the ad showed event
                                    Log.d("success","AdMob Inters Ad Showed")
                                    mInterstitialAd = null // Reset the interstitial ad
                                }
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            mInterstitialAd = null
                            isLoad = false
                        }
                    })
            } catch (e: Exception) {
                Log.d("failed","asywalul inters :${e.message}")
            }
        }
    }


    fun loadBanner(adViewContainer: FrameLayout) {
        try {

            Log.e("BASE UTIL", "LOAD BANNER")

            var unitIdBanner = ""
            if(BuildConfig.DEBUG){
                unitIdBanner = "ca-app-pub-3940256099942544/9214589741" //dev version
            }else{
                unitIdBanner = "ca-app-pub-7025020357054894/4360842094"  //prod version
            }
            adView?.adUnitId = unitIdBanner
            adView?.setAdSizes(getSize(adViewContainer))
            val adRequest = AdManagerAdRequest.Builder().build()
            adView?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("AdMob", "Ad loaded successfully unit = $id")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.d("AdMob", "Ad failed to load:"+ p0.message + "id = "+id)
                }

                override fun onAdOpened() {
                    Log.d("AdMob", "Ad opened")
                }

                override fun onAdClicked() {
                    Log.d("AdMob", "Ad clicked")
                }

                override fun onAdClosed() {
                    Log.d("AdMob", "Ad closed")
                }
            }
            adView?.loadAd(adRequest)
        }catch (e : Exception){
            Log.d("failed",e.message.toString())
        }
    }

    private fun getSize(adViewContainer: FrameLayout): AdSize {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    fun showInterstitial(){
        try {
            Log.d("showInters","execute")
            if(isLoad){
                Log.d("showIntersAdmob","true")
                mInterstitialAd?.show(this)
            }
        }catch (e : Exception){
            Log.d("showInters","false")
        }
    }

    fun loadNativeAd(adViewContainer: FrameLayout) {
        try {
            Log.d("BaseUtils", "Loading Native Ad")

            val unitIdNativeAd = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110" // Test Ad Unit ID
            } else {
                "ca-app-pub-7025020357054894/6838061112" // Production Ad Unit ID
            }

            val adLoader = AdLoader.Builder(this, unitIdNativeAd)
                .forNativeAd { nativeAd ->
                    // Jika Native Ad berhasil dimuat, tampilkan di View
                    displayNativeAd(nativeAd, adViewContainer)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("BaseUtils", "Native Ad failed to load: ${adError.message}")
                    }

                    override fun onAdLoaded() {
                        Log.d("BaseUtils", "Native Ad loaded successfully")
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.d("BaseUtils", "Failed to load Native Ad: ${e.message}")
        }
    }

    private fun displayNativeAd(nativeAd: NativeAd, adViewContainer: FrameLayout) {
        val adView = LayoutInflater.from(this).inflate(R.layout.native_ad_layout, null) as NativeAdView

        // Set Native Ad Data (headline, body, call to action)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.mediaView = adView.findViewById(R.id.ad_media) as MediaView
        val adImageView = adView.findViewById<ImageView>(R.id.ad_image) // ImageView untuk gambar

        // Set text untuk headline dan body
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        // Cek apakah ada konten media (video/gambar)
        if (nativeAd.mediaContent != null) {
            adView.mediaView?.visibility = View.VISIBLE
            adImageView.visibility = View.GONE // Sembunyikan ImageView jika ada video/gambar di MediaView
            adView.mediaView?.setMediaContent(nativeAd.mediaContent)
        } else if (nativeAd.images.isNotEmpty()) {
            adView.mediaView?.visibility = View.GONE // Sembunyikan MediaView jika hanya ada gambar
            adImageView.visibility = View.VISIBLE
            adImageView.setImageDrawable(nativeAd.images[0].drawable) // Set gambar di ImageView
        } else {
            adView.mediaView?.visibility = View.GONE
            adImageView.visibility = View.GONE // Sembunyikan jika tidak ada gambar atau video
        }

        // Tambahkan adView ke dalam container
        adViewContainer.removeAllViews()
        adViewContainer.addView(adView)

        // Set Native Ad
        adView.setNativeAd(nativeAd)
    }




}