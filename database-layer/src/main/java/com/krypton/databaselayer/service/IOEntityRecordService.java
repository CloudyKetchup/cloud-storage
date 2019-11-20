package com.krypton.databaselayer.service;

import java.util.List;

public interface IOEntityRecordService<T> extends RecordService<T> {

    List<T> findAll();

    T getByPath(String path);

    boolean delete(String path);

    boolean exists(String path);

}
