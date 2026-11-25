package com.library.action;

import com.library.dao.BookDAO;
import com.library.dao.RentalDAO;
import com.library.dao.UserDAO;
import com.library.model.DashboardStats;
import com.library.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class AdminDashboardAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return mapping.findForward("login");
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return mapping.findForward("notAdmin");
        }
        BookDAO bookDAO = new BookDAO();
        UserDAO userDAO = new UserDAO();
        RentalDAO rentalDAO = new RentalDAO();
        DashboardStats stats = new DashboardStats();
        stats.setTotalBooks(bookDAO.countBooks());
        stats.setAvailableBooks(bookDAO.countAvailableBooks());
        stats.setRentedBooks(bookDAO.countRentedBooks());
        stats.setTotalMembers(userDAO.countMembers());
        stats.setActiveRentals(rentalDAO.countActiveRentals());
        stats.setOverdueRentals(rentalDAO.countOverdueRentals());
        stats.setReturnedBooks(rentalDAO.countReturnedBooks());
        stats.setOutstandingFines(rentalDAO.totalOutstandingFines());
        request.setAttribute("stats", stats);
        request.setAttribute("overdueRentals", rentalDAO.listOverdueRentals());
        return mapping.findForward("success");
    }
}
