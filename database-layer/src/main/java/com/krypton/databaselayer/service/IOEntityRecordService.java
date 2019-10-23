package com.krypton.databaselayer.service;

public interface IOEntityRecordService<T> extends RecordService<T> {

    T getByPath(String path);

    boolean delete(String path);

    boolean exists(String path);

}
