package com.imzhiqiang.elkclone.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.imzhiqiang.elkclone.CurrencyRate
import com.imzhiqiang.elkclone.R
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dataStoreMap = hashMapOf<String, DataStore<ExchangeRateResponse>>()

fun getExchangeRateDataStore(context: Context, preKey: String): DataStore<ExchangeRateResponse> {
    return dataStoreMap.getOrPut(preKey, defaultValue = {
        DataStoreFactory.create(
            serializer = ExchangeRateResponseSerializer,
            produceFile = {
                context.applicationContext.dataStoreFile("exchange_rate_$preKey.json")
            })
    })
}

@Serializable
data class ExchangeRateResponse(
    @SerialName("base")
    val source: String = "",
    @SerialName("date")
    val date: String = "",
    @SerialName("time_last_updated")
    val updateAt: Long = 0,
    @SerialName("rates")
    val rates: Map<String, Double> = mapOf()
) {

    companion object {

        private val json = Json {
            ignoreUnknownKeys = true
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun getDefault(context: Context): ExchangeRateResponse {
            return json.decodeFromStream(context.applicationContext.resources.openRawResource(R.raw.usd))
        }
    }

    fun isEmpty(): Boolean {
        return source.isEmpty() || rates.isEmpty()
    }

    fun isExpired(): Boolean {
        return Instant.now() > Instant.ofEpochSecond(updateAt).plus(23L, ChronoUnit.HOURS)
    }


    fun toCurrencyRate(target: String): CurrencyRate {
        return CurrencyRate(
            source,
            target,
            rates.getOrDefault(target, 1.0),
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            OffsetDateTime.ofInstant(Instant.ofEpochSecond(updateAt), ZoneId.systemDefault())
        )
    }

}

object ExchangeRateResponseSerializer : Serializer<ExchangeRateResponse> {

    override val defaultValue: ExchangeRateResponse
        get() = ExchangeRateResponse()

    override suspend fun readFrom(input: InputStream): ExchangeRateResponse {
        try {
            return Json.decodeFromString(
                ExchangeRateResponse.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read ExchangeRateResponse", serialization)
        }
    }

    override suspend fun writeTo(t: ExchangeRateResponse, output: OutputStream) {
        output.write(Json.encodeToString(ExchangeRateResponse.serializer(), t).encodeToByteArray())
    }

}