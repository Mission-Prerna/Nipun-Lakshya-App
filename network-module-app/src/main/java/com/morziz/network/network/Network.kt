package com.morziz.network.network

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.morziz.network.config.ClientType
import com.morziz.network.config.ConfigManager
import com.morziz.network.config.NetworkConfig

class Network {
    companion object {
        var context: Context? = null

        var moduleDependency: ModuleDependency? = null

        fun init(module: ModuleDependency) {
            moduleDependency = module
        }

        fun getStringFromRes(resId: Int) = context?.getString(resId)

        fun addNetworkConfig(config: NetworkConfig) {
            ConfigManager.getInstance().addConfig(config)
        }

        fun <S> getClient(clientType: ClientType, clazz: Class<S>, identity: String): S? {
            if (clientType == ClientType.GRAPHQL && clazz == ApolloClient::class.java) {
                val apolloClient =
                    ConfigManager.getInstance().getApolloClient(identity) ?: return null
                return apolloClient as S
            } else if (clientType == ClientType.RETROFIT) {
                return ConfigManager.getInstance().getRetrofitServiceClient(identity, clazz)
            }
            return null
        }
    }
}