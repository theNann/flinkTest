import org.apache.commons.math3.analysis.function.Min;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.apache.flink.util.Collector;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import www.pyn.bean.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Main {

    public static void generaterBuffer(byte[] buffer, int idx, int data) {
        buffer[idx*4] = (byte) (data & 0xff);
        buffer[idx*4+1] = (byte) ((data >> 8 ) & 0xff);
        buffer[idx*4+2] = (byte) ((data >> 16 ) & 0xff);
        buffer[idx*4+3] = (byte) ((data >> 24 ) & 0xff);
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        Configuration.getInstance().setIp("10.222.140.17");
        Configuration.getInstance().setReadPort(6001);
        Configuration.getInstance().setWritePort(6002);
//        private String trainFilePath = "E:\\BIMRecommed\\input\\data_train.csv";
//        private String testFilePath = "E:\\BIMRecommed\\input\\data_test.csv";
        String trainDataPath = "/home/pyn/Desktop/BIMRecommed/input/data_train.csv";
        String testDataPath = "/home/pyn/Desktop/DataSet/data6.csv";
//        String testDataPath = "/home/pyn/Desktop/BIMRecommed/input/data_test.csv";
//        private String trainFilePath = "E:\\BIMRecommed\\input\\target_train.txt";
//        private String testFilePath = "E:\\BIMRecommed\\input\\target_test.txt";
        String trainTargetPath = "/home/pyn/Desktop/BIMRecommed/input/target_train.txt";
        String testTargetPath = "/home/pyn/Desktop/DataSet/target6.txt";
//        String testTargetPath = "/home/pyn/Desktop/BIMRecommed/input/target_test.txt";
        Configuration.getInstance().setTrainDataPath(trainDataPath);
        Configuration.getInstance().setTestDataPath(testDataPath);
        Configuration.getInstance().setTrainTargetPath(trainTargetPath);
        Configuration.getInstance().setTestTargetPath(testTargetPath);

        ExecutionEnvironment env;
        ParameterTool params;

        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        PrepareData prepareData = PrepareData.getInstance(env, trainDataPath, testDataPath);
        PrepareResult prepareResult = PrepareResult.getInstance(env, trainTargetPath, testTargetPath);
        long prepareTime = System.currentTimeMillis();

//        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(params, env, prepareData, prepareResult);
//        collaborativeFiltering.solveCollaborativeFiltering();

//        Recommender recommender = new Recommender(params, env, prepareData, prepareResult);
//        recommender.solveRecommender();

        Knn knn = new Knn(params, env, prepareData, prepareResult);
        knn.solveKnn();
        long endTime = System.currentTimeMillis();

        System.out.println("Prepare time: " + (prepareTime-startTime)*1.0/1000+"s");
        System.out.println("Cal time: " + (endTime-prepareTime)*1.0/1000 + "s ,Data size: " + prepareData.getTestData().size());
//        System.out.println("train_size " + prepareData.getTrainPosition().size()); //11550
//        System.out.println("test_size " + prepareData.getTestData().size()); // 4852


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //與c++交互
//        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
//        DataStream<byte[]> bytes = senv.addSource(new SocketByteStreamFunction(ip, port, 18*4,0L));
//
//        DataStream<PrimitiveData> primitiveDataDataStream = bytes.flatMap(new FlatMapFunction<byte[], PrimitiveData>() {
//            public void flatMap(byte[] bytes, Collector<PrimitiveData> collector) throws Exception {
//                PrimitiveData primitiveData = PrimitiveData.primitiveDataFromBytes(bytes);
//                collector.collect(primitiveData);
//            }
//        });
//
//        DataStream<Result> result = primitiveDataDataStream.flatMap(new Knn.knnMap());
//
//        result.writeToSocket(ip, 6002, new SerializationSchema<Result>() {
//            public byte[] serialize(Result re) {
//                int len = re.getVisibleObj().size()+1;
//                System.out.println("len : " + len);
//                byte[] buffer = new byte[(len+1)*4];
//                generaterBuffer(buffer, 0, len);
//                generaterBuffer(buffer, 1, re.getDataId());
//                for(int i = 0; i < re.getVisibleObj().size(); i++) {
//                    int tmp = re.getVisibleObj().get(i);
//                    generaterBuffer(buffer, i+2, tmp);
//                }
////                File file = new File("/home/pyn/Desktop/out.txt");
////                FileOutputStream in;
////                try {
////                    in = new FileOutputStream(file);
////                    String start = "start ";
////                    in.write(start.getBytes());
////                    for(int i = 0; i < re.getVisibleObj().size(); i++) {
////                        int tmp = re.getVisibleObj().get(i);
////                        byte[] bt = String.valueOf(tmp).concat(", ").getBytes();
////                        in.write(bt, 0 ,bt.length);
////                        generaterBuffer(buffer, i+2, tmp);
////                    }
////                    in.close();
////                } catch (FileNotFoundException e) {
////                    e.printStackTrace();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//
////                byte[] buffer = new byte[4*3];
////                generaterBuffer(buffer, 0, 2);
////                generaterBuffer(buffer, 1, 0);
////                generaterBuffer(buffer, 2, 1);
//
//                return buffer;
//            }
//        });
//        senv.execute("test");
//////////////////////////////////////////////////////////////////////////////////////////////////////////







//        final ParameterTool params = ParameterTool.fromArgs(args);
//        // set up the execution environment
//        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//
//        // make parameters available in the web interface
//        env.getConfig().setGlobalJobParameters(params);
//
//        List<Position> trainPosition = null;
//        System.out.println("Input!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//        DataSet<Position> dataTrainPositionDataSet = env.readCsvFile("E:\\pyn_playground\\flink_data.csv")
//                .pojoType(Position.class, "dataId", "px", "py", "pz");
//        trainPosition = dataTrainPositionDataSet.collect();
        // 也可以通过如下方式得到dataTrainPosition，即先读为Tuple4，然后通过map转化为Dataset<position>
//        final DataSet<Tuple4<Integer,Integer,Integer,Integer>> dataTrainPositionDataSet =
//                env.readCsvFile("E:\\pyn_playground\\flink_data.csv").
//                types(Integer.class, Integer.class, Integer.class, Integer.class);

//        trainPosition = dataTrainPositionDataSet.flatMap(new FlatMapFunction<Tuple4<Integer,Integer,Integer,Integer>, Position>() {
//            public void flatMap(Tuple4<Integer,Integer,Integer,Integer> tuple4, Collector<Position> out) {
//                out.collect(new Position(tuple4.f0, tuple4.f1, tuple4.f2, tuple4.f3));
//                System.out.println(tuple4.f0 +" " + tuple4.f1 + " " + tuple4.f2 + " " + tuple4.f3 + "MAPPPPPPPPPPPPPP");
//            }
//        }).collect();





//        DataSet<String> text = env.fromElements("hello world");
//        DataSet<Tuple2<String, Integer>> counts =
//                // split up the lines in pairs (2-tuples) containing: (word,1)
//                text.flatMap(new SocketWordCount.Tokenizer())
//                        // group by the tuple field "0" and sum up tuple field "1"
//                        .groupBy(0)
//                        .sum(1);
//
//        if (params.has("output")) {
//            counts.writeAsCsv(params.get("output"), "\n", " ");
//            // execute program
//            env.execute("Knn");
//        } else {
//            System.out.println("Printing result to stdout. Use --output to specify output path.");
//            counts.print();
//        }
    }
}
