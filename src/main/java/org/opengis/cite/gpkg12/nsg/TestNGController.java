package org.opengis.cite.gpkg12.nsg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import org.opengis.cite.gpkg12.TestRunArg;
import org.opengis.cite.gpkg12.TestRunArguments;
import org.opengis.cite.gpkg12.util.TestSuiteLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.occamlab.te.spi.executors.TestRunExecutor;
import com.occamlab.te.spi.executors.testng.TestNGExecutor;
import com.occamlab.te.spi.jaxrs.TestSuiteController;

/**
 * Main test run controller oversees execution of TestNG test suites.
 */
public class TestNGController implements TestSuiteController {

    private TestRunExecutor executor;

    private Properties etsProperties = new Properties();

    /**
     * A convenience method for running the test suite using a command-line interface. The default values of the test
     * run arguments are as follows:
     * <ul>
     * <li>XML properties file: ${user.home}/test-run-props.xml</li>
     * <li>outputDir: ${user.home}</li>
     * </ul>
     * <p>
     * <strong>Synopsis</strong>
     * </p>
     * 
     * <pre>
     * ets-*-aio.jar [-o|--outputDir $TMPDIR] [test-run-props.xml]
     * </pre>
     *
     * @param args
     *            Test run arguments (optional).
     * @throws Exception
     *             If the test run cannot be executed (usually due to unsatisfied pre-conditions).
     */
    public static void main( String[] args )
                            throws Exception {
        TestRunArguments testRunArgs = new TestRunArguments();
        JCommander cmd = new JCommander( testRunArgs );
        try {
            cmd.parse( args );
        } catch ( ParameterException px ) {
            System.out.println( px.getMessage() );
            cmd.usage();
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File xmlArgs = testRunArgs.getPropertiesFile();
        Document testRunProps = db.parse( xmlArgs );
        TestNGController controller = new TestNGController( testRunArgs.getOutputDir() );
        Source testResults = controller.doTestRun( testRunProps );
        System.out.println( "Test results: " + testResults.getSystemId() );
    }

    /**
     * Default constructor uses the location given by the "user.home" system property as the root output directory.
     */
    public TestNGController() {
    	this( new File( ValidateStringInput( System.getProperty( "user.home" ) )).toURI().toString() );
    }
    
    /**
     * Validate a string value to ensure it contains no illegal characters or content
     * 
     * @param inputString The string to validate
     * @return validated string
     */
    public static String ValidateStringInput( String inputString ) {

    	String cleanStr = "";
    	for (int ii = 0; ii < inputString.length(); ++ii) {
    		cleanStr += cleanChar(inputString.charAt(ii));
    	}
    	return cleanStr;
    }
    
    /**
     * Validate and clean a character of a string.  Note this is NOT performance optimized and that is on purpose.
     * If the order of the loop is switched so that the inputChar is tested and returned if valid, that will not
     * pass scans.
     * 
     * @param inputChar  A character of a string, for which we will check validity, replacing any illegal characters with %
     * @return a validated character
     */
    private static char cleanChar(char inputChar) {
        // 0 - 9
        for (int i = 48; i < 58; ++i) {
            if (inputChar == i) return (char) i;
        }

        // 'A' - 'Z'
        for (int i = 65; i < 91; ++i) {
            if (inputChar == i) return (char) i;
        }

        // 'a' - 'z'
        for (int i = 97; i < 123; ++i) {
            if (inputChar == i) return (char) i;
        }

        // other valid characters
        switch (inputChar) {
            case '/':
                return '/';
            case '\\':
                return '\\';
            case '.':
                return '.';
            case '-':
                return '-';
            case '_':
                return '_';
            case ' ':
                return ' ';
        }
        return '%';
    }

    /**
     * Construct a controller that writes results to the given output directory.
     * 
     * @param outputDir
     *            The location of the directory in which test results will be written; it will be created if it does not
     *            exist.
     */
    public TestNGController( String outputDir ) {
        try (InputStream is = getClass().getResourceAsStream( "ets.properties" )) {
            this.etsProperties.load( is );
        } catch ( IOException ex ) {
            TestSuiteLogger.log( Level.WARNING, "Unable to load ets.properties. " + ex.getMessage() );
        }
        URL tngSuite = TestNGController.class.getResource( "testng.xml" );
        File resultsDir = null;
        if ( null == outputDir || outputDir.isEmpty() ) {

			resultsDir = new File(  ValidateStringInput(   System.getProperty( "user.home" )) );

        } else if ( outputDir.startsWith( "file:" ) ) {
            resultsDir = new File( URI.create( outputDir ) );
        } else {
            resultsDir = new File( outputDir );
        }
        TestSuiteLogger.log( Level.CONFIG, "Using TestNG config: " + tngSuite );
        TestSuiteLogger.log( Level.CONFIG, "Using outputDirPath: " + resultsDir.getAbsolutePath() );
        // NOTE: setting third argument to 'true' enables the default listeners
        this.executor = new TestNGExecutor( tngSuite.toString(), resultsDir.getAbsolutePath(), false );
    }

    @Override
    public String getCode() {
        return etsProperties.getProperty( "ets-code" );
    }

    @Override
    public String getVersion() {
        return etsProperties.getProperty( "ets-version" );
    }

    @Override
    public String getTitle() {
        return etsProperties.getProperty( "ets-title" );
    }

    @Override
    public Source doTestRun( Document testRunArgs )
                            throws Exception {
        validateTestRunArgs( testRunArgs );
        return executor.execute( testRunArgs );
    }

    /**
     * Validates the test run arguments. The test run is aborted if any of these checks fail.
     *
     * @param testRunArgs
     *            A DOM Document containing a set of XML properties (key-value pairs).
     * @throws IllegalArgumentException
     *             If any arguments are missing or invalid for some reason.
     */
    void validateTestRunArgs( Document testRunArgs ) {
        if ( null == testRunArgs || !testRunArgs.getDocumentElement().getNodeName().equals( "properties" ) ) {
            throw new IllegalArgumentException( "Input is not an XML properties document." );
        }
        NodeList entries = testRunArgs.getDocumentElement().getElementsByTagName( "entry" );
        if ( entries.getLength() == 0 ) {
            throw new IllegalArgumentException( "No test run arguments found." );
        }
        Map<String, String> args = new HashMap<String, String>();
        for ( int i = 0; i < entries.getLength(); i++ ) {
            Element entry = (Element) entries.item( i );
            args.put( entry.getAttribute( "key" ), entry.getTextContent() );
        }
        if ( !args.containsKey( TestRunArg.IUT.toString() ) ) {
            throw new IllegalArgumentException( String.format( "Missing argument: '%s' must be present.",
                                                               TestRunArg.IUT ) );
        }
    }
}
