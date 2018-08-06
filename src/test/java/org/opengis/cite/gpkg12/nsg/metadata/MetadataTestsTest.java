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

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MetadataTestsTest {

    @Test
    public void testWithOneInvalidEntry()
                            throws Exception {
        MetadataTests metadataTests = new MetadataTests();
        metadataTests.initSchema();

        ResultSet resultSet = mockResultSetWithOneInvalidEntry();
        boolean isValid = metadataTests.hasValidEntry( resultSet );
        assertThat( isValid, is( false ) );
    }

    @Test
    public void testWithOneValidEntry()
                            throws Exception {
        MetadataTests metadataTests = new MetadataTests();
        metadataTests.initSchema();

        ResultSet resultSet = mockResultSetWithOneValidEntry();
        boolean isValid = metadataTests.hasValidEntry( resultSet );
        assertThat( isValid, is( true ) );
    }

    @Test
    public void testWithOneInvalidAndOneValidEntry()
                            throws Exception {
        MetadataTests metadataTests = new MetadataTests();
        metadataTests.initSchema();

        ResultSet resultSet = mockResultSetWithOneInvalidAndOneValidEntry();
        boolean isValid = metadataTests.hasValidEntry( resultSet );
        assertThat( isValid, is( true ) );
    }

    private ResultSet mockResultSetWithOneInvalidEntry()
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