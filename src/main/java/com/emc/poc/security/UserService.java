package com.emc.poc.security;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static final String SERVER_URL = "remote:localhost:2424/";
    public static final String DB_NAME = "idp";
    public static final String DB_ROOT_USERNAME = "root";
    public static final String DB_ROOT_PASSWORD = "dev";

    private OrientGraph txGraph = null;

    /**
     * Create a user in the DB
     *
     * @param email The user email (user name)
     * @param password The user password
     */
    public void createUser(String email, String password) {

        OrientGraphFactory factory = new OrientGraphFactory(SERVER_URL + DB_NAME, DB_ROOT_USERNAME, DB_ROOT_PASSWORD);
        OrientGraph txGraph = null;

        try {

            txGraph = factory.getTx();
            Vertex userVertex = txGraph.addVertex("class:User");
            userVertex.setProperty("email", email);
            userVertex.setProperty("password", passwordEncoder.encode(password));

        } finally {

            if(txGraph != null) {
                txGraph.shutdown();
            }

        }
    }


}
