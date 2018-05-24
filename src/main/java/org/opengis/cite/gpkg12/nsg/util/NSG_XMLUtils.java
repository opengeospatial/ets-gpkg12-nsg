package org.opengis.cite.gpkg12.nsg.util;

import org.opengis.cite.gpkg12.util.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NSG_XMLUtils extends XMLUtils {

    /**
     * @param element
     *            never <code>null</code>
     * @param tagName
     *            never <code>null</code>
     * @return The text value of the tag with the passed name which is a descendant of the passed element,
     *         <code>null</code> if the element does not contain the passed tag
     */
    public static String getXMLElementTextValue( Element element, String tagName ) {
        NodeList nl = element.getElementsByTagName( tagName );
        if ( nl != null && nl.getLength() > 0 ) {
            Element el = (Element) nl.item( 0 );
            return el.getFirstChild().getNodeValue();
        }

        return null;
    }

    /**
     *
     * @param nodes
     *            a list of nodes to search in, never <code>null</code>
     * @param tagName
     *            the name of tag, never <code>null</code>
     * @param value
     *            the text value
     * @return the first element with the passed tag name and value (case insensitive), may be <code>null</code> if no
     *         such element could be found
     */
    public static Element getElementByTextValue( NodeList nodes, String tagName, String value ) {
        for ( int ni = 0; ni < nodes.getLength(); ni++ ) {
            Element e = (Element) nodes.item( ni );
            if ( getXMLElementTextValue( e, tagName ).equalsIgnoreCase( value ) ) {
                return e;
            }
        }
        return null;
    }

}