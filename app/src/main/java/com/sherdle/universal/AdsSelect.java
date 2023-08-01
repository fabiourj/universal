

package com.sherdle.universal;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import java.util.Map;
import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//////////////////APPLOVIN///////////////////////////////////////////
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;



import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdkUtils;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
/////////////////////////////////////////////////////////////////////

public class AdsSelect  
        
{
    private static AdsSelect instance;
    private JSONObject adsConfig;
    private String mainBannerAds;
    private String mainInterstitialAds;
    private String adcolonyAppId;
    private String adcolonyBannerZoneId;
    private String adcolonyInterstitialZoneId;
    private String ironsourceAppId;

//////////////////APPLOVIN/////////////////////////////////////
    private MaxAdView applovin_adView;
    private MaxInterstitialAd applovin_interstitialAd;
    private int applovin_retryAttempt;
    //private AppLovinSdk appLovinSdk;

////////////////////////////////////////////////////////////////   

private AdsSelect(Context context) {



    // Tente carregar o arquivo ads.json remoto
    new Thread(() -> {
        try {
            URL url = new URL("https://meudominio.com/ads.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                // Parse o JSON
                JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                adsConfig = jsonArray.getJSONObject(0);

            // Extrair as informações da rede de anúncios
            mainBannerAds = adsConfig.getString("main_bannerAds");
            mainInterstitialAds = adsConfig.getString("main_interstitialAds");
            adcolonyAppId = adsConfig.getString("Adcolony_appId");
            adcolonyBannerZoneId = adsConfig.getString("Adcolony_bannerZoneId");
            adcolonyInterstitialZoneId = adsConfig.getString("Adcolony_interstitialZoneId");
            ironsourceAppId = adsConfig.getString("Ironsource_appId");

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Se a solicitação HTTP falhar, carregue o arquivo ads.json local
        try {
            InputStream inputStream = context.getAssets().open("ads.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            // Parse o JSON
            JSONArray jsonArray = new JSONArray(new String(buffer, "UTF-8"));
            adsConfig = jsonArray.getJSONObject(0);

             // Extrair as informações da rede de anúncios
            mainBannerAds = adsConfig.getString("main_bannerAds");
            mainInterstitialAds = adsConfig.getString("main_interstitialAds");
            adcolonyAppId = adsConfig.getString("Adcolony_appId");
            adcolonyBannerZoneId = adsConfig.getString("Adcolony_bannerZoneId");
            adcolonyInterstitialZoneId = adsConfig.getString("Adcolony_interstitialZoneId");
            ironsourceAppId = adsConfig.getString("Ironsource_appId");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();

///////////////////////////////APPLOVIN///////////////////////////////////
AppLovinSdk.initializeSdk(context);
AppLovinSdk.getInstance(context).setMediationProvider( "max" );
//////////////////////////////////////////////////////////////////////////    
}


    // Método para obter a instância do SDK do AppLovin
    /*
    public AppLovinSdk getAppLovinSdk() {
        return appLovinSdk;
    }
    */

    public static synchronized AdsSelect getInstance(Context context) {
        if (instance == null) {
            instance = new AdsSelect(context.getApplicationContext());
        }
        return instance;
    }

    // Getters para as informações da rede de anúncios
    public String getMainBannerAds() {
        return mainBannerAds;
    }

    public String getMainInterstitialAds() {
        return mainInterstitialAds;
    }

    public String getAdcolonyAppId() {
        return adcolonyAppId;
    }

    public String getAdcolonyBannerZoneId() {
        return adcolonyBannerZoneId;
    }

    public String getAdcolonyInterstitialZoneId() {
        return adcolonyInterstitialZoneId;
    }

    public String getIronsourceAppId() {
        return ironsourceAppId;
    }

    // Métodos para inicializar e exibir anúncios
    // TODO: Adicione o código para inicializar e exibir anúncios

    

    public void initialize_bannerAds(Context context) {
        // Aqui você pode adicionar o código para inicializar as redes de anúncios
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        switch (getMainBannerAds()) {
           
            case "Applovin MAX":
               // Add initialization code for Applovin MAX banner ad
          
        

                break;
            case "Adcolony":
                // Add initialization code for Adcolony banner ad
                break;
            case "Ironsource":
                // Add initialization code for Ironsource banner ad
                break;
        }
        // ...
    }

    public void showBannerAds() {
        // Aqui você pode adicionar o código para exibir os anúncios de banner
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        switch (getMainBannerAds()) {
           
            case "Applovin MAX":
                // Add code to show Applovin MAX banner ad
                break;
            case "Adcolony":
                // Add code to show Adcolony banner ad
                break;
            case "Ironsource":
                // Add code to show Ironsource banner ad
                break;
        }
        // ...
    }

     public void initialize_InterstitialAds(Activity activity) {
        // Aqui você pode adicionar o código para inicializar as redes de anúncios
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
       switch (getMainInterstitialAds()) {
            case "Applovin MAX":
                String adUnitId = "YOUR_AD_UNIT_ID"; // Substitua pelo seu Ad Unit ID
                MaxInterstitialAd applovin_interstitialAd = new MaxInterstitialAd(adUnitId, AppLovinSdk.getInstance(activity), activity);
                applovin_interstitialAd.setListener(new MaxAdListener() {
                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        // Ad loaded
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        // Ad load failed
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        // Ad display failed
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                        // Ad clicked
                    }
                    
                   @Override
                    public void onAdDisplayed(final MaxAd maxAd) {}

                   @Override
                    public void onAdHidden(final MaxAd maxAd)   {
                    // Interstitial ad is hidden. Pre-load the next ad
                     applovin_interstitialAd.loadAd();
                      }

                    // Implement other ad listener methods as needed
                });
                
                break;

            case "Adcolony":
                // Add initialization code for Adcolony banner ad
                break;
            case "Ironsource":
                // Add initialization code for Ironsource banner ad
                break;
        }
        // ...
    }

    public void show_InterstitialAds() {
        // Aqui você pode adicionar o código para exibir os anúncios intersticiais
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        switch (getMainInterstitialAds()) {
            
            case "Applovin MAX":
                // Add code to show Applovin MAX interstitial ad
                break;
            case "Adcolony":
                // Add code to show Adcolony interstitial ad
                break;
            case "Ironsource":
                // Add code to show Ironsource interstitial ad
                break;
        }
        


    }



}