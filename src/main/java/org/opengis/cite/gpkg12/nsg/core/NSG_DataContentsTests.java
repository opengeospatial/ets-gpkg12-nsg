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

                // test for: Table 26; Row 4
                collectInvalidMinValues( resultSet, invalidMinX, srsTabNam, dtFeat, dtTile, "min_x" );

                // test for: Table 26; Row 5
                collectInvalidMinValues( resultSet, invalidMinY, srsTabNam, dtFeat, dtTile, "min_y" );

                // test for: Table 26; Row 6
                collectInvalidMaxValues( resultSet, invalidMaxX, srsTabNam, dtFeat, dtTile, "max_x" );

                // test for: Table 26; Row 7
                collectInvalidMaxValues( resultSet, invalidMaxY, srsTabNam, dtFeat, dtTile, "max_y" );
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

    private void collectInvalidMaxValues( ResultSet resultSet, Collection<String> invalidValue, String srsTabNam,
                                          boolean dtFeat, boolean dtTile, String testBoundsColumn )
                            throws SQLException {
        if ( resultSet.getString( testBoundsColumn ) != null ) {
            double val = resultSet.getDouble( testBoundsColumn );
            if ( dtFeat ) {
                double bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                if ( val > bnd ) {
                    invalidValue.add( srsTabNam + ":" + val + ", should be: " + bnd );
                }
            } else if ( dtTile ) {
                double bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                if ( val > bnd ) {
                    invalidValue.add( srsTabNam + ":" + val + ", should be: " + bnd );
                }
            }
        }
    }

    private void collectInvalidMinValues( ResultSet resultSet, Collection<String> invalidValue,
                                          String srsTabNam, boolean dtFeat, boolean dtTile,
                                          String testBoundsColumn )
                            throws SQLException {
        if ( resultSet.getString( testBoundsColumn ) != null ) {
            double val = resultSet.getDouble( testBoundsColumn );
            if ( dtFeat ) {
                double bnd = this.checkFeatureBounds( srsTabNam, testBoundsColumn );
                if ( val < bnd ) {
                    invalidValue.add( srsTabNam + ":" + val + ", should be: " + bnd );
                }
            } else if ( dtTile ) {
                double bnd = this.checkTileBounds( srsTabNam, testBoundsColumn );
                if ( val < bnd ) {
                    invalidValue.add( srsTabNam + ":" + val + ", should be: " + bnd );
                }
            }
        }
    }

    /*
     * convenience routine to consistently convert specific bounds column from ST_Geometry into double
     */
    private double checkFeatureBounds( String tableName, String boundsColumn )
                            throws SQLException {
        String columName = checkColumnName( tableName );

        String function = findFunctionByBoundsColumn( boundsColumn );
        String queryStr = "SELECT " + function + "(" + columName + ") AS theResult FROM " + tableName + ";";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            assertTrue( resultSet.next(), "Invalid no result from " + function + "(" + columName + ") in table:  "
                                          + tableName );
            return resultSet.getDouble( "theResult" );
        }
    }

    private String checkColumnName( String tableName )
                            throws SQLException {
        String queryStr = "SELECT column_name FROM gpkg_geometry_columns WHERE table_name=\'" + tableName + "\';";

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            assertTrue( resultSet.next(), "Invalid no result from table: <gpkg_geometry_columns>" );
            return resultSet.getString( "column_name" );
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

    private String findFunctionByBoundsColumn( String boundsColumn ) {
        if ( boundsColumn.equals( "min_x" ) ) {
            return "ST_MinX";
        } else if ( boundsColumn.equals( "min_y" ) ) {
            return "ST_MinY";
        } else if ( boundsColumn.equals( "min_x" ) ) {
            return "ST_MinX";
        } else if ( boundsColumn.equals( "min_y" ) ) {
            return "ST_MinY";
        }
        return boundsColumn;
    }

}