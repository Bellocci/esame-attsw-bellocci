package com.examples.esameAttswBellocci.view;

import java.util.List;

import com.examples.esameAttswBellocci.model.Book;
import com.examples.esameAttswBellocci.model.Library;

public interface BookView {
	public void showAllBooks(List<Book> books);
	public void bookAdded(Book book);
	public void bookRemoved(Book book);
	public void showError(String message, Book book);
	public void closeViewError(String message, Library library);
}
