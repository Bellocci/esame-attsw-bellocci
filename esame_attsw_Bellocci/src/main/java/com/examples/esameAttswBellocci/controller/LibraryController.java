package com.examples.esameAttswBellocci.controller;

import com.examples.esameAttswBellocci.model.Library;
import com.examples.esameAttswBellocci.repository.LibraryRepository;
import com.examples.esameAttswBellocci.view.LibraryView;

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
		if(newLibrary.getId().trim().isEmpty())
			throw new IllegalArgumentException("Id library cannot be empty or only blank space");
		Library libraryFound = libraryRepository.findLibraryById(newLibrary.getId());
		if(libraryFound != null) {
			libraryView.showError("Already existing library with id " + libraryFound.getId(), libraryFound);
			return;
		}
		libraryRepository.saveLibrary(newLibrary);
		libraryView.libraryAdded(newLibrary);
	}
	
	public void deleteLibrary(Library library) {
		Library libraryFound = libraryRepository.findLibraryById(library.getId());
		if(libraryFound == null) {
			libraryView.libraryRemoved(library);
			libraryView.showError("Doesn't exist library with id " + library.getId(), library);
			return;
		}
		libraryRepository.deleteLibrary(library.getId());
		libraryView.libraryRemoved(libraryFound);
	}
	
	public void findLibrary(Library library) {
		Library libraryFound = libraryRepository.findLibraryById(library.getId());
		if(libraryFound == null) {
			libraryView.libraryRemoved(library);
			libraryView.showError("Doesn't exist library with id " + library.getId(), library);
			return;
		}
		libraryView.showAllBooksOfLibrary(libraryFound);
	}
}
