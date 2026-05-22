package com.kasumic.vpndetector.api

import retrofit2.http.GET
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

data class SecurityInfo(
    val proxy: Boolean?,
    val vpn: Boolean?,
    val tor: Boolean?,
    val hosting: Boolean?
)

data class ConnectionInfo(
    val isp: String?
)

data class IpApiResponse(
    val success: Boolean?,
    val ip: String?,
    val country: String?,
    val country_code: String?,
    val security: SecurityInfo?,
    val connection: ConnectionInfo?
)

interface IpApiService {
    @GET("/")
    suspend fun getIpInfo(): IpApiResponse
}

object NetworkClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ipwho.is/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val ipApiService: IpApiService = retrofit.create(IpApiService::class.java)
}
