package com.library.action;

import com.library.dao.BookDAO;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class NewRentalAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return mapping.findForward("login");
        }
        request.setAttribute("availableBooks", new BookDAO().listAvailableBooks());
        request.setAttribute("selectedBookId", request.getParameter("bookId"));
        return mapping.findForward("success");
    }
}
