package com.examples.esameattswbellocci.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.repository.mysql.LibraryMySQLRepository;
import com.examples.esameattswbellocci.view.BookView;

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
	
	private Library library;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		library = new Library("1", "library1");
		closeable = MockitoAnnotations.openMocks(this);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testAllBooksWhenLibraryIsContainedIntoDatabaseShouldReturnListOfBooksOfLibrary() {
		// setup
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		List<Book> books = asList(new Book());
		when(bookRepository.takeAllBooksOfLibrary(library.getId())).thenReturn(books);
		
		// exercise
		bookController.allBooks(library);
		
		// verify
		verify(bookView).showAllBooks(books);
	}
	
	@Test
	public void testAllBooksWhenLibraryDoesntExistIntoDatabaseShouldShowErrorInLibraryView() {
		// setup
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.allBooks(library);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}
	
	@Test
	public void testNewBookWhenBookDoesntAlreadyExist() {
		// setup
		Book book = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(book.getId())).thenReturn(null);
		
		// exercise
		bookController.newBook(library, book);
		
		// verify
		InOrder inOrder = inOrder(bookRepository, bookView);
		inOrder.verify(bookRepository).saveBookInTheLibrary(library, book);
		inOrder.verify(bookView).bookAdded(book);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testNewBookWhenBookAlreadyExist() {
		// setup
		Book alreadyAdded = new Book("1", "test");
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(newBook.getId())).thenReturn(alreadyAdded);
		
		// exercise
		bookController.newBook(library, newBook);
		
		// verify
		verify(bookView).showError("Already existing book with id 1", alreadyAdded);
		verifyNoMoreInteractions(ignoreStubs(bookRepository));
	}
	
	@Test
	public void testNewBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.newBook(library, newBook);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, bookRepository));
	}
	
	@Test
	public void testNewBookWhenAlreadyAddedBookIsPassedToBookRepositoryShouldShowErrorInBookView() {
		// setup
		Book alreadyAdded = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById("1")).thenReturn(library);
		when(bookRepository.findBookById(alreadyAdded.getId())).thenReturn(null);
		doThrow(new IllegalArgumentException("Database already contains the book with id 1"))
			.when(bookRepository).saveBookInTheLibrary(library, alreadyAdded);
		
		// exercise
		bookController.newBook(library, alreadyAdded);
		
		// verify
		verify(bookView).showError("Database already contains the book with id 1", alreadyAdded);
		verifyNoMoreInteractions(ignoreStubs(bookView));
	}
	
	@Test
	public void testDeleteBookWhenBookExists() {
		// setup
		Book bookDeleted = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(bookDeleted.getId())).thenReturn(bookDeleted);
		
		// exercise
		bookController.deleteBook(library, bookDeleted);
		
		// verify
		InOrder inOrder = inOrder(bookRepository, bookView);
		inOrder.verify(bookRepository).deleteBookFromLibrary("1", "1");
		inOrder.verify(bookView).bookRemoved(bookDeleted);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testDeleteBookWhenBookDoesntExist() {
		// setup
		Book bookNoFound = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(bookNoFound.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, bookNoFound);
		
		// verify
		verify(bookView).bookRemoved(bookNoFound);
		verify(bookView).showError("No existing book with id 1", bookNoFound);
		verifyNoMoreInteractions(ignoreStubs(bookRepository));
	}
	
	@Test
	public void testDeleteBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, newBook);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, bookRepository));
	}
	
	@Test
	public void testDeleteBookWhenDatabaseDoesntContainBookButItPassedToBookRepositoryShouldShowErrorInBookView() {
		// setup
		Book book = new Book("1", "not_exist");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById("1")).thenReturn(book);
		doThrow(new IllegalArgumentException("Database doesn't contain book with id 1"))
			.when(bookRepository).deleteBookFromLibrary("1", "1");
		
		// exercise
		bookController.deleteBook(library, book);
		
		// verify
		verify(bookRepository).deleteBookFromLibrary("1", "1");
		verify(bookView).showError("Database doesn't contain book with id 1", book);
		verifyNoMoreInteractions(ignoreStubs(bookRepository));
		verifyNoMoreInteractions(ignoreStubs(bookView));
	}
	
	@Test
	public void testDeleteBookWhenBookIsContainedInAnotherLibraryShouldShowErrorInBookView() {
		// setup
		Library library2 = new Library("2", "library2");
		Book book = new Book("1", "not_exist");
		book.setLibrary(library2);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById("1")).thenReturn(book);
		doThrow(new IllegalArgumentException("Library with id 1 doesn't contain book with id 1"))
			.when(bookRepository).deleteBookFromLibrary("1", "1");
		
		// exercise
		bookController.deleteBook(library, book);
		
		// verify
		verify(bookRepository).deleteBookFromLibrary("1", "1");
		verify(bookView).showError("Library with id 1 doesn't contain book with id 1", book);
		verifyNoMoreInteractions(ignoreStubs(bookRepository));
		verifyNoMoreInteractions(ignoreStubs(bookView));
	}
}
