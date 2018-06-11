package com.emc.poc;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.testng.reporters.jq.Main;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author
 * @create 2018-06-03 上午9:20
 **/

public class TestExtends {
    public OrientGraphFactory factory = new OrientGraphFactory("remote:139.219.102.105:2424/test1", "root", "easyops");


    public void testAddVertexWithSql() {

        boolean txMode = true;
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
        int number = 1;

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= number; ++i) {
            OrientVertex vertex = graph.command(new OCommandSQL("insert into v_test1 content {'name':123}")).execute();
            System.out.println(vertex.getId());
        }


        long endTime = System.currentTimeMillis();
        long cost_time = endTime - startTime;
        System.out.println("speed is: " + String.format("%.2f", (number / (cost_time/1000.0))) + " 条/秒");

        graph.shutdown();
    }

    public void testCreateProperty() {
        OrientBaseGraph graph = factory.getTx();

        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field1 LONG")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field2 LONG")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field3 STRING")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field4 STRING")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field5 STRING")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field6 STRING")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field7 EMBEDDEDLIST")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field8 EMBEDDEDLIST")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field9 EMBEDDEDLIST")).execute();
        graph.command(new OCommandSQL("CREATE PROPERTY v_x86_1.x86Field10 EMBEDDEDLIST")).execute();

        graph.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new TestExtends().testAddVertexWithSql();
    }
}
