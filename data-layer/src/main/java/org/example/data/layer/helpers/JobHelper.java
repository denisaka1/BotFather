package org.example.data.layer.helpers;

import org.example.data.layer.entities.Job;

public final class JobHelper {
    private JobHelper() {

    }

    public static String info(Job job) {
        return job.getType() + " duration: " + job.getDuration() + "h";
    }
}
