package app.entity;

import lombok.Data;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
@Data
public class GridEntity {

    private double intPrice;
    private double grid = intPrice * 0.007;
    private int lastNet = 0;
    private int minNet = -10;
    private int volume = 1000;
    private String fundCode = "878002";

}
