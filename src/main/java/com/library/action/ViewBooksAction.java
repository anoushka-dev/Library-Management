package com.library.action;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.User;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewBooksAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = getCurrentUser(request);
        BookDAO bookDAO = new BookDAO();
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        List<Book> books = isAdmin ? bookDAO.listBooks("") : bookDAO.listAvailableBooks();
        request.setAttribute("books", books);
        return mapping.findForward(isAdmin ? "admin" : "member");
    }
}
