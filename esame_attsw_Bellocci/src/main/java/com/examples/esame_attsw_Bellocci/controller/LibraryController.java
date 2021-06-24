package com.examples.esame_attsw_Bellocci.controller;

import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.view.LibraryView;

public class LibraryController {

	private LibraryView libraryView;
	private LibraryRepository libraryRepository;
	
	public LibraryController(LibraryView libraryView, LibraryRepository libraryRepository) {
		this.libraryRepository = libraryRepository;
		this.libraryView = libraryView;
	}

	public void getAllLibraries() {
		libraryView.showAllLibraries(libraryRepository.getAllLibraries());
	}

	public void newLibrary(Library new_library) {
		if(new_library.getId().trim().isEmpty())
			throw new IllegalArgumentException("Id library cannot be empty or only blank space");
		Library library_found = libraryRepository.findLibraryById(new_library.getId());
		if(library_found != null) {
			libraryView.showError("Already existing library with id " + library_found.getId(), library_found);
			return;
		}
		libraryRepository.saveLibrary(new_library);
		libraryView.libraryAdded(new_library);
	}
	
	public void deleteLibrary(Library library) {
		Library library_found = libraryRepository.findLibraryById(library.getId());
		if(library_found == null) {
			libraryView.libraryRemoved(library);
			libraryView.showError("Doesn't exist library with id " + library.getId(), library);
			return;
		}
		libraryRepository.deleteLibrary(library.getId());
		libraryView.libraryRemoved(library_found);
	}
	
	public void findLibrary(Library library) {
		Library library_found = libraryRepository.findLibraryById(library.getId());
		if(library_found == null) {
			libraryView.libraryRemoved(library);
			libraryView.showError("Doesn't exist library with id " + library.getId(), library);
			return;
		}
		libraryView.showAllBooksOfLibrary(library_found);
	}
}
