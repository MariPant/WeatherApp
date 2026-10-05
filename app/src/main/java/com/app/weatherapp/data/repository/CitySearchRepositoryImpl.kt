package com.app.weatherapp.data.repository

import com.app.weatherapp.data.mapper.ForecastMapper
import com.app.weatherapp.data.remote.GeocodingApi
import com.app.weatherapp.data.remote.safeApiCall
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.DataResult
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.domain.repository.CitySearchRepository
import java.util.Locale

class CitySearchRepositoryImpl(
    private val api: GeocodingApi,
    private val languageProvider: () -> String = { Locale.getDefault().language },
) : CitySearchRepository {

    override suspend fun search(query: String): DataResult<List<WeatherLocation>> {
        val trimmed = query.trim()
        if (trimmed.length < CitySearchRepository.MIN_QUERY_LENGTH) return DataResult.Success(emptyList())

        return when (val result = safeApiCall { api.searchCity(name = trimmed, language = languageProvider()) }) {
            is DataResult.Failure -> result
            is DataResult.Success -> {
                val cities = result.data.results.orEmpty().map { ForecastMapper.toDomain(it) }
                if (cities.isEmpty()) DataResult.Failure(DataError.CityNotFound) else DataResult.Success(cities)
            }
        }
    }
}
