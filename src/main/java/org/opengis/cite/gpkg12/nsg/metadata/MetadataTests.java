package org.opengis.cite.gpkg12.nsg.metadata;

import static java.util.logging.Level.WARNING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.opengis.cite.gpkg12.CommonFixture;
import org.opengis.cite.gpkg12.util.DatabaseUtility;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MetadataTests extends CommonFixture {

	private final static Logger LOG = Logger.getLogger(MetadataTests.class.getName());

	private Schema schema;

	private DocumentBuilder builder;

	@BeforeClass
	public void initSchema() throws SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL resource = getClass().getClassLoader()
			.getResource("org/opengis/cite/gpkg12/nsg/metadata/NMIS_v2.X_Schema/nas/nmis.xsd");
		this.schema = schemaFactory.newSchema(resource);
	}

	@BeforeClass
	public void initDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		this.builder = factory.newDocumentBuilder();
	}

	/**
	 * Validate metadata against NMIS xsd https://nsgreg.nga.mil/doc/view?i=2491
	 * @throws SQLException if access to gpkg failed
	 */
	@Test(description = "Validate against NMIS schema")
	public void metadataSchemaValidation() throws SQLException {
		if (this.schema == null)
			throw new SkipException("Schema required for validation could not be loaded.");

		if (DatabaseUtility.doesTableOrViewExist(this.databaseConnection, "gpkg_metadata")) {

			try (final Statement statement = this.databaseConnection.createStatement();
					final ResultSet resultSet = statement.executeQuery("SELECT metadata FROM gpkg_metadata;")) {
				List<String> xmlEntries = findXmlEntries(resultSet);
				validateXmlEntries(xmlEntries);
			}
		}
		else {
			throw new SkipException("Table gpkg_metadata required to evaluate metadata.");
		}
	}

	void validateXmlEntries(List<String> xmlEntries) {
		if (xmlEntries.isEmpty())
			throw new AssertionError("GeoPackage does not contain a NMIS v2.2 metadata as XML.");
		if (xmlEntries.size() == 1) {
			validateXmlEntry(xmlEntries.get(0));
		}
		else {
			boolean hasValidMetadataEntry = hasValidEntry(xmlEntries);
			if (!hasValidMetadataEntry)
				throw new AssertionError("GeoPackage does not contain at least one valid NMIS v2.2 metadata as XML.");
		}
	}

	private void validateXmlEntry(String xmlEntry) {
		try {
			ValidationErrorHandler validationResult = validate(xmlEntry);
			if (validationResult.errorsDetected()) {
				String errors = validationResult.getErrors()
					.stream()
					.map(Object::toString)
					.collect(Collectors.joining(","));
				throw new AssertionError(
						"GeoPackage does not contain a valid NMIS v2.2 metadata as XML. Results of the schema validation: "
								+ errors);
			}
		}
		catch (IOException | SAXException e) {
			LOG.log(WARNING, "An error occurred during validation.", e);
			throw new AssertionError("Validation of the NMIS v2.2 metadata as XML failed: " + e.getMessage());
		}
	}

	List<String> findXmlEntries(ResultSet resultSet) throws SQLException {
		List<String> xmlEntries = new ArrayList<>();
		while (resultSet.next()) {
			String metadataEntry = resultSet.getString("metadata");
			try (StringReader reader = new StringReader(metadataEntry)) {
				InputSource inputSource = new InputSource(reader);
				this.builder.parse(inputSource);
				xmlEntries.add(metadataEntry);
			}
			catch (SAXException | IOException e) {
			}
		}
		return xmlEntries;
	}

	private boolean hasValidEntry(List<String> metadataEntries) {
		for (String metadataEntry : metadataEntries) {
			if (isEntryValid(metadataEntry))
				return true;
		}
		return false;
	}

	private boolean isEntryValid(String metadataEntry) {
		try {
			ValidationErrorHandler validationResult = validate(metadataEntry);
			if (!validationResult.errorsDetected()) {
				return true;
			}
		}
		catch (IOException | SAXException e) {
			LOG.log(WARNING, "An error occurred during validation.", e);
		}
		return false;
	}

	private ValidationErrorHandler validate(String metadataEntry) throws IOException, SAXException {
		Validator validator = schema.newValidator();
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errHandler);
		Source source = new StreamSource(new ByteArrayInputStream(metadataEntry.getBytes()));
		validator.validate(source);
		return errHandler;
	}

}
