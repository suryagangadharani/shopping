package com.example.mensfashionstore.service;

import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.repository.UserRepository;
import com.example.mensfashionstore.repository.OrderRepository;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) { 
        return userRepository.findByEmail(email); 
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User registerUser(String email, String password, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole("USER");
        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());
        return save(user);
    }

    public User registerUser(String email, String password, String fullName, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole(role);
        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());
        return save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        try {
            // Delete all orders for this user first (cascade deletes order items)
            List<Order> userOrders = orderRepository.findByUserId(id);
            for (Order order : userOrders) {
                orderRepository.deleteById(order.getId());
            }
            // Now delete the user
            userRepository.deleteById(id);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                throw new IllegalArgumentException(
                    "Cannot delete user: they have existing orders. " +
                    "Please remove all orders for this user first.", e);
            }
            throw e;
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setIsActive(userDetails.getIsActive());
        return save(user);
    }

    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setIsActive(!user.getIsActive());
        return save(user);
    }

    public User changeUserRole(Long id, String newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        return save(user);
    }

    public User updateLastLogin(Long id) {
        User user = getUserById(id);
        user.setLastLogin(LocalDateTime.now());
        return save(user);
    }

    public long getTotalUserCount() {
        return userRepository.countTotalUsers();
    }

    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}


