package com.quadrigasoftware

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreUserRepository(
    private val db: Firestore = FirestoreOptions.getDefaultInstance().service,
    private val collectionName: String = "users"
) : UserRepository {

    override suspend fun getUser(email: String): UserRecord? = withContext(Dispatchers.IO) {
        val docRef = db.collection(collectionName).document(email)
        val snapshot = docRef.get().get() // Using Java Future .get()
        
        if (snapshot.exists()) {
            val roles = (snapshot.get("roles") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            val permissions = (snapshot.get("permissions") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            @Suppress("UNCHECKED_CAST")
            val metadata = (snapshot.get("metadata") as? Map<String, *>)?.filterValues { it is String } as? Map<String, String> ?: emptyMap()
            
            UserRecord(
                email = email,
                roles = roles,
                permissions = permissions,
                metadata = metadata
            )
        } else {
            null
        }
    }

    override suspend fun saveUser(user: UserRecord): Unit = withContext(Dispatchers.IO) {
        val docRef = db.collection(collectionName).document(user.email)
        val data = mapOf(
            "roles" to user.roles.toList(),
            "permissions" to user.permissions.toList(),
            "metadata" to user.metadata
        )
        docRef.set(data, SetOptions.merge()).get()
    }

    override suspend fun deleteUser(email: String): Unit = withContext(Dispatchers.IO) {
        db.collection(collectionName).document(email).delete().get()
    }

    override suspend fun getAllUsers(): List<UserRecord> = withContext(Dispatchers.IO) {
        val querySnapshot = db.collection(collectionName).get().get()
        querySnapshot.documents.map { doc ->
            val email = doc.id
            val roles = (doc.get("roles") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            val permissions = (doc.get("permissions") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
            @Suppress("UNCHECKED_CAST")
            val metadata = (doc.get("metadata") as? Map<String, *>)?.filterValues { it is String } as? Map<String, String> ?: emptyMap()
            
            UserRecord(
                email = email,
                roles = roles,
                permissions = permissions,
                metadata = metadata
            )
        }
    }
}
