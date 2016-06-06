package app.repository;

import app.entity.GridEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
public interface GridEntityRepository extends JpaRepository<GridEntity, String>, JpaSpecificationExecutor {
    List<GridEntity> findByTrading(boolean trading);
}
