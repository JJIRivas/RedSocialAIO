package com.example.redsocialaio;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
//import com.example.redsocialaio.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

//    private FirebaseAuth mAuth;
//    private FragmentLoginBinding binding;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentLoginBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Inicializar Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//
//        // Registro
//        binding.buttonRegister.setOnClickListener(v -> {
//            String email = binding.editTextEmail.getText().toString().trim();
//            String password = binding.editTextPassword.getText().toString().trim();
//            if (!email.isEmpty() && !password.isEmpty()) {
//                registerUser(email, password);
//            } else {
//                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Login
//        binding.buttonLogin.setOnClickListener(v -> {
//            String email = binding.editTextEmail.getText().toString().trim();
//            String password = binding.editTextPassword.getText().toString().trim();
//            if (!email.isEmpty() && !password.isEmpty()) {
//                loginUser(email, password);
//            } else {
//                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void registerUser(String email, String password) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(requireActivity(), task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(requireContext(), "Registro exitoso: " + user.getEmail(), Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(requireContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    private void loginUser(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(requireActivity(), task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(requireContext(), "Bienvenido: " + user.getEmail(), Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(requireContext(), "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
}

