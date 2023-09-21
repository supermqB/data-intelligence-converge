package com.lrhealth.data.converge.scheduled.model;

import lombok.Data;

import java.util.Objects;

/**
 * @author zhaohui
 * @version 1.0
 */
@Data
public class FileTask {

    private Integer taskId;

    private String fileName;

    public FileTask(Integer taskId, String fileName) {
        this.taskId = taskId;
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileTask fileTask = (FileTask) o;
        return Objects.equals(taskId, fileTask.taskId) && Objects.equals(fileName, fileTask.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, fileName);
    }
}
