package com.library.action;

import com.library.dao.RentalDAO;
import com.library.model.Rental;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewOverdueRentalsAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RentalDAO rentalDAO = new RentalDAO();
        List<Rental> rentals = rentalDAO.listOverdueRentals();
        request.setAttribute("rentals", rentals);
        return mapping.findForward("success");
    }
}
