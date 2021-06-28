package com.examples.esameattswbellocci.hibernate.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

public class HibernateUtilTest {

	private static Properties settings;

	@BeforeClass
	public static void setupSettingsHibernate() {
		settings = new Properties();
		
		settings.put(AvailableSettings.DRIVER, "org.h2.Driver");
		settings.put(AvailableSettings.URL, "jdbc:h2:mem:test");
		settings.put(AvailableSettings.USER, "user");
		settings.put(AvailableSettings.PASS, "password");
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(AvailableSettings.SHOW_SQL, "true");
		settings.put(AvailableSettings.FORMAT_SQL, "true");
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
	}
	
	@Before
	public void setup() {
		HibernateUtil.setSessionFactory(null);
	}
	
	@After
	public void closeSessionFactory() {
		if(HibernateUtil.getValueSessionFactory() != null && !HibernateUtil.getValueSessionFactory().isClosed())
			HibernateUtil.getValueSessionFactory().close();
	}
	
	@Test
	public void testGetSessionFactoryWhenSettingsIsNotEmptyAndSessionFactoryIsNullShouldInitializeUsingSettings() {
		// setup
		HibernateUtil.setProperties(settings);
		
		// exercise & verify
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
	}
	
	@Test
	public void testGetSessionFactoryWhenSessionFactoryAndSettingsAreNullShouldInitalizeByXMLFile() {
		// setup
		HibernateUtil.setProperties(null);
		HibernateUtil.setPathConfigurationFile("src/test/resources/");
		
		// exercise & verify
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
	}
	
	@Test
	public void testGetSessionFactoryWhenItIsAlreadyInitializeShouldReturnTheSameObject() {
		// setup
		HibernateUtil.setProperties(settings);
		// exercise
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		// verify
		assertThat(sessionFactory).isNotNull();
		assertThat(HibernateUtil.getSessionFactory()).isEqualTo(sessionFactory);
	}
	 
	@Test
	public void testResetSessionFactoryWhenSessionFactoryIsNotNullAndOpenShouldCloseItAndIstanciateToNull() {
		// setup
		HibernateUtil.setProperties(null);
		SessionFactory sessionFactory = createSessionFactory();
		HibernateUtil.setSessionFactory(sessionFactory);
		assertThat(HibernateUtil.getValueSessionFactory()).isNotNull();
		// exercise
		HibernateUtil.resetSessionFactory();
		//verify
		assertThat(HibernateUtil.getValueSessionFactory()).isNull();
	}
	
	private SessionFactory createSessionFactory() {
		Configuration configuration = new Configuration();
    	
    	configuration.setProperties(settings);
    	
    	configuration.addAnnotatedClass(Library.class);
        configuration.addAnnotatedClass(Book.class);
        
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        return configuration.buildSessionFactory(serviceRegistry);
	}
	
	@Test
	public void testResetSessionFactoryWhenSessionFactoryIsNullItsIstanceDoesntChange() {
		// setup
		assertThat(HibernateUtil.getValueSessionFactory()).isNull();
		// exercise
		HibernateUtil.resetSessionFactory();
		//verify
		assertThat(HibernateUtil.getValueSessionFactory()).isNull();
	}
	
	@Test
	public void testResetSessionFactoryWhenSessionFactoryIsClosedButNotNullShouldSetItNull() {
		// setup
		SessionFactory sessionFactory = createSessionFactory();
		HibernateUtil.setSessionFactory(sessionFactory);
		HibernateUtil.getValueSessionFactory().close();
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
		// exercise
		HibernateUtil.resetSessionFactory();
		// verify
		assertThat(HibernateUtil.getValueSessionFactory()).isNull();
	}
}
