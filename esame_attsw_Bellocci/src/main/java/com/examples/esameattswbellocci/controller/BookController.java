package com.examples.esameattswbellocci.controller;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.view.BookView;

public class BookController {

	private BookRepository bookRepository;
	private BookView bookView;
	private LibraryController libraryController;
	
	private static final String CLOSE_VIEW_ERROR_MESSAGE = "Doesnt exist library with id ";
	
	public BookController(BookRepository bookRepository, BookView bookView, LibraryController libraryController) {
		this.bookRepository = bookRepository;
		this.bookView = bookView;
		this.libraryController = libraryController;
	}
	
	public void allBooks(Library library) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError(CLOSE_VIEW_ERROR_MESSAGE + library.getId(), library);
			return;
		}
		bookView.showAllBooks(bookRepository.getAllBooksOfLibrary(library.getId()));
	}
	
	public void newBook(Library library, Book book) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError(CLOSE_VIEW_ERROR_MESSAGE + library.getId(), library);
			return;
		}
		Book bookFound = bookRepository.findBookById(book.getId());
		if(bookFound != null) {
			bookView.showError("Already existing book with id " + bookFound.getId(), bookFound);
			return;
		}
		bookRepository.saveBookInTheLibrary(library, book);
		bookView.bookAdded(book);
	}
	
	public void deleteBook(Library library, Book book) {
		if(libraryController.getLibraryRepository().findLibraryById(library.getId()) == null) {
			bookView.closeViewError(CLOSE_VIEW_ERROR_MESSAGE + library.getId(), library);
			return;
		}
		if(bookRepository.findBookById(book.getId()) == null) {
			bookView.bookRemoved(book);
			bookView.showError("No existing book with id " + book.getId(), book);
			return;
		}
		bookRepository.deleteBookFromLibrary(library.getId(), book.getId());
		bookView.bookRemoved(book);
	}
}
