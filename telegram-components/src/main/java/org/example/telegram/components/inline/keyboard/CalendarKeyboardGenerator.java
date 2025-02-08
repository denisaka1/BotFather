package org.example.telegram.components.inline.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarKeyboardGenerator {

    public static InlineKeyboardMarkup generateCalendar(int year, int month) {
        LocalDate today = LocalDate.now();
        LocalDate lastAllowedDate = today.plusMonths(1);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(InlineKeyboardButton.builder().text("📅 " + getMonthName(month) + " " + year).callbackData("noop").build()));

        String[] weekDays = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        List<InlineKeyboardButton> daysRow = new ArrayList<>();
        for (String day : weekDays) {
            daysRow.add(InlineKeyboardButton.builder().text(day).callbackData("noop").build());
        }
        keyboard.add(daysRow);

        List<InlineKeyboardButton> row = new ArrayList<>();
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < dayOfWeek; i++) {
            row.add(InlineKeyboardButton.builder().text(" ").callbackData("noop").build());
        }

        boolean hasNextMonthDates = false;
        boolean hasPreviousMonthDates = false;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);

            if (currentDate.isBefore(today)) {
                row.add(InlineKeyboardButton.builder().text("❌").callbackData("noop").build());
            } else if (!currentDate.isAfter(lastAllowedDate)) {
                row.add(InlineKeyboardButton.builder()
                        .text(String.valueOf(day))
                        .callbackData("date:" + String.format("%02d/%02d/%04d", day, month, year))
                        .build());
            } else {
                row.add(InlineKeyboardButton.builder().text("❌").callbackData("noop").build());
            }

            if (row.size() == 7) {
                keyboard.add(row);
                row = new ArrayList<>();
            }

            if (currentDate.getMonthValue() > month && currentDate.isBefore(lastAllowedDate)) {
                hasNextMonthDates = true;
            }
        }

        if (!row.isEmpty()) {
            while (row.size() < 7) {
                row.add(InlineKeyboardButton.builder().text(" ").callbackData("noop").build());
            }
            keyboard.add(row);
        }

        List<InlineKeyboardButton> navRow = new ArrayList<>();

        if (YearMonth.from(today).isBefore(yearMonth)) {
            hasPreviousMonthDates = true;
        }

        if (YearMonth.from(lastAllowedDate).isAfter(yearMonth)) {
            hasNextMonthDates = true;
        }

        if (hasPreviousMonthDates) {
            int prevMonth = month == 1 ? 12 : month - 1;
            int prevYear = month == 1 ? year - 1 : year;
            navRow.add(InlineKeyboardButton.builder()
                    .text("⬅️ Previous")
                    .callbackData("month:" + prevYear + "-" + prevMonth)
                    .build());
        }

        navRow.add(InlineKeyboardButton.builder().text("<< Back To Menu").callbackData("BACK").build());

        if (hasNextMonthDates) {
            int nextMonth = month == 12 ? 1 : month + 1;
            int nextYear = month == 12 ? year + 1 : year;
            navRow.add(InlineKeyboardButton.builder()
                    .text("Next ➡️")
                    .callbackData("month:" + nextYear + "-" + nextMonth)
                    .build());
        }

        keyboard.add(navRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private static String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}

