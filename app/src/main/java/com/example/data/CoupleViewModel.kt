package com.example.data

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class CoupleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CoupleDatabase.getDatabase(application)
    private val dao = db.coupleDao()

    val messages: StateFlow<List<ChatMessage>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<Memory>> = dao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<RelationshipGoal>> = dao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompts: StateFlow<List<DailyPrompt>> = dao.getAllPrompts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = dao.getAllWishlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bucketList: StateFlow<List<BucketListPostcard>> = dao.getAllBucketList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupleState: StateFlow<CoupleState> = dao.getCoupleState()
        .map { it ?: defaultCoupleState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCoupleState())

    // Active poke visual trigger (null if no active poke, otherwise shows a sweet message/overlay)
    private val _activePoke = MutableStateFlow<String?>(null)
    val activePoke: StateFlow<String?> = _activePoke.asStateFlow()

    // Active call screen simulation
    private val _activeCallType = MutableStateFlow<String?>(null) // null, "voice", "video"
    val activeCallType: StateFlow<String?> = _activeCallType.asStateFlow()

    // Wheel Spinning State
    private val _wheelTargetIndex = MutableStateFlow<Int?>(null)
    val wheelTargetIndex: StateFlow<Int?> = _wheelTargetIndex.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed DB if empty
            seedDatabase()
            updateUserBattery()
        }
    }

    private fun defaultCoupleState() = CoupleState(
        id = 1,
        partnerBatteryPercent = 85,
        partnerIsCharging = false,
        partnerLocationName = "Cozy Library 📚",
        partnerScreenState = "Active",
        partnerFocusMode = false,
        partnerGhostMode = false,
        partnerMood = "Glowy Pink",
        userMood = "Cozy Yellow",
        plantGrowth = 35,
        petHappiness = 75,
        userBatteryPercent = 100,
        avatarUserAction = "idle",
        avatarPartnerAction = "idle",
        weatherCondition = "Starry ✨",
        radioTrackName = "Cozy Lo-Fi Beats 🎵",
        radioIsPlaying = false,
        radioProgress = 0.3f
    )

    private suspend fun seedDatabase() {
        // Seed CoupleState if empty
        val currentState = dao.getCoupleStateOnce()
        if (currentState == null) {
            dao.insertCoupleState(defaultCoupleState())
        }

        // Seed default prompts if empty
        val currentPrompts = dao.getAllPromptsOnce()
        if (currentPrompts.isEmpty()) {
            dao.insertPrompts(listOf(
                DailyPrompt(
                    id = 1,
                    question = "What is a small detail about me that you notice and love?",
                    answerUser = "The way your eyes crinkle when you laugh!",
                    answerPartner = "How you hum sweet little songs while making tea.",
                    isAnsweredUser = true,
                    isAnsweredPartner = true
                ),
                DailyPrompt(
                    id = 2,
                    question = "If we could jet off to any place in the world tonight, where are we going?",
                    answerUser = "",
                    answerPartner = "A warm, starry overwater cabin in Bora Bora! 🏝️",
                    isAnsweredUser = false,
                    isAnsweredPartner = true
                ),
                DailyPrompt(
                    id = 3,
                    question = "What is your absolute favorite memory of our early dating days?",
                    answerUser = "",
                    answerPartner = "Getting caught in that sudden warm summer rainstorm and sharing one tiny umbrella.",
                    isAnsweredUser = false,
                    isAnsweredPartner = true
                )
            ))
        }

        // Seed goals if empty
        val currentGoals = dao.getAllGoalsOnce()
        if (currentGoals.isEmpty()) {
            dao.insertGoals(listOf(
                RelationshipGoal(1, "Daily Text Streak 💬", "Consistency", 9, 10),
                RelationshipGoal(2, "Date Nights Shared 🍕", "Quality Time", 3, 5),
                RelationshipGoal(3, "Virtual Garden Flowers 🌸", "Growth", 35, 100),
                RelationshipGoal(4, "Sweet Compliments Sent 💖", "Words of Affirmation", 14, 20)
            ))
        }

        // Seed Wishlist if empty
        val currentWishlist = dao.getAllWishlistItemsOnce()
        if (currentWishlist.isEmpty()) {
            dao.insertWishlistItem(WishlistItem(name = "Matching Custom Keychains 🔑", price = "$15", link = "https://example.com/keychains", addedBy = "partner"))
            dao.insertWishlistItem(WishlistItem(name = "Cozy Oversized Couples Hoodie 🧥", price = "$49", link = "https://example.com/hoodies", addedBy = "me"))
        }

        // Seed Bucket List if empty
        val currentBucket = dao.getAllBucketListOnce()
        if (currentBucket.isEmpty()) {
            dao.insertBucketList(listOf(
                BucketListPostcard(title = "Stargazing in a Bubble Tent ⛺", location = "Iceland 🌌", description = "Spend a freezing cold night watching the glowing Northern lights through a transparent warm dome!"),
                BucketListPostcard(title = "Picnic under Cherry Blossoms 🌸", location = "Kyoto, Japan ⛩️", description = "Share strawberry mochi beneath pink cherry blossom trees in full bloom."),
                BucketListPostcard(title = "Romantic Gondola Ride 🛶", location = "Venice, Italy 🇮🇹", description = "Glide down narrow historic canals while being serenaded by a gondolier."),
                BucketListPostcard(title = "Cozy Log Cabin Weekend 🌲", location = "Banff, Canada 🇨🇦", description = "Light a wood fireplace, drink hot chocolate, and watch the snow fall in the mountains.")
            ))
        }

        // Seed initial message if empty
        val currentMsgs = dao.getAllMessagesOnce()
        if (currentMsgs.isEmpty()) {
            dao.insertMessage(ChatMessage(sender = "partner", text = "Hi darling! Hope your day is going wonderfully. Missing you! 💕"))
        }
    }

    // Read real local device battery capacity
    fun updateUserBattery() {
        val batteryManager = getApplication<Application>().getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
        val level = batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        viewModelScope.launch {
            val state = coupleState.value
            dao.updateCoupleState(state.copy(userBatteryPercent = level))
        }
    }

    // Interactive Methods
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dao.insertMessage(ChatMessage(sender = "me", text = text))
            
            // Advance complement goals if appropriate
            if (text.contains("love", ignoreCase = true) || text.contains("miss", ignoreCase = true) || text.contains("beautiful", ignoreCase = true)) {
                incrementGoalProgress(4) // Sent Sweet Compliments
            }

            // Simulate automatic interactive cute partner reply!
            delay(1200)
            val partnerReplies = listOf(
                "Aww, you always know how to make me blush! 🥰",
                "Hehehe you are the absolute sweetest! My heart is melting! 🍯",
                "Missing you twice as much now! Can't wait to see you soon! 🌸",
                "Sending you a giant warm virtual hug right back! 🤗❤️",
                "You are my absolute favorite human. Truly. 💫",
                "Boop! Love you to the moon and back! 🚀💛"
            )
            val randomReply = partnerReplies[Random.nextInt(partnerReplies.size)]
            dao.insertMessage(ChatMessage(sender = "partner", text = randomReply))
            
            // Advance Chat Streak Goal
            incrementGoalProgress(1)
        }
    }

    fun submitPromptAnswer(promptId: Int, answer: String) {
        if (answer.isBlank()) return
        viewModelScope.launch {
            val all = prompts.value
            val target = all.find { it.id == promptId }
            if (target != null) {
                val updated = target.copy(
                    answerUser = answer,
                    isAnsweredUser = true
                )
                dao.updatePrompt(updated)
                
                // Increase plant growth or connection metrics!
                val state = coupleState.value
                val newGrowth = (state.plantGrowth + 10).coerceAtMost(100)
                dao.updateCoupleState(state.copy(plantGrowth = newGrowth))
            }
        }
    }

    fun addMemory(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            dao.insertMemory(Memory(title = title, content = content))
            
            // Increment date nights/memories goal progress
            incrementGoalProgress(2)
        }
    }

    private suspend fun incrementGoalProgress(goalId: Int) {
        val currentList = goals.value
        val target = currentList.find { it.id == goalId }
        if (target != null && target.current < target.target) {
            val updated = target.copy(current = target.current + 1)
            dao.updateGoal(updated)
            
            // If goal completed (current reaches target), boost pet happiness!
            if (updated.current == updated.target) {
                val state = coupleState.value
                val newHappiness = (state.petHappiness + 15).coerceAtMost(100)
                dao.updateCoupleState(state.copy(petHappiness = newHappiness))
            }
        }
    }

    // Mood Synchronization
    fun changeUserMood(newMood: String) {
        viewModelScope.launch {
            val state = coupleState.value
            dao.updateCoupleState(state.copy(userMood = newMood))
        }
    }

    // Poke / Nudge system
    fun sendPoke() {
        viewModelScope.launch {
            // Shake/vibrate user device to show visual reaction
            triggerVibration()
            
            // Simulate the partner immediately poking back after a brief delay
            delay(1500)
            val partnerPokes = listOf(
                "👉 Partner sent you a tiny Tickle Poke! *Giggle*",
                "💖 Partner blew you a warm fuzzy Kiss Nudge!",
                "🦖 Partner sent a friendly T-Rex Roar Nudge!",
                "✨ Partner showered you with Pixie Dust Sparkles!",
                "🍩 Partner sent a sweet virtual Donut Poke!"
            )
            _activePoke.value = partnerPokes[Random.nextInt(partnerPokes.size)]
            triggerVibration()
            
            // Clear poke announcement after 3 seconds
            delay(3500)
            _activePoke.value = null
        }
    }

    fun dismissPoke() {
        _activePoke.value = null
    }

    // Decision Wheel Spin
    fun spinDecisionWheel(options: List<String>) {
        if (options.isEmpty() || _isSpinning.value) return
        viewModelScope.launch {
            _isSpinning.value = true
            _wheelTargetIndex.value = null
            
            // Simulate realistic rotational spinning delay
            delay(2000)
            
            val target = Random.nextInt(options.size)
            _wheelTargetIndex.value = target
            _isSpinning.value = false
            
            // Send match choice message to the chat so the choice is immortalized!
            val selectedOption = options[target]
            dao.insertMessage(ChatMessage(
                sender = "me",
                text = "🎯 I spun the Decision Wheel and it landed on: '$selectedOption'! Let's do that! 🥰"
            ))
            
            // Simulate partner agreement
            delay(1000)
            dao.insertMessage(ChatMessage(
                sender = "partner",
                text = "Perfect! I'm totally down for '$selectedOption'! 🥳✨"
            ))
        }
    }

    // Toggle Ghost Mode (Severs / anonymizes your shared stats instantly)
    fun toggleGhostMode() {
        viewModelScope.launch {
            val state = coupleState.value
            val nextGhostMode = !state.partnerGhostMode
            dao.updateCoupleState(state.copy(partnerGhostMode = nextGhostMode))
        }
    }

    // Virtual World Gardening and Feeding Interactions
    fun waterPlant() {
        viewModelScope.launch {
            val state = coupleState.value
            val newGrowth = (state.plantGrowth + 10).coerceAtMost(100)
            dao.updateCoupleState(state.copy(plantGrowth = newGrowth))
            dao.insertMessage(ChatMessage(sender = "me", text = "💦 I watered our digital Love Flower in the virtual room! It's growing beautifully! 🌱"))
        }
    }

    fun feedPet() {
        viewModelScope.launch {
            val state = coupleState.value
            val newHappiness = (state.petHappiness + 12).coerceAtMost(100)
            dao.updateCoupleState(state.copy(petHappiness = newHappiness))
            dao.insertMessage(ChatMessage(sender = "me", text = "🍖 I fed our little digital puppy in the virtual room! He's doing a happy dance! 🐶❤️"))
        }
    }

    // Avatar action synchronization
    fun setAvatarAction(action: String) {
        viewModelScope.launch {
            val state = coupleState.value
            dao.updateCoupleState(state.copy(avatarUserAction = action))
            
            // Partner reacts or participates after a delay!
            delay(1500)
            val partnerAction = when (action) {
                "sleeping" -> {
                    dao.insertMessage(ChatMessage(sender = "partner", text = "Zzz... I saw you went to sleep in our virtual bedroom. Snuggling up next to you! 😴🛌"))
                    "sleeping"
                }
                "cooking" -> {
                    dao.insertMessage(ChatMessage(sender = "partner", text = "Ooh, cooking together in our virtual kitchen! I'll chop the vegetables! 🥕🍳"))
                    "cooking"
                }
                "watching_tv" -> {
                    dao.insertMessage(ChatMessage(sender = "partner", text = "Screen sharing in the virtual Theatre! Watching a movie with you on the giant screen! 🍿🎬"))
                    "watching_tv"
                }
                else -> "idle"
            }
            val latestState = dao.getCoupleStateOnce() ?: state
            dao.updateCoupleState(latestState.copy(avatarPartnerAction = partnerAction))
        }
    }

    // Weather change
    fun changeWeather(weather: String) {
        viewModelScope.launch {
            val state = coupleState.value
            dao.updateCoupleState(state.copy(weatherCondition = weather))
        }
    }

    // Radio Track changes
    fun toggleRadio() {
        viewModelScope.launch {
            val state = coupleState.value
            val nextPlaying = !state.radioIsPlaying
            dao.updateCoupleState(state.copy(radioIsPlaying = nextPlaying))
            if (nextPlaying) {
                dao.insertMessage(ChatMessage(sender = "me", text = "📻 I started streaming '${state.radioTrackName}' on our Synced Radio! Let's listen together! 🎵"))
            }
        }
    }

    fun changeRadioTrack(track: String) {
        viewModelScope.launch {
            val state = coupleState.value
            dao.updateCoupleState(state.copy(radioTrackName = track, radioIsPlaying = true))
            dao.insertMessage(ChatMessage(sender = "me", text = "📻 I changed our Synced Radio track to '${track}'! 🎶"))
        }
    }

    // Wishlist Methods
    fun addWishlistItem(name: String, price: String, link: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertWishlistItem(WishlistItem(name = name, price = price, link = link, addedBy = "me"))
            
            // Auto response from partner after a brief delay
            delay(1200)
            dao.insertMessage(ChatMessage(sender = "partner", text = "Ooh! I saw you added '$name' ($price) to your gift wishlist! Saving it for my secret plans... 😉🎁"))
        }
    }

    fun removeWishlistItem(id: Int) {
        viewModelScope.launch {
            dao.deleteWishlistItem(id)
        }
    }

    // Bucket List completion
    fun toggleBucketListCompleted(postcardId: Int) {
        viewModelScope.launch {
            val current = bucketList.value.find { it.id == postcardId }
            if (current != null) {
                val updated = current.copy(
                    isCompleted = !current.isCompleted,
                    completedDate = if (!current.isCompleted) "June 2026" else ""
                )
                dao.updateBucketList(updated)
                
                if (updated.isCompleted) {
                    dao.insertMessage(ChatMessage(sender = "me", text = "🏞️ We checked off another bucket list dream: '${updated.title}'! Our love is an adventure! 💖"))
                }
            }
        }
    }

    // Audio/Video Calling Call state simulation
    fun startCall(type: String) {
        viewModelScope.launch {
            triggerVibration()
            _activeCallType.value = type
            dao.insertMessage(ChatMessage(sender = "me", text = "📞 Initiated a ${type.uppercase()} call stream... connecting via WebRTC..."))
            
            // Auto accept from partner
            delay(2000)
            dao.insertMessage(ChatMessage(sender = "partner", text = "Connected to our WebRTC room! Hey honey! 😍"))
        }
    }

    fun endCall() {
        viewModelScope.launch {
            _activeCallType.value = null
            dao.insertMessage(ChatMessage(sender = "me", text = "❌ Ended peer-to-peer call stream."))
        }
    }

    // Emergency SOS Trigger
    fun triggerEmergencySOS() {
        viewModelScope.launch {
            triggerVibration()
            // High priority message
            dao.insertMessage(ChatMessage(sender = "me", text = "🚨 [EMERGENCY SOS] Darling, I triggered the emergency safe word! Please check on me! Current Location: Cozy Library 📚"))
            
            // Immediate partner reaction
            delay(1000)
            dao.insertMessage(ChatMessage(sender = "partner", text = "⚠️ Darling! I saw the emergency alarm! I'm calling you right now! Are you okay?? 😭❤️"))
        }
    }

    // Trigger local haptic vibration
    private fun triggerVibration() {
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        }
    }
}
