package app.entity;

import lombok.Data;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */

@Data
@Entity
public class DailyEntity implements Serializable{

    @Id
    private String id;

    private String code;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name=" content", columnDefinition="mediumtext", nullable=true)
    private String content;

    @LastModifiedDate
    private Date lastUpdateTime;

    @PrePersist
    void prePersist(){
        this.setLastUpdateTime( new Date() );
    }

    @PreUpdate
    void preUpdate(){
        this.setLastUpdateTime( new Date() );
    }

}
