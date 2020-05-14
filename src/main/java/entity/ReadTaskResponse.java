package entity;

import java.io.Serializable;

/**
 * Created by 周思成 on  2020/5/8 12:39
 */

public class ReadTaskResponse implements Serializable {

    private static final long serialVersionUID = -4427655712599189824L;


    private boolean response;

    private String msg;

    public ReadTaskResponse(boolean response, String msg) {
        this.response = response;
        this.msg = msg;
    }
    public ReadTaskResponse(){

    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
