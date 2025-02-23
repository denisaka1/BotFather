package org.example.bots.manager.constants;

import java.util.ArrayList;

public final class Callback {
    private Callback() {
    }

    public static final String SELECT_BOT = "select_bot_";
    public static final String EDIT_BOT_NAME = "edit_bot_name_";
    public static final String EDIT_BOT_WORKING_HOURS = "edit_bot_working_hours_";
    public static final String EDIT_BOT_TOKEN = "edit_bot_token_";
    public static final String EDIT_BOT_WELCOME_MESSAGE = "edit_bot_welcome_message_";
    public static final String EDIT_BOT_JOBS = "edit_bot_jobs";
    public static final String DELETE_BOT = "delete_bot_";
    public static final String BACK_TO_BOTS_LIST = "back_to_bots_list";

    public static final String SELECT_APPOINTMENTS_BOT = "select_appointment_bot_";
    public static final String BACK_TO_APPOINTMENTS_BOT = "back_to_appointments_bot_";
    public static final String SELECT_DATE = "select_date_";
    public static final String BACK_TO_DATES = "back_to_dates_";
    public static final String SELECT_APPOINTMENT = "select_appointment_";
    public static final String BACK_TO_APPOINTMENTS = "back_to_appointments_";
    public static final String CONFIRM_APPOINTMENT = "confirm_appointment_";
    public static final String CANCEL_APPOINTMENT = "cancel_appointment_";

    public static final ArrayList<String> SCHEDULE_CALLBACKS = new ArrayList<>() {{
        add(SELECT_APPOINTMENTS_BOT);
        add(BACK_TO_APPOINTMENTS_BOT);
        add(SELECT_DATE);
        add(BACK_TO_DATES);
        add(SELECT_APPOINTMENT);
        add(BACK_TO_APPOINTMENTS);
        add(CONFIRM_APPOINTMENT);
        add(CANCEL_APPOINTMENT);
    }};

}
