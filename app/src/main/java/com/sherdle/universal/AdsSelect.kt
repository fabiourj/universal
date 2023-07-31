package com.sherdle.universal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AdsSelect private constructor(context: Context) {
    private var adsConfig: JSONObject? = null

    // Getters para as informações da rede de anúncios
    var mainBannerAds: String? = null
    var mainInterstitialAds: String? = null
    var adcolonyAppId: String? = null
    var adcolonyBannerZoneId: String? = null
    var adcolonyInterstitialZoneId: String? = null
    var ironsourceAppId: String? = null

    init {
        // Tente carregar o arquivo ads.json remoto
        Thread(Runnable {
            try {
                val url = URL("https://meudominio.com/ads.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    reader.close()
                    inputStream.close()

                    // Parse o JSON
                    val jsonArray = JSONArray(stringBuilder.toString())
                    adsConfig = jsonArray.getJSONObject(0)

                    // Extrair as informações da rede de anúncios
                    mainBannerAds = adsConfig.getString("main_bannerAds")
                    mainInterstitialAds = adsConfig.getString("main_interstitialAds")
                    adcolonyAppId = adsConfig.getString("Adcolony_appId")
                    adcolonyBannerZoneId = adsConfig.getString("Adcolony_bannerZoneId")
                    adcolonyInterstitialZoneId = adsConfig.getString("Adcolony_interstitialZoneId")
                    ironsourceAppId = adsConfig.getString("Ironsource_appId")
                    return@Runnable
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Se a solicitação HTTP falhar, carregue o arquivo ads.json local
            try {
                val inputStream = context.assets.open("ads.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                // Parse o JSON
                val jsonArray = JSONArray(String(buffer, "UTF-8"))
                adsConfig = jsonArray.getJSONObject(0)

                // Extrair as informações da rede de anúncios
                mainBannerAds = adsConfig.getString("main_bannerAds")
                mainInterstitialAds = adsConfig.getString("main_interstitialAds")
                adcolonyAppId = adsConfig.getString("Adcolony_appId")
                adcolonyBannerZoneId = adsConfig.getString("Adcolony_bannerZoneId")
                adcolonyInterstitialZoneId = adsConfig.getString("Adcolony_interstitialZoneId")
                ironsourceAppId = adsConfig.getString("Ironsource_appId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    // Métodos para inicializar e exibir anúncios
    // TODO: Adicione o código para inicializar e exibir anúncios
    fun initialize_bannerAds(context: Context?) {
        // Aqui você pode adicionar o código para inicializar as redes de anúncios
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        when (mainBannerAds) {
            "Applovin MAX" -> {}
            "Adcolony" -> {}
            "Ironsource" -> {}
        }
        // ...
    }

    fun showBannerAds() {
        // Aqui você pode adicionar o código para exibir os anúncios de banner
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        when (mainBannerAds) {
            "Applovin MAX" -> {}
            "Adcolony" -> {}
            "Ironsource" -> {}
        }
        // ...
    }

    fun initialize_InterstitialAds(context: Context?) {
        // Aqui você pode adicionar o código para inicializar as redes de anúncios
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        when (mainInterstitialAds) {
            "Applovin MAX" -> {}
            "Adcolony" -> {}
            "Ironsource" -> {}
        }
        // ...
    }

    fun show_InterstitialAds() {
        // Aqui você pode adicionar o código para exibir os anúncios intersticiais
        // Você pode usar os getters para obter as informações da rede de anúncios
        // Por exemplo:
        when (mainInterstitialAds) {
            "Applovin MAX" -> {}
            "Adcolony" -> {}
            "Ironsource" -> {}
        }
    }

    companion object {
        private var instance: AdsSelect? = null
        @Synchronized
        fun getInstance(context: Context): AdsSelect? {
            if (instance == null) {
                instance = AdsSelect(context.applicationContext)
            }
            return instance
        }
    }
}