package com.lilly.ble.di

import com.lilly.ble.BleRepository
import com.lilly.ble.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
}

val repositoryModule = module{
    single{
        BleRepository()
    }
}