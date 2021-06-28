package com.examples.esameAttswBellocci.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.examples.esameAttswBellocci.controller.LibraryController;
import com.examples.esameAttswBellocci.model.Library;
import com.examples.esameAttswBellocci.view.LibraryView;

public class LibrarySwingView extends JFrame implements LibraryView {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txtId;
	private JTextField txtName;
	private JButton btnAdd;
	private JButton btnDelete;
	private JScrollPane scrollPane;
	private JLabel lblErrorMessage;
	private JButton btnOpen;
	private JList<Library> listLibraries;
	private DefaultListModel<Library> listLibraryModel;
	
	private transient LibraryController libraryController;
	private BookSwingView bookSwingView = new BookSwingView();
	
	protected DefaultListModel<Library> getListLibraryModel() {
		return listLibraryModel;
	}
	
	protected JLabel getLblErrorMessage() {
		return lblErrorMessage;
	}
	
	
	public void setLibraryController(LibraryController libraryController) {
		this.libraryController = libraryController;
	}
	
	public void setBookSwingView(BookSwingView bookSwingView) {
		this.bookSwingView = bookSwingView;
	}

	/**
	 * Create the frame.
	 */
	public LibrarySwingView() {
		setTitle("Library View");
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 661, 383);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{57, 35, 179, 77, 360, 0};
		gbl_contentPane.rowHeights = new int[]{19, 19, 25, 158, 25, 15, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
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
		
		JLabel lblId = new JLabel("id");
		lblId.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.gridwidth = 2;
		gbc_lblId.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		contentPane.add(lblId, gbc_lblId);
		
		txtId.setColumns(10);
		GridBagConstraints gbc_txtId = new GridBagConstraints();
		gbc_txtId.anchor = GridBagConstraints.NORTH;
		gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtId.insets = new Insets(0, 0, 5, 0);
		gbc_txtId.gridwidth = 3;
		gbc_txtId.gridx = 2;
		gbc_txtId.gridy = 0;
		contentPane.add(txtId, gbc_txtId);
		
		JLabel lblName = new JLabel("name");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.gridwidth = 2;
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		contentPane.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		txtName.setName("nameTextBox");
		txtName.setColumns(10);
		txtName.addKeyListener(btnAddEnabler);
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.anchor = GridBagConstraints.NORTH;
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.gridwidth = 3;
		gbc_txtName.gridx = 2;
		gbc_txtName.gridy = 1;
		contentPane.add(txtName, gbc_txtName);
		
		btnAdd = new JButton("Add library");
		btnAdd.setEnabled(false);
		btnAdd.addKeyListener(btnAddEnabler);
		btnAdd.addActionListener(
				e -> libraryController.newLibrary(new Library(txtId.getText(), txtName.getText()))
		);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.NORTH;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 3;
		gbc_btnAdd.gridy = 2;
		contentPane.add(btnAdd, gbc_btnAdd);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 3;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		listLibraryModel = new DefaultListModel<>();
		listLibraries = new JList<>(listLibraryModel);
		listLibraries.addListSelectionListener(e ->  {
			btnDelete.setEnabled(listLibraries.getSelectedIndex() != -1);
			btnOpen.setEnabled(listLibraries.getSelectedIndex() != -1);
		});
		listLibraries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listLibraries.setName("libraryList");
		listLibraries.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Library library = (Library) value;
				return super.getListCellRendererComponent(list,
						getDisplayString(library),
						index, isSelected, cellHasFocus);
			}
		});
		scrollPane.setViewportView(listLibraries);
		
		btnOpen = new JButton("Open library");
		btnOpen.setEnabled(false);
		btnOpen.addActionListener(
			e -> libraryController.findLibrary(listLibraries.getSelectedValue())
		);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnOpen.insets = new Insets(0, 0, 5, 5);
		gbc_btnOpen.gridx = 2;
		gbc_btnOpen.gridy = 4;
		contentPane.add(btnOpen, gbc_btnOpen);
		
		btnDelete = new JButton("Delete library");
		btnDelete.setEnabled(false);
		btnDelete.addActionListener(
				e -> libraryController.deleteLibrary(listLibraries.getSelectedValue())
		);
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 4;
		gbc_btnDelete.gridy = 4;
		contentPane.add(btnDelete, gbc_btnDelete);
		
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setName("errorLabelMessage");
		lblErrorMessage.setForeground(Color.RED);
		lblErrorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
		gbc_lblErrorMessage.anchor = GridBagConstraints.NORTH;
		gbc_lblErrorMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblErrorMessage.gridwidth = 5;
		gbc_lblErrorMessage.gridx = 0;
		gbc_lblErrorMessage.gridy = 5;
		contentPane.add(lblErrorMessage, gbc_lblErrorMessage);
	}

	@Override
	public void showAllLibraries(List<Library> libraries) {
		libraries.stream().forEach(listLibraryModel::addElement);
	}

	@Override
	public void libraryAdded(Library library) {
		listLibraryModel.addElement(library);
		clearErrorMessage();
	}

	@Override
	public void libraryRemoved(Library library) {
		listLibraryModel.removeElement(library);
		clearErrorMessage();
	}

	@Override
	public void showError(String errorMessage, Library library) {
		lblErrorMessage.setText(errorMessage + " : " + getDisplayString(library));
	}

	@Override
	public void showAllBooksOfLibrary(Library library) {
		clearErrorMessage();
		bookSwingView.setLibrary(library);
		bookSwingView.setVisible(true);
		this.setVisible(false);
		bookSwingView.getBookController().allBooks(library);
	}

	private void clearErrorMessage() {
		lblErrorMessage.setText(" ");
	}
	
	private String getDisplayString(Library library) {
		return library.getId() + " - " + library.getName();
	}
}
