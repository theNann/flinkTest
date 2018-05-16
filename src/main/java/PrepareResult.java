import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import www.pyn.bean.Result;

import javax.print.attribute.ResolutionSyntax;
import java.util.*;

/**
 * Created by pyn on 2018/4/17.
 */
public class PrepareResult {
    private ExecutionEnvironment env;
    private HashMap<Integer,Result> trainResult;
    private HashMap<Integer,Result> testResult;
    private String trainFilePath = "E:\\BIMRecommed\\input\\target_train.txt";
    private String testFilePath = "E:\\BIMRecommed\\input\\target_test.txt";
//    private String trainFilePath = "/home/pyn/Desktop/BIMRecommed/input/target_train.txt";
//    private String testFilePath = "/home/pyn/Desktop/BIMRecommed/input/target_test.txt";
    private static PrepareResult prepareResult = null;

    private PrepareResult(ExecutionEnvironment env) {
        this.env = env;
        trainResult = new HashMap<Integer, Result>();
        testResult = new HashMap<Integer, Result>();
        readTrainResult();
        readTestResult();
    }

    public static PrepareResult getInstance(ExecutionEnvironment env) {
        if(prepareResult == null) {
            prepareResult = new PrepareResult(env);
        }
        return prepareResult;
    }

    public void readTrainResult() {
        this.trainResult = readResult(trainFilePath);
    }
    public void readTestResult() {
        this.testResult = readResult(testFilePath);
    }

    public HashMap<Integer,Result> readResult(String filePath) {
        DataSet<String> lines = env.readTextFile(filePath);
        DataSet<Result> ds = lines.flatMap(new FlatMapFunction<String, Result>() {
            public void flatMap(String s, Collector<Result> collector) throws Exception {
                String[] split = s.split(", ");
                int dataId = Integer.valueOf(split[0]);
                List<Integer> visibleObj = new ArrayList<Integer>();
                for(int i = 1; i < split.length; i++) {
                    visibleObj.add(Integer.valueOf(split[i]));
                }
                Collections.sort(visibleObj);
                Result result = new Result(dataId, visibleObj);
                collector.collect(result);
            }
        });
        try {
            List<Result> rs = ds.collect();
            HashMap<Integer, Result> hashMap = new HashMap<Integer, Result>();
            for(int i = 0; i < rs.size(); i++) {
                hashMap.put(rs.get(i).getDataId(), rs.get(i));
            }
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Integer,Result> getTrainResult() {
        return trainResult;
    }

    public HashMap<Integer,Result> getTestResult() {
        return testResult;
    }
}
