package org.opengis.cite.gpkg12.nsg.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CrsListingUtils {

    private static final String NSG_CRS_LISTING = "/org/opengis/cite/gpkg12/nsg/core/NSG_CRS_WKT.xml";

    private CrsListingUtils() {
    }

    /**
     * Parses the CRS list.
     * 
     * @return the CrsList with all available rows, may be empty but never <code>null</code>.
     */
    public static CrsList parseCrsListing() {
        CrsList crsList = new CrsList();
        NodeList rows = openCrsListing();

        for ( int rowIndex = 0; rowIndex < rows.getLength(); rowIndex++ ) {
            Element row = (Element) rows.item( rowIndex );
            String id = getXMLElementTextValue( row, "srs_id" );
            String definition = getXMLElementTextValue( row, "definition" );
            String organization_coordsys_id = getXMLElementTextValue( row, "organization_coordsys_id" );
            String description = getXMLElementTextValue( row, "description" );
            crsList.addListing( id, definition, organization_coordsys_id, description );
        }
        return crsList;

    }

    private static NodeList openCrsListing() {
        InputStream crsListing = CrsListingUtils.class.getResourceAsStream( NSG_CRS_LISTING );
        String rootName = "Row";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder ioe = dbf.newDocumentBuilder();
            Document dom = ioe.parse( crsListing );
            if ( dom != null ) {
                Element docElems = dom.getDocumentElement();
                if ( docElems != null ) {
                    return docElems.getElementsByTagName( rootName );
                }
            }
        } catch ( ParserConfigurationException | SAXException | IOException e ) {
            // TODO
            // LOG.log( Level.SEVERE, "Could not open CRS listing.", e );
        }

        return null;
    }

    private static String getXMLElementTextValue( Element element, String tagName ) {
        NodeList nl = element.getElementsByTagName( tagName );
        if ( nl != null && nl.getLength() > 0 ) {
            Element el = (Element) nl.item( 0 );
            return el.getFirstChild().getNodeValue();
        }
        return null;
    }

}
