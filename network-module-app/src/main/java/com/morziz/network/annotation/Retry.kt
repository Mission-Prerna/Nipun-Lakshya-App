package com.morziz.network.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Retry(val retryPolicy: RetryPolicyType, val retryCount: Int)

enum class RetryPolicyType {
    linear,
    exponential
}