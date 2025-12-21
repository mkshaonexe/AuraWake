package com.aura.wake.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class Friendship(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String
)

@Serializable
data class Community(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("icon_name") val iconName: String? = null,
    @SerialName("member_count") val memberCount: Int = 0
)

@Serializable
data class GlobalMessage(
    val id: String? = null, 
    @SerialName("community_id") val communityId: String,
    @SerialName("user_id") val userId: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profiles") val profile: Profile? = null 
)
