package com.medical.medicarepro.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.User;
import com.medical.medicarepro.utils.FirebaseManager;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUsers;
    private FloatingActionButton fabAddUser;
    private Button btnLogout;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        firebaseManager = FirebaseManager.getInstance();

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        fabAddUser = findViewById(R.id.fabAddUser);
        btnLogout = findViewById(R.id.btnLogout);

        setupRecyclerView();
        loadUsers();

        fabAddUser.setOnClickListener(v -> showAddUserDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(userList, user -> showUserOptions(user));
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        firebaseManager.getAllUsers(new FirebaseManager.DatabaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                userList.clear();
                userList.addAll(result);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminPanelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseManager.logout();
                    Intent intent = new Intent(AdminPanelActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(AdminPanelActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showAddUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etUsername);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        String[] roles = {"Medical Officer", "Nurse"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String name = etName.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();
                    String role = spinnerRole.getSelectedItem().toString();

                    if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                        Toast.makeText(this, "Username, password and name are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createUser(username, password, name, location, role);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createUser(String username, String password, String name, String location, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(username + "@medicare.com");
        user.setLocation(location.isEmpty() ? "Central" : location);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(System.currentTimeMillis());

        firebaseManager.addUser(user, new FirebaseManager.FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(AdminPanelActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminPanelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserOptions(User user) {
        String[] options = {"Edit", "Reset Password", "Delete"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(user.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            editUser(user);
                            break;
                        case 1:
                            resetPassword(user);
                            break;
                        case 2:
                            deleteUser(user);
                            break;
                    }
                })
                .show();
    }

    private void editUser(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etLocation.setText(user.getLocation());

        String[] roles = {"Medical Officer", "Nurse"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        int position = user.getRole().equals("Medical Officer") ? 0 : 1;
        spinnerRole.setSelection(position);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    user.setName(etName.getText().toString().trim());
                    user.setEmail(etEmail.getText().toString().trim());
                    user.setLocation(etLocation.getText().toString().trim());
                    user.setRole(spinnerRole.getSelectedItem().toString());

                    firebaseManager.updateUser(user, new FirebaseManager.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AdminPanelActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminPanelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetPassword(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password for " + user.getName())
                .setView(dialogView)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPassword = etNewPassword.getText().toString().trim();
                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "Enter new password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    firebaseManager.resetUserPassword(user.getId(), newPassword, new FirebaseManager.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AdminPanelActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminPanelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseManager.deleteUser(user.getId(), new FirebaseManager.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AdminPanelActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminPanelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // UserAdapter
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> users;
        private OnUserClickListener listener;

        interface OnUserClickListener {
            void onUserClick(User user);
        }

        UserAdapter(List<User> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvName.setText(user.getName());
            holder.tvUsername.setText("@" + user.getUsername());
            holder.tvRole.setText(user.getRole());
            holder.tvLocation.setText(user.getLocation());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUsername, tvRole, tvLocation;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvLocation = itemView.findViewById(R.id.tvLocation);
            }
        }
    }
}