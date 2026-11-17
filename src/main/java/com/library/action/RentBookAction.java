package com.library.action;

import com.library.dao.RentalDAO;
import com.library.form.RentalForm;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class RentBookAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return mapping.findForward("login");
        }
        RentalForm rentalForm = (RentalForm) form;
        RentalDAO rentalDAO = new RentalDAO();
        rentalDAO.createRental(currentUser.getUserId(), rentalForm.getBookId(), rentalForm.getQuantity(), rentalForm.getRentalDays());
        request.setAttribute("successMessage", "Rental created successfully");
        return mapping.findForward("success");
    }
}
