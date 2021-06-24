package com.examples.esame_attsw_Bellocci.controller;

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
}
