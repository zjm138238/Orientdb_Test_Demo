package com.emc.poc;

/**
 * @author
 * @create 2018-04-16 下午8:02
 **/

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMulThread {

    public static long total_time = 0;

    public static void testMain(int startVertexNumber, int threadNumber, int vertexNumber) {
        total_time = 0;
        StartVertexNumber = startVertexNumber;
        ThreadNumber = threadNumber;
        VertexNumber  = vertexNumber;
        EndVertexNumber = StartVertexNumber+VertexNumber-1;

        CountDownLatch begin = new CountDownLatch(1);

        System.out.println("thread number is " + ThreadNumber);
        System.out.println("vertex number is " + VertexNumber);
        System.out.println("start vertex number is " + StartVertexNumber);
        System.out.println("end vertex number is " + EndVertexNumber);

        //设置最大的并发数量
        ExecutorService exec = Executors.newFixedThreadPool(ThreadNumber);

        CountDownLatch end = new CountDownLatch(ThreadNumber);

        for (int i=1; i <= ThreadNumber; i++) {
            // 执行一个线程时会首先执行线程的run方法
            exec.execute(new MyTestThread(begin, end));
        }
        long startTime = System.currentTimeMillis();
        //当n个线程，初始化完成后，解锁，让n个线程在服务器上一起竞争着cpu跑，来模拟n个并发线程访问
        begin.countDown();

        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;

            System.out.println("the average time cost is: " + (total_time*1.0)/(ThreadNumber*1.0) + " ms");
            System.out.println("speed is: " + (VertexNumber*1.0)/(((total_time*1.0)/(ThreadNumber*1.0))/1000.0) + " 条/秒");
            System.out.println("speed is: " + String.format("%.2f", (VertexNumber*1.0)/(((total_time*1.0)/(ThreadNumber*1.0))/1000.0)) + " 条/秒");

        }
        exec.shutdown();
        new MyTestThread().testDelete();
        System.out.println("--------------------main method end-------------------------------");
    }

    public static void testMainThread(int threadNumber) throws Exception {
        // threadNumber个线程写入10个顶点
        testMain(1, threadNumber, 10);

        long sleepTime = 10000;

        System.out.println("sleep " + sleepTime + " ms ......");
        // 睡眠10s后继续往下执行
        Thread.sleep(sleepTime);

        // threadNumber个线程写入100个顶点
        testMain(1, threadNumber, 100);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);

        // threadNumber个线程写入500个顶点
        testMain(1, threadNumber, 500);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);
        testMain(1, threadNumber, 1000);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);
        testMain(1, threadNumber, 5000);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);
        testMain(1, threadNumber, 10000);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);

        //-------添加10万顶点--------
//        testMain(20001, threadNumber, 10000);
    }


    public static void main(String[] args) throws Exception {
        // 测试1个线程写入
        testMainThread(1);
        // 测试8个线程写入
        testMainThread(8);
        // 测试16个线程写入
        testMainThread(16);
        testMainThread(32);
        testMainThread(48);
        testMainThread(64);

        //-------添加10万顶点--------
//        testMainThread(10);
    }


    public static int StartVertexNumber = 1;

    public static int ThreadNumber = 1;
    public static int VertexNumber = 10000;

    public static int EndVertexNumber = StartVertexNumber+VertexNumber-1;
}

class MyTestThread implements Runnable {
    private CountDownLatch begin;
    private CountDownLatch end;


    MyTestThread(CountDownLatch begin,
                    CountDownLatch end) {
        this.begin = begin;
        this.end = end;
    }

    MyTestThread() {
    }


    @Override
    public void run() {
        try {
            begin.await();

            this.testAddVertexWithSql();
            this.testDelete();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            end.countDown();
        }

    }

    public OrientGraphFactory factory = new OrientGraphFactory("remote:139.219.102.105:2424/test1", "root", "easyops");
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public void testAddVertexWithSql() {

        boolean txMode = true;
        // 开启事务
        OrientBaseGraph graph = txMode ? factory.getTx() : factory.getNoTx();

        long startTime = System.currentTimeMillis();

        // 循环写入EndVertexNumber-StartVertexNumber+1个顶点
        for (int i = TestMulThread.StartVertexNumber; i <= TestMulThread.EndVertexNumber; ++i) {
            graph.command(new OCommandSQL("insert into v_test1 content {\n" +
                    "  \"name\": \"test"+i+"\",\n" +
                    "  \"IPS_String\": \"99.13.31.0, 99.13.31.1, 99.13.31.2, 99.13.31.3, 99.13.31.4\",\n" +
                    "  \"IPS_EmbeddedList\": [\"99.13.31.0\", \"99.13.31.1\", \"99.13.31.2\", \"99.13.31.3\", \"99.13.31.4\"],\n" +
                    "  \"businessId\": \"ac4643ea7481916003bfcf41aa48ac3a\",\n" +
                    "  \"_category\": \"ttt\",\n" +
                    "  \"memo\": \"aaa\",\n" +
                    "  \"instanceId\": " + i + ",\n" +
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

        long endTime = System.currentTimeMillis();
        long cost_time = endTime - startTime;
        System.out.println("speed is: " + String.format("%.2f", (TestMulThread.VertexNumber*1.0)/((cost_time*1.0)/1000.0)) + " 条/秒");
        TestMulThread.total_time +=  cost_time;

        graph.shutdown();
    }

    public void testDelete() {
        OrientBaseGraph graph = factory.getTx();

        //返回成功条数
        int deleted = graph.command(new OCommandSQL("delete vertex v_test1 where instanceId <= 10010")).execute();
//        int deleted = graph.command(new OCommandSQL("DELETE vertex v_test2 where e=0")).execute();
        System.out.println("deleted: " + deleted);

        graph.shutdown();
    }
}