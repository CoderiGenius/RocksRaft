package service;

import core.NodeImpl;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public interface ElectionService {

    public boolean startElection();

    /**
     * 检查是否启动election
     */
    public static void checkToStartElection(){

        if ( ! NodeImpl.getNodeImple().checkNodeStatePreCandidate()) {
            ElectionService electionService = new ElectionServiceImpl();
            electionService.startElection();
        }
    }

    public void startPrevote();
}
