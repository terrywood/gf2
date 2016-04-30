package app.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
@Data
@Entity
public class GridEntity {
    @Id
    private String fundCode;
    private double intPrice;
    private double grid;
    private int position;
    private int minNet;
    private int volume;

}
