package com.krypton.databaselayer.service;

public interface IOEntityRecordService<T> extends RecordService<T> {

    T getByPath(String path);

}
