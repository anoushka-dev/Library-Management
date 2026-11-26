package com.library.action;

import com.library.dao.UserDAO;
import com.library.form.LoginForm;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class LoginAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LoginForm loginForm = (LoginForm) form;
        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(loginForm.getEmail(), loginForm.getPassword());
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            request.setAttribute("errorMessage", "Invalid email or password");
            return mapping.findForward("failure");
        }

        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        return mapping.findForward("admin");
    }
}
