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
	
	public void newBook(Library library, Book newBook) {
		if(!searchLibraryIntoDatabase(library))
			return;
		Book bookFound = bookRepository.findBookById(newBook.getId());
		if(bookFound != null) {
			bookView.showError("Already existing book with id " + bookFound.getId(), bookFound);
			return;
		}
		try {
			bookRepository.saveBookInTheLibrary(library, newBook);
			bookView.bookAdded(newBook);
		} catch(IllegalArgumentException e) {
			bookView.showError(e.getMessage(), new Book(newBook.getId(), "???"));
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
			bookRepository.deleteBookFromLibrary(book.getId());
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
