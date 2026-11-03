package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.group12.chatly.data.model.Friendship;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ContactRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel managing Contacts, Search Users, and Friendship invitations.
 */
public class ContactViewModel extends ViewModel {

    private final ContactRepository contactRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<AuthState<List<User>>> searchResultState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<List<User>>> friendsListState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<List<Friendship>>> pendingRequestsState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<Friendship>> sendRequestState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<Void>> actionState = new MutableLiveData<>(AuthState.idle());

    private ListenerRegistration requesterListener;
    private ListenerRegistration receiverListener;
    private final List<Friendship> cachedRequesterFriendships = new ArrayList<>();
    private final List<Friendship> cachedReceiverFriendships = new ArrayList<>();

    public ContactViewModel() {
        this(ContactRepository.getInstance(), AuthRepository.getInstance());
    }

    public ContactViewModel(@NonNull ContactRepository contactRepository, @NonNull AuthRepository authRepository) {
        this.contactRepository = contactRepository;
        this.authRepository = authRepository;
    }

    public LiveData<AuthState<List<User>>> getSearchResultState() {
        return searchResultState;
    }

    public LiveData<AuthState<List<User>>> getFriendsListState() {
        return friendsListState;
    }

    public LiveData<AuthState<List<Friendship>>> getPendingRequestsState() {
        return pendingRequestsState;
    }

    public LiveData<AuthState<Friendship>> getSendRequestState() {
        return sendRequestState;
    }

    public LiveData<AuthState<Void>> getActionState() {
        return actionState;
    }

    /**
     * Search users by keyword (Email or Display Name).
     */
    public void searchUsers(@NonNull String query) {
        String currentUid = authRepository.getCurrentUserId();
        searchResultState.setValue(AuthState.loading());
        contactRepository.searchUsers(query, currentUid, new AuthCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                searchResultState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                searchResultState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Sends friend request to target user.
     */
    public void sendFriendRequest(@NonNull String targetUserId) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) {
            sendRequestState.setValue(AuthState.error("User is not authenticated."));
            return;
        }

        sendRequestState.setValue(AuthState.loading());
        contactRepository.sendFriendRequest(currentUid, targetUserId, new AuthCallback<Friendship>() {
            @Override
            public void onSuccess(Friendship result) {
                sendRequestState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                sendRequestState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Accepts received friend request.
     */
    public void acceptFriendRequest(@NonNull String friendshipId) {
        actionState.setValue(AuthState.loading());
        contactRepository.acceptFriendRequest(friendshipId, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                actionState.setValue(AuthState.success(null));
                loadPendingRequests();
            }

            @Override
            public void onFailure(String errorMessage) {
                actionState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Rejects or removes a friend.
     */
    public void removeFriendship(@NonNull String friendshipId) {
        actionState.setValue(AuthState.loading());
        contactRepository.removeFriendship(friendshipId, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                actionState.setValue(AuthState.success(null));
                loadPendingRequests();
            }

            @Override
            public void onFailure(String errorMessage) {
                actionState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Loads pending friend requests received by current user.
     */
    public void loadPendingRequests() {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        pendingRequestsState.setValue(AuthState.loading());
        contactRepository.getPendingReceivedRequests(currentUid, new AuthCallback<List<Friendship>>() {
            @Override
            public void onSuccess(List<Friendship> result) {
                pendingRequestsState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                pendingRequestsState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Starts listening to accepted friends realtime.
     */
    public void startListeningToFriends() {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        stopListening();
        friendsListState.setValue(AuthState.loading());

        requesterListener = contactRepository.listenAcceptedAsRequester(currentUid, new AuthCallback<List<Friendship>>() {
            @Override
            public void onSuccess(List<Friendship> result) {
                cachedRequesterFriendships.clear();
                cachedRequesterFriendships.addAll(result);
                combineAndFetchFriendProfiles(currentUid);
            }

            @Override
            public void onFailure(String errorMessage) {
                friendsListState.setValue(AuthState.error(errorMessage));
            }
        });

        receiverListener = contactRepository.listenAcceptedAsReceiver(currentUid, new AuthCallback<List<Friendship>>() {
            @Override
            public void onSuccess(List<Friendship> result) {
                cachedReceiverFriendships.clear();
                cachedReceiverFriendships.addAll(result);
                combineAndFetchFriendProfiles(currentUid);
            }

            @Override
            public void onFailure(String errorMessage) {
                friendsListState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    private void combineAndFetchFriendProfiles(String currentUid) {
        Set<String> friendIds = new HashSet<>();
        for (Friendship f : cachedRequesterFriendships) {
            friendIds.add(f.getReceiverId());
        }
        for (Friendship f : cachedReceiverFriendships) {
            friendIds.add(f.getRequesterId());
        }
        friendIds.remove(currentUid);

        if (friendIds.isEmpty()) {
            friendsListState.setValue(AuthState.success(new ArrayList<>()));
            return;
        }

        contactRepository.getUsersByIds(new ArrayList<>(friendIds), new AuthCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                friendsListState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                friendsListState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void stopListening() {
        if (requesterListener != null) {
            requesterListener.remove();
            requesterListener = null;
        }
        if (receiverListener != null) {
            receiverListener.remove();
            receiverListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
}
