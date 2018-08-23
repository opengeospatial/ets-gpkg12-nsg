package org.opengis.cite.gpkg12.nsg.metadata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MetadataTestsTest {

    private MetadataTests metadataTests;

    @Before
    public void initMetadataTests()
            throws SAXException, ParserConfigurationException {
        this.metadataTests = new MetadataTests();
        metadataTests.initSchema();
        metadataTests.initDocumentBuilder();
    }

    @Test(expected = AssertionError.class)
    public void testWithOneInvalidEntry()
                            throws Exception {
        ResultSet resultSet = mockResultSetWithOneStringEntry();
        List<String> xmlEntries = metadataTests.findXmlEntries( resultSet );
        assertThat( xmlEntries.size(), is( 0 ) );

        metadataTests.validateXmlEntries( xmlEntries );
    }

    @Test
    public void testWithOneValidEntry()
                            throws Exception {
        ResultSet resultSet = mockResultSetWithOneValidEntry();
        List<String> xmlEntries = metadataTests.findXmlEntries( resultSet );
        assertThat( xmlEntries.size(), is( 1 ) );

        metadataTests.validateXmlEntries( xmlEntries );
    }

    @Test
    public void testWithOneInvalidAndOneValidEntry()
                            throws Exception {
        ResultSet resultSet = mockResultSetWithOneInvalidAndOneValidEntry();
        List<String> xmlEntries = metadataTests.findXmlEntries( resultSet );
        assertThat( xmlEntries.size(), is( 1 ) );

        metadataTests.validateXmlEntries( xmlEntries );
    }

    private ResultSet mockResultSetWithOneStringEntry()
                            throws SQLException {
        ResultSet mock = mock( ResultSet.class );
        when( mock.next() ).thenReturn( true, false );
        when( mock.getString( "metadata" ) ).thenReturn( simpleStringMetadata() );
        return mock;
    }

    private ResultSet mockResultSetWithOneValidEntry()
                            throws SQLException, IOException {
        ResultSet mock = mock( ResultSet.class );
        when( mock.next() ).thenReturn( true, false );
        when( mock.getString( "metadata" ) ).thenReturn( validMetadata() );
        return mock;
    }

    private ResultSet mockResultSetWithOneInvalidAndOneValidEntry()
                            throws SQLException, IOException {
        ResultSet mock = mock( ResultSet.class );
        when( mock.next() ).thenReturn( true, true, false );
        when( mock.getString( "metadata" ) ).thenReturn( emptyMetadata(), validMetadata() );
        return mock;
    }

    private String validMetadata()
                            throws IOException {
        InputStream metadata = getClass().getResourceAsStream( "nmisExampleInstanceShort_ISM6.xml" );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ( ( nRead = metadata.read( data, 0, data.length ) ) != -1 ) {
            buffer.write( data, 0, nRead );
        }
        buffer.flush();
        return new String( buffer.toByteArray(), StandardCharsets.UTF_8 );
    }

    private String simpleStringMetadata() {
        return "invalid";
    }

    private String emptyMetadata() {
        return "";
    }

}