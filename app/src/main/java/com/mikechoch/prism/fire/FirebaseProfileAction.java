package com.mikechoch.prism.fire;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.constant.Key;
import com.mikechoch.prism.constant.Message;
import com.mikechoch.prism.fire.callback.OnFetchEmailForUsernameCallback;
import com.mikechoch.prism.fire.callback.OnFirebaseUserRegistrationCallback;
import com.mikechoch.prism.fire.callback.OnPrismUserProfileExistCallback;
import com.mikechoch.prism.fire.callback.OnPrismUserRegistrationCallback;
import com.mikechoch.prism.fire.callback.OnUsernameTakenCallback;
import com.mikechoch.prism.helper.ProfileHelper;

import java.util.HashMap;
import java.util.Map;

public class FirebaseProfileAction {

    /**
     *
     * @param username
     * @param callback
     */
    public static void isUsernameTaken(String username, OnUsernameTakenCallback callback) {
        DatabaseReference accountsReference = Default.ACCOUNT_REFERENCE;

        accountsReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usernameSnapshot) {
                boolean usernameTaken = usernameSnapshot.exists();
                callback.onSuccess(usernameTaken);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure();
            }
        });
    }

    /**
     *
     * @param user
     * @param fullname
     * @param username
     * @param callback
     */
    public static void createPrismUserInFirebase(FirebaseUser user, String fullname, String username, OnPrismUserRegistrationCallback callback) {
        DatabaseReference accountReference = Default.ACCOUNT_REFERENCE;
        DatabaseReference currentUserReference = Default.USERS_REFERENCE.child(user.getUid());
        DatabaseReference notificationPreference = currentUserReference.child(Key.DB_REF_USER_PREFERENCES);
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : ProfileHelper.generateDefaultProfilePic();

        Map<String, Object> profileMap = new HashMap<String, Object>() {{
            put(Key.USER_PROFILE_FULL_NAME, fullname);
            put(Key.USER_PROFILE_USERNAME, username);
            put(Key.USER_PROFILE_PIC, photoUrl);
        }};

        Map<String, Object> preferencesMap = new HashMap<String, Object>() {{
            put(Key.PREFERENCE_ALLOW_LIKE_NOTIFICATION, true);
            put(Key.PREFERENCE_ALLOW_REPOST_NOTIFICATION, true);
            put(Key.PREFERENCE_ALLOW_FOLLOW_NOTIFICATION, true);
        }};

        currentUserReference.updateChildren(profileMap);
        notificationPreference.updateChildren(preferencesMap);
        accountReference.child(username).setValue(user.getEmail());
        user.updateProfile(new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build());

        callback.onSuccess();
    }

    public static void doesUserHaveUsername(FirebaseUser firebaseUser, OnPrismUserProfileExistCallback callback) {
        DatabaseReference usersReference = Default.USERS_REFERENCE;
        usersReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot currentUserSnapshot) {
                callback.onSuccess(currentUserSnapshot.hasChild(Key.USER_PROFILE_USERNAME));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure();
            }
        });
    }

    public static void registerUserWithEmailAndPassword(String email, String password, OnFirebaseUserRegistrationCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    callback.onSuccess(user);
                } else {
                    Log.e(Default.TAG_DB, Message.USER_ACCOUNT_CREATION_FAIL);
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException weakPassword) {
                        callback.onFailure(weakPassword);
                    } catch (FirebaseAuthInvalidCredentialsException invalidEmail) {
                        callback.onFailure(invalidEmail);
                    } catch (FirebaseAuthUserCollisionException existEmail) {
                        callback.onFailure(existEmail);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }


    public static void fetchEmailForUsername(String username, OnFetchEmailForUsernameCallback callback) {
        DatabaseReference usernameAccountReference = Default.ACCOUNT_REFERENCE.child(username);
        usernameAccountReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usernameSnapshot) {
                if (usernameSnapshot.exists()) {
                    callback.onSuccess((String) usernameSnapshot.getValue());
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure();
            }
        });
    }

}