package com.examples.esame_attsw_Bellocci.view;

import java.util.List;

import com.examples.esame_attsw_Bellocci.model.Library;

public interface LibraryView {
	public void showAllLibraries(List<Library> libraries);
	public void libraryAdded(Library library);
	public void libraryRemoved(Library library);
	public void showError(String error_message, Library library);
	public void showAllBooksOfLibrary(Library library);
}
