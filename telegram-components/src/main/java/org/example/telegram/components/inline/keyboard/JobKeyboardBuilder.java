package org.example.telegram.components.inline.keyboard;
import org.example.data.layer.entities.Job;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.ArrayList;
import java.util.List;

public class JobKeyboardBuilder {

    public static InlineKeyboardMarkup createJobSelectionKeyboard(List<Job> jobs, String selectedDate, String selectedTime) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Job job : jobs) {
            InlineKeyboardButton jobButton = InlineKeyboardButton.builder()
                    .text(job.getType() + " (" + job.getDuration() + "h)")
                    .callbackData("jobSelected@" + job.getId() + ":" + job.getType() + ":" + job.getDuration() + "@" + selectedDate + "@" + selectedTime)
                    .build();

            keyboard.add(List.of(jobButton));
        }

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("<< Back To Time Selection")
                .callbackData("backToTimeSelection@" + selectedDate)
                .build();

        keyboard.add(List.of(backButton));

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}

