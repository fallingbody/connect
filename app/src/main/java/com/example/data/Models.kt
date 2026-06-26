package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "me" or "partner"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageResId: Int = 0 // for UI representation or mock assets
)

@Entity(tableName = "relationship_goals")
data class RelationshipGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Quality Time", "Words of Affirmation", etc.
    val current: Int,
    val target: Int
)

@Entity(tableName = "daily_prompts")
data class DailyPrompt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answerUser: String = "",
    val answerPartner: String = "",
    val isAnsweredUser: Boolean = false,
    val isAnsweredPartner: Boolean = false
)

@Entity(tableName = "couple_state")
data class CoupleState(
    @PrimaryKey val id: Int = 1, // Singleton row
    val partnerBatteryPercent: Int = 82,
    val partnerIsCharging: Boolean = false,
    val partnerLocationName: String = "Central Coffee Shop ☕",
    val partnerScreenState: String = "Screen On", // "Screen On", "Screen Off", "Asleep"
    val partnerFocusMode: Boolean = false,
    val partnerGhostMode: Boolean = false,
    val partnerMood: String = "Glowy Pink", // "Glowy Pink", "Cozy Yellow", "Calm Blue", "Energetic Red"
    val userMood: String = "Cozy Yellow",
    val plantGrowth: Int = 45, // 0 to 100%
    val petHappiness: Int = 80, // 0 to 100%
    val userBatteryPercent: Int = 100,
    val avatarUserAction: String = "idle", // "idle", "sleeping", "cooking", "watching_tv"
    val avatarPartnerAction: String = "idle",
    val weatherCondition: String = "Starry ✨", // "Sunny ☀️", "Rainy 🌧️", "Snowy ❄️", "Starry ✨"
    val radioTrackName: String = "Cozy Lo-Fi Beats 🎵",
    val radioIsPlaying: Boolean = false,
    val radioProgress: Float = 0.3f
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: String,
    val link: String = "",
    val addedBy: String // "me" or "partner"
)

@Entity(tableName = "bucket_list")
data class BucketListPostcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedDate: String = ""
)
