package com.getfront.catalog.store

import com.getfront.catalog.entity.FrontAccount
import kotlinx.coroutines.flow.Flow

interface FrontAccountStore {

    suspend fun insert(accounts: List<FrontAccount>)

    suspend fun getAll(): List<FrontAccount>

    suspend fun getById(id: String): FrontAccount?

    suspend fun accounts(): Flow<List<FrontAccount>>

    suspend fun count(): Int

    suspend fun remove(id: String)

    suspend fun clear()
}
