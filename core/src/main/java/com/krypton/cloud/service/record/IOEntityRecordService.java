package com.krypton.cloud.service.record;

public interface IOEntityRecordService<T> extends RecordService<T> {

    T getByPath(String path);

}
