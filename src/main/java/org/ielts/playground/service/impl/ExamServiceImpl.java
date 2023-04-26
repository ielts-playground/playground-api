package org.ielts.playground.service.impl;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.dto.ExamIdDTO;
import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.ExamAnswer;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.ExamAnswerRetrievalResponse;
import org.ielts.playground.model.response.ResultAllExamIdResponse;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;
import org.ielts.playground.repository.ComponentRepository;
import org.ielts.playground.repository.ExamAnswerRepository;
import org.ielts.playground.repository.ExamEvalRepository;
import org.ielts.playground.repository.ExamTestRepository;
import org.ielts.playground.service.ExamService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {
    // private final ExamServiceImpl self;
    private final ComponentRepository componentRepository;
    private final ExamTestRepository examTestRepository;
    private final ExamAnswerRepository examAnswerRepository;

    private final ExamEvalRepository examEvalRepository;

    public ExamServiceImpl(
            // @Lazy ExamServiceImpl self,
            ComponentRepository componentRepository,
            ExamTestRepository examTestRepository,
            ExamAnswerRepository examAnswerRepository, ExamEvalRepository examEvalRepository) {
        // this.self = self;
        this.componentRepository = componentRepository;
        this.examTestRepository = examTestRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.examEvalRepository = examEvalRepository;
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
        // this.self.saveAnswersAsync(examAnswers);
        this.saveAnswers(examAnswers);
    }

    @Override
    public WritingTestRetrievalResponse retrieveWritingTest(Long examId) {
        final WritingTestRetrievalResponse response = new WritingTestRetrievalResponse();
        final List<ComponentWithPartNumber> componentWithPartNumbers = this.componentRepository
                .findByExamIdAndPartType(examId, PartType.WRITING);
        response.setExamId(examId);
        if (!componentWithPartNumbers.isEmpty()) {
            response.setComponents(componentWithPartNumbers.stream()
                    .map(componentWithPartNumber -> {
                        final Component component = componentWithPartNumber.getComponent();
                        final WritingTestRetrievalResponse.ComponentWithPartNumber toComponent = new WritingTestRetrievalResponse.ComponentWithPartNumber();
                        toComponent.setType(component.getType());
                        toComponent.setKei(component.getKei());
                        toComponent.setValue(component.getValue());
                        toComponent.setSize(component.getSize());
                        toComponent.setOptions(component.getOptions());
                        toComponent.setPartNumber(componentWithPartNumber.getPartNumber());
                        return toComponent;
                    })
                    .collect(Collectors.toList()));
        }
        return response;
    }

    @Override
    public ExamAnswerRetrievalResponse retrieveExamAnswer(Long examId, PartType partType) {
        final ExamAnswerRetrievalResponse response = new ExamAnswerRetrievalResponse();
        response.setExamId(examId);
        response.setSkill(partType.getValue());
        final List<ExamAnswer> examAnswers = this.examAnswerRepository
                .findByExamIdAndPartType(examId, partType);
        final Map<String, String> answers = new HashMap<>();
        for (ExamAnswer examAnswer : examAnswers) {
            answers.put(examAnswer.getKei(), examAnswer.getValue());
        }
        response.setAnswers(answers);
        return response;
    }

    @Override
    public ResultAllExamIdResponse getAllExamNotGraded(Long page, Long size) {

        List<Long> examPage = examEvalRepository.getAllExamIdNotGradedByPage(size, page * size);
        Long allExam = examEvalRepository.getAllExamIdNotGraded();
        ResultAllExamIdResponse resultAllExamIdResponse = ResultAllExamIdResponse.builder()
                .examIds(examPage.stream().map(examId -> new ExamIdDTO(examId)).collect(Collectors.toList()))
                .page(page)
                .size(size)
                .total(allExam)
                .build();
        return resultAllExamIdResponse;
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

    public void saveAnswers(final List<ExamAnswer> examAnswers) {
        this.examAnswerRepository.saveAll(examAnswers);
    }
}
