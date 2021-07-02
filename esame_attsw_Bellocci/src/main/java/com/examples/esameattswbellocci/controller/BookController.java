package com.examples.esameattswbellocci.controller;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.view.BookView;

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
		if(!searchLibraryIntoDatabase(library))
			return;
		bookView.showAllBooks(bookRepository.takeAllBooksOfLibrary(library.getId()));
	}
	
	public void newBook(Library library, Book book) {
		if(!searchLibraryIntoDatabase(library))
			return;
		Book bookFound = bookRepository.findBookById(book.getId());
		if(bookFound != null) {
			bookView.showError("Already existing book with id " + bookFound.getId(), bookFound);
			return;
		}
		try {
			bookRepository.saveBookInTheLibrary(library, book);
			bookView.bookAdded(book);
		} catch(IllegalArgumentException e) {
			bookView.showError(e.getMessage(), book);
		}
	}
	
	public void deleteBook(Library library, Book book) {
		if(!searchLibraryIntoDatabase(library))
			return;
		if(bookRepository.findBookById(book.getId()) == null) {
			bookView.bookRemoved(book);
			bookView.showError("No existing book with id " + book.getId(), book);
			return;
		}
		try {
			bookRepository.deleteBookFromLibrary(book.getId(), library.getId());
			bookView.bookRemoved(book);
		} catch(IllegalArgumentException e) {
			bookView.showError(e.getMessage(), book);
		}
	}
	
	private boolean searchLibraryIntoDatabase(Library library) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError("Doesnt exist library with id " + library.getId(), library);
			return false;
		}
		return true;
	}
}
