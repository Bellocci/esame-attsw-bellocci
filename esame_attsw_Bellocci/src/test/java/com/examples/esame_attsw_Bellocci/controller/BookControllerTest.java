package com.examples.esame_attsw_Bellocci.controller;

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
		verify(bookView).closeViewError("Doesnt exist library with id 1 ", library);
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
		Book already_added = new Book("1", "test");
		Book new_book = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(new_book.getId())).thenReturn(already_added);
		
		// exercise
		bookController.newBook(library, new_book);
		
		// verify
		verify(bookView).showError("Already existing book with id 1", already_added);
		verifyNoMoreInteractions(ignoreStubs(bookRepository, libraryRepository, libraryController));
	}
	
	@Test
	public void testNewBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Library library = new Library("1", "library1");
		Book new_book = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.newBook(library, new_book);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1 ", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}
	
	@Test
	public void testDeleteBookWhenBookExists() {
		// setup
		Library library = new Library("1", "library1");
		Book book_to_delete = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(book_to_delete.getId())).thenReturn(book_to_delete);
		
		// exercise
		bookController.deleteBook(library, book_to_delete);
		
		// verify
		InOrder inOrder = inOrder(bookRepository, bookView);
		inOrder.verify(bookRepository).deleteBookFromLibrary(library.getId(), book_to_delete.getId());
		inOrder.verify(bookView).bookRemoved(book_to_delete);
	}
	
	@Test
	public void testDeleteBookWhenBookDoesntExist() {
		// setup
		Library library = new Library("1", "library1");
		Book book_no_found = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		when(bookRepository.findBookById(book_no_found.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, book_no_found);
		
		// verify
		verify(bookView).bookRemoved(book_no_found);
		verify(bookView).showError("No existing book with id 1", book_no_found);
		verifyNoMoreInteractions(ignoreStubs(bookRepository, libraryRepository, libraryController));
	}
	
	@Test
	public void testDeleteBookWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Library library = new Library("1", "library1");
		Book new_book = new Book("1", "book1");
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(null);
		
		// exercise
		bookController.deleteBook(library, new_book);
		
		// verify
		verify(bookView).closeViewError("Doesnt exist library with id 1 ", library);
		verifyNoMoreInteractions(ignoreStubs(libraryRepository, libraryController));
	}
}
