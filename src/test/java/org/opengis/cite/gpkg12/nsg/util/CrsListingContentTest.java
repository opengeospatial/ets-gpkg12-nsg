package org.opengis.cite.gpkg12.nsg.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.util.FactoryException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsListingContentTest {

    private CRSFactory crsFactory = org.geotoolkit.factory.FactoryFinder.getCRSFactory( null );

    @Test
    public void testSrsDefinitions() {
        CrsList crsList = CrsListingUtils.parseCrsListing();

        List<String> crsIds = crsList.getCrsIds();
        List<String> invalidDefinitions = new ArrayList<>();
        for ( String id : crsIds ) {
            assertThat( crsIds.size(), is( 11 ) );
            String definitionBySrsId = crsList.getDefinitionBySrsId( id );
            try {
                crsFactory.createFromWKT( definitionBySrsId );
            } catch ( FactoryException e ) {
                invalidDefinitions.add( id );
            }
        }

        if ( !invalidDefinitions.isEmpty() ) {
            String invalidCrs = invalidDefinitions.stream().map( Object::toString ).collect( Collectors.joining( ", " ) );
            String msg = String.format( "%s of %s crs definitions could not be parsed. The definitions of the following crs cold not be parsed: %s",
                                        invalidDefinitions.size(), crsIds.size(), invalidCrs );
            throw new AssertionError( msg );
        }
    }

}
