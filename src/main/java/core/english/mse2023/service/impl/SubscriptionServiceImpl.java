package core.english.mse2023.service.impl;

import core.english.mse2023.dto.SubscriptionCreationDTO;
import core.english.mse2023.model.Lesson;
import core.english.mse2023.model.Subscription;
import core.english.mse2023.model.dictionary.LessonStatus;
import core.english.mse2023.model.dictionary.SubscriptionStatus;
import core.english.mse2023.repository.LessonRepository;
import core.english.mse2023.repository.SubscriptionRepository;
import core.english.mse2023.repository.UserRepository;
import core.english.mse2023.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String LESSON_TOPIC_TEMPLATE = "Урок №%s";

    private final SubscriptionRepository subscriptionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void createSubscription(SubscriptionCreationDTO creationDTO) {

        Subscription subscription = new Subscription();

        subscription.setStartDate(creationDTO.getStartDate());
        subscription.setEndDate(creationDTO.getEndDate());

        subscription.setType(creationDTO.getType());

        if (subscription.getStartDate().after(new Date())) {
            subscription.setStatus(SubscriptionStatus.NOT_YET_STARTED);
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }

        subscription.setStudent(userRepository.findByTelegramId(creationDTO.getStudentTelegramId()));
        subscription.setTeacher(userRepository.findByTelegramId(creationDTO.getTeacherTelegramId()));
        subscription.setLessonsRest(creationDTO.getLessonsRest());

        List<Lesson> lessons = new ArrayList<>();

        for (int i = 0; i < creationDTO.getLessonsRest(); i++) {
            lessons.add(createLesson(subscription, String.format(LESSON_TOPIC_TEMPLATE, (i + 1))));
        }

        lessons.get(0).setDate(subscription.getStartDate());

        subscriptionRepository.save(subscription);

        lessonRepository.saveAll(lessons);

    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public List<Lesson> getAllLessonsForSubscription(UUID subscriptionId) {
        return lessonRepository.getAllBySubscriptionId(subscriptionId);
    }

    @Override
    public Lesson createLesson(Subscription subscription, String topic) {
        Lesson lesson = new Lesson();

        lesson.setStatus(LessonStatus.NOT_STARTED_YET);
        lesson.setSubscription(subscription);
        lesson.setTopic(topic);

        return lesson;
    }

    @Override
    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.getSubscriptionsById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);

        List<Lesson> lessons = lessonRepository.getAllBySubscriptionId(subscriptionId);

        for (Lesson lesson : lessons) {
            lesson.setStatus(LessonStatus.CANCELLED);
        }

        subscriptionRepository.save(subscription);
        lessonRepository.saveAll(lessons);
    }

}
