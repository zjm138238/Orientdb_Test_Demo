package com.emc.poc;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

@Test
public class TestGraphTransactionConsistency {
    private boolean txMode = false;

    public void testQueryVertexWithSql() {

        txMode = false;
        long startTime = System.currentTimeMillis();
        OrientBaseGraph database = txMode ? factory.getTx() : factory.getNoTx();


        //顶点
        Iterable<OrientVertex> vertices = database.command(new OCommandSQL(
                "select * from `v_test1`  where name='test10000'")).execute();
//        Iterable<OrientVertex> vertices = database.command(new OCommandSQL(
//                "select * from `v_test1`  where instanceId='id100'")).execute();
//        Iterable<OrientVertex> vertices = database.command(new OCommandSQL(
//                "select * from `v_test1`  where name like 'test1000%'")).execute();
//        Iterable<OrientVertex> vertices = database.command(new OCommandSQL(
//                "select * from `v_test1`  where ctime>=\"2018-04-11 10:15:34\" and ctime<='2018-04-11 10:15:35'")).execute();


        int count = 0;
        for (OrientVertex v : vertices) {
            System.out.println("rid： "+v.getIdentity());
            count++;
        }

        System.out.println("总记录数： "+count);

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");

    }

    public void testQueryEdgeWithSql() {

        txMode = false;
        OrientBaseGraph database = txMode ? factory.getTx() : factory.getNoTx();

        long startTime = System.currentTimeMillis();
        //边
//        Iterable<OrientEdge> edges = database.command(new OCommandSQL(
//                "select * from `e_test1`  where instance_in='11000100'")).execute();
//        Iterable<OrientEdge> edges = database.command(new OCommandSQL(
//                "select * from `e_test1`  where instance_out='11000'")).execute();
//        Iterable<OrientEdge> edges = database.command(new OCommandSQL(
//                "select * from `e_test1`  where instance_in>='11000100' and instance_in<='11001000'")).execute();
        Iterable<OrientEdge> edges = database.command(new OCommandSQL(
                "select * from `e_test1`  where instance_out like '10010%'")).execute();

        int count = 0;
        for (OrientEdge e : edges) {
            System.out.println("rid： "+e.getIdentity());
            count++;
        }

        System.out.println("总记录数： "+count);

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");

    }

    private final static int TXNUM = 100;
    private final static int TXBATCH = 50;
    private final static int EDGENUM = 10;
    private static int THREADS = 8;
    //test01
//    public static final OrientGraphFactory factory = new OrientGraphFactory("remote:139.219.102.105:2424;139.219.67.235:2424/test1", "root", "easyops");
    public static final OrientGraphFactory factory = new OrientGraphFactory("remote:139.219.102.105:2424/test1", "root", "easyops");
    //public static final OrientGraphFactory factory = new OrientGraphFactory("remote:99.13.30.182:80;99.13.30.183:80/neo4j", "root", "root");
//    public static final OrientGraphFactory factory = new OrientGraphFactory(
//            "remote:99.13.30.182:2424/neo4j", "root", "root");

    //aa
    //public static final OrientGraphFactory factory = new OrientGraphFactory("remote:99.13.30.182:80/aaa", "root", "root");

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void testAddEdgeWithSql() {

        //OGlobalConfiguration.RID_BAG_EMBEDDED_TO_SBTREEBONSAI_THRESHOLD.setValue(Integer.MAX_VALUE);
        txMode = true;
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
        //graph.getRawGraph().declareIntent(new OIntentMassiveInsert());
        int j = 5;
        long startTime = System.currentTimeMillis();
//        for (int i = 1; i <= 1000; ++i) {
//        for (int i = 1001; i <= 1100; ++i) {
        for (int i = 1100; i <= 1109; ++i) {
                graph.command(new OCommandSQL("create edge e_test1 from " +
                        "(select * from `v_test1` where name=\"test"+i+"\") to " +
                        "(select * from `v_test1` where name=\"test"+(i+1)+"\") content {" +
                        "\"instance_out\": \""+i+"\",\"instance_in\": \""+i+100+"\"}")).execute();
        }
        //graph.getRawGraph().declareIntent(null);

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");
    }

    public void testAdd() throws InterruptedException {

        txMode = true;
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
        graph.getRawGraph().declareIntent(new OIntentMassiveInsert());

        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < 500; ++i) {
                graph.command(new OCommandSQL("insert into v_test1 content {\n" +
                        "  \"name\": \"test"+i+"\",\n" +
                        "  \"IPS_String\": \"99.13.31.0, 99.13.31.1, 99.13.31.2, 99.13.31.3, 99.13.31.4\",\n" +
                        "  \"IPS_EmbeddedList\": [\"99.13.31.0\", \"99.13.31.1\", \"99.13.31.2\", \"99.13.31.3\", \"99.13.31.4\"],\n" +
                        "  \"businessId\": \"ac4643ea7481916003bfcf41aa48ac3a\",\n" +
                        "  \"_category\": \"ttt\",\n" +
                        "  \"memo\": \"aaa\",\n" +
                        "  \"instanceId\": \"id"+i+"\",\n" +
                        "  \"_object_id\": \"BUSINESS\",\n" +
                        "  \"creator\": \"easyops\",\n" +
                        "  \"ctime\": \""+format.format(new Date())+"\",\n" +
                        "  \"org\": 1009,\n" +
                        "  \"_id\": {\n" +
                        "    \"$oid\": \"5a4deb0ede74fe6f9d2b7392\"\n" +
                        "  },\n" +
                        "  \"_ts\": 1515055886,\n" +
                        "  \"_version\": 1,\n" +
                        "  \"__SYNC__BATCH__\": 1515787201\n" +
                        "}")).execute();

                if (i % 500 == 0) {
                    graph.commit();
                }
            }
        } finally {
            //graph.shutdown();
            graph.commit();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");
    }

    public void testAddVertexWithSql() {

        OGlobalConfiguration.RID_BAG_EMBEDDED_TO_SBTREEBONSAI_THRESHOLD.setValue(Integer.MAX_VALUE);
        OGlobalConfiguration.USE_WAL.setValue(false);
        txMode = false;
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
        OGlobalConfiguration.RID_BAG_EMBEDDED_TO_SBTREEBONSAI_THRESHOLD.setValue(Integer.MAX_VALUE);
        OGlobalConfiguration.USE_WAL.setValue(false);
        graph.getRawGraph().declareIntent(new OIntentMassiveInsert());

        long startTime = System.currentTimeMillis();

//        for (int i = 1; i <= 1000; ++i) {
        for (int i = 1001; i <= 1100; ++i) {
//        for (int i = 1101; i <= 1110; ++i) {
//        for (int i = 4001; i <= 5000; ++i) {
//        for (int i = 1001; i <= 1100; ++i) {
//        for (int i = 1101; i <= 1110; ++i) {
//        for (int i = 10000; i <= 20000; ++i) {
            graph.command(new OCommandSQL("insert into v_test1 content {\n" +
                    "  \"name\": \"test"+i+"\",\n" +
                    "  \"IPS_String\": \"99.13.31.0, 99.13.31.1, 99.13.31.2, 99.13.31.3, 99.13.31.4\",\n" +
                    "  \"IPS_EmbeddedList\": [\"99.13.31.0\", \"99.13.31.1\", \"99.13.31.2\", \"99.13.31.3\", \"99.13.31.4\"],\n" +
                    "  \"businessId\": \"ac4643ea7481916003bfcf41aa48ac3a\",\n" +
                    "  \"_category\": \"ttt\",\n" +
                    "  \"memo\": \"aaa\",\n" +
                    "  \"instanceId\": \"id"+i+"\",\n" +
                    "  \"_object_id\": \"BUSINESS\",\n" +
                    "  \"creator\": \"easyops\",\n" +
                    "  \"ctime\": \""+format.format(new Date())+"\",\n" +
                    "  \"org\": 1009,\n" +
                    "  \"_id\": {\n" +
                    "    \"$oid\": \"5a4deb0ede74fe6f9d2b7392\"\n" +
                    "  },\n" +
                    "  \"_ts\": 1515055886,\n" +
                    "  \"_version\": 1,\n" +
                    "  \"__SYNC__BATCH__\": 1515787201\n" +
                    "}")).execute();
        }
        graph.getRawGraph().declareIntent(null);

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");

        graph.shutdown();
    }


    public void testAddVertexWithThread() throws InterruptedException {

        THREADS = 100;
        final Thread[] threads = new Thread[THREADS];

        txMode = true;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < THREADS; ++i) {
            final int threadNum = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
                    try {
                        //System.out.println("THREAD " + threadNum + " Start transactions (" + TXNUM + ")");
//                        for (int i = 2001; i <= 3000; ++i) {
//                        for (int i = 3001; i <= 3100; ++i) {
                        for (int i = 3101; i <= 3110; ++i) {
//                        for (int i = 20001; i <=30000; ++i) {
                            graph.command(new OCommandSQL("insert into v_test1 content {\n" +
                                    "  \"name\": \"test"+i+"\",\n" +
                                    "  \"IPS_String\": \"99.13.31.0, 99.13.31.1, 99.13.31.2, 99.13.31.3, 99.13.31.4\",\n" +
                                    "  \"IPS_EmbeddedList\": [\"99.13.31.0\", \"99.13.31.1\", \"99.13.31.2\", \"99.13.31.3\", \"99.13.31.4\"],\n" +
                                    "  \"businessId\": \"ac4643ea7481916003bfcf41aa48ac3a\",\n" +
                                    "  \"_category\": \"ttt\",\n" +
                                    "  \"memo\": \"aaa\",\n" +
                                    "  \"instanceId\": \"id"+i+"\",\n" +
                                    "  \"_object_id\": \"BUSINESS\",\n" +
                                    "  \"creator\": \"easyops\",\n" +
                                    "  \"ctime\": \""+format.format(new Date())+"\",\n" +
                                    "  \"org\": 1009,\n" +
                                    "  \"_id\": {\n" +
                                    "    \"$oid\": \"5a4deb0ede74fe6f9d2b7392\"\n" +
                                    "  },\n" +
                                    "  \"_ts\": 1515055886,\n" +
                                    "  \"_version\": 1,\n" +
                                    "  \"__SYNC__BATCH__\": 1515787201\n" +
                                    "}")).execute();
                        }
                    } finally {
                        graph.shutdown();
                    }
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < THREADS; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");
    }


    public void testAddEdgeWithThread() throws InterruptedException {

        THREADS = 100;
        final Thread[] threads = new Thread[THREADS];

        txMode = true;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < THREADS; ++i) {
            final int threadNum = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
                    try {
//                        for (int i = 10001; i <= 10100; ++i) {
//                        for (int i = 10001; i <= 11000; ++i) {
                        for (int i = 10001; i <= 10010; ++i) {
                            graph.command(new OCommandSQL("create edge e_test1 from " +
                                    "(select * from `v_test1` where name=\"test"+(i+threadNum*110)+"\") to " +
                                    "(select * from `v_test1` where name=\"test"+(i+threadNum*110+1)+"\") content {" +
                                    "\"instance_out\": \""+i+"\",\"instance_in\": \""+i+100+"\"}")).execute();
                        }
                    } finally {
                        graph.shutdown();
                    }
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < THREADS; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("用时： " + (endTime - startTime) + " ms");
    }

    public void testDelete() {
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();

//        resultTwo = database.query(new OSQLSynchQuery<ODocument>("select expand(outE()) from " + result.get(1).getIdentity()));
//        Assert.assertEquals(resultTwo.size(), 1);

//        database.command(new OCommandSQL("DELETE FROM testFromOneE unsafe")).execute();
//        database.command(new OCommandSQL("DELETE FROM testFromTwoE unsafe")).execute();
        //返回成功条数
        int deleted = graph.command(new OCommandSQL("DELETE vertex v_test2 where e=0")).execute();
        System.out.println("deleted: " + deleted);
    }

    public void testUpdate() {
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();

        //返回成功条数
        int updated = graph.command(new OCommandSQL("update v_test2 set e=1 where e=0")).execute();
        System.out.println("updated: " + updated);
    }

    public void testCount() {
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();

//        resultTwo = database.query(new OSQLSynchQuery<ODocument>("select expand(outE()) from " + result.get(1).getIdentity()));
//        Assert.assertEquals(resultTwo.size(), 1);

//        database.command(new OCommandSQL("DELETE FROM testFromOneE unsafe")).execute();
//        database.command(new OCommandSQL("DELETE FROM testFromTwoE unsafe")).execute();
        //返回成功条数
        int count = graph.command(new OCommandSQL("delete vertex v_test1")).execute();
        System.out.println("count: " + count);
    }
}