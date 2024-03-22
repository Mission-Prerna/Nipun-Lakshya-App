package com.morziz.network.config

import okhttp3.Interceptor

open class NetworkConfig private constructor(builder: Builder) {
    var baseUrl: String
    var identity: String? = null
    var headers: HashMap<String, String>? = null
    var interceptors: List<Interceptor>? = null
        get() = if (field == null) ArrayList() else field
    var clientType: ClientType

    init {
        baseUrl = builder.baseUrl
        identity = builder.identity
        headers = builder.headers
        interceptors = builder.interceptors
        clientType = builder.clientType
    }

    class Builder {
        internal lateinit var baseUrl: String
        internal var identity: String? = null
        internal var headers: HashMap<String, String>? = null
        internal var interceptors: List<Interceptor>? = null
        internal lateinit var clientType: ClientType

        fun baseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }
        fun identity(identity: String?) = apply { this.identity = identity }
        fun headers(headers: HashMap<String, String>) = apply { this.headers = headers }
        fun interceptors(interceptors: List<Interceptor>?) =
            apply { this.interceptors = interceptors }

        fun clientType(clientType: ClientType) =
            apply { this.clientType = clientType }

        fun build(): NetworkConfig = NetworkConfig(this)
    }

}