package com.medical.medicarepro.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.medical.medicarepro.models.HealthRecord;
import com.medical.medicarepro.models.Patient;
import com.medical.medicarepro.models.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private static final String TAG = "FirebaseManager";

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = auth.getCurrentUser();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // ==================== AUTHENTICATION METHODS ====================

    public void login(String email, String password, final FirebaseCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    currentUser = authResult.getUser();
                    callback.onSuccess(currentUser);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void loginWithUsername(String username, String password, final FirebaseCallback<User> callback) {
        Log.d(TAG, "Searching for username: " + username);

        databaseReference.child("users")
                .orderByChild("username")
                .equalTo(username)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    User foundUser = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
                            user.setId(snapshot.getKey());
                            foundUser = user;
                            Log.d(TAG, "User found: " + user.getName() + ", Role: " + user.getRole());
                            break;
                        }
                    }

                    final User finalFoundUser = foundUser;

                    if (finalFoundUser != null) {
                        // Also sign in with Firebase Auth using email
                        String email = finalFoundUser.getEmail();
                        if (email != null && !email.isEmpty()) {
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        currentUser = authResult.getUser();
                                        callback.onSuccess(finalFoundUser);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Still return user even if Firebase Auth fails
                                        callback.onSuccess(finalFoundUser);
                                    });
                        } else {
                            callback.onSuccess(finalFoundUser);
                        }
                    } else {
                        callback.onFailure(new Exception("Invalid username or password"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Database query failed", e);
                    callback.onFailure(new Exception("Database error: " + e.getMessage()));
                });
    }

    public void logout() {
        auth.signOut();
        currentUser = null;
    }

    public void getCurrentUserData(final FirebaseCallback<User> callback) {
        // Try to get from Firebase Auth first
        if (currentUser == null) {
            callback.onFailure(new Exception("No user logged in"));
            return;
        }

        String userEmail = currentUser.getEmail();
        Log.d(TAG, "Getting user data for email: " + userEmail);

        // Search for user by email in database
        databaseReference.child("users")
                .orderByChild("email")
                .equalTo(userEmail)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    User foundUser = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(snapshot.getKey());
                            foundUser = user;
                            Log.d(TAG, "User found in DB: " + user.getName() + ", Role: " + user.getRole());
                            break;
                        }
                    }

                    final User finalFoundUser = foundUser;

                    if (finalFoundUser != null) {
                        callback.onSuccess(finalFoundUser);
                    } else {
                        // Create default user if not found
                        User defaultUser = new User();
                        defaultUser.setName("Medical Officer");
                        defaultUser.setEmail(currentUser.getEmail());
                        defaultUser.setLocation("Central");
                        defaultUser.setRole("Medical Officer");
                        Log.d(TAG, "Using default user");
                        callback.onSuccess(defaultUser);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user data", e);
                    callback.onFailure(e);
                });
    }

    // ==================== PATIENT METHODS ====================

    public void getAllPatients(final DatabaseCallback<List<Patient>> callback) {
        databaseReference.child("patients")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Patient patient = snapshot.getValue(Patient.class);
                        if (patient != null) {
                            patient.setId(snapshot.getKey());
                            patients.add(patient);
                        }
                    }
                    callback.onSuccess(patients);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getPatientById(String patientId, final DatabaseCallback<Patient> callback) {
        databaseReference.child("patients").child(patientId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if (patient != null) {
                        patient.setId(dataSnapshot.getKey());
                    }
                    callback.onSuccess(patient);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void addPatient(Patient patient, final FirebaseCallback<String> callback) {
        String docId = databaseReference.child("patients").push().getKey();
        patient.setId(docId);

        Map<String, Object> patientMap = new HashMap<>();
        patientMap.put("id", docId);
        patientMap.put("firstName", patient.getFirstName());
        patientMap.put("lastName", patient.getLastName());
        patientMap.put("fullName", patient.getFullName());
        patientMap.put("nhn", patient.getNhn());
        patientMap.put("sex", patient.getSex());
        patientMap.put("dob", patient.getDob());
        patientMap.put("location", patient.getLocation());
        patientMap.put("createdAt", System.currentTimeMillis());

        if (patient.getBloodGroup() != null) patientMap.put("bloodGroup", patient.getBloodGroup());
        if (patient.getEthnicity() != null) patientMap.put("ethnicity", patient.getEthnicity());
        if (patient.getPhoneNumber() != null) patientMap.put("phoneNumber", patient.getPhoneNumber());
        if (patient.getEmail() != null) patientMap.put("email", patient.getEmail());
        if (patient.getAddress() != null) patientMap.put("address", patient.getAddress());
        if (patient.getSummary() != null) patientMap.put("summary", patient.getSummary());

        databaseReference.child("patients").child(docId).setValue(patientMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(docId))
                .addOnFailureListener(callback::onFailure);
    }

    public void updatePatient(Patient patient, final FirebaseCallback<Void> callback) {
        Map<String, Object> patientMap = new HashMap<>();
        patientMap.put("firstName", patient.getFirstName());
        patientMap.put("lastName", patient.getLastName());
        patientMap.put("fullName", patient.getFullName());
        patientMap.put("nhn", patient.getNhn());
        patientMap.put("sex", patient.getSex());
        patientMap.put("dob", patient.getDob());
        patientMap.put("location", patient.getLocation());
        patientMap.put("updatedAt", System.currentTimeMillis());

        if (patient.getBloodGroup() != null) patientMap.put("bloodGroup", patient.getBloodGroup());
        if (patient.getEthnicity() != null) patientMap.put("ethnicity", patient.getEthnicity());
        if (patient.getPhoneNumber() != null) patientMap.put("phoneNumber", patient.getPhoneNumber());
        if (patient.getEmail() != null) patientMap.put("email", patient.getEmail());
        if (patient.getAddress() != null) patientMap.put("address", patient.getAddress());
        if (patient.getSummary() != null) patientMap.put("summary", patient.getSummary());

        databaseReference.child("patients").child(patient.getId()).updateChildren(patientMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deletePatient(String patientId, final FirebaseCallback<Void> callback) {
        databaseReference.child("patients").child(patientId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // ==================== HEALTH RECORDS METHODS ====================

    public void addHealthRecord(HealthRecord record, final FirebaseCallback<String> callback) {
        String docId = databaseReference.child("health_records").push().getKey();
        record.setId(docId);

        Map<String, Object> recordMap = new HashMap<>();
        recordMap.put("id", docId);
        recordMap.put("patientId", record.getPatientId());
        recordMap.put("type", record.getType());
        recordMap.put("value", record.getValue());
        recordMap.put("unit", record.getUnit());
        recordMap.put("note", record.getNote());
        recordMap.put("recordedBy", record.getRecordedBy());
        recordMap.put("recordedAt", System.currentTimeMillis());

        databaseReference.child("health_records").child(docId).setValue(recordMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(docId))
                .addOnFailureListener(callback::onFailure);
    }

    public void getPatientRecords(String patientId, final DatabaseCallback<List<HealthRecord>> callback) {
        databaseReference.child("health_records")
                .orderByChild("patientId")
                .equalTo(patientId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<HealthRecord> records = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HealthRecord record = snapshot.getValue(HealthRecord.class);
                        if (record != null) {
                            record.setId(snapshot.getKey());
                            records.add(record);
                        }
                    }
                    records.sort((a, b) -> Long.compare(b.getRecordedAt(), a.getRecordedAt()));
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllHealthRecords(final DatabaseCallback<List<HealthRecord>> callback) {
        databaseReference.child("health_records")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<HealthRecord> records = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HealthRecord record = snapshot.getValue(HealthRecord.class);
                        if (record != null) {
                            record.setId(snapshot.getKey());
                            records.add(record);
                        }
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteHealthRecord(String recordId, final FirebaseCallback<Void> callback) {
        databaseReference.child("health_records").child(recordId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // ==================== USER MANAGEMENT METHODS (ADMIN) ====================

    public void getAllUsers(final DatabaseCallback<List<User>> callback) {
        databaseReference.child("users")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getRole() != null && !user.getRole().equals("Admin")) {
                            user.setId(snapshot.getKey());
                            users.add(user);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserById(String userId, final DatabaseCallback<User> callback) {
        databaseReference.child("users").child(userId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(dataSnapshot.getKey());
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void addUser(User user, final FirebaseCallback<String> callback) {
        String userId = databaseReference.child("users").push().getKey();
        user.setId(userId);
        user.setCreatedAt(System.currentTimeMillis());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", user.getUsername());
        userMap.put("password", user.getPassword());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("location", user.getLocation());
        userMap.put("role", user.getRole());
        userMap.put("phone", user.getPhone());
        userMap.put("isActive", user.isActive());
        userMap.put("createdAt", user.getCreatedAt());

        databaseReference.child("users").child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User added: " + user.getName() + " with role: " + user.getRole());
                    callback.onSuccess(userId);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUser(User user, final FirebaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", user.getName());
        updates.put("email", user.getEmail());
        updates.put("location", user.getLocation());
        updates.put("role", user.getRole());
        updates.put("phone", user.getPhone());
        updates.put("isActive", user.isActive());
        updates.put("updatedAt", System.currentTimeMillis());

        databaseReference.child("users").child(user.getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void resetUserPassword(String userId, String newPassword, final FirebaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);

        databaseReference.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteUser(String userId, final FirebaseCallback<Void> callback) {
        databaseReference.child("users").child(userId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // ==================== SEARCH METHODS ====================

    public void searchPatientsByName(String searchQuery, final DatabaseCallback<List<Patient>> callback) {
        databaseReference.child("patients")
                .orderByChild("fullName")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Patient patient = snapshot.getValue(Patient.class);
                        if (patient != null) {
                            patient.setId(snapshot.getKey());
                            patients.add(patient);
                        }
                    }
                    callback.onSuccess(patients);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void searchPatientsByNHN(String nhn, final DatabaseCallback<List<Patient>> callback) {
        databaseReference.child("patients")
                .orderByChild("nhn")
                .equalTo(nhn)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Patient patient = snapshot.getValue(Patient.class);
                        if (patient != null) {
                            patient.setId(snapshot.getKey());
                            patients.add(patient);
                        }
                    }
                    callback.onSuccess(patients);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ==================== STATISTICS METHODS ====================

    public void getTotalPatientsCount(final DatabaseCallback<Integer> callback) {
        databaseReference.child("patients").get()
                .addOnSuccessListener(dataSnapshot -> {
                    int count = (int) dataSnapshot.getChildrenCount();
                    callback.onSuccess(count);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getTodayRecordsCount(final DatabaseCallback<Integer> callback) {
        long todayStart = getTodayStartTimestamp();
        databaseReference.child("health_records")
                .orderByChild("recordedAt")
                .startAt(todayStart)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    int count = (int) dataSnapshot.getChildrenCount();
                    callback.onSuccess(count);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private long getTodayStartTimestamp() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // ==================== CALLBACK INTERFACES ====================

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}