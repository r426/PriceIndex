package com.ryeslim.priceindex.retrofit

import com.ryeslim.priceindex.data.Query
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface QueryApi {
  @GET("currentprice.json")
  fun getQueryAsync(): Deferred<Response<Query>>
}