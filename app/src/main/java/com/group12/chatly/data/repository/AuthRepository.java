package com.group12.chatly.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.data.model.User;

/**
 * Repository responsible for user authentication and user profile management in Firebase.
 */
public class AuthRepository {

    private static final String COLLECTION_USERS = "users";
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
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed.";
                        callback.onFailure(errorMsg);
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Failed to retrieve user after registration.");
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
                                .addOnSuccessListener(aVoid -> callback.onSuccess(newUser))
                                .addOnFailureListener(e -> {
                                    // Even if Firestore write encounters an error, account is created in Auth
                                    callback.onSuccess(newUser);
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
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign in failed.";
                        callback.onFailure(errorMsg);
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        callback.onFailure("User profile not found.");
                        return;
                    }

                    getCurrentUserProfile(callback);
                });
    }

    /**
     * Fetches the current user profile from Firestore.
     */
    public void getCurrentUserProfile(@NonNull AuthCallback<User> callback) {
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
                                callback.onSuccess(user);
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
                        callback.onSuccess(fallbackUser);
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to load user profile.";
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Signs out the current user.
     */
    public void logout() {
        firebaseAuth.signOut();
    }
}
