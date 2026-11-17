package com.library.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ReturnForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    private int rentalId;
    private int quantity;

    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (rentalId <= 0) {
            errors.add("rentalId", new ActionMessage("errors.required", "Rental"));
        }
        if (quantity <= 0) {
            errors.add("quantity", new ActionMessage("errors.required", "Return Quantity"));
        }
        return errors;
    }
}
