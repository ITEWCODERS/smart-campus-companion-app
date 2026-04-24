package com.example.smartcompanionapp.data.repository

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

class AnnouncementRepository(
    private val dao: AnnouncementDao,
    private val firestore: FirebaseFirestore,
    private val userId: String = ""
) {
    private val collection = firestore.collection("announcements")

    val allAnnouncements: Flow<List<Announcement>> = dao.getAllNews(userId)
    val topUnread: Flow<Announcement?> = dao.getTopUnreadAnnouncement(userId)

    // ── NEW: emits announcements that are truly new to this device ────────────
    // ViewModel observes this and fires exactly ONE notification per new item.
    // Using SharedFlow (replay=0) so old events don't re-fire on re-subscription.
    private val _newAnnouncementsFlow = MutableSharedFlow<List<Announcement>>(replay = 0)
    val newAnnouncementsFlow: SharedFlow<List<Announcement>> = _newAnnouncementsFlow.asSharedFlow()

    // Tracks titles we've already notified about this session to prevent duplicates.
    // Persists in memory for the lifetime of the repository (= ViewModel lifetime).
    private val notifiedTitles = mutableSetOf<String>()

    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Starts the Firestore real-time listener.
     * Any change to the collection pushes a full snapshot instantly.
     * New announcements are diffed and emitted via [newAnnouncementsFlow].
     */
    fun startRealtimeListener(scope: CoroutineScope) {
        listenerRegistration?.remove()

        listenerRegistration = collection
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                scope.launch(Dispatchers.IO) {
                    try {
                        // Preserve existing read states
                        val existingReadMap = dao.getAllTitles(userId)
                            .mapNotNull { title ->
                                dao.getByTitle(title, userId)?.let { title to it.isRead }
                            }
                            .toMap()

                        val remote = snapshot.documents.mapNotNull { doc ->
                            val title   = doc.getString("title")    ?: return@mapNotNull null
                            val content = doc.getString("content")  ?: return@mapNotNull null
                            val date    = doc.getLong("datePosted") ?: System.currentTimeMillis()
                            Announcement(
                                title      = title,
                                content    = content,
                                datePosted = date,
                                isRead     = existingReadMap[title] ?: false,
                                userId     = userId
                            )
                        }

                        // Diff: which titles are new to Room AND not yet notified?
                        val existingTitles = dao.getAllTitles(userId).toHashSet()
                        val trulyNew = remote.filter { announcement ->
                            announcement.title !in existingTitles &&
                                    announcement.title !in notifiedTitles
                        }

                        // Write all to Room (upsert triggers allAnnouncements Flow)
                        dao.syncAnnouncements(remote, userId)

                        // Emit new items for notification — only if there are any
                        if (trulyNew.isNotEmpty()) {
                            // Fetch the stored versions (with auto-generated Room IDs)
                            val storedNew = trulyNew.mapNotNull {
                                dao.getByTitle(it.title, userId)
                            }
                            // Mark as notified BEFORE emitting to prevent race conditions
                            notifiedTitles.addAll(trulyNew.map { it.title })
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

    suspend fun markAsRead(id: Int) = dao.markAsRead(id, userId)

    /**
     * Writes a new announcement to Firestore + upserts locally.
     * The real-time listener will pick it up on ALL other devices automatically.
     * The poster's device gets the local upsert immediately (no listener round-trip needed).
     * We also mark the title as notified here so when the listener fires back on
     * the poster's device it does NOT emit a duplicate notification.
     */
    suspend fun insertSingle(announcement: Announcement) {
        // Pre-mark as notified so the listener callback on THIS device skips it
        notifiedTitles.add(announcement.title)

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

    /**
     * One-off manual sync (pull-to-refresh).
     * Does NOT trigger duplicate notifications because [notifiedTitles] guards them.
     */
    suspend fun syncFromFirestore(): List<Announcement> {
        val snapshot = collection
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .get()
            .await()

        val existingReadMap = dao.getAllTitles(userId)
            .mapNotNull { title ->
                dao.getByTitle(title, userId)?.let { title to it.isRead }
            }
            .toMap()

        val remote = snapshot.documents.mapNotNull { doc ->
            val title   = doc.getString("title")    ?: return@mapNotNull null
            val content = doc.getString("content")  ?: return@mapNotNull null
            val date    = doc.getLong("datePosted") ?: System.currentTimeMillis()
            Announcement(
                title      = title,
                content    = content,
                datePosted = date,
                isRead     = existingReadMap[title] ?: false,
                userId     = userId
            )
        }

        // Diff for notifications on first sync (e.g. existing announcements on login)
        val existingTitles = dao.getAllTitles(userId).toHashSet()
        val trulyNew = remote.filter { it.title !in existingTitles && it.title !in notifiedTitles }

        dao.syncAnnouncements(remote, userId)

        if (trulyNew.isNotEmpty()) {
            val storedNew = trulyNew.mapNotNull { dao.getByTitle(it.title, userId) }
            notifiedTitles.addAll(trulyNew.map { it.title })
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