package org.example.data.layer.helpers;

import org.example.data.layer.entities.TimeRange;
import org.example.data.layer.entities.WorkingHours;

public final class WorkingHoursHelper {
    private WorkingHoursHelper() {

    }

    public static String info(WorkingHours workingHours) {
        if (workingHours.getTimeRanges().isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder(workingHours.getDay() + " ");
        for (TimeRange timeRange : workingHours.getTimeRanges()) {
            stringBuilder.append(timeRange.getStartTime()).append(" - ").append(timeRange.getEndTime()).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2); // remove last ", "

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}
