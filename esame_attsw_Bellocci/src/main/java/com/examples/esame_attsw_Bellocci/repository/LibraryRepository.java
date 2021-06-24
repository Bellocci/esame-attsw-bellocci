package com.examples.esame_attsw_Bellocci.repository;

import java.util.List;

import com.examples.esame_attsw_Bellocci.model.Library;

public interface LibraryRepository {

	public List<Library> getAllLibraries();
	public Library findLibraryById(String id_library);
	public void saveLibrary(Library library);
	public void deleteLibrary(String id_library);
}
