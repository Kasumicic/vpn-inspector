package com.kasumic.vpndetector.api

import retrofit2.http.GET
import retrofit2.http.Path
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Dns
import java.net.InetAddress
import java.net.Inet6Address

data class CountryInfo(
    val iso: String?
)

data class IpApiResponse(
    val ip: String?,
    val country: CountryInfo?
)

interface IpApiService {
    @GET("json/")
    suspend fun getIpInfo(): IpApiResponse

    @GET("json/{ip}")
    suspend fun getIpInfoFor(@Path("ip") ip: String): IpApiResponse
}

data class IpApiIsDatacenter(
    val datacenter: String?,
    val domain: String?,
    val network: String?
)

data class IpApiIsResponse(
    val ip: String?,
    val is_crawler: Boolean?,
    val is_proxy: Boolean?,
    val is_vpn: Boolean?,
    val is_tor: Boolean?,
    val is_datacenter: Boolean?,
    val datacenter: IpApiIsDatacenter?
)

interface IpApiIsService {
    @GET("/")
    suspend fun getIpTypeInfo(): IpApiIsResponse

    @GET("/")
    suspend fun getIpTypeInfoFor(@retrofit2.http.Query("ip") ip: String): IpApiIsResponse
}

class IPv6FirstDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return Dns.SYSTEM.lookup(hostname).sortedBy {
            if (it is Inet6Address) 0 else 1
        }
    }
}

object NetworkClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val userAgentInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .build()
        chain.proceed(requestWithUserAgent)
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .dns(IPv6FirstDns())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sypexgeo.net/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val ipApiService: IpApiService = retrofit.create(IpApiService::class.java)

    private val ipApiIsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.ipapi.is/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val ipApiIsService: IpApiIsService = ipApiIsRetrofit.create(IpApiIsService::class.java)
}
