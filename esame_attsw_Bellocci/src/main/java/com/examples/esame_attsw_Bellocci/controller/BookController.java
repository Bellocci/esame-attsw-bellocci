package com.examples.esame_attsw_Bellocci.controller;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.BookRepository;
import com.examples.esame_attsw_Bellocci.view.BookView;

public class BookController {

	private BookRepository bookRepository;
	private BookView bookView;
	private LibraryController libraryController;
	
	public BookController(BookRepository bookRepository, BookView bookView, LibraryController libraryController) {
		this.bookRepository = bookRepository;
		this.bookView = bookView;
		this.libraryController = libraryController;
	}
	
	public void allBooks(Library library) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError("Doesnt exist library with id " + library.getId() + " ", library);
			return;
		}
		bookView.showAllBooks(bookRepository.getAllBooksOfLibrary(library.getId()));
	}
	
	public void newBook(Library library, Book book) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError("Doesnt exist library with id " + library.getId() + " ", library);
			return;
		}
		Book book_found = bookRepository.findBookById(book.getId());
		if(book_found != null) {
			bookView.showError("Already existing book with id " + book_found.getId(), book_found);
			return;
		}
		bookRepository.saveBookInTheLibrary(library, book);
		bookView.bookAdded(book);
	}
}
