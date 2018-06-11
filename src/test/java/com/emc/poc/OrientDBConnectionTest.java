package com.emc.poc;

import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * OrientDB POC Testing and Investigation:
 *
 * Connection Testing
 *
 * @author Simon O'Brien
 */
public class OrientDBConnectionTest {

    public static final String DB_URL = "remote:99.13.30.182:2424/";
    public static final String DB_NAME = "cmdb";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";
    public static final String DB_ROOT_USERNAME = "root";
    public static final String DB_ROOT_PASSWORD = "root";

    /**
     * TestA that person can connect to db
     */
    @Test
    public void connectionTest2(){

        OrientGraph txGraph = null;

        try {

            OrientGraphFactory factory = new OrientGraphFactory("remote:99.13.30.182:80/cmdb", "root", "root");

            txGraph = factory.getTx();

            //assertThat(txGraph, notNullValue());

        } finally {

            if(txGraph != null) {
                txGraph.shutdown();
            }

        }


    }
    @Test
    public void connectionTest(){

        OrientGraph txGraph = null;

        try {

            OrientGraphFactory factory = new OrientGraphFactory(DB_URL + DB_NAME, DB_USERNAME, DB_PASSWORD);
            txGraph = factory.getTx();

            assertThat(txGraph, notNullValue());

        } finally {

            if(txGraph != null) {
                txGraph.shutdown();
            }

        }
    }

    /**
     * TestA that root person can connect to db
     */
    @Test
    public void connectionTestRootUser(){

        OrientGraph txGraph = null;

        try {

            OrientGraphFactory factory = new OrientGraphFactory(DB_URL + DB_NAME, DB_ROOT_USERNAME, DB_ROOT_PASSWORD);
            txGraph = factory.getTx();

            assertThat(txGraph, notNullValue());

        } finally {

            if(txGraph != null) {
                txGraph.shutdown();
            }

        }
    }

    /**
     * TestA if invalid person can connect to db
     */
    @Test(expectedExceptions = OSecurityAccessException.class)
    public void connectionTestInvalidUser() {

        OrientGraphFactory factory = new OrientGraphFactory(DB_URL + DB_NAME, "invaliduser", DB_PASSWORD);
        factory.getTx();
    }

    /**
     * TestA if root person can connect to db that doesn't exist
     */
    @Test(expectedExceptions = OConfigurationException.class)
    public void connectionTestInvalidDB(){

        OrientGraphFactory factory = new OrientGraphFactory(DB_URL + "invaliddb", DB_ROOT_USERNAME, DB_ROOT_PASSWORD);
        factory.getTx();
    }


}
