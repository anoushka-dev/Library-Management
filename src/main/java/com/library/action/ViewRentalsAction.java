package com.library.action;

import com.library.dao.RentalDAO;
import com.library.model.Rental;
import com.library.model.User;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewRentalsAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return mapping.findForward("login");
        }
        RentalDAO rentalDAO = new RentalDAO();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return mapping.findForward("login");
        }
        List<Rental> rentals = rentalDAO.listAllRentals();
        request.setAttribute("rentals", rentals);
        return mapping.findForward("success");
    }
}
