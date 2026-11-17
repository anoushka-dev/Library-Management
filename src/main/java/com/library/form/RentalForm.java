package com.library.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class RentalForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    private int bookId;
    private int quantity;
    private int rentalDays;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (bookId <= 0) {
            errors.add("bookId", new ActionMessage("errors.required", "Book"));
        }
        if (quantity <= 0) {
            errors.add("quantity", new ActionMessage("errors.required", "Quantity"));
        }
        if (rentalDays <= 0) {
            errors.add("rentalDays", new ActionMessage("errors.required", "Rental Days"));
        }
        return errors;
    }
}
