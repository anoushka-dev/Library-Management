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
    private int rentalDays = 14;
    private String renterName;
    private String renterEmail;
    private String renterPhone;
    private String renterAddress;

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getRenterEmail() {
        return renterEmail;
    }

    public void setRenterEmail(String renterEmail) {
        this.renterEmail = renterEmail;
    }

    public String getRenterPhone() {
        return renterPhone;
    }

    public void setRenterPhone(String renterPhone) {
        this.renterPhone = renterPhone;
    }

    public String getRenterAddress() {
        return renterAddress;
    }

    public void setRenterAddress(String renterAddress) {
        this.renterAddress = renterAddress;
    }

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
        if (rentalDays < 0) {
            errors.add("rentalDays", new ActionMessage("errors.required", "Rental Days"));
        }
        if (renterName == null || renterName.trim().isEmpty()) {
            errors.add("renterName", new ActionMessage("errors.required", "Renter Name"));
        }
        if (renterEmail == null || !renterEmail.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.add("renterEmail", new ActionMessage("errors.invalid", "Email"));
        }
        return errors;
    }
}
