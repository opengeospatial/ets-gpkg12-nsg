package org.opengis.cite.gpkg12.nsg.tiles;

import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.opengis.cite.gpkg12.tiles.TileTests;
import org.testng.annotations.Test;

public class NSG_TileTests extends TileTests {

    private static final int MIN_ZOOM = 0;

    private static final int MAX_ZOOM = 24;

    private static final double TOLERANCE = 1.0e-10;

    /**
     * NSG Req 19: Data validity SHALL be assessed against data value constraints specified in Table 26 below using a
     * test suite. Data validity MAY be enforced by SQL triggers.
     *
     * 19-D: Addresses Table 26 Rows 12-17 (regarding table "gpkg_tile_matrix")
     *
     * @throws SQLException
     */
    @Test(groups = { "NSG" }, description = "NSG Req 19-D (Data Validity: gpkg_tile_matrix)")
    public void dataValidity_gpkg_tile_matrix()
                            throws SQLException {
        // test for: Table 26; Row 17
        String queryStr = "SELECT table_name FROM gpkg_contents WHERE data_type=\'tiles\';";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            while ( resultSet.next() ) {
                String tableName = resultSet.getString( "table_name" ).trim();
                int firstZoom = 999;
                int lastZoom = -1;
                String zoomLevelQuery = "SELECT DISTINCT zoom_level FROM " + tableName + " ORDER BY zoom_level;";

                // test for: Table 26; Row 12 & 17
                try (final Statement tileStatement = this.databaseConnection.createStatement();
                                        final ResultSet tileResultSet = tileStatement.executeQuery( zoomLevelQuery )) {
                    while ( tileResultSet.next() ) {
                        int zoom = tileResultSet.getInt( "zoom_level" );
                        firstZoom = Math.min( firstZoom, zoom );
                        lastZoom = Math.max( lastZoom, zoom );
                    }
                }

                assertTrue( ( firstZoom >= this.MIN_ZOOM ),
                            MessageFormat.format( "The "
                                                                          + tableName
                                                                          + " table contains an invalid minimum zoom_level: {0}, should be: {1}",
                                                  Integer.toString( firstZoom ), Integer.toString( this.MIN_ZOOM ) ) );
                assertTrue( ( lastZoom <= this.MAX_ZOOM ),
                            MessageFormat.format( "The "
                                                                          + tableName
                                                                          + " table contains an invalid maximum zoom_level: {0}, should be: {1}",
                                                  Integer.toString( firstZoom ), Integer.toString( this.MAX_ZOOM ) ) );

                String zoomLevelAndExtentQuery = "SELECT zoom_level, tile_width, tile_height, pixel_x_size, pixel_y_size FROM gpkg_tile_matrix WHERE table_name=\'"
                                                 + tableName + "\' ORDER BY zoom_level;";

                try (final Statement tileStatement = this.databaseConnection.createStatement();
                                        final ResultSet tileResultSet = tileStatement.executeQuery( zoomLevelAndExtentQuery )) {
                    boolean firstFound = false;
                    boolean lastFound = false;
                    double pixelSzX = 0.0D;
                    double pixelSzY = 0.0D;

                    while ( tileResultSet.next() ) {
                        int zoom = tileResultSet.getInt( "zoom_level" );
                        int tileWidth = tileResultSet.getInt( "tile_width" );
                        int tileHeight = tileResultSet.getInt( "tile_height" );

                        double lastPixelSzX = pixelSzX;
                        double lastPixelSzY = pixelSzY;
                        pixelSzX = tileResultSet.getDouble( "pixel_x_size" );
                        pixelSzY = tileResultSet.getDouble( "pixel_y_size" );

                        // test for: Table 26; Row 12 (again)
                        assertTrue( ( ( zoom >= firstZoom ) && ( zoom <= lastZoom ) ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid zoom_level: {0} for {1}, should be between {2} and {3}",
                                                          Integer.toString( zoom ), tableName,
                                                          Integer.toString( firstZoom ), Integer.toString( lastZoom ) ) );

                        if ( !firstFound ) {
                            firstFound = ( zoom == firstZoom );
                        }

                        if ( !lastFound ) {
                            lastFound = ( zoom == lastZoom );
                        }

                        // test for: Table 26; Row 13
                        assertTrue( ( tileWidth == 256 ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid tile_width: {0} for {1}, should be 256",
                                                          Integer.toString( tileWidth ), tableName ) );

                        // test for: Table 26; Row 14
                        assertTrue( ( tileHeight == 256 ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid tile_height: {0} for {1}, should be 256",
                                                          Integer.toString( tileHeight ), tableName ) );

                        // test for: Table 26; Row 15
                        double deltaX = Math.abs( ( pixelSzX * 2.0D ) - lastPixelSzX );
                        assertTrue( ( ( zoom == firstZoom ) || ( deltaX < this.TOLERANCE ) ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid pixel_x_size: {0} for {1}",
                                                          String.format( "%.10f", pixelSzX ), tableName ) );

                        // test for: Table 26; Row 16
                        double deltaY = Math.abs( ( pixelSzY * 2.0D ) - lastPixelSzY );
                        assertTrue( ( ( zoom == firstZoom ) || ( deltaY < this.TOLERANCE ) ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid pixel_y_size: {0} for {1}",
                                                          String.format( "%.10f", pixelSzY ), tableName ) );
                    }

                    // test for: Table 26; Row 12 & 17 (again)
                    assertTrue( firstFound,
                                MessageFormat.format( "The gpkg_tile_matrix contains an invalid zoom_level: no zoom level 0 for {0}",
                                                      tableName ) );
                    assertTrue( lastFound,
                                MessageFormat.format( "The gpkg_tile_matrix contains an invalid zoom_level: no max zoom level for {0}",
                                                      tableName ) );
                }
            }
        }
    }

    /**
     * NSG Req 20: The gpkg_tile_matrix table SHALL contain tile_width and tile_height column values of 256 for every
     * table_name tile pyramid data table.
     *
     * NSG Req 21: Every tile_data tile in every table_name tile pyramid data table shall have a width and height of 256
     * pixels.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test(groups = { "NSG" }, description = "NSG Req 20 & 21 (Tile widths and heights)")
    public void tileSizeTests()
                            throws SQLException, IOException {
        // test Req 20
        Collection<String> tableNameAndExtents = collectTableNameAndExtents();
        assertTrue( tableNameAndExtents.isEmpty(),
                    MessageFormat.format( "The gpkg_tile_matrix table contains invalid tile width/height values for tables: {0}",
                                          tableNameAndExtents.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );

        // test Req 21
        String tableNameQuery = "SELECT DISTINCT table_name FROM gpkg_tile_matrix;";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( tableNameQuery )) {
            while ( resultSet.next() ) {
                String tableName = resultSet.getString( "table_name" );
                String subQueryStr = "SELECT zoom_level, tile_column, tile_row, tile_data FROM " + tableName;

                try (final Statement subStatement = this.databaseConnection.createStatement();
                                        final ResultSet subResultSet = subStatement.executeQuery( subQueryStr )) {
                    while ( subResultSet.next() ) {
                        byte[] image = subResultSet.getBytes( "tile_data" );
                        ImageInputStream iis = ImageIO.createImageInputStream( new ByteArrayInputStream( image ) );
                        Iterator readers = ImageIO.getImageReaders( iis );

                        while ( readers.hasNext() ) {
                            ImageReader read = (ImageReader) readers.next();
                            read.setInput( iis, true );
                            int width = read.getWidth( 0 );
                            int height = read.getHeight( 0 );

                            assertTrue( ( width == 256 ),
                                        MessageFormat.format( "The pyramid data table (for {0}) contains a tile image (at zoom_level:  {1}, (col,row): {2},{3}) with an invalid tile_width: {4}",
                                                              tableName,
                                                              Integer.toString( subResultSet.getInt( "zoom_level" ) ),
                                                              Integer.toString( subResultSet.getInt( "tile_column" ) ),
                                                              Integer.toString( subResultSet.getInt( "tile_row" ) ),
                                                              Integer.toString( width ) ) );
                            assertTrue( ( height == 256 ),
                                        MessageFormat.format( "The pyramid data table (for {0}) contains a tile image (at zoom_level:  {1}, (col,row): {2},{3}) with an invalid tile_height: {4}",
                                                              tableName,
                                                              Integer.toString( subResultSet.getInt( "zoom_level" ) ),
                                                              Integer.toString( subResultSet.getInt( "tile_column" ) ),
                                                              Integer.toString( subResultSet.getInt( "tile_row" ) ),
                                                              Integer.toString( height ) ) );
                        }
                    }
                }
            }
        }
    }

    /**
     * NSG Req 22: The gpkg_tile_matrix table SHALL contain pixel_x_size and pixel_y_size column values that differ by a
     * factor of 2 between all adjacent zoom levels for each tile pyramid data table per OGC GeoPackage Clause 2.2.3. It
     * SHALL NOT contain pixel sizes that vary by irregular intervals or by regular intervals other than a factor of 2
     * between adjacent zoom levels per OGC GeoPackage Clause 3.2.1.
     *
     * @throws SQLException
     */
    @Test(groups = { "NSG" }, description = "NSG Req 22 (pixels sizes factor of 2)")
    public void pixelsSizeTests()
                            throws SQLException {
        String queryStr = "SELECT table_name FROM gpkg_contents WHERE data_type=\'tiles\';";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            while ( resultSet.next() ) {
                String tableName = resultSet.getString( "table_name" ).trim();
                String subQueryStr = "SELECT pixel_x_size, pixel_y_size FROM gpkg_tile_matrix " + "WHERE table_name=\'"
                                     + tableName + "\' ORDER BY zoom_level;";

                try (final Statement subStatement = this.databaseConnection.createStatement();
                                        final ResultSet subResultSet = subStatement.executeQuery( subQueryStr )) {
                    double pixelSzX = -1.0D;
                    double pixelSzY = -1.0D;

                    while ( subResultSet.next() ) {
                        double lastPixelSzX = pixelSzX;
                        double lastPixelSzY = pixelSzY;
                        pixelSzX = subResultSet.getDouble( "pixel_x_size" );
                        pixelSzY = subResultSet.getDouble( "pixel_y_size" );

                        if ( lastPixelSzX < 0.0D ) {
                            lastPixelSzX = pixelSzX * 2.0D;
                            lastPixelSzY = pixelSzY * 2.0D;
                        }

                        double deltaX = Math.abs( ( pixelSzX * 2.0D ) - lastPixelSzX );
                        assertTrue( ( deltaX < this.TOLERANCE ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid pixel_x_size: {0} for {1}",
                                                          String.format( "%.10f", pixelSzX ), tableName ) );
                        double deltaY = Math.abs( ( pixelSzY * 2.0D ) - lastPixelSzY );
                        assertTrue( ( deltaY < this.TOLERANCE ),
                                    MessageFormat.format( "The gpkg_tile_matrix contains an invalid pixel_y_size: {0} for {1}",
                                                          String.format( "%.10f", pixelSzY ), tableName ) );
                    }
                }
            }
        }
    }

    /**
     * NSG Req 23: The (min_x, min_y, max_x, max_y) values in the gpkg_tile_matrix_set table SHALL be the maximum bounds
     * of the CRS specified for the tile pyramid data table and SHALL be used to determine the geographic position of
     * each tile in the tile pyramid data table.
     *
     * @throws SQLException
     */
    @Test(groups = { "NSG" }, description = "NSG Req 23 (bounding box in gpkg_tile_matrix_set)")
    public void boundingBoxTests()
                            throws SQLException {
        String queryStr = "SELECT table_name, srs_id, min_x, min_y, max_x, max_y FROM gpkg_tile_matrix_set;";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            while ( resultSet.next() ) {
                String srsID = resultSet.getString( "srs_id" ).trim();
                String tableName = resultSet.getString( "table_name" ).trim();
                double[] mbr = findMbrBySrsId( srsID );

                assertTrue( ( mbr != null ),
                            MessageFormat.format( "The gpkg_tile_matrix_set contains an invalid CRS definition: {0} for table {1}",
                                                  srsID, tableName ) );

                double minX = resultSet.getDouble( "min_x" );
                assertTrue( ( minX == mbr[0] ),
                            MessageFormat.format( "The gpkg_tile_matrix_set contains an invalid min_x value: {0} for table {1} (should be {2})",
                                                  Double.valueOf( minX ), tableName, Double.valueOf( mbr[0] ) ) );

                double minY = resultSet.getDouble( "min_y" );
                assertTrue( ( minY == mbr[1] ),
                            MessageFormat.format( "The gpkg_tile_matrix_set contains an invalid min_y value: {0} for table {1} (should be {2})",
                                                  Double.valueOf( minY ), tableName, Double.valueOf( mbr[1] ) ) );

                double maxX = resultSet.getDouble( "max_x" );
                assertTrue( ( maxX == mbr[2] ),
                            MessageFormat.format( "The gpkg_tile_matrix_set contains an invalid max_x value: {0} for table {1} (should be {2})",
                                                  Double.valueOf( maxX ), tableName, Double.valueOf( mbr[2] ) ) );

                double maxY = resultSet.getDouble( "max_y" );
                assertTrue( ( maxY == mbr[3] ),
                            MessageFormat.format( "The gpkg_tile_matrix_set contains an invalid max_y value: {0} for table {1} (should be {2})",
                                                  Double.valueOf( maxY ), tableName, Double.valueOf( mbr[3] ) ) );
            }
        }
    }

    private Collection<String> collectTableNameAndExtents()
                            throws SQLException {
        String tableNameAndExtentsQuery = "SELECT table_name, zoom_level, tile_width, tile_height FROM gpkg_tile_matrix "
                                          + "WHERE NOT ((tile_width=256) AND (tile_height=256));";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( tableNameAndExtentsQuery )) {
            final Collection<String> tableNamesAndExtents = new LinkedList<>();

            while ( resultSet.next() ) {
                String tableName = resultSet.getString( "table_name" );
                int tileWidth = resultSet.getInt( "tile_width" );
                int tileHeight = resultSet.getInt( "tile_height" );
                tableNamesAndExtents.add( tableName + ": (tile_width)" + tileWidth + ", (tile_height)" + tileHeight );
            }
            return tableNamesAndExtents;
        }
    }

    private double[] findMbrBySrsId( String srsID ) {
        if ( srsID.equals( "3395" ) ) {
            return new double[] { -20037508.342789244D, -20037508.342789244D, 20037508.342789244D, 20037508.342789244D };
        } else if ( srsID.equals( "5041" ) ) {
            return new double[] { -14440759.350252D, -14440759.350252D, 18440759.350252D, 18440759.350252D };
        } else if ( srsID.equals( "4326" ) ) {
            return new double[] { -180.0D, -90.0D, 180.0D, 90.0D };
        }
        return null;
    }

}