package com.library.action;

import com.library.dao.BookDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class DeleteBookAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idParam = request.getParameter("bookId");
        if (idParam == null || idParam.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Book id is required");
            return mapping.findForward("failure");
        }

        int bookId = Integer.parseInt(idParam);
        BookDAO bookDAO = new BookDAO();
        if (!bookDAO.canDeleteBook(bookId)) {
            request.setAttribute("errorMessage", "Book cannot be deleted because active rentals exist");
            return mapping.findForward("failure");
        }
        bookDAO.deleteBook(bookId);
        return mapping.findForward("success");
    }
}
