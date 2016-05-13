package app.repository;

import app.entity.DailyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
public interface DailyEntityRepository extends JpaRepository<DailyEntity, String>, JpaSpecificationExecutor {

    @Modifying
    @Query("update DailyEntity t set t.content = ? ,t.lastUpdateTime = now() where t.id = ? ")
    public void updateContent( String content,String id);
}
