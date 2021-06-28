package com.examples.esameattswbellocci.repository;

import java.util.List;

import com.examples.esameattswbellocci.model.Library;

public interface LibraryRepository {

	public List<Library> getAllLibraries();
	public Library findLibraryById(String idLibrary);
	public void saveLibrary(Library library);
	public void deleteLibrary(String idLibrary);
}
