/**
 * Created by pyn on 2018/4/3.
 */


import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import www.pyn.bean.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO 添加direction，测试输入数据的正确性（训练集和测试集）(已完成)
//TODO 从target.txt文件中读取训练集和测试集的结果，建议新建一个类PrepareResult来操作（已完成）
//TODO 之后添加一个工具包，计算两个向量之前的相似度，以及两个集合之间的相似度(已完成)
//TODO 调试为什么knn-8中选取5个邻居的acc和recall与python版本结果不同，猜测是因为position应该改成欧几里得距离而不是cosine（已完成）
//TODO 添加推荐算法(已完成)
//TODO 加快效率，要能做到每秒30帧（每个测试数据30ms得出结果）（已完成，协同过滤每秒计算79次，KNN结合协同过滤每秒计算27次）:
//TODO 1）KNN和推荐时可用小顶堆维护求TopK问题(尚无明显效果，当前训练集数目不多，排序也挺快) 2）集合运算优化，使用Hashmap求交并 3)用List代替Set存储结果集，大大提高了效率
//TODO 实现OcclusionCulling与FLink的socket帧数据的通信（已完成）
//TODO 给每帧数据编号，Flink这边能按编号顺序发送结果，並且能在OcclusionCulling中顯示（已完成，使用writeToSocket API）
//TODO 增加更多的训练和测试数据，着重提高recall,
//TODO 修改求集合的相似度，利用AUB = A+B-AnB计算试试(提高了),同时看getNearestNeighbor有没有可改进的(无改进)
@SuppressWarnings("serial")
public class PrepareData {
    private ExecutionEnvironment env;
    private HashMap<Integer, Position> trainPosition;
    private HashMap<Integer, Direction> trainDirection;
    private GridData[][][] trainData;
    private DataSet<PrimitiveData> testDataDS;
    private HashMap<Integer, PrimitiveData> testData;
    private String trainFilePath;
    private String testFilePath;
    private static PrepareData prepareData = null;
    private PrepareData(ExecutionEnvironment env, String trainFilePath, String testFilePath) {
        this.env  = env;
        this.trainFilePath = trainFilePath;
        this.testFilePath = testFilePath;
        trainPosition = new HashMap<Integer, Position>();
        trainDirection = new HashMap<Integer, Direction>();
        testData = new HashMap<Integer, PrimitiveData>();
        trainData = new GridData[SceneInfo.xGridNumber][SceneInfo.yGridNumber][SceneInfo.zGridNumber];
        for(int i = 0; i < SceneInfo.xGridNumber; i++) {
            for(int j = 0; j < SceneInfo.yGridNumber; j++) {
                for(int k = 0; k < SceneInfo.zGridNumber; k++) {
                    trainData[i][j][k] = new GridData();
                }
            }
        }
        readTrainPosition();
        readTrainDirection();
        readTestData();
        getTrainData();
    }
    public void getTrainData() {
        for(int i = 0; i < trainPosition.size(); i++) {
            Position position = trainPosition.get(i);
            Direction direction = trainDirection.get(i);
            int x = position.getPx()
        }
    }

    public static PrepareData getInstance(ExecutionEnvironment env, String trainFilePath, String testFilePath) {
        if(prepareData == null) {
            prepareData = new PrepareData(env, trainFilePath, testFilePath);
        } else {
            return prepareData;
        }
        return prepareData;
    }

    public void readTrainPosition() {
        DataSet<Position> trainPositionDataSet = env.readCsvFile(trainFilePath)
                .includeFields("1111000000")
                .pojoType(Position.class, "dataId", "px", "py", "pz");

        try {
            List<Position> list = trainPositionDataSet.collect();
            trainPosition.clear();
            for(int i = 0; i < list.size(); i++) {
                int dataId = list.get(i).getDataId();
                Position position = list.get(i);
                trainPosition.put(dataId, position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("PrepareData trainPosition size : " + trainPosition.size());
    }

    public void readTrainDirection() {
        DataSet<Direction> trainDirectionDataSet = env.readCsvFile(trainFilePath)
                .includeFields("1000111000")
                .pojoType(Direction.class, "dataId", "dx", "dy", "dz");
        try {
            List<Direction> list = trainDirectionDataSet.collect();
            trainDirection.clear();
            for(int i = 0; i < list.size(); i++) {
                int dataId = list.get(i).getDataId();
                trainDirection.put(dataId, list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readTestData() {
//        System.out.println("PrepareData_readTestPosition!");
        this.testDataDS = env.readCsvFile(testFilePath)
                .includeFields("1111111000")
                .pojoType(PrimitiveData.class, "dataId", "px", "py", "pz", "dx", "dy", "dz");

        try {
            List<PrimitiveData> list = testDataDS.collect();
            System.out.println("tesetData Collect!!!!!!!!!!!!!!!!!!!!!!!!!");
            testData.clear();
            for(int i = 0; i < list.size(); i++) {
                int dataId = list.get(i).getDataId();
                testData.put(dataId, list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ExecutionEnvironment getEnv() {
        return env;
    }

    public HashMap<Integer, Position> getTrainPosition() {
        return trainPosition;
    }

    public HashMap<Integer, Direction> getTrainDirection() {
        return trainDirection;
    }

    public DataSet<PrimitiveData> getTestDataDS() {
        return testDataDS;
    }

    public String getTrainFilePath() {
        return trainFilePath;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public void setTrainFilePath(String filePath) {
        trainFilePath = filePath;
    }

    public void setTestFilePath(String filePath) {
        testFilePath = filePath;
    }

    public HashMap<Integer, PrimitiveData> getTestData() {
        return testData;
    }

}
