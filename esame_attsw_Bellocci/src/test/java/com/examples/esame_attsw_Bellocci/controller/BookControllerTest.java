package com.examples.esame_attsw_Bellocci.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.BookRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;
import com.examples.esame_attsw_Bellocci.view.BookView;

public class BookControllerTest {

	@Mock
	private BookRepository bookRepository;
	
	@Mock
	private BookView bookView;
	
	@Mock
	private LibraryController libraryController;
	
	@Mock
	private LibraryMySQLRepository libraryRepository;
	
	@InjectMocks
	private BookController bookController;
	
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
	public void testAllBooksWhenLibraryIsContainedIntoDatabaseShouldReturnListOfBooksOfLibrary() {
		Library library = new Library("1", "library1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		List<Book> books = asList(new Book());
		when(bookRepository.getAllBooksOfLibrary(library.getId())).thenReturn(books);
		bookController.allBooks(library);
		verify(bookView).showAllBooks(books);
	}
	
	@Test
	public void testAllBooksWhenLibraryDoesntExistIntoDatabaseShouldShowErrorInLibraryView() {
		Library library = new Library("1", "library1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		bookController.allBooks(library);
		verify(bookView).closeViewError("Doesnt exist library with id 1 ", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}

}
