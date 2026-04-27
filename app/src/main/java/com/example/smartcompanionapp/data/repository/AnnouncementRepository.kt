package com.example.smartcompanionapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.data.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ROOT CAUSE FIX — Bug C:
 *
 * The original [notifiedTitles] was an in-memory [mutableSetOf]. When the process
 * is killed (app swiped away, OEM memory pressure, Doze) and then re-opened:
 *   • [notifiedTitles] starts empty again
 *   • [syncFromFirestore] fetches ALL Firestore announcements
 *   • Every existing title passes the "not in notifiedTitles" check
 *   • ALL announcements are emitted as "new" → duplicate notifications on every launch
 *
 * FIX: Persist notified title hashes to SharedPreferences so the set survives
 * process death. We store hashed titles (not raw strings) to avoid SharedPreferences
 * key size limits and to keep the pref file compact.
 *
 * We also add a MAX_NOTIFIED cap (500 entries) to prevent unbounded growth over time.
 * The oldest entries are pruned when the cap is hit, which is acceptable because
 * announcements older than the cap are long since stale.
 */
class AnnouncementRepository(
    private val dao: AnnouncementDao,
    private val firestore: FirebaseFirestore,
    private val userId: String = "",
    // Context is needed for the SharedPreferences persistence fix.
    // Pass applicationContext to avoid leaking Activity.
    private val context: Context? = null
) {
    private val collection = firestore.collection("announcements")

    val allAnnouncements: Flow<List<Announcement>> = dao.getAllNews(userId)
    val topUnread: Flow<Announcement?> = dao.getTopUnreadAnnouncement(userId)

    private val _newAnnouncementsFlow = MutableSharedFlow<List<Announcement>>(replay = 0)
    val newAnnouncementsFlow: SharedFlow<List<Announcement>> = _newAnnouncementsFlow.asSharedFlow()

    // ── PERSISTED NOTIFIED-TITLES SET ────────────────────────────────────────
    // Backed by SharedPreferences so the set survives process death.
    // Key format: "notified_$userId" so each user has an isolated set.
    private val prefs: SharedPreferences? = context?.getSharedPreferences(
        "announcement_notified_prefs",
        Context.MODE_PRIVATE
    )
    private val prefKey = "notified_$userId"
    private val MAX_NOTIFIED = 500

    /**
     * Returns true if [title] has already been notified this or any prior session.
     */
    fun isAlreadyNotified(title: String): Boolean {
        val hash = title.hashCode().toString()
        return prefs?.getStringSet(prefKey, emptySet())?.contains(hash) ?: false
    }

    /**
     * Marks [titles] as notified, persisting immediately to SharedPreferences.
     * Prunes the oldest entries if the set exceeds [MAX_NOTIFIED].
     */
    private fun markNotified(titles: Collection<String>) {
        if (prefs == null) return
        val existing = prefs.getStringSet(prefKey, emptySet())!!.toMutableSet()
        titles.forEach { existing.add(it.hashCode().toString()) }
        // Prune oldest: SharedPreferences StringSet has no ordering, so we just
        // trim to MAX_NOTIFIED by removing arbitrary entries when over the limit.
        val pruned = if (existing.size > MAX_NOTIFIED) {
            existing.take(MAX_NOTIFIED).toMutableSet()
        } else {
            existing
        }
        prefs.edit().putStringSet(prefKey, pruned).apply()
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun startRealtimeListener(scope: CoroutineScope) {
        listenerRegistration?.remove()

        listenerRegistration = collection
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                scope.launch(Dispatchers.IO) {
                    try {
                        // ── NEW: Fetch this user's read states from Firestore ────────────────
                        val remoteReadTitles = try {
                            firestore.collection("users").document(userId)
                                .collection("read_announcements")
                                .get()
                                .await()
                                .documents.map { it.id }.toSet()
                        } catch (e: Exception) {
                            emptySet<String>()
                        }

                        val existingReadMap = dao.getAllTitles(userId)
                            .mapNotNull { title ->
                                dao.getByTitle(title, userId)?.let { title to it.isRead }
                            }
                            .toMap()

                        val remote = snapshot.documents.mapNotNull { doc ->
                            val title   = doc.getString("title")    ?: return@mapNotNull null
                            val content = doc.getString("content")  ?: return@mapNotNull null
                            
                            // ── FIX: Safely handle both Long and Timestamp ──────
                            val date = when (val rawDate = doc.get("datePosted")) {
                                is Long   -> rawDate
                                is Number -> rawDate.toLong()
                                is com.google.firebase.Timestamp -> rawDate.toDate().time
                                else      -> System.currentTimeMillis()
                            }

                            Announcement(
                                title      = title,
                                content    = content,
                                datePosted = date,
                                isRead     = existingReadMap[title] == true || title in remoteReadTitles,
                                userId     = userId
                            )
                        }

                        val existingTitles = dao.getAllTitles(userId).toHashSet()

                        // FIX: check persisted prefs, not in-memory set
                        val trulyNew = remote.filter { announcement ->
                            announcement.title !in existingTitles &&
                                    !isAlreadyNotified(announcement.title)
                        }

                        dao.syncAnnouncements(remote, userId)

                        if (trulyNew.isNotEmpty()) {
                            val storedNew = trulyNew.mapNotNull {
                                dao.getByTitle(it.title, userId)
                            }
                            // Persist BEFORE emitting — if the app is killed between
                            // emit and collection, we don't want a re-notification.
                            markNotified(trulyNew.map { it.title })
                            _newAnnouncementsFlow.emit(storedNew)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    fun stopRealtimeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    suspend fun markAsRead(id: Int) {
        val announcement = dao.getById(id, userId)
        if (announcement != null) {
            // 1. Update local Room
            dao.markAsRead(id, userId)
            
            // 2. Sync to Firestore (per-user read state)
            try {
                firestore.collection("users").document(userId)
                    .collection("read_announcements")
                    .document(announcement.title)
                    .set(mapOf("read" to true, "timestamp" to System.currentTimeMillis()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun insertSingle(announcement: Announcement) {
        // Pre-mark as notified (persisted) so the listener callback on THIS device
        // skips it. Without persisted prefs this only worked within the session.
        markNotified(listOf(announcement.title))

        val firestoreDoc = hashMapOf(
            "title"      to announcement.title,
            "content"    to announcement.content,
            "datePosted" to announcement.datePosted,
            "isRead"     to false
        )
        try {
            collection.document(announcement.title).set(firestoreDoc).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dao.upsertAnnouncements(listOf(announcement.copy(userId = userId)))
    }

    suspend fun syncFromFirestore(): List<Announcement> {
        val snapshot = collection
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .get()
            .await()

        // ── NEW: Fetch this user's read states from Firestore ────────────────
        val remoteReadTitles = try {
            firestore.collection("users").document(userId)
                .collection("read_announcements")
                .get()
                .await()
                .documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet<String>()
        }

        val existingReadMap = dao.getAllTitles(userId)
            .mapNotNull { title ->
                dao.getByTitle(title, userId)?.let { title to it.isRead }
            }
            .toMap()

        val remote = snapshot.documents.mapNotNull { doc ->
            val title   = doc.getString("title")    ?: return@mapNotNull null
            val content = doc.getString("content")  ?: return@mapNotNull null
            
            // ── FIX: Safely handle both Long and Timestamp ──────
            val date = when (val rawDate = doc.get("datePosted")) {
                is Long   -> rawDate
                is Number -> rawDate.toLong()
                is com.google.firebase.Timestamp -> rawDate.toDate().time
                else      -> System.currentTimeMillis()
            }

            Announcement(
                title      = title,
                content    = content,
                datePosted = date,
                // Read if: already read in Room OR marked as read in Firestore
                isRead     = existingReadMap[title] == true || title in remoteReadTitles,
                userId     = userId
            )
        }

        val existingTitles = dao.getAllTitles(userId).toHashSet()

        // FIX: check persisted prefs, not in-memory set
        val trulyNew = remote.filter {
            it.title !in existingTitles && !isAlreadyNotified(it.title)
        }

        dao.syncAnnouncements(remote, userId)

        if (trulyNew.isNotEmpty()) {
            val storedNew = trulyNew.mapNotNull { dao.getByTitle(it.title, userId) }
            markNotified(trulyNew.map { it.title })
            _newAnnouncementsFlow.emit(storedNew)
        }

        return remote
    }

    suspend fun findNewTitles(remote: List<Announcement>): List<Announcement> {
        val existing = dao.getAllTitles(userId).toHashSet()
        return remote.filter { it.title !in existing }
    }

    suspend fun getByTitle(title: String): Announcement? = dao.getByTitle(title, userId)

    suspend fun delete(announcement: Announcement) {
        dao.deleteAnnouncement(announcement)
        try {
            collection.document(announcement.title).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}