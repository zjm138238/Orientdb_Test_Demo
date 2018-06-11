package com.emc.poc;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Iterator;

@Test
public class GraphTransactionConsistency {
    private boolean          txMode  = true;

    public void testQuery() {
        final OrientGraphFactory factory = new OrientGraphFactory("remote:192.168.100.123:2424/demo", "root", "easyops");

        OrientBaseGraph database = txMode ? factory.getTx() : factory.getNoTx();

        System.out.println("Checking consistency of database...");
        System.out.println("Records found V=" + database.countVertices() + " E=" + database.countEdges());

        //查询
        final Iterable<OrientVertex> vertices = database.command(new OCommandSQL("select from person")).execute();
        for (OrientVertex v : vertices) {
            final ODocument doc = v.getRecord();

            Assert.assertNotNull(doc);
            System.out.println("doc:"+doc.toJSON());

            final ORidBag out = doc.field("out_Owns");
            if (out != null) {
                for (Iterator<OIdentifiable> it = out.rawIterator(); it.hasNext();) {
                    final OIdentifiable edge = it.next();
                    Assert.assertNotNull(edge);

                    final ODocument rec = edge.getRecord();
                    Assert.assertNotNull(rec);

                    Assert.assertNotNull(rec.field("out"));
                    Assert.assertEquals(((ODocument) rec.field("out")).getIdentity(), v.getIdentity());

                    Assert.assertNotNull(rec.field("in"));

                    System.out.println("edge:"+edge);
                    System.out.println("rec:"+rec.toJSON());
                    //rec.field("out") 会把这个顶点对象读出来
                    System.out.println("rec.field(\"out\"):"+((ODocument) rec.field("out")).toJSON());
                    System.out.println("v.getIdentity():"+v.getIdentity());
                    System.out.println("rec.field(\"in\"):"+rec.field("in"));
                }
            }

            final ORidBag in = doc.field("in_");
            if (in != null) {
                for (Iterator<OIdentifiable> it = in.rawIterator(); it.hasNext();) {
                    final OIdentifiable edge = it.next();
                    Assert.assertNotNull(edge);

                    final ODocument rec = edge.getRecord();
                    Assert.assertNotNull(rec);

                    Assert.assertNotNull(rec.field("in"));
                    Assert.assertEquals(((ODocument) rec.field("in")).getIdentity(), v.getIdentity());

                    Assert.assertNotNull(rec.field("out"));


                    System.out.println("edge:"+edge);
                    System.out.println("rec:"+rec);
                    System.out.println("rec.field(\"out\"):"+rec.field("out"));
                    System.out.println("v.getIdentity():"+v.getIdentity());
                    System.out.println("rec.field(\"in\"):"+rec.field("in"));
                }
            }
        }

        System.out.println("Consistency ok.");

        database.shutdown();
    }

    private final static int TXNUM   = 100;
    private final static int TXBATCH = 50;
    private final static int EDGENUM = 10;
    private final static int THREADS = 8;
    public void testAdd() throws InterruptedException {
        final OrientGraphFactory factory = new OrientGraphFactory("remote:192.168.100.123:2424/demo2", "root", "easyops");

        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();

        try {
            for (int i = 0; i < TXNUM; ++i) {
                //新增顶点
                final OrientVertex v1 = graph.addVertex("class:v_test1", "v", i, "type", "Main", "lastUpdate", new Date());

                for (int e = 0; e < EDGENUM; ++e) {
                    final OrientVertex v2 = graph.addVertex("class:v_test2", "v", i, "e", e, "type", "Connected", "lastUpdate", new Date());
                    //新增v1到v2的E边
                    //v1.addEdge("e_test", v2);
                    //新增边时顺便添加属性
                    v1.addEdge("e_test", v2, "e_test", null, "name","test","type","test");
                }

                if (i % TXBATCH == 0) {
                    System.out.println(" Committing batch of " + TXBATCH + " (i=" + i + ")");
                    System.out.flush();

                    graph.commit();

                    System.out.println(
                            " Commit ok - records found V=" + graph.countVertices() + " E=" + graph.countEdges());
                    System.out.flush();
                }
            }
        } finally {
            graph.shutdown();
        }

    }
    public void testAddWithThread() throws InterruptedException {

        final OrientGraphFactory factory = new OrientGraphFactory("remote:192.168.100.123:2424/demo", "root", "easyops");

        final Thread[] threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; ++i) {
            final int threadNum = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
                    try {
                        System.out.println("THREAD " + threadNum + " Start transactions (" + TXNUM + ")");
                        for (int i = 0; i < TXNUM; ++i) {
                            //新增顶点
                            final OrientVertex v1 = graph.addVertex(null, "v", i, "type", "Main", "lastUpdate", new Date());

                            for (int e = 0; e < EDGENUM; ++e) {
                                final OrientVertex v2 = graph.addVertex(null, "v", i, "e", e, "type", "Connected", "lastUpdate", new Date());
                                //新增v1到v2的E边
                                v1.addEdge("E", v2);
                                //新增边时顺便添加属性
                                v1.addEdge("e_test", v2, "e_test", null, "name","test","type","test");
                            }

                            if (i % TXBATCH == 0) {
                                System.out.println("THREAD " + threadNum + " Committing batch of " + TXBATCH + " (i=" + i + ")");
                                System.out.flush();

                                graph.commit();

                                System.out.println(
                                        "THREAD " + threadNum + " Commit ok - records found V=" + graph.countVertices() + " E=" + graph.countEdges());
                                System.out.flush();
                            }
                        }
                    } finally {
                        graph.shutdown();
                    }
                }
            });

            Thread.sleep(1000);
            threads[i].start();
        }

        for (int i = 0; i < THREADS; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

//    public void testDelete() {
////        resultTwo = database.query(new OSQLSynchQuery<ODocument>("select expand(outE()) from " + result.get(1).getIdentity()));
////        Assert.assertEquals(resultTwo.size(), 1);
//
//        database.command(new OCommandSQL("DELETE FROM testFromOneE unsafe")).execute();
//        database.command(new OCommandSQL("DELETE FROM testFromTwoE unsafe")).execute();
//        int deleted = database.command(new OCommandSQL("DELETE VERTEX testFromV")).execute();
//    }

//    public void testTransactionConsistency() throws InterruptedException {
//        final OrientGraphFactory factory = new OrientGraphFactory("remote:192.168.100.123:2424/demo", "root", "easyops");
//
//        database = txMode ? factory.getTx() : factory.getNoTx();
//
//        System.out.println("Checking consistency of database...");
//        System.out.println("Records found V=" + database.countVertices() + " E=" + database.countEdges());
//
//        //查询
//        final Iterable<OrientVertex> vertices = database.command(new OCommandSQL("select from person")).execute();
//        for (OrientVertex v : vertices) {
//            final ODocument doc = v.getRecord();
//
//            Assert.assertNotNull(doc);
//            System.out.println("doc:"+doc.toJSON());
//
//            final ORidBag out = doc.field("out_Owns");
//            if (out != null) {
//                for (Iterator<OIdentifiable> it = out.rawIterator(); it.hasNext();) {
//                    final OIdentifiable edge = it.next();
//                    Assert.assertNotNull(edge);
//
//                    final ODocument rec = edge.getRecord();
//                    Assert.assertNotNull(rec);
//
//                    Assert.assertNotNull(rec.field("out"));
//                    Assert.assertEquals(((ODocument) rec.field("out")).getIdentity(), v.getIdentity());
//
//                    Assert.assertNotNull(rec.field("in"));
//
//                    System.out.println("edge:"+edge);
//                    System.out.println("rec:"+rec.toJSON());
//                    //rec.field("out") 会把这个顶点对象读出来
//                    System.out.println("rec.field(\"out\"):"+((ODocument) rec.field("out")).toJSON());
//                    System.out.println("v.getIdentity():"+v.getIdentity());
//                    System.out.println("rec.field(\"in\"):"+rec.field("in"));
//                }
//            }
//
//            final ORidBag in = doc.field("in_");
//            if (in != null) {
//                for (Iterator<OIdentifiable> it = in.rawIterator(); it.hasNext();) {
//                    final OIdentifiable edge = it.next();
//                    Assert.assertNotNull(edge);
//
//                    final ODocument rec = edge.getRecord();
//                    Assert.assertNotNull(rec);
//
//                    Assert.assertNotNull(rec.field("in"));
//                    Assert.assertEquals(((ODocument) rec.field("in")).getIdentity(), v.getIdentity());
//
//                    Assert.assertNotNull(rec.field("out"));
//
//
//                    System.out.println("edge:"+edge);
//                    System.out.println("rec:"+rec);
//                    System.out.println("rec.field(\"out\"):"+rec.field("out"));
//                    System.out.println("v.getIdentity():"+v.getIdentity());
//                    System.out.println("rec.field(\"in\"):"+rec.field("in"));
//                }
//            }
//        }
//
//        System.out.println("Consistency ok.");
//
//        final Thread[] threads = new Thread[THREADS];
//
//        for (int i = 0; i < THREADS; ++i) {
//            final int threadNum = i;
//            threads[i] = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();
//                    try {
//                        System.out.println("THREAD " + threadNum + " Start transactions (" + TXNUM + ")");
//                        for (int i = 0; i < TXNUM; ++i) {
//                            final OrientVertex v1 = graph.addVertex(null, "v", i, "type", "Main", "lastUpdate", new Date());
//
//                            for (int e = 0; e < EDGENUM; ++e) {
//                                final OrientVertex v2 = graph.addVertex(null, "v", i, "e", e, "type", "Connected", "lastUpdate", new Date());
//                                //新增v1到v2的E边
//                                v1.addEdge("E", v2);
//                            }
//
//                            if (i % TXBATCH == 0) {
//                                System.out.println("THREAD " + threadNum + " Committing batch of " + TXBATCH + " (i=" + i + ")");
//                                System.out.flush();
//
//                                graph.commit();
//
//                                System.out.println(
//                                        "THREAD " + threadNum + " Commit ok - records found V=" + graph.countVertices() + " E=" + graph.countEdges());
//                                System.out.flush();
//                            }
//                        }
//                    } finally {
//                        graph.shutdown();
//                    }
//                }
//            });
//
//            Thread.sleep(1000);
//            threads[i].start();
//        }
//
//        for (int i = 0; i < THREADS; ++i) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        database.shutdown();
//    }
}