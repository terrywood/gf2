package app.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


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
    @Column(nullable = false)
    private boolean trading;

}
