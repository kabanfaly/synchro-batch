package com.michelin.synchro.batch;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.jpa.repository.JpaRepository;

public class DataWriter<T, R extends JpaRepository> implements ItemWriter<T> {

    private final R repository;
    
    public DataWriter(R repository) {
        this.repository = repository;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        items.stream().forEachOrdered(item -> {
            repository.save(item);
        });
    }
}
