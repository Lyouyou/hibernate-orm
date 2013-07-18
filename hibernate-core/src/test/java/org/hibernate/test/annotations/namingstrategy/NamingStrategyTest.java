// $Id$
package org.hibernate.test.annotations.namingstrategy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import org.jboss.logging.Logger;
import org.junit.Test;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.EJB3NamingStrategy;
import org.hibernate.cfg.Mappings;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.metamodel.MetadataBuilder;
import org.hibernate.metamodel.spi.MetadataImplementor;
import org.hibernate.metamodel.spi.relational.Database;
import org.hibernate.metamodel.spi.relational.Schema;
import org.hibernate.testing.FailureExpectedWithNewMetamodel;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestMethod;
import org.hibernate.testing.junit4.TestSessionFactoryHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test harness for ANN-716.
 *
 * @author Hardy Ferentschik
 */
public class NamingStrategyTest extends BaseCoreFunctionalTestMethod {
	private static final Logger log = Logger.getLogger( NamingStrategyTest.class );

	@Test
	public void testWithCustomNamingStrategy() throws Exception {
		try {
			getTestConfiguration().addAnnotatedClass( Address.class ).addAnnotatedClass( Person.class );
			getSessionFactoryHelper().setCallback(
					new TestSessionFactoryHelper.CallbackImpl() {
						@Override
						public void configure(final Configuration configuration) {
							configuration.setNamingStrategy( new DummyNamingStrategy() );
						}

						@Override
						public void configure(final MetadataBuilder metadataBuilder) {
							metadataBuilder.with( new DummyNamingStrategy() );
						}
					}
			).getSessionFactory();
		}
		catch ( Exception e ) {
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter( writer ) );
			log.debug( writer.toString() );
			fail( e.getMessage() );
		}
	}

	@Test
	@FailureExpectedWithNewMetamodel
	public void testWithEJB3NamingStrategy() throws Exception {
		try {

			getTestConfiguration().addAnnotatedClass( A.class ).addAnnotatedClass( AddressEntry.class );
			getSessionFactoryHelper().setCallback(
					new TestSessionFactoryHelper.CallbackImpl() {
						@Override
						public void configure(final Configuration configuration) {
							configuration.setNamingStrategy( EJB3NamingStrategy.INSTANCE );
						}

						@Override
						public void configure(final MetadataBuilder metadataBuilder) {
							metadataBuilder.with( EJB3NamingStrategy.INSTANCE );
						}

						@Override
						public void afterConfigurationBuilt(
								final Mappings mappings, final Dialect dialect) {
							boolean foundIt = false;

							for ( Iterator iter = mappings.iterateTables(); iter.hasNext(); ) {
								Table table = (Table) iter.next();
								log.info( "testWithEJB3NamingStrategy table = " + table.getName() );
								if ( table.getName().equalsIgnoreCase( "A_ADDRESS" ) ) {
									foundIt = true;
								}
								// make sure we use A_ADDRESS instead of AEC_address
								assertFalse(
										"got table name mapped to: AEC_address (should be A_ADDRESS) which violates JPA-2 spec section 11.1.8 ([OWNING_ENTITY_NAME]_[COLLECTION_ATTRIBUTE_NAME])",
										table.getName().equalsIgnoreCase( "AEC_address" )
								);
							}
							assertTrue(
									"table not mapped to A_ADDRESS which violates JPA-2 spec section 11.1.8", foundIt
							);
						}

						@Override
						public void afterMetadataBuilt(final MetadataImplementor metadataImplementor) {
							Database database = metadataImplementor.getDatabase();
							Schema schema = database.getDefaultSchema();
							boolean foundIt = false;
							for ( org.hibernate.metamodel.spi.relational.Table table : schema.getTables() ) {
								String name = table.getTableName().getName().getText();
								log.info( "testWithEJB3NamingStrategy table = " + name );
								if ( name.equalsIgnoreCase( "A_ADDRESS" ) ) {
									foundIt = true;
								}
								// make sure we use A_ADDRESS instead of AEC_address
								assertFalse(
										"got table name mapped to: AEC_address (should be A_ADDRESS) which violates JPA-2 spec section 11.1.8 ([OWNING_ENTITY_NAME]_[COLLECTION_ATTRIBUTE_NAME])",
										name.equalsIgnoreCase( "AEC_address" )
								);

							}

							assertTrue(
									"table not mapped to A_ADDRESS which violates JPA-2 spec section 11.1.8", foundIt
							);
						}
					}
			).getSessionFactory();


		}
		catch ( Exception e ) {
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter( writer ) );
			log.debug( writer.toString() );
			fail( e.getMessage() );
		}
	}

	@Test
	public void testWithoutCustomNamingStrategy() throws Exception {
		try {
			getTestConfiguration().addAnnotatedClass( Address.class ).addAnnotatedClass( Person.class );
			getSessionFactoryHelper().getSessionFactory();

		}
		catch ( Exception e ) {
			StringWriter writer = new StringWriter();
			e.printStackTrace( new PrintWriter( writer ) );
			log.debug( writer.toString() );
			fail( e.getMessage() );
		}
	}
}
