package org.example.bots.manager.constants;

import java.util.List;

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
    public static final List<String> EDIT_BOTS_CALLBACKS = List.of(
            EDIT_BOT_NAME, EDIT_BOT_WORKING_HOURS, EDIT_BOT_TOKEN,
            EDIT_BOT_WELCOME_MESSAGE, EDIT_BOT_JOBS
    );

    public static final String DELIMITER_SCHEDULE_STATE = "&";
    public static final String DELIMITER_SCHEDULE_STATE_DATES = "@";
    public static final String SELECT_APPOINTMENTS_BOT = "select_appointment_bot" + DELIMITER_SCHEDULE_STATE;
    public static final String BACK_TO_APPOINTMENTS_BOT = "back_to_appointments_bot" + DELIMITER_SCHEDULE_STATE;
    public static final String SELECT_DATE = "select_date" + DELIMITER_SCHEDULE_STATE;
    public static final String UPDATE_DATE = "update_date" + DELIMITER_SCHEDULE_STATE;
    public static final String BACK_TO_DATES = "back_to_dates" + DELIMITER_SCHEDULE_STATE;
    public static final String SELECT_APPOINTMENT = "select_appointment" + DELIMITER_SCHEDULE_STATE;
    public static final String BACK_TO_APPOINTMENTS = "back_to_appointments" + DELIMITER_SCHEDULE_STATE;
    public static final String CONFIRM_APPOINTMENT = "confirm_appointment" + DELIMITER_SCHEDULE_STATE;
    public static final String CANCEL_APPOINTMENT = "cancel_appointment" + DELIMITER_SCHEDULE_STATE;
    public static final String DISPLAY_CLIENT_DETAILS = "display_client_details" + DELIMITER_SCHEDULE_STATE;

    public static final List<String> SCHEDULE_CALLBACKS = List.of(
            SELECT_APPOINTMENTS_BOT, BACK_TO_APPOINTMENTS_BOT, SELECT_DATE,
            BACK_TO_DATES, SELECT_APPOINTMENT, BACK_TO_APPOINTMENTS,
            CONFIRM_APPOINTMENT, CANCEL_APPOINTMENT, UPDATE_DATE, DISPLAY_CLIENT_DETAILS
    );
}
