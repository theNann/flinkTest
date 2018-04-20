package www.pyn.bean;

import java.util.List;
import java.util.Set;

/**
 * Created by pyn on 2018/4/18.
 */
public class Result {
    private int dataId;
    private Set<Integer> visibleObj;
    public Result(int dataId, Set<Integer> visibleObj) {
        this.dataId = dataId;
        this.visibleObj = visibleObj;
    }

    public int getDataId() {
        return dataId;
    }

    public Set<Integer> getVisibleObj() {
        return visibleObj;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public void setVisibleObj(Set<Integer> visibleObj) {
        this.visibleObj = visibleObj;
    }

    @Override
    public String toString() {
        return "Result{" +
                "dataId=" + dataId +
                ", visibleObj=" + visibleObj +
                '}';
    }
}
