package com.examples.esame_attsw_Bellocci.hibernate.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public class HibernateUtilTest {

	private StandardServiceRegistry registry;
	private static Properties settings;

	@BeforeClass
	public static void setupSettingsHibernate() {
		settings = new Properties();
		
		settings.put(Environment.DRIVER, "org.h2.Driver");
		settings.put(Environment.URL, "jdbc:h2:mem:test");
		settings.put(Environment.USER, "user");
		settings.put(Environment.PASS, "password");
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
	}
	
	@Before
	public void setup() {
		HibernateUtil.setSessionFactory(null);
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
	public void testResetSessionFactoryWhenSessionFactoryIsNotNullShouldCloseSessionFractoryAndIstanciateItToNull() {
		// setup
		HibernateUtil.setProperties(null);
		SessionFactory sessionFactory = createSessionFactory();
		HibernateUtil.setSessionFactory(sessionFactory);
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
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
}
