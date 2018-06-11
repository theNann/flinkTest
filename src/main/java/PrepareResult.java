import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import www.pyn.bean.Result;
import www.pyn.tools.Configuration;

import javax.print.attribute.ResolutionSyntax;
import java.util.*;

/**
 * Created by pyn on 2018/4/17.
 */
public class PrepareResult {
    private ExecutionEnvironment env;
    private HashMap<Integer,Result>trainResult;
    private HashMap<Integer,Result> testResult;
    private String[] trainFilePath;
    private String[] testFilePath;
    private static PrepareResult prepareResult = null;

    private PrepareResult(ExecutionEnvironment env) {
        this.env = env;
        this.trainFilePath = Configuration.getInstance().getTrainTargetPath();
        this.testFilePath = Configuration.getInstance().getTestTargetPath();
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

    public HashMap<Integer,Result> readResult(String[] filePath) {
        HashMap<Integer, Result> hashMap = new HashMap<Integer, Result>();
        hashMap.clear();
        for(int idx = 0; idx < filePath.length; idx += 1) {
            DataSet<String> lines = env.readTextFile(filePath[idx]);
            DataSet<Result> ds = lines.flatMap(new FlatMapFunction<String, Result>() {
                public void flatMap(String s, Collector<Result> collector) throws Exception {
                    String[] split = s.split(", ");
                    int dataId = Integer.valueOf(split[0]);
                    List<Integer> visibleObj = new ArrayList<Integer>();
                    for (int i = 1; i < split.length; i++) {
                        visibleObj.add(Integer.valueOf(split[i]));
                    }
                    Collections.sort(visibleObj);
                    Result result = new Result(dataId, visibleObj);
                    collector.collect(result);
                }
            });
            try {
                List<Result> rs = ds.collect();
                for (int i = 0; i < rs.size(); i++) {
                    hashMap.put(rs.get(i).getDataId(), rs.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hashMap;
    }

    public HashMap<Integer,Result> getTrainResult() {
        return trainResult;
    }

    public HashMap<Integer,Result> getTestResult() {
        return testResult;
    }
}
