package com.examples.esame_attsw_Bellocci.view.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.examples.esame_attsw_Bellocci.controller.BookController;
import com.examples.esame_attsw_Bellocci.model.Library;

public class BookSwingView extends JFrame {

	private JPanel contentPane;
	
	private BookController bookController;
	
	private Library library;
	
	protected BookController getBookController() {
		return bookController;
	}
	
	public void setLibrary(Library library) {
		this.library = library;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BookSwingView frame = new BookSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BookSwingView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}

}
