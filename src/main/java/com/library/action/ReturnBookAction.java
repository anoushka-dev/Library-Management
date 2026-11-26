package com.library.action;

import com.library.dao.RentalDAO;
import com.library.form.ReturnForm;
import com.library.model.Rental;
import com.library.model.User;
import java.util.List;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
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
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return mapping.findForward("login");
        }
        ReturnForm returnForm = (ReturnForm) form;
        ActionErrors errors = returnForm.validate(mapping, request);
        if (!errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            List<Rental> rentals = new RentalDAO().listAllRentals();
            request.setAttribute("rentals", rentals);
            return mapping.findForward("failure");
        }
        RentalDAO rentalDAO = new RentalDAO();
        rentalDAO.processReturn(returnForm.getRentalId(), returnForm.getQuantity());
        request.setAttribute("successMessage", "Books returned successfully");
        return mapping.findForward("success");
    }
}
