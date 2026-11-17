package com.library.action;

import com.library.dao.UserDAO;
import com.library.form.RegisterForm;
import com.library.model.User;
import com.library.util.PasswordUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class RegisterAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RegisterForm registerForm = (RegisterForm) form;
        UserDAO userDAO = new UserDAO();
        if (userDAO.findByEmail(registerForm.getEmail()) != null) {
            request.setAttribute("errorMessage", "Email already registered");
            return mapping.findForward("failure");
        }

        User user = new User();
        user.setFullName(registerForm.getFullName());
        user.setEmail(registerForm.getEmail());
        user.setPasswordHash(PasswordUtil.hashPassword(registerForm.getPassword()));
        user.setPhone(registerForm.getPhone());
        user.setAddress(registerForm.getAddress());
        user.setRole("MEMBER");
        userDAO.register(user);
        request.setAttribute("successMessage", "Registration successful. Please log in.");
        return mapping.findForward("success");
    }
}
