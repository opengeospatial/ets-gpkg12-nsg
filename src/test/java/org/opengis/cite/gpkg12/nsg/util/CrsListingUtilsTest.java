package org.opengis.cite.gpkg12.nsg.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsListingUtilsTest {

    @Test
    public void testParseCrsLIsting() {
        CrsList crsList = CrsListingUtils.parseCrsListing();

        assertThat( crsList, notNullValue() );
        assertThat( crsList.getDefinitionBySrsId( "5041" ), startsWith( "PROJCRS[\"WGS84/UPSNorth(E,N)" ) );
        assertThat( crsList.getOrganizationCoordsysIdBySrsId( "5042" ), is( "5042" ) );
    }

}