package org.opengis.cite.gpkg12.nsg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsList {

    private List<CrsListing> crsListingList = new ArrayList<>();

    /**
     * @return all parsed {@link CrsListing}s
     */
    protected List<String> getCrsIds() {
        return crsListingList.stream().map(CrsListing::getId).collect(Collectors.toList());
    }

    /**
     * Adds a new CrsListing to the list.
     * 
     * @param id
     *            the srs_id
     * @param definition
     *            the definition
     * @param organization_coordsys_id
     *            the organization_coordsys_id
     * @param description
     *            the description
     */
    public void addListing( String id, String definition, String organization_coordsys_id, String description ) {
        crsListingList.add( new CrsListing( id, definition, organization_coordsys_id, description ) );
    }

    /**
     * @param srsID
     *            the srsId of the CrsListing to identify, never <code>null</code>
     * @return the organization_coordsys_id of the entry with the passed srs_id, <code>null</code> if not available
     */
    public String getOrganizationCoordsysIdBySrsId( String srsID ) {
        for ( CrsListing crsListing : crsListingList ) {
            if ( srsID.equals( crsListing.id ) )
                return crsListing.organization_coordsys_id;
        }
        return null;
    }

    /**
     * @param srsID
     *            the srsId of the CrsListing to identify, never <code>null</code>
     * @return the definition of the entry with the passed srs_id, <code>null</code> if not available
     */
    public String getDefinitionBySrsId( String srsID ) {
        for ( CrsListing crsListing : crsListingList ) {
            if ( srsID.equals( crsListing.id ) )
                return crsListing.definition;
        }
        return null;
    }

    /**
     * @param srsID
     *            the srsId of the CrsListing to identify, never <code>null</code>
     * @return the description of the entry with the passed srs_id, <code>null</code> if not available
     */
    public String getDescriptionBySrsId( String srsID ) {
        for ( CrsListing crsListing : crsListingList ) {
            if ( srsID.equals( crsListing.id ) )
                return crsListing.description;
        }
        return null;
    }

    private class CrsListing {
        private String id;

        private String definition;

        private String organization_coordsys_id;

        private String description;

        public CrsListing( String id, String definition, String organization_coordsys_id, String description ) {
            this.id = id;
            this.definition = definition;
            this.organization_coordsys_id = organization_coordsys_id;
            this.description = description;
        }

        public String getId(){
            return id;
        }
    }

}
