package com.samagra.ancillaryscreens.fcm

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance
import com.user.model.UpdateFCMTokenMutation
import com.user.model.type.Mentor_tokens_insert_input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationRepository {

    fun postFCMToken(
        token: String,
        mentorId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = Network.Companion.getClient(
                clientType = ClientType.GRAPHQL,
                clazz = ApolloClient::class.java,
                identity = CommonConstants.IDENTITY_HASURA
            )
            val model =
                Mentor_tokens_insert_input.builder().token(token).mentor_id(mentorId).build()
            val mutation = UpdateFCMTokenMutation.builder().model(model).build()
            client?.mutate(mutation)
                ?.enqueue(object : ApolloCall.Callback<UpdateFCMTokenMutation.Data>() {
                    override fun onResponse(response: Response<UpdateFCMTokenMutation.Data>) {
                        response.data?.let {
                            onSuccess()
                        } ?: run {
                            val error = StringBuilder("Server insertion failed : ")
                            response.errors?.let {
                                if (it.isNotEmpty()) {
                                    error.append(it[0].message)
                                }
                            }
                            onFailure(RuntimeException(RuntimeException(error.toString())))
                        }
                    }

                    override fun onFailure(e: ApolloException) {
                        onFailure(e)
                    }
                })
        }
    }

}