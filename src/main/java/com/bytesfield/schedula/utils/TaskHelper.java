package com.bytesfield.schedula.utils;

import com.bytesfield.schedula.models.enums.TaskType;
import org.quartz.CronExpression;

public class TaskHelper {
    private TaskHelper() {
    }

    public static Boolean isCronTask(TaskType type) {
        return "CRON".equalsIgnoreCase(String.valueOf(type));
    }

    public static boolean isValidCron(String expression) {
        return CronExpression.isValidExpression(expression);
    }

    public static Boolean isTimestampTask(TaskType type) {
        return "TIMESTAMP".equalsIgnoreCase(String.valueOf(type));
    }
}
