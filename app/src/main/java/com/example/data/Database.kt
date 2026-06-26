package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoupleDao {
    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()

    // Memories
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<Memory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    // Relationship Goals
    @Query("SELECT * FROM relationship_goals")
    fun getAllGoals(): Flow<List<RelationshipGoal>>

    @Update
    suspend fun updateGoal(goal: RelationshipGoal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<RelationshipGoal>)

    // Daily Prompts
    @Query("SELECT * FROM daily_prompts")
    fun getAllPrompts(): Flow<List<DailyPrompt>>

    @Update
    suspend fun updatePrompt(prompt: DailyPrompt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<DailyPrompt>)

    // Couple State (Singleton state)
    @Query("SELECT * FROM couple_state WHERE id = 1")
    fun getCoupleState(): Flow<CoupleState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupleState(state: CoupleState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCoupleState(state: CoupleState)

    // Seeding queries (non-Flow)
    @Query("SELECT * FROM couple_state WHERE id = 1")
    suspend fun getCoupleStateOnce(): CoupleState?

    @Query("SELECT * FROM daily_prompts")
    suspend fun getAllPromptsOnce(): List<DailyPrompt>

    @Query("SELECT * FROM relationship_goals")
    suspend fun getAllGoalsOnce(): List<RelationshipGoal>

    @Query("SELECT * FROM wishlist_items")
    suspend fun getAllWishlistItemsOnce(): List<WishlistItem>

    @Query("SELECT * FROM bucket_list")
    suspend fun getAllBucketListOnce(): List<BucketListPostcard>

    @Query("SELECT * FROM chat_messages")
    suspend fun getAllMessagesOnce(): List<ChatMessage>

    // Wishlist
    @Query("SELECT * FROM wishlist_items ORDER BY id DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItem(id: Int)

    // Bucket List
    @Query("SELECT * FROM bucket_list ORDER BY id ASC")
    fun getAllBucketList(): Flow<List<BucketListPostcard>>

    @Update
    suspend fun updateBucketList(item: BucketListPostcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBucketList(items: List<BucketListPostcard>)
}

@Database(
    entities = [ChatMessage::class, Memory::class, RelationshipGoal::class, DailyPrompt::class, CoupleState::class, WishlistItem::class, BucketListPostcard::class],
    version = 2,
    exportSchema = false
)
abstract class CoupleDatabase : RoomDatabase() {
    abstract fun coupleDao(): CoupleDao

    companion object {
        @Volatile
        private var INSTANCE: CoupleDatabase? = null

        fun getDatabase(context: Context): CoupleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoupleDatabase::class.java,
                    "couple_connect_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
