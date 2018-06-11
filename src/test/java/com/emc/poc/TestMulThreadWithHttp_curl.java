package com.emc.poc;

/**
 * @author
 * @create 2018-04-16 下午8:02
 **/

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMulThreadWithHttp_curl {

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
            exec.execute(new MyTestThreadWithHttp2(begin, end));
        }
        //当60个线程，初始化完成后，解锁，让六十个线程在4个双核的cpu服务器上一起竞争着跑，来模拟60个并发线程访问tomcat
        begin.countDown();

        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            System.out.println("the average time cost is: " + (total_time*1.0)/(ThreadNumber*1.0) + " ms");
            System.out.println("speed is: " + (VertexNumber*1.0)/(((total_time*1.0)/(ThreadNumber*1.0))/1000.0) + " 条/秒");
            System.out.println("speed is: " + String.format("%.2f", (VertexNumber*1.0)/(((total_time*1.0)/(ThreadNumber*1.0))/1000.0)) + " 条/秒");

        }
        exec.shutdown();
        //new MyTestThreadWithHttp2().testDelete();
        System.out.println("--------------------main method end-------------------------------");
    }

    public static void testMainThread(int threadNumber) throws Exception {

        testMain(1, threadNumber, 10);

        long sleepTime = 10000;

        System.out.println("sleep " + sleepTime + " ms ...... ");
        Thread.sleep(sleepTime);
        testMain(1, threadNumber, 100);

        System.out.println("sleep " + sleepTime + " ms ......");
        Thread.sleep(sleepTime);
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
    }


    public static void main(String[] args) throws Exception {
//        testMainThread(1);
//        testMainThread(8);
//        testMainThread(16);
//        testMainThread(32);
//        testMainThread(48);
        testMainThread(64);

    }


    public static int StartVertexNumber = 1;

    public static int ThreadNumber = 1;
    public static int VertexNumber = 10;

    public static int EndVertexNumber = StartVertexNumber+VertexNumber-1;
}

class MyTestThreadWithHttp2 implements Runnable {
    private CountDownLatch begin;
    private CountDownLatch end;


    MyTestThreadWithHttp2(CountDownLatch begin,
                    CountDownLatch end) {
        this.begin = begin;
        this.end = end;
    }

    MyTestThreadWithHttp2() {
    }


    @Override
    public void run() {
        try {
            begin.await();

            this.testAddVertexWithHttp();
//            this.testDelete();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            end.countDown();
        }

    }

    public OrientGraphFactory factory = new OrientGraphFactory("remote:139.219.102.105:2424/test1", "root", "easyops");

    public void testAddVertexWithHttp() {

        long startTime = System.currentTimeMillis();

        for (int i = TestMulThreadWithHttp_curl.StartVertexNumber; i <= TestMulThreadWithHttp_curl.EndVertexNumber; ++i) {
                try{
//                    String bashCommand = "/Users/sara/test_curl.sh";
                    String bashCommand = "curl -X POST http://139.219.102.105:2480/command/test1/sql -H 'Accept-Encoding: gzip,deflate' -H 'Authorization: Basic cm9vdDplYXN5b3Bz' -H 'Cache-Control: no-cache' -H 'Content-Type: application/json' -H 'Postman-Token: 1adba119-560f-4ebd-a95f-ed4e838c600e' -d '{\n" +
                            "    \"command\":\"insert into v_test1 content {\\\"name\\\":\\\"test2000\\\",\\\"IPS_String\\\":\\\"99.13.31.0,99.13.31.1,99.13.31.2,99.13.31.3,99.13.31.4\\\",\\\"IPS_EmbeddedList\\\":[\\\"99.13.31.0\\\",\\\"99.13.31.1\\\",\\\"99.13.31.2\\\",\\\"99.13.31.3\\\",\\\"99.13.31.4\\\"],\\\"businessId\\\":\\\"ac4643ea7481916003bfcf41aa48ac3a\\\",\\\"_category\\\":\\\"ttt\\\",\\\"memo\\\":\\\"aaa\\\",\\\"instanceId\\\":2000,\\\"_object_id\\\":\\\"BUSINESS\\\",\\\"creator\\\":\\\"easyops\\\",\\\"ctime\\\":\\\"2018-04-3010:44:26\\\",\\\"org\\\":1009,\\\"_id\\\":{\\\"$oid\\\":\\\"5a4deb0ede74fe6f9d2b7392\\\"},\\\"_ts\\\":1515055886,\\\"_version\\\":1,\\\"__SYNC__BATCH__\\\":1515787201}\"\n" +
                            "}'";
//                    System.out.println(bashCommand);
                    Runtime runtime = Runtime.getRuntime();
//                    Process pro = runtime.exec(bashCommand);
                    Process pro = runtime.exec(new String[] { "/bin/sh", "-c", bashCommand });
                    int status = pro.waitFor();
                    if (status != 0)
                    {
                        System.out.println("Failed to call shell's command ");
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                    StringBuffer strbr = new StringBuffer();
                    String line;
                    while ((line = br.readLine())!= null)
                    {
                        strbr.append(line).append("\n");
                    }

//                    String result = strbr.toString();
//                    System.out.println(result);

                }
                catch (Exception ec)
                {
                    ec.printStackTrace();
                }

        }

        long endTime = System.currentTimeMillis();
        long cost_time = endTime - startTime;

        System.out.println("speed is: " + String.format("%.2f", (TestMulThreadWithHttp_curl.VertexNumber*1.0)/((cost_time*1.0)/1000.0)) + " 条/秒");
        TestMulThreadWithHttp_curl.total_time +=  cost_time;

    }

    public void testDelete() {
        OrientBaseGraph graph = factory.getTx();

        //返回成功条数
        int deleted = graph.command(new OCommandSQL("delete vertex v_test1 where instanceId <= 10010")).execute();
//        int deleted = graph.command(new OCommandSQL("DELETE vertex v_test2 where e=0")).execute();
        System.out.println("deleted: " + deleted);

        graph.shutdown();
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient client = new OkHttpClient();

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Accept-Encoding", "gzip,deflate")
                .addHeader("Authorization", "Basic cm9vdDplYXN5b3Bz")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}