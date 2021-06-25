package com.examples.esame_attsw_Bellocci.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.examples.esame_attsw_Bellocci.controller.BookController;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.view.BookView;

public class BookSwingView extends JFrame implements BookView {

	private JPanel contentPane;
	private JTextField txtId;
	private JTextField txtName;
	private JButton btnAdd;
	private DefaultListModel<Book> listBooksModel;
	private JList<Book> listBooks;
	private JButton btnBack;
	private JButton btnDelete;
	private JLabel lblErrorMessage;
	
	private BookController bookController;
	
	private Library library;
	private LibrarySwingView librarySwingView;
	
	protected DefaultListModel<Book> getListBooksModel() {
		return listBooksModel;
	}
	
	protected BookController getBookController() {
		return bookController;
	}
	
	protected JLabel getLblErrorMessage() {
		return lblErrorMessage;
	}
	
	public void setLibrary(Library library) {
		this.library = library;
	}
	
	public void setBookController(BookController bookController) {
		this.bookController = bookController;
	}
	
	public void setLibrarySwingView(LibrarySwingView librarySwingView) {
		this.librarySwingView = librarySwingView;
	}

	/**
	 * Create the frame.
	 */
	public BookSwingView() {
		setTitle("Book View");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 618, 424);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblId = new JLabel("id");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		contentPane.add(lblId, gbc_lblId);
		
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(
						!txtId.getText().trim().isEmpty() &&
						!txtName.getText().trim().isEmpty()
				);
			}
		};
		
		txtId = new JTextField();
		txtId.setName("idTextBox");
		txtId.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.gridwidth = 2;
		gbc_txtId.insets = new Insets(0, 0, 5, 0);
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.gridx = 1;
		gbc_txtId.gridy = 0;
		contentPane.add(txtId, gbc_txtId);
		txtId.setColumns(10);
		
		JLabel lblName = new JLabel("name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		contentPane.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.setName("nameTextBox");
		txtName.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 2;
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 1;
		contentPane.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		btnAdd = new JButton("Add book");
		btnAdd.setEnabled(false);
		btnAdd.addKeyListener(btnAddEnabler);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bookController.newBook(library, new Book(txtId.getText(), txtName.getText()));
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridwidth = 2;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 1;
		gbc_btnAdd.gridy = 2;
		contentPane.add(btnAdd, gbc_btnAdd);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.gridheight = 6;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		listBooksModel = new DefaultListModel<>();
		listBooks = new JList<>(listBooksModel);
		listBooks.setName("bookList");
		listBooks.addListSelectionListener(e -> btnDelete.setEnabled(listBooks.getSelectedIndex() != -1));
		listBooks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(listBooks);
		
		btnBack = new JButton("Back to libraries");
		btnBack.addActionListener(e -> {
			listBooksModel.removeAllElements();
			clearLblErrorMessage();
			librarySwingView.setVisible(true);
			this.setVisible(false);
		});
		GridBagConstraints gbc_btnBack = new GridBagConstraints();
		gbc_btnBack.insets = new Insets(0, 0, 5, 5);
		gbc_btnBack.gridx = 1;
		gbc_btnBack.gridy = 11;
		contentPane.add(btnBack, gbc_btnBack);
		
		btnDelete = new JButton("Delete book");
		btnDelete.setEnabled(false);
		btnDelete.addActionListener(e -> bookController.deleteBook(library, listBooks.getSelectedValue()));
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 2;
		gbc_btnDelete.gridy = 11;
		contentPane.add(btnDelete, gbc_btnDelete);
		
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setForeground(Color.RED);
		lblErrorMessage.setName("errorLabelMessage");
		GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
		gbc_lblErrorMessage.gridwidth = 3;
		gbc_lblErrorMessage.gridx = 0;
		gbc_lblErrorMessage.gridy = 13;
		contentPane.add(lblErrorMessage, gbc_lblErrorMessage);
	}

	@Override
	public void showAllBooks(List<Book> books) {
		books.stream().forEach(listBooksModel::addElement);
	}

	@Override
	public void bookAdded(Book book) {
		listBooksModel.addElement(book);
		clearLblErrorMessage();
	}

	@Override
	public void bookRemoved(Book book) {
		listBooksModel.removeElement(book);
		clearLblErrorMessage();
	}

	@Override
	public void showError(String message, Book book) {
		lblErrorMessage.setText(message + book);
	}

	@Override
	public void closeViewError(String message, Library library) {
		clearLblErrorMessage();
		listBooksModel.removeAllElements();
		this.setVisible(false);
		librarySwingView.setVisible(true);
		librarySwingView.libraryRemoved(library);
		librarySwingView.showError(message, library);
	}

	private void clearLblErrorMessage() {
		lblErrorMessage.setText(" ");
	}
}
