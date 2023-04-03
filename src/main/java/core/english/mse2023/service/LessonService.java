package core.english.mse2023.service;

import core.english.mse2023.model.Lesson;
import core.english.mse2023.model.Subscription;
import core.english.mse2023.model.dictionary.AttendanceType;
import core.english.mse2023.model.dictionary.LessonStatus;

import java.util.List;
import java.util.UUID;

public interface LessonService {

    Lesson getLessonById(UUID id);

    void cancelLessonsFromSubscription(UUID subscriptionId);

    List<Lesson> getAllLessonsForSubscription(UUID subscriptionId);

    void createBaseLessonsForSubscription(Subscription subscription);

    Lesson createLesson(Subscription subscription, String topic);

    void setAttendance(UUID lessonId, AttendanceType attendanceType);
    LessonStatus cancelLesson(UUID lessonId);
}