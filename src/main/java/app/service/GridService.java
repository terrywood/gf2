package app.service;

import app.entity.GridEntity;
import app.repository.GridEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class GridService {
    private static final Logger log = LoggerFactory.getLogger(GridService.class);
    @Autowired
    private GridEntityRepository gridEntityRepository;

    @Cacheable("gridCache")
    public List<GridEntity> findAll() {
        log.info("GridService find all");
        return gridEntityRepository.findByTrading(true);
    }

    @CacheEvict(value = "gridCache", allEntries = true)
    public void save(GridEntity obj) {
        log.info("GridService save GridEntity");
        this.gridEntityRepository.save(obj);
    }

    private void simulateSlowService() {
        try {
            long time = 5000L;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
