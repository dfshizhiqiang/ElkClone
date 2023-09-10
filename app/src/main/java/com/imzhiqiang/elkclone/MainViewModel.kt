package com.imzhiqiang.elkclone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imzhiqiang.elkclone.data.Currency
import com.imzhiqiang.elkclone.data.ExchangeRateResponse
import com.imzhiqiang.elkclone.data.Preference
import com.imzhiqiang.elkclone.data.allCurrencyList
import com.imzhiqiang.elkclone.data.getExchangeRateDataStore
import com.imzhiqiang.elkclone.data.hotCurrencyList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.OffsetDateTime

data class CurrencyRate(
    val source: String,
    val target: String,
    val rate: Double,
    val date: LocalDate,
    val updateAt: OffsetDateTime,
    val fromCache: Boolean = false
)

data class UiState(
    val shouldUpdate: Boolean = true,
    val success: Boolean = false,
    val error: Throwable? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val httpClient = HttpClient(OkHttp) {
        install(Logging)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val context get() = getApplication<Application>().applicationContext

    val fullCurrencyList =
        mapOf("hot" to hotCurrencyList) + allCurrencyList.sortedBy { it.currencyCode }
            .groupBy { it.currencyCode[0].toString() }

    private val sourceCurrency = Preference.getSourceCurrencyFlow(context)
    private val targetCurrency = Preference.getTargetCurrencyFlow(context)

    private val _currencyRate =
        MutableStateFlow(ExchangeRateResponse.getDefault(context).toCurrencyRate("CNY"))
    val currencyRate: StateFlow<CurrencyRate> = _currencyRate.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val queryResult: Flow<List<Currency>> = _query.map { q ->
        allCurrencyList.filter { it.currencyCode.contains(q) || it.getDisplayName().contains(q) }
    }

    init {
        viewModelScope.launch {
            combine(sourceCurrency, targetCurrency) { source, target ->
                updateCurrencyRate(source, target)
            }.collectLatest { result ->
                result.onSuccess { r ->
                    _currencyRate.update { r }
                }
            }
        }
    }

    private suspend fun updateCurrencyRate(source: String, target: String): Result<CurrencyRate> {
        val dataStore = getExchangeRateDataStore(context, source)
        val cached: ExchangeRateResponse? = dataStore.data.firstOrNull()
        if (cached != null && !cached.isEmpty() && !cached.isExpired()) {
            return Result.success(cached.toCurrencyRate(target).copy(fromCache = true))
        }
        return try {
            val response: ExchangeRateResponse =
                httpClient.get("https://api.exchangerate-api.com/v4/latest/$source").body()
            dataStore.updateData { response }
            Result.success(response.toCurrencyRate(target))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun refreshCurrencyRate() {
        viewModelScope.launch {
            val source = Preference.getSourceCurrencyFlow(context).first()
            val target = Preference.getTargetCurrencyFlow(context).first()
            updateCurrencyRate(source, target)
                .onSuccess { r ->
                    _currencyRate.update { r }
                    if (r.fromCache) {
                        _uiState.update { UiState(shouldUpdate = false) }
                    } else {
                        _uiState.update { UiState(success = true, error = null) }
                    }
                }
                .onFailure { err ->
                    _uiState.update { UiState(success = false, error = err) }
                }
        }
    }

    fun switchCurrency() {
        viewModelScope.launch {
            val source = Preference.getSourceCurrencyFlow(context).first()
            val target = Preference.getTargetCurrencyFlow(context).first()
            Preference.setSourceAndTargetCurrency(context, target, source)
        }
    }

    fun setTargetCurrency(target: String) {
        viewModelScope.launch {
            Preference.setTargetCurrency(context, target)
        }
    }

    fun consumeUiState() {
        _uiState.update { UiState() }
    }

    fun setQuery(query: String) {
        _query.value = query
    }
}