package app.entity;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class GridTrading {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    private Double price;
    private Date logTime;
    private Integer amount;
    private String type;
    private String fund;
    private Integer lastNet;

    @PrePersist
    void prePersist(){
        this.setLogTime( new Date() );
    }

}