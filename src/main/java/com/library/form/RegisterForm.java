package com.library.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class RegisterForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String address;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("fullName", new ActionMessage("errors.required", "Full Name"));
        }
        if (email == null || email.trim().isEmpty()) {
            errors.add("email", new ActionMessage("errors.required", "Email"));
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("password", new ActionMessage("errors.required", "Password"));
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.add("confirmPassword", new ActionMessage("errors.match", "Password confirmation"));
        }
        return errors;
    }
}
