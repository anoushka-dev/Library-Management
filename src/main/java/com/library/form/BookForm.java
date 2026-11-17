package com.library.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class BookForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private String publicationYear;
    private String description;
    private String totalQuantity;
    private String availableQuantity;
    private String rentedQuantity;
    private String searchText;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(String totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(String availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getRentedQuantity() {
        return rentedQuantity;
    }

    public void setRentedQuantity(String rentedQuantity) {
        this.rentedQuantity = rentedQuantity;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (title == null || title.trim().isEmpty()) {
            errors.add("title", new ActionMessage("errors.required", "Title"));
        }
        if (author == null || author.trim().isEmpty()) {
            errors.add("author", new ActionMessage("errors.required", "Author"));
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            errors.add("isbn", new ActionMessage("errors.required", "ISBN"));
        }
        if (totalQuantity == null || totalQuantity.trim().isEmpty()) {
            errors.add("totalQuantity", new ActionMessage("errors.required", "Total Quantity"));
        }
        return errors;
    }
}
