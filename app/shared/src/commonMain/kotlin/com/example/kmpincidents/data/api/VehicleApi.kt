package com.example.kmpincidents.data.api

import com.example.kmpincidents.data.model.VehicleInfo
import com.example.kmpincidents.data.model.ApiResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class VehicleApi(private val client: HttpClient) {
    private val baseUrl = "https://opendata.rdw.nl/api/odata/v4/m9d7-ebf2"

    suspend fun getVehicleInfo(licensePlate: String): ApiResult<VehicleInfo> {
        return try {
            val response = client.get(baseUrl) {
                url {
                    parameters.append("\$select", "kenteken,voertuigsoort,merk,handelsbenaming,eerste_kleur")
                    encodedParameters.append("\$filter", "kenteken%20eq%20%27$licensePlate%27")
                    parameters.append("\$format", "json")
                }
            }
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body<VehicleInfo>())
            } else {
                ApiResult.HttpError(response.status.value, response.status.description)
            }
        } catch (e: Exception) {
            ApiResult.Unknown(e)
        }
    }
}
