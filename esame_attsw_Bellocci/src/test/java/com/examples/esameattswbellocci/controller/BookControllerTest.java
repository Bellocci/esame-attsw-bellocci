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
		// setup
		Library library = new Library("1", "library1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		List<Book> books = asList(new Book());
		when(bookRepository.getAllBooksOfLibrary(library.getId())).thenReturn(books);
		
		// exercise
		bookController.allBooks(library);
		
		// verify
		verify(bookView).showAllBooks(books);
	}
	
	@Test
	public void testAllBooksWhenLibraryDoesntExistIntoDatabaseShouldShowErrorInLibraryView() {
		// setup
		Library library = new Library("1", "library1");
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
		Library library = new Library("1", "library1");
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
	}
	
	@Test
	public void testNewBookWhenBookAlreadyExist() {
		// setup
		Library library = new Library("1", "library1");
		Book alreadyAdded = new Book("1", "test");
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(newBook.getId())).thenReturn(alreadyAdded);
		
		// exercise
		bookController.newBook(library, newBook);
		
		// verify
		verify(bookView).showError("Already existing book with id 1", alreadyAdded);
		verifyNoMoreInteractions(ignoreStubs(bookRepository, libraryRepository, libraryController));
	}
	
	@Test
	public void testNewBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Library library = new Library("1", "library1");
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.newBook(library, newBook);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}
	
	@Test
	public void testDeleteBookWhenBookExists() {
		// setup
		Library library = new Library("1", "library1");
		Book bookDeleted = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(bookDeleted.getId())).thenReturn(bookDeleted);
		
		// exercise
		bookController.deleteBook(library, bookDeleted);
		
		// verify
		InOrder inOrder = inOrder(bookRepository, bookView);
		inOrder.verify(bookRepository).deleteBookFromLibrary(library.getId(), bookDeleted.getId());
		inOrder.verify(bookView).bookRemoved(bookDeleted);
	}
	
	@Test
	public void testDeleteBookWhenBookDoesntExist() {
		// setup
		Library library = new Library("1", "library1");
		Book bookNoFound = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(bookNoFound.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, bookNoFound);
		
		// verify
		verify(bookView).bookRemoved(bookNoFound);
		verify(bookView).showError("No existing book with id 1", bookNoFound);
		verifyNoMoreInteractions(ignoreStubs(bookRepository, libraryRepository, libraryController));
	}
	
	@Test
	public void testDeleteBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Library library = new Library("1", "library1");
		Book newBook = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, newBook);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}
}
