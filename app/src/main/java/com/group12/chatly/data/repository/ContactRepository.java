package com.group12.chatly.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.group12.chatly.data.model.Friendship;
import com.group12.chatly.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository managing contacts, user search, and friendship requests.
 */
public class ContactRepository {

    public static final String COLLECTION_FRIENDSHIPS = "friendships";
    public static final String COLLECTION_USERS = "users";

    private static volatile ContactRepository instance;
    private final FirebaseFirestore firestore;

    public ContactRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public static ContactRepository getInstance() {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null) {
                    instance = new ContactRepository(FirebaseFirestore.getInstance());
                }
            }
        }
        return instance;
    }

    /**
     * Builds a deterministic unique document ID for two users regardless of who initiated.
     */
    public static String getCompositeFriendshipId(@NonNull String userId1, @NonNull String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * Searches users by email, phone, or display name prefix.
     */
    public void searchUsers(@NonNull String query,
                            @Nullable String currentUserId,
                            @NonNull AuthCallback<List<User>> callback) {
        final String sanitizedQuery = query.trim();
        if (sanitizedQuery.isEmpty()) {
            callback.onSuccess(Collections.emptyList());
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .orderBy("email")
                .startAt(sanitizedQuery)
                .endAt(sanitizedQuery + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> results = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null && (currentUserId == null || !user.getUserId().equals(currentUserId))) {
                            results.add(user);
                        }
                    }

                    if (!results.isEmpty()) {
                        callback.onSuccess(results);
                        return;
                    }

                    // Fallback to searching by displayName
                    firestore.collection(COLLECTION_USERS)
                            .orderBy("displayName")
                            .startAt(sanitizedQuery)
                            .endAt(sanitizedQuery + "\uf8ff")
                            .limit(20)
                            .get()
                            .addOnSuccessListener(nameSnapshot -> {
                                List<User> nameResults = new ArrayList<>();
                                for (DocumentSnapshot doc : nameSnapshot.getDocuments()) {
                                    User user = doc.toObject(User.class);
                                    if (user != null && (currentUserId == null || !user.getUserId().equals(currentUserId))) {
                                        nameResults.add(user);
                                    }
                                }
                                callback.onSuccess(nameResults);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Sends a friend request to target user.
     */
    public void sendFriendRequest(@NonNull String currentUserId,
                                  @NonNull String targetUserId,
                                  @NonNull AuthCallback<Friendship> callback) {
        if (currentUserId.equals(targetUserId)) {
            callback.onFailure("Cannot send friend request to yourself.");
            return;
        }

        String friendshipId = getCompositeFriendshipId(currentUserId, targetUserId);

        firestore.collection(COLLECTION_FRIENDSHIPS)
                .document(friendshipId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Friendship existing = snapshot.toObject(Friendship.class);
                        if (existing != null) {
                            if (Friendship.STATUS_ACCEPTED.equals(existing.getStatus())) {
                                callback.onFailure("You are already friends with this user.");
                                return;
                            } else if (Friendship.STATUS_PENDING.equals(existing.getStatus())) {
                                if (existing.getRequesterId().equals(currentUserId)) {
                                    callback.onFailure("Friend request already sent.");
                                } else {
                                    // The other user already requested friendship -> auto-accept
                                    acceptFriendRequest(friendshipId, new AuthCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            existing.setStatus(Friendship.STATUS_ACCEPTED);
                                            callback.onSuccess(existing);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            callback.onFailure(errorMessage);
                                        }
                                    });
                                }
                                return;
                            }
                        }
                    }

                    // Create new friend request
                    Friendship friendship = new Friendship(friendshipId, currentUserId, targetUserId, Friendship.STATUS_PENDING);
                    firestore.collection(COLLECTION_FRIENDSHIPS)
                            .document(friendshipId)
                            .set(friendship)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(friendship))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Accepts a pending friend request.
     */
    public void acceptFriendRequest(@NonNull String friendshipId, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_FRIENDSHIPS)
                .document(friendshipId)
                .update("status", Friendship.STATUS_ACCEPTED, "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Rejects or removes a friendship.
     */
    public void removeFriendship(@NonNull String friendshipId, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_FRIENDSHIPS)
                .document(friendshipId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Gets pending friend requests received by current user.
     */
    public void getPendingReceivedRequests(@NonNull String currentUserId, @NonNull AuthCallback<List<Friendship>> callback) {
        firestore.collection(COLLECTION_FRIENDSHIPS)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", Friendship.STATUS_PENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Friendship> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Friendship f = doc.toObject(Friendship.class);
                        if (f != null) list.add(f);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Realtime listener for accepted friendships where current user is requester.
     */
    public ListenerRegistration listenAcceptedAsRequester(@NonNull String currentUserId,
                                                          @NonNull AuthCallback<List<Friendship>> callback) {
        return firestore.collection(COLLECTION_FRIENDSHIPS)
                .whereEqualTo("requesterId", currentUserId)
                .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    List<Friendship> list = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Friendship f = doc.toObject(Friendship.class);
                            if (f != null) list.add(f);
                        }
                    }
                    callback.onSuccess(list);
                });
    }

    /**
     * Realtime listener for accepted friendships where current user is receiver.
     */
    public ListenerRegistration listenAcceptedAsReceiver(@NonNull String currentUserId,
                                                         @NonNull AuthCallback<List<Friendship>> callback) {
        return firestore.collection(COLLECTION_FRIENDSHIPS)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    List<Friendship> list = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Friendship f = doc.toObject(Friendship.class);
                            if (f != null) list.add(f);
                        }
                    }
                    callback.onSuccess(list);
                });
    }

    /**
     * Fetches User documents by list of user IDs.
     */
    public void getUsersByIds(@NonNull List<String> userIds, @NonNull AuthCallback<List<User>> callback) {
        if (userIds.isEmpty()) {
            callback.onSuccess(Collections.emptyList());
            return;
        }

        // Firestore 'whereIn' supports up to 30 items
        List<String> batch = userIds.subList(0, Math.min(userIds.size(), 30));
        firestore.collection(COLLECTION_USERS)
                .whereIn("userId", batch)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) users.add(u);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
