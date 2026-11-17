package com.library.action;

import com.library.dao.RentalDAO;
import com.library.form.ReturnForm;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ReturnBookAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return mapping.findForward("login");
        }
        ReturnForm returnForm = (ReturnForm) form;
        RentalDAO rentalDAO = new RentalDAO();
        rentalDAO.processReturn(returnForm.getRentalId(), returnForm.getQuantity());
        request.setAttribute("successMessage", "Books returned successfully");
        return mapping.findForward("success");
    }
}
