package com.example.kmpincidents.di

import com.example.kmpincidents.data.api.AuthApi
import com.example.kmpincidents.data.api.IncidentApi
import com.example.kmpincidents.data.api.UserApi
import com.example.kmpincidents.data.api.VehicleApi
import com.example.kmpincidents.data.repository.AuthRepository
import com.example.kmpincidents.data.repository.IncidentRepository
import com.example.kmpincidents.data.repository.UserRepository
import com.example.kmpincidents.data.store.IncidentDataStore
import com.example.kmpincidents.data.store.TokenPreferences
import com.example.kmpincidents.util.PhotoFileResolver
import com.example.kmpincidents.viewmodel.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single {
        createHttpClient(Json { ignoreUnknownKeys = true })
    }
}

val dataModule = module {
    single { TokenPreferences() }
    single { IncidentDataStore() }
    single { PhotoFileResolver() }
    single { AuthApi(get()) }
    single { UserApi(get(), get()) }
    single { IncidentApi(get(), get()) }
    single { VehicleApi(get()) }
    single { AuthRepository(get(), get()) }
    single { UserRepository(get()) }
    single { IncidentRepository(get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { MyIncidentListViewModel(get(), get(), get(), get(), get()) }
    viewModel { MyIncidentDetailViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { ReportIncidentViewModel(get(), get(), get()) }
    viewModel { UserViewModel(get()) }
    viewModel { UserManagementViewModel(get(), get()) }
    viewModel { IncidentManagementViewModel(get(), get()) }
    viewModel { IncidentDetailViewModel(get(), get(), get()) }
    viewModel { StatsViewModel(get(), get()) }
}