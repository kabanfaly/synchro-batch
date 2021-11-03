package com.michelin.gst.synchro.writer;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.jpa.repository.JpaRepository;

public class DataWriter<T, R extends JpaRepository> implements ItemWriter<T> {

    private final R dao;
    
    public DataWriter(R dao) {
        this.dao = dao;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        items.stream().forEachOrdered(item -> {          
            dao.save(item);
        });
    }
}
