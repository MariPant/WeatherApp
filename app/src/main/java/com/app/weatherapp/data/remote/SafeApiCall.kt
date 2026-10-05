package com.app.weatherapp.data.remote

import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Runs a network call and converts failures into [DataError].
 * CancellationException is always rethrown so structured concurrency keeps working.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    DataResult.Failure(e.toDataError())
}

private fun Exception.toDataError(): DataError = when (this) {
    is SocketTimeoutException -> DataError.Timeout
    is InterruptedIOException -> DataError.Timeout // OkHttp call timeout
    is UnknownHostException, is ConnectException -> DataError.NoInternet
    is HttpException -> DataError.Server(code())
    is SerializationException -> DataError.Server(null)
    is IOException -> DataError.NoInternet
    else -> DataError.Unknown
}
