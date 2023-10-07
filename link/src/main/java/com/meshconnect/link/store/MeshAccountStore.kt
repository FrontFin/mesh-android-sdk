package com.meshconnect.link.store

import kotlinx.coroutines.flow.Flow

interface MeshAccountStore {

    suspend fun insert(accounts: List<MeshAccount>)

    suspend fun getAll(): List<MeshAccount>

    suspend fun getById(id: String): MeshAccount?

    fun accounts(): Flow<List<MeshAccount>>

    suspend fun count(): Int

    suspend fun remove(id: String)

    suspend fun clear()
}
