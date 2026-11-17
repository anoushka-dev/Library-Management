package com.library.action;

import com.library.dao.BookDAO;
import com.library.form.BookForm;
import com.library.model.Book;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class SearchBooksAction extends AbstractAction {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BookForm bookForm = (BookForm) form;
        BookDAO bookDAO = new BookDAO();
        List<Book> books = bookDAO.listBooks(bookForm.getSearchText());
        request.setAttribute("books", books);
        return mapping.findForward("success");
    }
}
