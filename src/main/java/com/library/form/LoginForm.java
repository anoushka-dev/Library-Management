package com.library.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class LoginForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

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

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (email == null || email.trim().isEmpty()) {
            errors.add("email", new ActionMessage("errors.required", "Email"));
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("password", new ActionMessage("errors.required", "Password"));
        }
        return errors;
    }
}
