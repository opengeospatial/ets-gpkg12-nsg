package org.opengis.cite.gpkg12.nsg.metadata;

import static org.opengis.cite.gpkg12.ETSAssert.assertSchemaValid;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.opengis.cite.gpkg12.CommonFixture;
import org.opengis.cite.gpkg12.util.DatabaseUtility;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class MetadataTests extends CommonFixture {

    private final static Logger LOG = Logger.getLogger( MetadataTests.class.getName() );

    /**
     * Validate metadata against NMIS xsd https://nsgreg.nga.mil/doc/view?i=2491
     *
     * @throws SQLException
     *             if access to gpkg failed
     */
    @Test(description = "Validate against NMIS schema")
    public void metadataSchemaValidation()
                            throws SQLException {
        Schema schema = createSchema();
        if ( schema == null )
            throw new SkipException( "Schema required for validation could not be loaded." );
        
        if (DatabaseUtility.doesTableOrViewExist(this.databaseConnection, "gpkg_metadata")) {

	        try (final Statement statement = this.databaseConnection.createStatement();
	                                final ResultSet resultSet = statement.executeQuery( "SELECT metadata FROM gpkg_metadata;" )) {
	            while ( resultSet.next() ) {
	                String xmlResult = resultSet.getString( "metadata" );
	                Validator validator = schema.newValidator();
	                Source source = new StreamSource( new ByteArrayInputStream( xmlResult.getBytes() ) );
	                assertSchemaValid( validator, source );
	            }
	        }
        } else {
        	throw new SkipException( "Table gpkg_metadata required to evaluate metadata." );
        }
    }

    private Schema createSchema() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        try {
            URL resource = getClass().getClassLoader().getResource( "org/opengis/cite/gpkg12/nsg/metadata/NMIS_v2.X_Schema/nas/nmis.xsd" );
            return schemaFactory.newSchema( resource );
        } catch ( Exception e ) {
            LOG.log( Level.SEVERE, "Could not load schema 'mis.xsd'", e );
        }
        return null;
    }

}
