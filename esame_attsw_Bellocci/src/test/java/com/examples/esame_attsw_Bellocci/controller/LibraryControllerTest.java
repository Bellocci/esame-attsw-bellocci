package com.examples.esame_attsw_Bellocci.controller;

import static org.assertj.core.api.Assertions.*;
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

import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.view.LibraryView;

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
		libraryController = new LibraryController(libraryView, libraryRepository);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testGetAllLibrariesShouldRequestTheLibrariesFromDatabaseAndReturnThemToTheView() {
		// setup
		List<Library> list_libraries = new ArrayList<Library>();	
		when(libraryRepository.getAllLibraries()).thenReturn(list_libraries);
		
		// exercise
		libraryController.getAllLibraries();
		
		// verify
		verify(libraryView).showAllLibraries(list_libraries);
	}
	

	@Test
	public void testNewLibraryWhenItDoesntExistShouldAddToDatabaseAndReturnThemToTheView() {
		// setup
		Library new_library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(null);
		
		// exercise
		libraryController.newLibrary(new_library);
		
		// verify
		InOrder inOrder = inOrder(libraryRepository, libraryView);
		inOrder.verify(libraryRepository).saveLibrary(new_library);
		inOrder.verify(libraryView).libraryAdded(new_library);
	}
	
	@Test
	public void testNewLibraryWhenItAlreadyExistShouldNotAddLibraryAndShowErrorToView() {
		// setup
		Library library = new Library("1", "library1");
		when(libraryRepository.findLibraryById("1")).thenReturn(library);
		
		// exercise
		libraryController.newLibrary(library);
		
		// verify
		verify(libraryView).showError("Already existing library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository));
	}
	
	@Test
	public void testNewLibraryWhenIdIsEmptyShouldThrow() {
		// setup
		Library library = new Library("", "library1");
		
		// exercise & verify
		assertThatThrownBy(() -> libraryController.newLibrary(library))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Id library cannot be empty or only blank space");
	}
	
	@Test
	public void testNewLibraryWhenIdAreOnlyBlankSpaceShouldThrow() {
		Library library = new Library("  ", "library1");
		assertThatThrownBy(() -> libraryController.newLibrary(library))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Id library cannot be empty or only blank space");
	}
	
}
