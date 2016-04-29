package app.repository;

import app.entity.GridTrading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by terry.wu on 2016/4/29 0029.
 */
public interface GridTradingRepository  extends JpaRepository<GridTrading, Long>, JpaSpecificationExecutor {
}
