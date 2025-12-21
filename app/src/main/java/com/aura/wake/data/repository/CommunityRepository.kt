package com.aura.wake.data.repository

import com.aura.wake.data.model.GlobalMessage
import com.aura.wake.data.model.Profile
import com.aura.wake.data.model.Community
import com.aura.wake.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityRepository(private val authRepository: AuthRepository = AuthRepository()) {

    suspend fun getCommunities(): List<Community> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database.from("communities")
                .select().decodeList<Community>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Community>()
        }
    }

    suspend fun getMessages(communityId: String): List<Pair<GlobalMessage, Profile?>> = withContext(Dispatchers.IO) {
        try {
            // Fetch messages
            val result = SupabaseClient.database.from("messages")
                .select {
                    filter {
                         eq("community_id", communityId)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
            val messages = result.decodeList<GlobalMessage>()

            // Collect User IDs to fetch profiles
            val userIds = messages.map { it.userId }.distinct()
            
            if (userIds.isEmpty()) return@withContext emptyList<Pair<GlobalMessage, Profile?>>()

            val profiles = SupabaseClient.database.from("profiles")
                .select {
                    filter {
                        isIn("id", userIds)
                    }
                }.decodeList<Profile>().associateBy { it.id }

            // Combine
            messages.map { msg ->
                msg to profiles[msg.userId]
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Pair<GlobalMessage, Profile?>>()
        }
    }

    suspend fun sendMessage(communityId: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.id ?: return@withContext false
        try {
            val message = GlobalMessage(
                communityId = communityId,
                userId = currentUserId,
                content = content
            )
            SupabaseClient.database.from("messages").insert(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
