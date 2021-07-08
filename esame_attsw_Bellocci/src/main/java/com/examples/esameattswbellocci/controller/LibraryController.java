package com.examples.esameattswbellocci.controller;

import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.view.LibraryView;

public class LibraryController {

	private LibraryView libraryView;
	private LibraryRepository libraryRepository;
	
	public LibraryController(LibraryView libraryView, LibraryRepository libraryRepository) {
		this.libraryRepository = libraryRepository;
		this.libraryView = libraryView;
	}
	
	protected LibraryRepository getLibraryRepository() {
		return this.libraryRepository;
	}

	public void allLibraries() {
		libraryView.showAllLibraries(libraryRepository.takeAllLibraries());
	}

	public void newLibrary(Library newLibrary) {
		Library libraryFound = libraryRepository.findLibraryById(newLibrary.getId());
		if(libraryFound != null) {
			libraryView.showError("Already existing library with id " + libraryFound.getId(), libraryFound);
			return;
		}
		try {
			libraryRepository.saveLibrary(newLibrary);
			libraryView.libraryAdded(newLibrary);
		} catch(IllegalArgumentException e) {
			libraryView.showError(e.getMessage(), new Library(newLibrary.getId(), "???"));
		}
	}
	
	public void deleteLibrary(Library library) {
		if(libraryRepository.findLibraryById(library.getId()) == null) {
			removedLibraryAndShowError(library);
			return;
		}
		try {
			libraryRepository.deleteLibrary(library.getId());
			libraryView.libraryRemoved(library);
		} catch(IllegalArgumentException e) {
			libraryView.showError(e.getMessage(), library);
		}
	}
	
	public void findLibrary(Library library) {
		if(libraryRepository.findLibraryById(library.getId()) == null) {
			removedLibraryAndShowError(library);
			return;
		}
		libraryView.showAllBooksOfLibrary(library);
	}
	
	private void removedLibraryAndShowError(Library library) {
		libraryView.libraryRemoved(library);
		libraryView.showError("Doesn't exist library with id " + library.getId(), library);
	}
}
