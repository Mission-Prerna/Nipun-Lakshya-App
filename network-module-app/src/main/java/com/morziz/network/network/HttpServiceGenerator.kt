package com.morziz.network.network

import android.content.Context
import android.os.Build
import com.external.network.BuildConfig
import com.google.gson.GsonBuilder
import com.morziz.network.custom.ResultCallAdapterFactory
import com.morziz.network.helpers.NoConnectivityException
import com.morziz.network.network.KeyType.Companion.googleReactive
import com.morziz.network.network.KeyType.Companion.normal
import com.morziz.network.network.KeyType.Companion.reactive
import com.morziz.network.network.KeyType.Companion.simple
import com.morziz.network.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

private const val TIMEOUT_RESPONSE: Long = 30
private const val TIMEOUT_CONNECTION: Long = 10
private val retrofitMap = HashMap<String, Retrofit?>()

//URL's to be used
private val logging = HttpLoggingInterceptor()

/**
 * Generates the instance for provided service class
 *
 * @param context         in which context service will be fired
 * @param serviceClass    which service is required
 * @param responseTimeout if any custom timeout is required
 * @return
 */
@JvmOverloads
fun <S> generate(
    context: Context,
    type: String,
    serviceClass: Class<S>,
    responseTimeout: Long = TIMEOUT_RESPONSE,
    moduleDependency: ModuleDependency
): S {
    val key = getRetrofitKey(type, context, moduleDependency)
    val retrofit: Retrofit = retrofitMap[key]
        ?: createRetrofit(context, type, responseTimeout, moduleDependency).also { retrofitMap[key] = it }
    return retrofit.create(serviceClass)
}

fun createRetrofit(
    context: Context,
    type: String,
    responseTimeout: Long,
    moduleDependency: ModuleDependency
): Retrofit {
    val gson = GsonBuilder().serializeNulls().create()
    var builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(getIntendedUrl(context, type, moduleDependency))
        .client(getHttpClient(type, context, responseTimeout, moduleDependency))

    if (type == reactive || type == googleReactive) {
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    }
    if (type == simple) {
        builder.addCallAdapterFactory(ResultCallAdapterFactory.create())
    }
    return builder.build()
}

private fun getRetrofitKey(type: String, context: Context, moduleDependency: ModuleDependency): String {
    return getIntendedUrl(context, type, moduleDependency) + type
}

@JvmOverloads
fun getIntendedUrl(context: Context, type: String = normal, moduleDependency: ModuleDependency): String {
    return moduleDependency.getBaseUrl(type)
}

private fun getHttpClient(
    objType: String,
    context: Context,
    responseTimeout: Long,
    moduleDependency: ModuleDependency
): OkHttpClient {

    val client = OkHttpClient.Builder()
        .readTimeout(responseTimeout, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)


    if (BuildConfig.BUILD_TYPE.equals(ENV_RELEASE, ignoreCase = true)) {
        // For PROD environments
        logging.level = HttpLoggingInterceptor.Level.NONE
    } else {

        // For basic information logging
        //logging.setLevel(Level.BASIC);

        // For basic + headers information logging
        //logging.setLevel(Level.HEADERS);

        // For detailed information logging
        // [IMPORTANT] Use this level only if necessary
        // because logs will clutter our Android monitor if we’re receiving large data sets
        logging.level = HttpLoggingInterceptor.Level.BODY
    }

    client.interceptors().addAll(getInterceptor(objType, context, moduleDependency))

    return client.build()
}

private fun getInterceptor(objType: String, context: Context, moduleDependency: ModuleDependency): List<Interceptor> {
    val intercepters = mutableListOf<Interceptor>()
    when (objType) {
        googleReactive -> intercepters.add(GoogleRequestHeaderInterceptor(context, moduleDependency))
        else -> {
            intercepters.add(RequestHeaderInterceptor(moduleDependency))
            intercepters.add(ConnectivityInterceptor(context))
        }
    }
    intercepters.add(logging)
    if (moduleDependency.getExtraInterceptors() != null) {
        for (interceptor in moduleDependency.getExtraInterceptors()!!) {
            intercepters.add(interceptor)
        }
    }
    return intercepters
}

/**
 * Helper class which performs the default tasks like adding query params to the [Interceptor]
 * for the GoogleDirectionsApi.
 */
private class GoogleRequestHeaderInterceptor(
    private val context: Context,
    private val moduleDependency: ModuleDependency
) :
    Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val url =
            request.url.newBuilder().addQueryParameter("key", moduleDependency.getGoogleKeys()).build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}


/**
 * Interceptor to add default headers to request
 */
private class RequestHeaderInterceptor(private val moduleDependency: ModuleDependency) : Interceptor {


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        var builder = original.newBuilder()
            .header("Accept", "application/json")
            .addHeader("Accept-Language", Locale.getDefault().language)
            .addHeader("timezone", TimeZone.getDefault().id)
            .addHeader("platform", "Android")
            .addHeader("deviceModel", Build.MODEL)
            .addHeader("deviceManufacturer", Build.MANUFACTURER)
            .addHeader("deviceVersion", Build.VERSION.SDK_INT.toString())

        moduleDependency.getHeaders()?.let {
            for (pair in it) {
                builder.addHeader(pair.key, pair.value)
            }
        }

        return chain.proceed(builder.build())
    }
}

/**
 * Helper class adds an interceptor to check for internet connectivity.
 * In case of no connectivity a [NoConnectivityException] is thrown and the
 * request chain is not proceeded.
 */
private class ConnectivityInterceptor(private val mContext: Context) :
    Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtils.isInternetConnected(mContext)) {
            throw NoConnectivityException()
        }
        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }
}