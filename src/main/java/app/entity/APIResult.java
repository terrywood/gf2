package app.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by terry.wu on 2016/5/3 0003.
 */
@Data
public class APIResult {
    int total;
    boolean success;
    String error_no;
    String error_info;
    List<APIData> data;
}
