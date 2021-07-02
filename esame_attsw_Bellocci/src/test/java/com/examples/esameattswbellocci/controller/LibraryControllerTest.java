package com.examples.esameattswbellocci.controller;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.view.LibraryView;

public class LibraryControllerTest {

	@Mock
	private LibraryRepository libraryRepository;
	
	@Mock
	private LibraryView libraryView;
	
	@InjectMocks
	private LibraryController libraryController;
	
	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testAllLibrariesShouldRequestTheLibrariesFromDatabaseAndReturnThemToTheView() {
		// setup
		List<Library> listLibraries = new ArrayList<Library>();	
		when(libraryRepository.takeAllLibraries()).thenReturn(listLibraries);
		
		// exercise
		libraryController.allLibraries();
		
		// verify
		verify(libraryView).showAllLibraries(listLibraries);
	}
	

	@Test
	public void testNewLibraryWhenItDoesntExistShouldRequestToAddIntoDatabaseAndRequireToReturnThemToTheView() {
		// setup
		Library newLibrary = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(null);
		
		// exercise
		libraryController.newLibrary(newLibrary);
		
		// verify
		InOrder inOrder = inOrder(libraryRepository, libraryView);
		inOrder.verify(libraryRepository).saveLibrary(newLibrary);
		inOrder.verify(libraryView).libraryAdded(newLibrary);
	}
	
	@Test
	public void testNewLibraryWhenItAlreadyExistShouldRequestShowErrorToTheView() {
		// setup
		Library alreadyAdded = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(alreadyAdded);
		
		// exercise
		libraryController.newLibrary(alreadyAdded);
		
		// verify
		verify(libraryView).showError("Already existing library with id 1", alreadyAdded);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testNewLibraryWhenIdIsEmptyShouldRequestShowErrorToTheView() {
		// setup
		Library library = new Library("", "library1");
		
		// exercise
		libraryController.newLibrary(library);
		
		// verify
		verify(libraryView).showError("Library id cannot be empty or only blank space", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testNewLibraryWhenIdAreOnlyBlankSpaceShouldRequestShowErrorToTheView() {
		// setup
		Library library = new Library("  ", "library1");
		
		// exercise
		libraryController.newLibrary(library);
				
		// verify
		verify(libraryView).showError("Library id cannot be empty or only blank space", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testNewLibraryWhenLibraryAlreadyAddedIsPassedAsArgumentToLibraryRepositoryShouldCatchAndShowErrorToTheView() {
		// setup
		Library alreadyAdded = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(null);
		doThrow(new IllegalArgumentException("Database already contains library with id 1"))
			.when(libraryRepository).saveLibrary(alreadyAdded);
		
		// exercise
		libraryController.newLibrary(alreadyAdded);
		
		// verify
		verify(libraryView).showError("Database already contains library with id 1", alreadyAdded);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testDeleteLibraryWhenLibraryExistShouldRequestLibraryRepositoryAndLibraryViewToRemoveLibrary() {
		// setup
		Library library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(library);
		
		// exercise
		libraryController.deleteLibrary(library);
		
		// verify
		InOrder inOrder = inOrder(libraryRepository, libraryView);
		inOrder.verify(libraryRepository).deleteLibrary("1");
		inOrder.verify(libraryView).libraryRemoved(library);
	}
	
	@Test
	public void testDeleteLibraryWhenLibraryDoesntExistShouldRequestLibraryViewToRemoveLibraryAndShowError() {
		// setup
		Library library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(null);
		
		// exercise
		libraryController.deleteLibrary(library);
		
		// verify
		verify(libraryView).libraryRemoved(library);
		verify(libraryView).showError("Doesn't exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testDeleteLibraryWhenLibraryNotExistIntoDatabaseIsPassedAsArgumentOfLibraryRepositoryShouldCatchAndShowError() {
		// setup
		Library libraryNotExist = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(libraryNotExist);
		doThrow(new IllegalArgumentException("Database doesn't contain library with id 1"))
			.when(libraryRepository).deleteLibrary(libraryNotExist.getId());
		
		// exercise
		libraryController.deleteLibrary(libraryNotExist);
		
		// verify
		verify(libraryView).showError("Database doesn't contain library with id 1", libraryNotExist);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
		verifyNoMoreInteractions(ignoreStubs(libraryView));
	}
	
	@Test
	public void testFindLibraryWhenLibraryExist() {
		// setup
		Library library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(library);
		
		// exercise
		libraryController.findLibrary(library);
		
		// verify
		InOrder inOrder = inOrder(libraryRepository, libraryView);
		inOrder.verify(libraryRepository).findLibraryById("1");
		inOrder.verify(libraryView).showAllBooksOfLibrary(library);
	}
	
	@Test
	public void testFindLibraryWhenLibraryDoesntExist() {
		// setup
		Library library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(null);
		
		// exercise
		libraryController.findLibrary(library);
		
		// verify
		InOrder inOrder = inOrder(libraryRepository, libraryView);
		inOrder.verify(libraryRepository).findLibraryById("1");
		inOrder.verify(libraryView).libraryRemoved(library);
		inOrder.verify(libraryView).showError("Doesn't exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
}
