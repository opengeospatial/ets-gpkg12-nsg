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
        assertThat( crsList.getDefinitionBySrsId( "5041" ).trim(), startsWith( "PROJCRS[\"WGS 84 / UPS North (E,N)" ) );
        assertThat( crsList.getOrganizationCoordsysIdBySrsId( "5042" ), is( "5042" ) );
        assertThat( crsList.getDescriptionBySrsId( "5773" ),
                    is( "Height surface resulting from the application of the EGM96 geoid model to the WGS 84 ellipsoid. Replaces EGM84 geoid height (CRS code 5798). Replaced by EGM2008 geoid height (CRS code 3855)." ) );
    }

}