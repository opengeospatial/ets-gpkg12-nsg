package org.opengis.cite.gpkg12.nsg.core;

import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.opengis.cite.gpkg12.ErrorMessage;
import org.opengis.cite.gpkg12.ErrorMessageKeys;
import org.opengis.cite.gpkg12.core.DataContentsTests;
import org.testng.annotations.Test;

public class NSG_DataContentsTests extends DataContentsTests {

    /**
     * --- NSG Req 19: Data validity SHALL be assessed against data value constraints specified in Table 26 below using
     * a test suite. Data validity MAY be enforced by SQL triggers.
     *
     * --- 19-B: Addresses Table 26 Rows 3-7 (regarding table "gpkg_contents")
     *
     * @throws SQLException
     */
    @Test(groups = { "NSG" }, description = "NSG Req 19-B (Data Validity: gpkg_contents)")
    public void dataValidity_gpkg_contents()
                            throws SQLException {
        String queryStr = "SELECT srs_id,table_name,data_type,min_x,min_y,max_x,max_y FROM gpkg_contents;";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            final Collection<String> invalidDataTypes = new LinkedList<>();
            final Collection<String> invalidMinX = new LinkedList<>();
            final Collection<String> invalidMinY = new LinkedList<>();
            final Collection<String> invalidMaxX = new LinkedList<>();
            final Collection<String> invalidMaxY = new LinkedList<>();

            while ( resultSet.next() ) {
                String srsTabNam = resultSet.getString( "table_name" ).trim();
                String srsDataTyp = resultSet.getString( "data_type" ).trim();

                boolean dtFeat = ( srsDataTyp.equals( "features" ) );
                boolean dtTile = ( srsDataTyp.equals( "tiles" ) );

                if ( !dtFeat && !dtTile ) {
                    invalidDataTypes.add( srsTabNam + ":" + srsDataTyp );
                }

                double val;
                double bnd;

                // --- test for: Table 26; Row 4

                String testBoundsColumn = "min_x";
                if ( resultSet.getString( testBoundsColumn ) != null ) {
                    val = resultSet.getDouble( testBoundsColumn );
                    if ( dtFeat ) {
                        bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                        if ( val < bnd ) {
                            invalidMinX.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    } else if ( dtTile ) {
                        bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                        if ( val < bnd ) {
                            invalidMinX.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    }
                }

                // --- test for: Table 26; Row 5

                testBoundsColumn = "min_y";
                if ( resultSet.getString( testBoundsColumn ) != null ) {
                    val = resultSet.getDouble( testBoundsColumn );
                    if ( dtFeat ) {
                        bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                        if ( val < bnd ) {
                            invalidMinY.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    } else if ( dtTile ) {
                        bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                        if ( val < bnd ) {
                            invalidMinY.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    }
                }

                // --- test for: Table 26; Row 6

                testBoundsColumn = "max_x";
                if ( resultSet.getString( testBoundsColumn ) != null ) {
                    val = resultSet.getDouble( testBoundsColumn );
                    if ( dtFeat ) {
                        bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                        if ( val > bnd ) {
                            invalidMaxX.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    } else if ( dtTile ) {
                        bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                        if ( val > bnd ) {
                            invalidMaxX.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    }
                }

                // --- test for: Table 26; Row 7

                testBoundsColumn = "max_y";
                if ( resultSet.getString( testBoundsColumn ) != null ) {
                    val = resultSet.getDouble( testBoundsColumn );
                    if ( dtFeat ) {
                        bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                        if ( val > bnd ) {
                            invalidMaxY.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    } else if ( dtTile ) {
                        bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                        if ( val > bnd ) {
                            invalidMaxY.add( srsTabNam + ":" + val + ", should be: " + bnd );
                        }
                    }
                }
            }
            resultSet.close();
            statement.close();

            assertTrue( invalidDataTypes.isEmpty(),
                        MessageFormat.format( "The gpkg_contents table contains invalid data type values for tables: {0}",
                                              invalidDataTypes.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            assertTrue( invalidMinX.isEmpty(),
                        MessageFormat.format( "The gpkg_contents table contains invalid minimum X bounds values for tables: {0}",
                                              invalidMinX.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            assertTrue( invalidMinY.isEmpty(),
                        MessageFormat.format( "The gpkg_contents table contains invalid minimum Y bounds values for tables: {0}",
                                              invalidMinY.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            assertTrue( invalidMaxX.isEmpty(),
                        MessageFormat.format( "The gpkg_contents table contains invalid maximum X bounds values for tables: {0}",
                                              invalidMaxX.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            assertTrue( invalidMaxY.isEmpty(),
                        MessageFormat.format( "The gpkg_contents table contains invalid maximum Y bounds values for tables: {0}",
                                              invalidMaxY.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
        }
    }

    /*
     * convenience routine to consistently convert specific bounds column from ST_Geometry into double
     */
    private double checkFeatureBounds( String tableName, String boundsColumn )
                            throws SQLException {
        String queryStr = "SELECT column_name FROM gpkg_geometry_columns WHERE table_name=\'" + tableName + "\';";
        String columName;

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            assertTrue( resultSet.next(), "Invalid no result from table: <gpkg_geometry_columns>" );
            columName = resultSet.getString( "column_name" );
        }

        String function = boundsColumn;
        if ( boundsColumn.equals( "min_x" ) ) {
            function = "ST_MinX";
        } else if ( boundsColumn.equals( "min_y" ) ) {
            function = "ST_MinY";
        } else if ( boundsColumn.equals( "min_x" ) ) {
            function = "ST_MinX";
        } else if ( boundsColumn.equals( "min_y" ) ) {
            function = "ST_MinY";
        }

        queryStr = "SELECT " + function + "(" + columName + ") AS theResult FROM " + tableName + ";";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            assertTrue( resultSet.next(), "Invalid no result from " + function + "(" + columName + ") in table:  "
                                          + tableName );
            return resultSet.getDouble( "theResult" );
        }
    }

    /*
     * convenience routine to consistently return specific bounds column as double
     */
    private double checkTileBounds( String tableName, String boundsColumn )
                            throws SQLException {
        String queryStr = "SELECT " + boundsColumn + " FROM gpkg_tile_matrix_set WHERE table_name = \'" + tableName
                          + "\';";
        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            assertTrue( resultSet.next(), ErrorMessage.format( ErrorMessageKeys.BAD_TILE_MATRIX_SET_TABLE_DEFINITION ) );

            return resultSet.getDouble( boundsColumn );
        }
    }

}