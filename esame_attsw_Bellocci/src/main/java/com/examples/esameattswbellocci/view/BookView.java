package com.examples.esameattswbellocci.view;

import java.util.List;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

public interface BookView {
	public void showAllBooks(List<Book> books);
	public void bookAdded(Book book);
	public void bookRemoved(Book book);
	public void showError(String message, Book book);
	public void closeViewError(String message, Library library);
}
