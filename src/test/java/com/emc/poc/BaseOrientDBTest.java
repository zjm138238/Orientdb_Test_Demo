package com.emc.poc;


import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Base OrientDB Test that handles creating a test database
 *
 * @author Simon O'Brien
 */
public class BaseOrientDBTest {

    public static final String SERVER_URL = "remote:99.13.30.182:2424/";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";
    public static final String DB_ROOT_USERNAME = "root";
    public static final String DB_ROOT_PASSWORD = "root";

    private OrientGraph txGraph = null;
    private String dbName = "cmdb";

    /**
     * Create a test DB before the suite is run
     *
     * @throws Exception
     */
    @BeforeSuite
    public void setupSuite() throws Exception {

        // create a random name for the DB
        setDbName(RandomStringUtils.randomAlphanumeric(32));

        // connect to the server as admin
        OServerAdmin serverAdmin = new OServerAdmin(SERVER_URL + getDbName());
        serverAdmin.connect(DB_ROOT_USERNAME, DB_ROOT_PASSWORD);

        // check that we are connected
        assertThat(serverAdmin.isConnected(), equalTo(true));

        // create the DB if it doesn't already exist
        if(!serverAdmin.existsDatabase()) {
            serverAdmin.createDatabase("graph", "plocal");
            System.out.println("Created DB:" + getDbName());
        } else {
            assertThat("DB '" + getDbName() + "' could not be created as it already exists", false);
        }
    }

    /**
     * Delete the test DB after the suite has run
     *
     * @throws Exception
     */
    @AfterSuite
    public void tearDownSuite() throws Exception {

        // connect to the server as admin
        OServerAdmin serverAdmin = new OServerAdmin(SERVER_URL + getDbName());
        serverAdmin.connect(DB_ROOT_USERNAME, DB_ROOT_PASSWORD);

        // check that we are connected
        assertThat(serverAdmin.isConnected(), equalTo(true));

        // delete the DB if it exists
        if(serverAdmin.existsDatabase()) {
            serverAdmin.dropDatabase("plocal");
            System.out.println("Deleted DB:" + getDbName());
        } else {
            assertThat("DB '" + getDbName() + "' could not be deleted as it does not exist", false);
        }
    }

    /**
     * Set the DB connection after each test
     */
    @BeforeMethod
    public void setupTest() {

        OrientGraphFactory factory = new OrientGraphFactory(SERVER_URL + getDbName(), DB_USERNAME, DB_PASSWORD);
        setTxGraph(factory.getTx());

        assertThat(getTxGraph(), notNullValue());

        System.out.println("Connected to DB:" + getDbName());
    }

    /**
     * Close the DB connection after each test
     */
    @AfterMethod
    public void tearDownTest() {

        OrientGraph txGraph = getTxGraph();

        if(txGraph != null) {
            txGraph.shutdown();
        }

        System.out.println("Disconnected from DB:" + getDbName());
    }

    /**
     * Get the transactional orient graph DB
     *
     * @return the transactional orient graph DB
     */
    public OrientGraph getTxGraph() {
        return txGraph;
    }

    /**
     * Set the transactional orient graph DB
     *
     * @param txGraph the transactional orient graph DB
     */
    public void setTxGraph(OrientGraph txGraph) {
        this.txGraph = txGraph;
    }

    /**
     * Get the DB name
     *
     * @return the DB name
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Set the DB name
     *
     * @param dbName the DB name
     */
    private void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
