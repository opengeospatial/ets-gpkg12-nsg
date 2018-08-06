package org.opengis.cite.gpkg12.nsg.metadata;

import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.opengis.cite.gpkg12.CommonFixture;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class MetadataTests extends CommonFixture {

    private Schema schema;

    @BeforeClass
    public void initSchema()
                            throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        URL resource = getClass().getClassLoader().getResource( "org/opengis/cite/gpkg12/nsg/metadata/NMIS_v2.X_Schema/nas/nmis.xsd" );
        this.schema = schemaFactory.newSchema( resource );
    }

    /**
     * Validate metadata against NMIS xsd https://nsgreg.nga.mil/doc/view?i=2491
     *
     * @throws SQLException
     *             if access to gpkg failed
     */
    @Test(description = "Validate against NMIS schema")
    public void metadataSchemaValidation()
                            throws SQLException {
        if ( this.schema == null )
            throw new SkipException( "Schema required for validation could not be loaded." );

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( "SELECT metadata FROM gpkg_metadata;" )) {
            boolean foundValidMetadataEntry = hasValidEntry( resultSet );
            assertTrue( foundValidMetadataEntry,
                        "GeoPackage does not contain at least one metadata entry valid against NIMS." );
        }
    }

    boolean hasValidEntry( ResultSet resultSet )
                            throws SQLException {
        while ( resultSet.next() ) {
            String metadataEntry = resultSet.getString( "metadata" );
            boolean isValid = isEntryValid( metadataEntry );
            if ( isValid )
                return true;
        }
        return false;
    }

    private boolean isEntryValid( String metadataEntry ) {
        if ( metadataEntry == null || metadataEntry.isEmpty() )
            return false;
        return isSchemaValid( metadataEntry );
    }

    private boolean isSchemaValid( String metadataEntry ) {
        try {
            Validator validator = schema.newValidator();
            Source source = new StreamSource( new ByteArrayInputStream( metadataEntry.getBytes() ) );
            ValidationErrorHandler errHandler = new ValidationErrorHandler();
            validator.setErrorHandler( errHandler );
            validator.validate( source );
            return errHandler.errorsDetected();
        } catch ( Exception var4 ) {
            return false;
        }
    }

}
