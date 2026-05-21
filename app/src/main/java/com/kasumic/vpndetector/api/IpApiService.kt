package com.kasumic.vpndetector.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JsonClass(generateAdapter = true)
data class IpApiResponse(
    val status: String?,
    val query: String?,
    val country: String?,
    val countryCode: String?,
    val proxy: Boolean?,
    val hosting: Boolean?,
    val isp: String?
)

interface IpApiService {
    @GET("json/")
    suspend fun getIpInfo(
        @Query("fields") fields: String = "status,query,country,countryCode,proxy,hosting,isp"
    ): IpApiResponse
}

object NetworkClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://ip-api.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val ipApiService: IpApiService = retrofit.create(IpApiService::class.java)
}
