package com.examples.esame_attsw_Bellocci.repository;

import java.util.List;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public interface BookRepository {

	public List<Book> getAllBooksOfLibrary(String idLibrary);
	public Book findBookById(String idBook);
	public void saveBookInTheLibrary(Library library, Book newBook);
	public void deleteBookFromLibrary(String idLibrary, String idBook);
}
