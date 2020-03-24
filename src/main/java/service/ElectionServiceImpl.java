package service;

import exceptions.ElectionException;

import java.util.concurrent.Callable;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public class ElectionServiceImpl implements ElectionService , Callable  {
    @Override
    public boolean startElection() {

    }

    @Override
    public Object call() {
        return startElection();
    }
}
