package com.library.action;

import com.library.dao.BookDAO;
import com.library.form.BookForm;
import com.library.model.Book;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class UpdateBookAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BookForm bookForm = (BookForm) form;
        BookDAO bookDAO = new BookDAO();
        Book existing = bookDAO.findById(bookForm.getBookId());
        if (existing == null) {
            request.setAttribute("errorMessage", "Book not found");
            return mapping.findForward("failure");
        }

        existing.setIsbn(bookForm.getIsbn());
        existing.setTitle(bookForm.getTitle());
        existing.setAuthor(bookForm.getAuthor());
        existing.setCategory(bookForm.getCategory());
        existing.setPublisher(bookForm.getPublisher());
        existing.setDescription(bookForm.getDescription());
        existing.setPublicationYear(bookForm.getPublicationYear() == null || bookForm.getPublicationYear().trim().isEmpty() ? null : Integer.valueOf(bookForm.getPublicationYear()));
        int newTotal = Integer.parseInt(bookForm.getTotalQuantity());
        int rented = existing.getRentedQuantity();
        int available = Math.max(0, newTotal - rented);
        existing.setTotalQuantity(newTotal);
        existing.setAvailableQuantity(available);
        existing.setRentedQuantity(rented);
        bookDAO.updateBook(existing);
        request.setAttribute("successMessage", "Book updated successfully");
        return mapping.findForward("success");
    }
}
