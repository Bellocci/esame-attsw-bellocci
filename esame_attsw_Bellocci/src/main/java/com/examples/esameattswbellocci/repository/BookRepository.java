package com.examples.esameattswbellocci.repository;

import java.util.List;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

public interface BookRepository {

	public List<Book> takeAllBooksOfLibrary(String idLibrary);
	public Book findBookById(String idBook);
	public void saveBookInTheLibrary(Library library, Book newBook);
	public void deleteBookFromLibrary(String idBook);
}
