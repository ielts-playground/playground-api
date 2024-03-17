package org.ielts.playground.service.impl;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.common.exception.UnauthorizedRequestException;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.dto.ExamIdDTO;
import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.Exam;
import org.ielts.playground.model.entity.ExamAnswer;
import org.ielts.playground.model.entity.ExamEval;
import org.ielts.playground.model.entity.ExamTest;
import org.ielts.playground.model.entity.User;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.ExamAnswerRetrievalResponse;
import org.ielts.playground.model.response.ExamFinalResultResponse;
import org.ielts.playground.model.response.ResultAllExamIdResponse;
import org.ielts.playground.model.response.UserInfoResponse;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;
import org.ielts.playground.repository.ComponentRepository;
import org.ielts.playground.repository.ExamAnswerRepository;
import org.ielts.playground.repository.ExamEvalRepository;
import org.ielts.playground.repository.ExamRepository;
import org.ielts.playground.repository.ExamTestRepository;
import org.ielts.playground.repository.UserRepository;
import org.ielts.playground.service.ExamService;
import org.ielts.playground.utils.SecurityUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {
    // private final ExamServiceImpl self;
    private final SecurityUtils securityUtils;

    private final ComponentRepository componentRepository;
    private final ExamRepository examRepository;
    private final ExamTestRepository examTestRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamEvalRepository examEvalRepository;
    private final UserRepository userRepository;

    public ExamServiceImpl(
            // @Lazy ExamServiceImpl self,
            SecurityUtils securityUtils,
            ComponentRepository componentRepository,
            ExamRepository examRepository,
            ExamTestRepository examTestRepository,
            ExamAnswerRepository examAnswerRepository,
            ExamEvalRepository examEvalRepository,
            UserRepository userRepository) {
        // this.self = self;
        this.securityUtils = securityUtils;
        this.componentRepository = componentRepository;
        this.examRepository = examRepository;
        this.examTestRepository = examTestRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.examEvalRepository = examEvalRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void submit(ExamSubmissionRequest request) {
        final Long examTestId = request.getExamTestId();
        final ExamTest examTest = this.examTestRepository.findById(examTestId)
                .orElseThrow(() -> new NotFoundException(ValidationConstants.EXAM_TEST_NOT_FOUND));
        final boolean isSubmitter = this.examRepository.findById(examTest.getExamId())
                .map(Exam::getUserId)
                .map(userId -> Objects.equals(userId, this.securityUtils.getLoggedUserId()))
                .orElse(false);
        if (!isSubmitter) {
            throw new UnauthorizedRequestException(ValidationConstants.UNAUTHORIZED);
        }
        if (this.examAnswerRepository.existsByExamPartId(examTestId)) {
            throw new BadRequestException(ValidationConstants.EXAM_TEST_ALREADY_ANSWERED);
        }
        final PartType skill = PartType.of(request.getSkill());
        final List<ExamAnswer> examAnswers = new ArrayList<>();
        request.getAnswers().forEach((key, value) -> {
            examAnswers.add(ExamAnswer.builder()
                    .examTestId(examTestId)
                    .skill(skill)
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

        List<Tuple> examPage = examEvalRepository.getAllExamIdNotGradedByPage(size, page * size);

        Long allExam = examEvalRepository.getAllExamIdNotGraded();
        ResultAllExamIdResponse resultAllExamIdResponse = ResultAllExamIdResponse.builder()
                .examIds(examPage.stream().map(e-> new ExamIdDTO(
                        e.get(0, BigInteger.class).longValue(),
                        e.get(1, String.class),
                        e.get(2, String.class),
                        e.get(3, String.class),
                        e.get(4, Timestamp.class).toString()
                )).collect(Collectors.toList()))
                .page(page)
                .size(size)
                .total(allExam)
                .build();
        return resultAllExamIdResponse;
    }

    @Override
    public ExamFinalResultResponse retrieveFinalResult(Long examId) {
        final ExamEval examEval = this.examEvalRepository.findByExamId(examId)
                .orElseThrow(NotFoundException::new);
        final User examinee = this.userRepository.findByExamId(examId)
                .orElseThrow(NotFoundException::new);
        final String examiner = this.userRepository.findById(examEval.getCreatedBy())
                .map(User::getUsername)
                .orElse(null);
        return ExamFinalResultResponse.builder()
                .examId(examId)
                .readingCorrectAnswers(examEval.getReadingPoint())
                .listeningCorrectAnswers(examEval.getListeningPoint())
                .writingEvaluation(examEval.getWritingPoint())
                .examiner(examiner)
                .examinee(UserInfoResponse.builder()
                        .username(examinee.getUsername())
                        .email(examinee.getEmail())
                        .firstName(examinee.getFirstName())
                        .lastName(examinee.getLastName())
                        .phoneNumber(examinee.getPhoneNumber())
                        .build())
                .build();
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
