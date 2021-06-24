package com.examples.esame_attsw_Bellocci.repository;

import java.util.List;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public interface BookRepository {

	public List<Book> getAllBooksOfLibrary(String id_library);
	public Book findBookById(String id_book);
	public void saveBookInTheLibrary(Library library, Book new_book);
	public void deleteBookFromLibrary(String id_library, String id_book);
}
