package com.example.demo.domain.model;

import com.example.demo.domain.enums.RepeatType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleRepeatRule {

    private final RepeatType type;
    private final Integer interval;
    private final List<Integer> daysOfWeek;
    private final Integer monthDay;
    private final LocalDate endDate;

    public ScheduleRepeatRule(RepeatType type,
                              Integer interval,
                              List<Integer> daysOfWeek,
                              Integer monthDay,
                              LocalDate endDate) {
        this.type = type;
        this.interval = interval;
        this.daysOfWeek = daysOfWeek;
        this.monthDay = monthDay;
        this.endDate = endDate;
    }

    public RepeatType getType() {
        return type;
    }

    public Integer getInterval() {
        return interval;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public Integer getMonthDay() {
        return monthDay;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String toSummary() {
        if (type == null || type == RepeatType.NONE) {
            return "반복 없음";
        }
        int intervalValue = interval != null && interval > 0 ? interval : 1;

        return switch (type) {
            case DAILY -> intervalValue == 1 ? "매일" : intervalValue + "일 간격";
            case WEEKLY -> buildWeeklySummary(intervalValue);
            case MONTHLY -> buildMonthlySummary(intervalValue);
            case YEARLY -> buildYearlySummary();
            default -> "반복 없음";
        };
    }

    private String buildWeeklySummary(int intervalValue) {
        StringBuilder sb = new StringBuilder();
        if (intervalValue == 1) {
            sb.append("매주");
        } else {
            sb.append(intervalValue).append("주 간격");
        }
        if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
            sb.append(" · ");
            sb.append(daysOfWeek.stream()
                    .map(this::koreanDayOfWeek)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        return sb.toString();
    }

    private String buildMonthlySummary(int intervalValue) {
        if (monthDay != null) {
            if (intervalValue == 1) {
                return "매월 " + monthDay + "일";
            }
            return intervalValue + "개월 간격 · " + monthDay + "일";
        }
        return "매월";
    }

    private String buildYearlySummary() {
        return "매년";
    }

    private String koreanDayOfWeek(int zeroBasedDay) {
        // 0=일요일, 1=월요일, ...
        DayOfWeek dayOfWeek = DayOfWeek.of(((zeroBasedDay + 6) % 7) + 1);
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    public static ScheduleRepeatRule fromEntityFields(
            String repeatType,
            Integer repeatInterval,
            Integer[] repeatDays,
            Integer repeatMonthDay,
            LocalDateTime repeatEndDate
    ) {
        RepeatType typeEnum = repeatType == null ? RepeatType.NONE : RepeatType.valueOf(repeatType);
        List<Integer> days = repeatDays == null ? null : List.of(repeatDays);
        LocalDate end = repeatEndDate == null ? null : repeatEndDate.toLocalDate();
        return new ScheduleRepeatRule(typeEnum, repeatInterval, days, repeatMonthDay, end);
    }
}


