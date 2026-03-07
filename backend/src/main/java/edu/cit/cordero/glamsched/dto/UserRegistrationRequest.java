package edu.cit.cordero.glamsched.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String fullName;
    private String email;
    private String password;
    private String role; // CLIENT or ARTIST

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
