package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.TaskDao
import com.example.smartcompanionapp.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val dao: TaskDao,
    private val firestore: FirebaseFirestore
) {
    // We will use a nested structure: users/{userId}/tasks/{taskId}
    // to ensure data isolation and avoid ID collisions between different users.
    private fun getUserTasksCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("tasks")

    fun getAllTasks(userId: String): Flow<List<Task>> = dao.getAllTasks(userId)

    suspend fun insertTask(task: Task) {
        // Capture the generated ID from Room
        val generatedId = dao.insertTask(task)
        // Update the task object with the generated ID for Firestore sync
        val taskWithId = task.copy(id = generatedId.toInt())
        // Sync to Firestore
        syncTaskToFirestore(taskWithId)
    }

    suspend fun updateTask(task: Task) {
        dao.updateTask(task)
        // Sync to Firestore
        syncTaskToFirestore(task)
    }

    suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
        // Sync to Firestore (delete)
        try {
            getUserTasksCollection(task.userId)
                .document(task.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle or log error
        }
    }

    private suspend fun syncTaskToFirestore(task: Task) {
        try {
            // Using ID as document path within the user's specific collection
            getUserTasksCollection(task.userId)
                .document(task.id.toString())
                .set(task)
                .await()
        } catch (e: Exception) {
            // Handle or log error
        }
    }
    
    // Refresh local Room DB from the user's specific Firestore collection
    suspend fun refreshTasksFromFirestore(userId: String) {
        try {
            val snapshot = getUserTasksCollection(userId).get().await()
            val remoteTasks = snapshot.toObjects(Task::class.java)
            remoteTasks.forEach { dao.insertTask(it) }
        } catch (e: Exception) {
            // Handle or log error
        }
    }
}
