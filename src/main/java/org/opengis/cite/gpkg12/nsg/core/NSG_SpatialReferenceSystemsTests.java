package org.opengis.cite.gpkg12.nsg.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.referencing.CRS;
import org.opengis.cite.gpkg12.ErrorMessage;
import org.opengis.cite.gpkg12.ErrorMessageKeys;
import org.opengis.cite.gpkg12.core.SpatialReferenceSystemsTests;
import org.opengis.cite.gpkg12.nsg.util.NSG_XMLUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NSG_SpatialReferenceSystemsTests extends SpatialReferenceSystemsTests {
    private final String NSG_CRS_listing = "NSG_CRS_WKT.xml";

    private final String Annex_C_3395_Table = "Annex_C_3395_Table.txt";

    private final String Annex_E_4326_Table = "Annex_E_4326_Table.txt";

    private String XML_root = "Row";

    /*
     * --- NSG Req 3: The CRSs listed in Table 4, Table 5, and Table 6 SHALL be the only CRSs used by raster tile
     * pyramid and vector feature data tables in a GeoPackage.
     */

    @Test(groups = { "NSG" }, description = "NSG Req 3 (identifed CRSs)")
    public void NSG_CRS_Test()
                            throws SQLException {
        NodeList crsList = NSG_XMLUtils.openXMLDocument( this.getClass().getResourceAsStream( this.NSG_CRS_listing ),
                                                         this.XML_root );
        Assert.assertTrue( crsList != null,
                           ErrorMessage.format( ErrorMessageKeys.UNDEFINED_SRS, " - no designated CRS Lookup Table" ) );

        if ( crsList != null ) {
            String queryStr = "SELECT srs_id, organization_coordsys_id FROM gpkg_spatial_ref_sys";

            try (final Statement statement = this.databaseConnection.createStatement();
                                    final ResultSet resultSet = statement.executeQuery( queryStr )) {
                final Collection<String> invalidSrsIds = new LinkedList<>();
                final Collection<String> invalidOrgIds = new LinkedList<>();

                while ( resultSet.next() ) {
                    String srsID = resultSet.getString( "srs_id" ).trim();
                    String orgID = resultSet.getString( "organization_coordsys_id" ).trim();

                    if ( srsID.equals( "0" ) || orgID.equals( "0" ) ) {
                        continue;
                    }
                    if ( srsID.equals( "-1" ) || orgID.equals( "-1" ) ) {
                        continue;
                    }

                    Element element = NSG_XMLUtils.getElement( crsList, "srs_id", srsID );
                    if ( element == null ) {
                        invalidSrsIds.add( srsID );
                    } else {
                        String crsOrgID = NSG_XMLUtils.getXMLElementTextValue( element, "organization_coordsys_id" ).trim();
                        if ( !crsOrgID.equals( orgID ) ) {
                            invalidOrgIds.add( orgID );
                        }
                    }
                }
                resultSet.close();
                statement.close();

                Assert.assertTrue( invalidSrsIds.isEmpty(),
                                   MessageFormat.format( "The gpkg_spatial_ref_sys table contains invalid srs_id values {0}",
                                                         invalidSrsIds.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
                Assert.assertTrue( invalidOrgIds.isEmpty(),
                                   MessageFormat.format( "The gpkg_spatial_ref_sys table contains invalid organization_coordsys_id values {0}",
                                                         invalidOrgIds.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            }
        }
    }

    final private List<Object[]> AnnexC_3395 = new ArrayList<Object[]>();

    final private List<Object[]> AnnexE_4326 = new ArrayList<Object[]>();

    final private double tolerance = 1.0e-10;

    private void Add2List( List<Object[]> table, int zoom, double scale, double pixelSz, long matrixWidth,
                           long matrixHeight ) {
        if ( table != null ) {
            Object[] row = { zoom, scale, pixelSz, matrixWidth, matrixHeight };
            table.add( row );
        }
    }

    private void Add2List( List<Object[]> table, String zoom, String scale, String pixelSz, String matrixWidth,
                           String matrixHeight ) {
        Add2List( table, Integer.parseInt( zoom ), Double.parseDouble( scale ), Double.parseDouble( pixelSz ),
                  Long.parseLong( matrixWidth ), Long.parseLong( matrixHeight ) );
    }

    private void PopulateAnnexC() {
        AnnexC_3395.clear();

        try (BufferedReader br = new BufferedReader(
                                                     new InputStreamReader(
                                                                            this.getClass().getResourceAsStream( this.Annex_C_3395_Table ),
                                                                            "UTF-8" ) )) {
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                List<String> items = Arrays.asList( line.split( "\\s*,\\s*" ) );
                if ( !items.isEmpty() && ( items.size() == 5 ) ) {
                    Add2List( AnnexC_3395, items.get( 0 ), items.get( 1 ), items.get( 2 ), items.get( 3 ),
                              items.get( 4 ) );
                } else {
                    throw new SkipException( "Annex C (EPSG:3395) Table is corrupt " );
                }
            }
        } catch ( IOException e ) {
            throw new SkipException( "Annex C (EPSG:3395) Table not available" );
        }

    }

    private void PopulateAnnexE() {
        AnnexE_4326.clear();

        try (BufferedReader br = new BufferedReader(
                                                     new InputStreamReader(
                                                                            this.getClass().getResourceAsStream( this.Annex_E_4326_Table ),
                                                                            "UTF-8" ) )) {
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                List<String> items = Arrays.asList( line.split( "\\s*,\\s*" ) );
                if ( !items.isEmpty() && ( items.size() == 5 ) ) {
                    Add2List( AnnexE_4326, items.get( 0 ), items.get( 1 ), items.get( 2 ), items.get( 3 ),
                              items.get( 4 ) );
                } else {
                    throw new SkipException( "Annex E (EPSG:4326) Table is corrupt " );
                }
            }
        } catch ( IOException e ) {
            throw new SkipException( "Annex E (EPSG:4326) Table not available" );
        }
    }

    @Test(groups = { "NSG" }, description = "NSG Req 4 & 5 (match Annex table)")
    public void NSG_MatchAnnexTableTest()
                            throws SQLException {
        // --- original intent was to implement here; but may make more sense to
        // implement in NSG_TileTests

        final Collection<String> invalidMatrixEntries = new LinkedList<>();

        String queryStr = "SELECT tm.table_name AS tabName, sel.data_type AS dataTyp, sel.crs_id AS crsID, tm.zoom_level AS zoomLvl, tm.matrix_width AS matrixW, tm.matrix_height AS matrixH, tm.tile_width AS tileW, tm.tile_height AS tileH, tm.pixel_x_size AS pixelSzX, tm.pixel_y_size AS pixelSzY "
                          + "FROM gpkg_tile_matrix tm "
                          + "INNER JOIN (SELECT gc.table_name, gc.data_type, gs.organization_coordsys_id as crs_id  from gpkg_contents gc inner join gpkg_spatial_ref_sys gs where gc.srs_id=gs.srs_id) AS sel "
                          + "ON tm.table_name=sel.table_name " + "WHERE crsID IN (3395, 4326) ORDER BY zoomLvl;";

        Statement ss = this.databaseConnection.createStatement();
        ResultSet rs = ss.executeQuery( queryStr );

        try (final Statement statement = this.databaseConnection.createStatement();
                                final ResultSet resultSet = statement.executeQuery( queryStr )) {
            PopulateAnnexC();
            PopulateAnnexE();
            invalidMatrixEntries.clear();

            List<Object[]> table = null;

            while ( resultSet.next() ) {
                String tabNam = resultSet.getString( "tabName" ).trim();
                String srsID = resultSet.getString( "crsID" ).trim();
                int zoomLvl = resultSet.getInt( "zoomLvl" );

                long mtrxW = resultSet.getLong( "matrixW" );
                long mtrxH = resultSet.getLong( "matrixH" );
                // int tileWidth = resultSet.getInt("tileW" );
                // int tileHeight = resultSet.getInt("tileH");
                double pxlXSz = resultSet.getDouble( "pixelSzX" );
                double pxlYSz = resultSet.getDouble( "pixelSzY" );

                if ( srsID.equals( "3395" ) ) {
                    table = this.AnnexC_3395; // --- test for: Req 4
                } else if ( ( srsID.equals( "5041" ) ) || ( srsID.equals( "5042" ) ) ) {
                    table = null;
                    // --- // --- test for: Req 4
                } else if ( srsID.equals( "4326" ) ) {
                    table = this.AnnexE_4326; // --- test for: Req 5
                } else {
                    table = null;
                }

                for ( int i = 0; ( table != null ) && ( i < table.size() ); i++ ) {
                    Object[] obj = table.get( i );
                    if ( zoomLvl == (int) obj[0] ) {
                        long imW = (long) obj[3];
                        long imH = (long) obj[4];
                        double pX = (double) obj[2];
                        double pY = (double) obj[2];

                        if ( Math.abs( pX - pxlXSz ) > this.tolerance ) {
                            invalidMatrixEntries.add( tabNam + " (" + srsID + ", Zoom Level: " + zoomLvl + "): "
                                                      + "Pixel Size X: " + pxlXSz + "; but expected " + pX );
                        } else if ( Math.abs( pY - pxlYSz ) > this.tolerance ) {
                            invalidMatrixEntries.add( tabNam + " (" + srsID + ", Zoom Level: " + zoomLvl + "): "
                                                      + "Pixel Size Y: " + pxlYSz + "; but expected " + pY );
                        } else if ( imW != mtrxW ) {
                            invalidMatrixEntries.add( tabNam + " (" + srsID + ", Zoom Level: " + zoomLvl + "): "
                                                      + "Matrix Width: " + mtrxW + "; but expected " + imW );
                        } else if ( imH != mtrxH ) {
                            invalidMatrixEntries.add( tabNam + " (" + srsID + ", Zoom Level: " + zoomLvl + "): "
                                                      + "Matrix Height: " + mtrxH + "; but expected " + imH );
                        }
                    }
                }
            }
            resultSet.close();
            statement.close();

            Assert.assertTrue( invalidMatrixEntries.isEmpty(),
                               MessageFormat.format( "The gpkg_tile_matrix table contains invalid Pixels Size or Matrix Size values for tables: {0}",
                                                     invalidMatrixEntries.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
        }
    }

    /*
     * --- NSG Req 6: The WGS 84 Geographic 2D CRS SHALL be used for 2D vector features. WGS 84 Geographic 2D
     * GeoPackages SHALL follow the technical guidance provided in Annex E: Implementation Guide for EPSG::4326 Tiles.
     */

    // --- TBD

    // @Test(groups = { "NSG" }, description = "NSG Req 6 (match Annex table)")

    /*
     * --- NSG Req 8: The CRS definitions in Table 7 through Table 19 below SHALL be used to specify the CRS used for
     * tiles and vector feature user data tables containing NSG data in a GeoPackage.
     * 
     * --- NSG Req 9: Other CRS definitions SHALL NOT be specified for GeoPackage SQL tables containing NSG data.
     */

    @Test(groups = { "NSG" }, description = "NSG Req 8 & 9 (CRS definitions)")
    public void NSG_CRSdefinitionsTest()
                            throws SQLException {
        NodeList crsList = NSG_XMLUtils.openXMLDocument( this.getClass().getResourceAsStream( this.NSG_CRS_listing ),
                                                         this.XML_root );
        Assert.assertTrue( crsList != null,
                           ErrorMessage.format( ErrorMessageKeys.UNDEFINED_SRS, " - no designated CRS Lookup Table" ) );

        if ( crsList != null ) {
            String queryStr = "SELECT srs_id,definition FROM gpkg_spatial_ref_sys";

            try (final Statement statement = this.databaseConnection.createStatement();
                                    final ResultSet resultSet = statement.executeQuery( queryStr )) {
                final Collection<String> invalidSrsDefs = new LinkedList<>();

                while ( resultSet.next() ) {
                    String srsID = resultSet.getString( "srs_id" ).trim();
                    if ( srsID.equals( "0" ) ) {
                        continue;
                    }
                    if ( srsID.equals( "-1" ) ) {
                        continue;
                    }

                    String srsDef = resultSet.getString( "definition" ).trim().replaceAll( "\\s+", "" );

                    Element element = NSG_XMLUtils.getElement( crsList, "srs_id", srsID );
                    if ( element != null ) {
                        String crsDef = NSG_XMLUtils.getXMLElementTextValue( element, "definition" ).trim().replaceAll( "\\s+",
                                                                                                                        "" );

                        System.out.println( crsDef );
                        System.out.println( srsDef );

                        String code;
                        try {
                            CoordinateReferenceSystem example = CRS.parseWKT( srsDef );
                            code = CRS.lookupIdentifier( example, true );
                            CoordinateReferenceSystem crs = CRS.decode( code );
                            System.out.println( crs.toString() );
                        } catch ( FactoryException e ) {
                            invalidSrsDefs.add( srsID + ":" + srsDef );
                            Assert.fail( MessageFormat.format( "The gpkg_spatial_ref_sys table contains invalid CRS defintions values for IDs {0}",
                                                               invalidSrsDefs.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
                            e.printStackTrace();
                        }

                    }
                }
                resultSet.close();
                statement.close();
            }
        }
    }

    /*
     * --- NSG Req 19: Data validity SHALL be assessed against data value constraints specified in Table 26 below using
     * a test suite. Data validity MAY be enforced by SQL triggers.
     * 
     * --- 19-A: Addresses Table 26 Rows 1-2 (regarding table "gpkg_spatial_ref_sys")
     */

    @Test(groups = { "NSG" }, description = "NSG Req 19-A (Data Validity: gpkg_spatial_ref_sys)")
    public void NSG_DataValidity()
                            throws SQLException {
        NodeList crsList = NSG_XMLUtils.openXMLDocument( this.getClass().getResourceAsStream( this.NSG_CRS_listing ),
                                                         this.XML_root );
        Assert.assertTrue( crsList != null,
                           ErrorMessage.format( ErrorMessageKeys.UNDEFINED_SRS, " - no designated CRS Lookup Table" ) );

        if ( crsList != null ) {
            String queryStr = "SELECT srs_id,organization,description FROM gpkg_spatial_ref_sys;";

            try (final Statement statement = this.databaseConnection.createStatement();
                                    final ResultSet resultSet = statement.executeQuery( queryStr )) {
                final Collection<String> invalidOrgs = new LinkedList<>();
                final Collection<String> invalidDesc = new LinkedList<>();

                while ( resultSet.next() ) {
                    String srsID = resultSet.getString( "srs_id" ).trim();
                    if ( srsID.equals( "0" ) ) {
                        continue;
                    }
                    if ( srsID.equals( "-1" ) ) {
                        continue;
                    }

                    // --- test for: Table 26; Row 1

                    String srsOrg = resultSet.getString( "organization" ).trim().toUpperCase();
                    if ( !srsOrg.equals( "EPSG" ) && !srsOrg.equals( "NGA" ) ) {
                        invalidOrgs.add( srsID + ":" + srsOrg );
                    }

                    // --- test for: Table 26; Row 2

                    String srsDesc = resultSet.getString( "description" ).trim();

                    boolean found = false;

                    Element element = NSG_XMLUtils.getElement( crsList, "srs_id", srsID );
                    if ( element != null ) {
                        if ( ( srsDesc != null ) && ( srsDesc.length() > 0 )
                             && ( !srsDesc.toUpperCase().equalsIgnoreCase( "NULL" ) )
                             && ( !srsDesc.toUpperCase().equalsIgnoreCase( "UNK" ) )
                             && ( !srsDesc.toUpperCase().equalsIgnoreCase( "UNKNOWN" ) )
                             && ( !srsDesc.toUpperCase().equalsIgnoreCase( "TBD" ) ) ) {
                            String crsDesc = NSG_XMLUtils.getXMLElementTextValue( element, "description" ).trim();
                            found = crsDesc.equalsIgnoreCase( srsDesc );
                            if ( !found && ( crsDesc.endsWith( "." ) || srsDesc.endsWith( "." ) ) ) {
                                if ( srsDesc.endsWith( "." ) )
                                    srsDesc = srsDesc.substring( 0, srsDesc.length() - 1 );
                                if ( crsDesc.endsWith( "." ) )
                                    crsDesc = crsDesc.substring( 0, crsDesc.length() - 1 );
                                found = crsDesc.equalsIgnoreCase( srsDesc );
                            }
                        }
                    }

                    if ( !found ) {
                        invalidDesc.add( srsID );
                    }
                }
                resultSet.close();
                statement.close();

                Assert.assertTrue( invalidOrgs.isEmpty(),
                                   MessageFormat.format( "The gpkg_spatial_ref_sys table contains invalid organization values for IDs: {0}, should be \'EPSG\' or \'NGA\'",
                                                         invalidOrgs.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
                Assert.assertTrue( invalidDesc.isEmpty(),
                                   MessageFormat.format( "The gpkg_spatial_ref_sys table contains invalid desciptions for IDs: {0}",
                                                         invalidDesc.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) ) );
            }
        }
    }

}