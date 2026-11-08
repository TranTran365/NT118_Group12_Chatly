package com.group12.chatly.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.data.model.User;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository responsible for user authentication and user profile management in Firebase.
 */
public class AuthRepository {

    public static final String COLLECTION_USERS = "users";
    private static volatile AuthRepository instance;

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository(@NonNull FirebaseAuth firebaseAuth, @NonNull FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    public static AuthRepository getInstance() {
        if (instance == null) {
            synchronized (AuthRepository.class) {
                if (instance == null) {
                    instance = new AuthRepository(
                            FirebaseAuth.getInstance(),
                            FirebaseFirestore.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Check if a user is currently signed in.
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Returns the currently signed-in FirebaseUser.
     */
    @Nullable
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Returns the UID of the currently signed-in user, or null if none.
     */
    @Nullable
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Registers a new user with email, password, and display name.
     * Updates Firebase Auth profile and creates a document in Firestore 'users'.
     */
    public void registerWithEmail(@NonNull String fullName,
                                  @NonNull String email,
                                  @NonNull String password,
                                  @NonNull AuthCallback<User> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Registration failed.";
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure(errorMsg);
                        }
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure("Failed to retrieve user after registration.");
                        }
                        return;
                    }

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build();

                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                        User newUser = new User(firebaseUser.getUid(), fullName, email);

                        firestore.collection(COLLECTION_USERS)
                                .document(newUser.getUserId())
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    if (callbackDispatched.compareAndSet(false, true)) {
                                        callback.onSuccess(newUser);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    String errorMsg = e != null && e.getMessage() != null
                                            ? "Account created, but failed to save user profile: " + e.getMessage()
                                            : "Account created, but failed to save user profile in Firestore.";
                                    if (callbackDispatched.compareAndSet(false, true)) {
                                        callback.onFailure(errorMsg);
                                    }
                                });
                    });
                });
    }

    /**
     * Signs in an existing user with email and password.
     */
    public void loginWithEmail(@NonNull String email,
                               @NonNull String password,
                               @NonNull AuthCallback<User> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Sign in failed.";
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure(errorMsg);
                        }
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure("User profile not found.");
                        }
                        return;
                    }

                    getCurrentUserProfile(new AuthCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            if (callbackDispatched.compareAndSet(false, true)) {
                                callback.onSuccess(result);
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            if (callbackDispatched.compareAndSet(false, true)) {
                                callback.onFailure(errorMessage);
                            }
                        }
                    });
                });
    }

    /**
     * Signs in with Google using ID Token.
     * If user document does not exist in Firestore, creates it automatically.
     */
    public void signInWithGoogleIdToken(@NonNull String idToken,
                                        @NonNull AuthCallback<User> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Google sign-in failed.";
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure(errorMsg);
                        }
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure("User not found after Google authentication.");
                        }
                        return;
                    }

                    // Check if document already exists in Firestore
                    firestore.collection(COLLECTION_USERS)
                            .document(firebaseUser.getUid())
                            .get()
                            .addOnCompleteListener(getTask -> {
                                if (getTask.isSuccessful() && getTask.getResult() != null && getTask.getResult().exists()) {
                                    User existingUser = getTask.getResult().toObject(User.class);
                                    if (existingUser != null) {
                                        if (callbackDispatched.compareAndSet(false, true)) {
                                            callback.onSuccess(existingUser);
                                        }
                                        return;
                                    }
                                }

                                // First time login: Create new user profile in Firestore
                                String displayName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Chatly User";
                                String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                                User newUser = new User(firebaseUser.getUid(), displayName, email);
                                if (firebaseUser.getPhotoUrl() != null) {
                                    newUser.setAvatarUrl(firebaseUser.getPhotoUrl().toString());
                                }
                                if (firebaseUser.getPhoneNumber() != null) {
                                    newUser.setPhoneNumber(firebaseUser.getPhoneNumber());
                                }

                                firestore.collection(COLLECTION_USERS)
                                        .document(newUser.getUserId())
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            if (callbackDispatched.compareAndSet(false, true)) {
                                                callback.onSuccess(newUser);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (callbackDispatched.compareAndSet(false, true)) {
                                                callback.onFailure("Failed to initialize Google user profile in Firestore: " + e.getMessage());
                                            }
                                        });
                            });
                });
    }

    /**
     * Sends password reset email.
     */
    public void sendPasswordResetEmail(@NonNull String email,
                                       @NonNull AuthCallback<Void> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        if (email.trim().isEmpty()) {
            callback.onFailure("Email cannot be empty.");
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onSuccess(null);
                        }
                    } else {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Failed to send password reset email.";
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure(errorMsg);
                        }
                    }
                });
    }

    /**
     * Fetches the current user profile from Firestore.
     */
    public void getCurrentUserProfile(@NonNull AuthCallback<User> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure("No user is currently authenticated.");
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.exists()) {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                if (callbackDispatched.compareAndSet(false, true)) {
                                    callback.onSuccess(user);
                                }
                                return;
                            }
                        }
                        // Fallback user from Firebase Auth if Firestore document is missing
                        String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
                        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                        User fallbackUser = new User(firebaseUser.getUid(), name, email);
                        if (firebaseUser.getPhotoUrl() != null) {
                            fallbackUser.setAvatarUrl(firebaseUser.getPhotoUrl().toString());
                        }
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onSuccess(fallbackUser);
                        }
                    } else {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Failed to load user profile.";
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure(errorMsg);
                        }
                    }
                });
    }

    /**
     * Updates user profile attributes in Firestore.
     */
    public void updateUserProfile(@Nullable String displayName,
                                  @Nullable String bio,
                                  @Nullable String avatarUrl,
                                  @NonNull AuthCallback<Void> callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User is not authenticated.");
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        if (displayName != null) updates.put("displayName", displayName);
        if (bio != null) updates.put("bio", bio);
        if (avatarUrl != null) updates.put("avatarUrl", avatarUrl);

        if (updates.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage() != null ? e.getMessage() : "Failed to update profile."));
    }

    /**
     * Updates the user's online status and lastSeen timestamp.
     */
    public void updateOnlineStatus(boolean isOnline) {
        String uid = getCurrentUserId();
        if (uid == null) return;

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("isOnline", isOnline);
        updates.put("lastSeen", com.google.firebase.Timestamp.now());

        try {
            firestore.collection(COLLECTION_USERS)
                    .document(uid)
                    .update(updates);
        } catch (Exception ignored) {
        }
    }

    /**
     * Updates the FCM device token in the user's Firestore document.
     */
    public void updateFcmToken(@NonNull String token, @Nullable AuthCallback<Void> callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            if (callback != null) callback.onFailure("User is not authenticated.");
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e != null ? e.getMessage() : "Failed to update FCM token.");
                });
    }

    /**
     * Retrieves the current FCM token from FirebaseMessaging and syncs it to Firestore.
     */
    public void syncCurrentFcmToken() {
        String uid = getCurrentUserId();
        if (uid == null) return;

        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            updateFcmToken(token, null);
                        }
                    });
        } catch (Exception ignored) {
            // FirebaseApp might not be initialized in Unit tests
        }
    }

    /**
     * Signs out the current user.
     */
    public void logout() {
        updateOnlineStatus(false);
        firebaseAuth.signOut();
    }

    /**
     * Cập nhật thông tin profile (Tên, Bio, SĐT) lên Firestore.
     */
    public void updateProfileDetails(@NonNull String fullName,
                                  @NonNull String bio,
                                  @NonNull String phone,
                                  @NonNull AuthCallback<User> callback) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure("No authenticated user.");
            return;
        }

        String uid = firebaseUser.getUid();
        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update("displayName", fullName,
                        "bio", bio,
                        "phoneNumber", phone)
                .addOnSuccessListener(aVoid -> getCurrentUserProfile(callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Tải ảnh Avatar lên Firebase Storage và lưu URL vào Firestore.
     */
    public void uploadAvatar(@NonNull android.net.Uri imageUri,
                             @NonNull AuthCallback<String> callback) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure("No authenticated user.");
            return;
        }

        String uid = firebaseUser.getUid();
        com.google.firebase.storage.StorageReference avatarRef =
                com.google.firebase.storage.FirebaseStorage.getInstance()
                        .getReference().child("avatars/" + uid + ".jpg");

        avatarRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return avatarRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String downloadUrl = task.getResult().toString();
                        firestore.collection(COLLECTION_USERS)
                                .document(uid)
                                .update("avatarUrl", downloadUrl)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(downloadUrl))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Upload failed.";
                        callback.onFailure(msg);
                    }
                });
    }
}
