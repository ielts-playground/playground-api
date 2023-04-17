package org.ielts.playground.service.impl;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.model.entity.ExamAnswer;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.repository.ExamAnswerRepository;
import org.ielts.playground.repository.ExamTestRepository;
import org.ielts.playground.service.ExamService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {
    private final ExamServiceImpl self;
    private final ExamTestRepository examTestRepository;
    private final ExamAnswerRepository examAnswerRepository;

    public ExamServiceImpl(
            @Lazy ExamServiceImpl self,
            ExamTestRepository examTestRepository,
            ExamAnswerRepository examAnswerRepository) {
        this.self = self;
        this.examTestRepository = examTestRepository;
        this.examAnswerRepository = examAnswerRepository;
    }

    @Override
    public void submit(ExamSubmissionRequest request) {
        final Long examTestId = request.getExamTestId();
        if (this.examTestRepository.findById(examTestId).isEmpty()) {
            throw new NotFoundException(ValidationConstants.EXAM_TEST_NOT_FOUND);
        }
        if (this.examAnswerRepository.existsByExamPartId(examTestId)) {
            throw new BadRequestException(ValidationConstants.EXAM_TEST_ALREADY_ANSWERED);
        }

        final List<ExamAnswer> examAnswers = new ArrayList<>();
        request.getAnswers().forEach((key, value) -> {
            examAnswers.add(ExamAnswer.builder()
                    .examTestId(examTestId)
                    .examPartId(-1L) // ignored
                    .kei(key)
                    .value(value)
                    .build());
        });
        this.self.saveAnswersAsync(examAnswers);
    }

    /**
     * Persists the exam's answers asynchronously.
     *
     * @param examAnswers the exam's answers.
     */
    @Async
    public void saveAnswersAsync(final List<ExamAnswer> examAnswers) {
        this.examAnswerRepository.saveAll(examAnswers);
    }
}
