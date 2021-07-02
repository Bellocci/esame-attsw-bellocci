package com.examples.esameattswbellocci.app.swing;

import java.awt.EventQueue;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.hibernate.cfg.AvailableSettings;

import com.examples.esameattswbellocci.controller.BookController;
import com.examples.esameattswbellocci.controller.LibraryController;
import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.repository.mysql.BookMySQLRepository;
import com.examples.esameattswbellocci.repository.mysql.LibraryMySQLRepository;
import com.examples.esameattswbellocci.view.swing.BookSwingView;
import com.examples.esameattswbellocci.view.swing.LibrarySwingView;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class LibrarySwingApp implements Callable<Void> {

	@Option(names = {"--useHibernateCfgXML"}, description = "true: Use hibernate.cfg.xml file to connect to database \n"
			+ "false: (Default) use args to connect to database")
	private boolean useHibernateCfgXML = false;
	
	@Option(names = {"--mysql-host"}, description = "MySQLDB host address")
	private String mysqlHost = "localhost";
	
	@Option(names = {"--mysql-port"}, description = "MySQLDB host port")
	private int mysqlPort = 3307;
	
	@Option(names = {"--db-name"}, description = "Database name")
	private String databaseName = "library";
	
	@Option(names = {"--db-user"}, description = "Database user")
	private String username = "user";
	
	@Option(names = {"--db-password"}, description = "Database password")
	private String password = "password";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new CommandLine(new LibrarySwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			if(!useHibernateCfgXML) {
				Properties settings = new Properties();
				
				settings.put(AvailableSettings.DRIVER, "com.mysql.cj.jdbc.Driver");
				settings.put(AvailableSettings.DRIVER, "com.mysql.cj.jdbc.Driver");
				settings.put(AvailableSettings.URL, "jdbc:mysql://"+mysqlHost+":"+
														mysqlPort+"/"+databaseName+"?useSSL=false");
				settings.put(AvailableSettings.USER, username);
				settings.put(AvailableSettings.PASS, password);
				settings.put(AvailableSettings.POOL_SIZE, "1");
				settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
				settings.put(AvailableSettings.SHOW_SQL, "true");
				settings.put(AvailableSettings.FORMAT_SQL, "true");
				settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
				settings.put(AvailableSettings.HBM2DDL_AUTO, "update");
				settings.put(AvailableSettings.HBM2DDL_HALT_ON_ERROR, "true");
				settings.put(AvailableSettings.HBM2DDL_CREATE_SCHEMAS, "true");
				settings.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
				
				HibernateUtil.setProperties(settings);
			}
			
			
			
			// Instanciate repository
			LibraryRepository libraryRepository = new LibraryMySQLRepository();
			BookRepository bookRepository = new BookMySQLRepository();
			
			// Istanciate view
			LibrarySwingView libraryView = new LibrarySwingView();
			BookSwingView bookView = new BookSwingView();
			
			// Istanciate controller
			LibraryController libraryController = new LibraryController(libraryView, libraryRepository);
			BookController bookController = new BookController(bookRepository, bookView, libraryController);
			
			// setting params
			libraryView.setLibraryController(libraryController);
			bookView.setBookController(bookController);
			bookView.setLibrarySwingView(libraryView);
			libraryView.setBookSwingView(bookView);
			
			try {
				HibernateUtil.getSessionFactory();
			} catch(IllegalArgumentException e) {
				libraryView.showError(e.getMessage(), null);
			} finally {
				// show view
				libraryView.setVisible(true);
				libraryController.allLibraries();
			}
			
		});
		return null;
	}

}
