package entity;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by 周思成 on  2020/3/30 21:07
 * yaml options entity
 */

public class Options {

        private CurrentNodeOptions currentNodeOptions;


    private OtherNodes[] otherNodes;


    public CurrentNodeOptions getCurrentNodeOptions() {
        return currentNodeOptions;
    }

    public void setCurrentNodeOptions(CurrentNodeOptions currentNodeOptions) {
        this.currentNodeOptions = currentNodeOptions;
    }

    public OtherNodes[] getOtherNodes() {
        return otherNodes;
    }

    public void setOtherNodes(OtherNodes[] otherNodes) {
        this.otherNodes = otherNodes;
    }

    @Override
    public String toString() {
        return "Options{" +
                "currentNodeOptions=" + currentNodeOptions +
                ", otherNodes=" + Arrays.toString(otherNodes) +
                '}';
    }
}

