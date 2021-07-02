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

	public void getAllLibraries() {
		libraryView.showAllLibraries(libraryRepository.getAllLibraries());
	}

	public void newLibrary(Library newLibrary) {
		if(newLibrary.getId().trim().isEmpty()) {
			libraryView.showError("Library id cannot be empty or only blank space", newLibrary);
			return;
		}
		Library libraryFound = libraryRepository.findLibraryById(newLibrary.getId());
		if(libraryFound != null) {
			libraryView.showError("Already existing library with id " + libraryFound.getId(), libraryFound);
			return;
		}
		try {
			libraryRepository.saveLibrary(newLibrary);
			libraryView.libraryAdded(newLibrary);
		} catch(IllegalArgumentException e) {
			libraryView.showError(e.getMessage(), newLibrary);
		}
	}
	
	public void deleteLibrary(Library library) {
		Library libraryFound = libraryRepository.findLibraryById(library.getId());
		if(libraryFound == null) {
			removedLibraryAndShowError(library);
			return;
		}
		try {
			libraryRepository.deleteLibrary(library.getId());
			libraryView.libraryRemoved(libraryFound);
		} catch(IllegalArgumentException e) {
			libraryView.showError(e.getMessage(), library);
		}
	}
	
	public void findLibrary(Library library) {
		Library libraryFound = libraryRepository.findLibraryById(library.getId());
		if(libraryFound == null) {
			removedLibraryAndShowError(library);
			return;
		}
		libraryView.showAllBooksOfLibrary(libraryFound);
	}
	
	private void removedLibraryAndShowError(Library library) {
		libraryView.libraryRemoved(library);
		libraryView.showError("Doesn't exist library with id 1", library);
	}
}
