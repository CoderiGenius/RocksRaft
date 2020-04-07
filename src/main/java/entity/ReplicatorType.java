package entity;

/**
 * Created by 周思成 on  2020/4/6 7:34
 */

public enum ReplicatorType {
    Follower, Learner;

    public final boolean isFollower() {
        return this == Follower;
    }

    public final boolean isLearner() {
        return this == Learner;
    }
}
