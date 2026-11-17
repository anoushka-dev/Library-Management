package com.library.action;

import com.library.dao.BookDAO;
import com.library.dao.RentalDAO;
import com.library.model.DashboardStats;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class MemberDashboardAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return mapping.findForward("login");
        }
        BookDAO bookDAO = new BookDAO();
        RentalDAO rentalDAO = new RentalDAO();
        DashboardStats stats = new DashboardStats();
        stats.setAvailableBooks(bookDAO.countAvailableBooks());
        stats.setActiveRentals(rentalDAO.listRentalsByUser(currentUser.getUserId()).size());
        stats.setOverdueRentals(rentalDAO.listOverdueRentals().size());
        request.setAttribute("stats", stats);
        return mapping.findForward("success");
    }
}
