package com.library.action;

import com.library.dao.BookDAO;
import com.library.form.BookForm;
import com.library.model.Book;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class AddBookAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BookForm bookForm = (BookForm) form;
        BookDAO bookDAO = new BookDAO();
        if (bookDAO.findByIsbn(bookForm.getIsbn()) != null) {
            request.setAttribute("errorMessage", "ISBN already exists");
            return mapping.findForward("failure");
        }

        Book book = new Book();
        book.setIsbn(bookForm.getIsbn());
        book.setTitle(bookForm.getTitle());
        book.setAuthor(bookForm.getAuthor());
        book.setCategory(bookForm.getCategory());
        book.setPublisher(bookForm.getPublisher());
        book.setDescription(bookForm.getDescription());
        book.setTotalQuantity(Integer.parseInt(bookForm.getTotalQuantity()));
        book.setAvailableQuantity(book.getTotalQuantity());
        book.setRentedQuantity(0);
        book.setPublicationYear(bookForm.getPublicationYear() == null || bookForm.getPublicationYear().trim().isEmpty() ? null : Integer.valueOf(bookForm.getPublicationYear()));
        bookDAO.createBook(book);
        request.setAttribute("successMessage", "Book added successfully");
        return mapping.findForward("success");
    }
}
