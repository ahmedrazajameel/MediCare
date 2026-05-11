package com.medical.medicarepro.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medical.medicarepro.models.Patient;
import com.medical.medicarepro.models.HealthRecord;
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

    public void login(String email, String password, final FirebaseCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    currentUser = authResult.getUser();
                    callback.onSuccess(currentUser);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void logout() {
        auth.signOut();
        currentUser = null;
    }

    public void getCurrentUserData(final FirebaseCallback<User> callback) {
        if (currentUser == null) {
            callback.onFailure(new Exception("No user logged in"));
            return;
        }

        databaseReference.child("users").child(currentUser.getUid())
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(dataSnapshot.getKey());
                            callback.onSuccess(user);
                        } else {
                            User defaultUser = new User();
                            defaultUser.setName("Medical Officer");
                            defaultUser.setEmail(currentUser.getEmail());
                            defaultUser.setLocation("Central");
                            defaultUser.setRole("Medical Officer");
                            callback.onSuccess(defaultUser);
                        }
                    } else {
                        User defaultUser = new User();
                        defaultUser.setName("Medical Officer");
                        defaultUser.setEmail(currentUser.getEmail());
                        defaultUser.setLocation("Central");
                        defaultUser.setRole("Medical Officer");
                        callback.onSuccess(defaultUser);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

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

        databaseReference.child("patients").child(patient.getId()).updateChildren(patientMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

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
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}