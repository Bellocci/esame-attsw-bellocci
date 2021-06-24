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

}
