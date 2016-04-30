package app.service;

import app.entity.GridEntity;
import app.repository.GridEntityRepository;
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
    @Autowired
    private GridEntityRepository gridEntityRepository;

    @Cacheable("grid")
    public List<GridEntity> findAll() {
        return gridEntityRepository.findAll();
    }

    @CacheEvict(value = "grid", allEntries = true)
    public void save(GridEntity obj) {
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
