package org.ielts.playground.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.ComponentPosition;
import org.ielts.playground.common.enumeration.ComponentType;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.InternalServerException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.dto.UserAnswerAndTrueAnswerDto;
import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.Exam;
import org.ielts.playground.model.entity.ExamTest;
import org.ielts.playground.model.entity.Part;
import org.ielts.playground.model.entity.PartAnswer;
import org.ielts.playground.model.entity.Test;
import org.ielts.playground.model.entity.TestAudio;
import org.ielts.playground.model.entity.type.Range;
import org.ielts.playground.model.entity.type.Raw;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.ComponentDataResponse;
import org.ielts.playground.model.response.DisplayAllDataResponse;
import org.ielts.playground.model.response.DisplayQuestionDataResponse;
import org.ielts.playground.model.response.OptionResponse;
import org.ielts.playground.model.response.TestCreationResponse;
import org.ielts.playground.repository.ComponentRepository;
import org.ielts.playground.repository.ComponentWriteRepository;
import org.ielts.playground.repository.ExamTestRepository;
import org.ielts.playground.repository.ExamRepository;
import org.ielts.playground.repository.PartAnswerRepository;
import org.ielts.playground.repository.PartRepository;
import org.ielts.playground.repository.TestAudioRepository;
import org.ielts.playground.repository.TestRepository;
import org.ielts.playground.service.TestService;
import org.ielts.playground.utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.validation.constraints.NotNull;
import java.util.Random;
import java.util.stream.Stream;

@Service
public class TestServiceImpl implements TestService {
    private final TestRepository testRepository;
    private final PartRepository partRepository;
    private final ComponentWriteRepository componentWriteRepository;
    private final ComponentRepository componentRepository;
    private final PartAnswerRepository partAnswerRepository;
    private final TestAudioRepository testAudioRepository;
    private final ExamRepository examRepository;
    private final ExamTestRepository examTestRepository;
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    public TestServiceImpl(
            TestRepository testRepository,
            PartRepository partRepository,
            ComponentWriteRepository componentWriteRepository,
            ComponentRepository componentRepository, PartAnswerRepository partAnswerRepository,
            TestAudioRepository testAudioRepository,
            ExamRepository examRepository, ExamTestRepository examTestRepository, ModelMapper modelMapper, SecurityUtils securityUtils) {
        this.testRepository = testRepository;
        this.partRepository = partRepository;
        this.componentWriteRepository = componentWriteRepository;
        this.componentRepository = componentRepository;
        this.partAnswerRepository = partAnswerRepository;
        this.testAudioRepository = testAudioRepository;
        this.examRepository = examRepository;
        this.examTestRepository = examTestRepository;
        this.modelMapper = modelMapper;
        this.securityUtils = securityUtils;
    }

    @Transactional
    @Override
    public TestCreationResponse create(TestCreationRequest request) {
        final PartType type = PartType.of(request.getSkill());
        if (Objects.isNull(type)) {
            throw new BadRequestException(ValidationConstants.PART_TYPE_INVALID);
        }

        final MultipartFile audio = request.getAudio();
        if (PartType.LISTENING.equals(type) && Objects.isNull(audio)) {
            throw new BadRequestException(ValidationConstants.AUDIO_MISSING);
        }

        final Long testId = this.testRepository.save(Test.builder()
                .createdBy(this.securityUtils.getLoggedUserId())
                .active(Boolean.TRUE)
                .build()).getId();

        final Map<Long, Long> partIds = this.createComponents(testId, type, request.getComponents());
        this.createAnswers(partIds, request.getAnswers());
        if (!Objects.isNull(audio)) {
            this.saveAudio(testId, audio);
        }

        return TestCreationResponse.builder()
                .id(testId)
                .build();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Map<Long, Long> createComponents(
            @NotNull final Long testId,
            @NotNull final PartType type,
            @NotNull List<TestCreationRequest.PartComponent> partComponents) {
        final Map<Long, Long> partIds = new HashMap<>();
        final Map<Long, List<Component>> parts = new HashMap<>();
        final List<Component> components = new ArrayList<>();

        try {
            partComponents.forEach(partComponent -> {
                final Long partNumber = partComponent.getPart();
                Component component = this.modelMapper.map(partComponent, Component.class);
                component.setType(ComponentType.of(partComponent.getType()));
                parts.computeIfAbsent(partNumber, k -> new ArrayList<>()).add(component);
            });

            for (Map.Entry<Long, List<Component>> entry : parts.entrySet()) {
                final Part part = this.partRepository.save(Part.builder()
                        .testId(testId)
                        .number(entry.getKey())
                        .type(type)
                        .build());
                entry.getValue().forEach(component -> {
                    component.setPartId(part.getId());
                    components.add(component);
                });
                partIds.put(entry.getKey(), part.getId());
            }
            if (!components.isEmpty()) {
                this.componentWriteRepository.saveAll(components);
            }
        } catch (Exception ex) {
            throw new InternalServerException(ex.getMessage(), ex);
        } finally {
            parts.clear();
            components.clear();
        }

        return partIds;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createAnswers(
            @NotNull final Map<Long, Long> partIds,
            @NotNull final List<TestCreationRequest.PartAnswer> partAnswers) {
        try {
            final List<PartAnswer> answers = partAnswers.stream()
                    .map(partAnswer -> PartAnswer.builder()
                            .partId(partIds.get(partAnswer.getPart()))
                            .kei(partAnswer.getKei())
                            .value(partAnswer.getValue())
                            .build())
                    .collect(Collectors.toList());
            if (!answers.isEmpty()) {
                this.partAnswerRepository.saveAll(answers);
            }
        } catch (Exception ex) {
            throw new InternalServerException(ex.getMessage(), ex);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAudio(
            @NotNull final Long testId,
            @NotNull final MultipartFile audio) {
        try {
            final String name = Optional.ofNullable(audio.getOriginalFilename())
                    .map(StringUtils::cleanPath)
                    .orElse(null);
            this.testAudioRepository.save(TestAudio.builder()
                    .testId(testId)
                    .name(name)
                    .type(audio.getContentType())
                    .data(audio.getBytes())
                    .build());
        } catch (Exception ex) {
            throw new InternalServerException(ex.getMessage(), ex);
        }
    }

    private enum ClientComponentType {
        CHOOSE_ANSWER("choose-answer"),
        CHOOSE_TWO_ANSWER("choose-two-answer"),
        ANSWER_PARAGRAPH("answer-paragraph"),
        UNKNOWN("unknown"),
        ;
        private final String value;

        ClientComponentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    @Getter
    @Setter
    static class ClientComponentTypeBuilder {
        private boolean hasBox;
        private boolean hasQuestion;
        private boolean hasOptions;

        public ClientComponentType build() {
            if (this.hasQuestion && this.hasOptions) {
                return ClientComponentType.CHOOSE_ANSWER;
            }
            if (this.hasQuestion || this.hasBox) {
                return ClientComponentType.ANSWER_PARAGRAPH;
            }
            return ClientComponentType.UNKNOWN;
        }
    }

    @Getter
    @Setter
    @Builder
    static class ComponentRange {
        private ComponentPosition position;
        private Range range;
        private ClientComponentTypeBuilder componentTypeBuilder;
        private List<Component> components;
    }

    @Getter
    @Setter
    static class ClosureValue<T> {
        private T value;

        public ClosureValue(T initial) {
            this.value = initial;
        }
    }

    @Override
    public DisplayAllDataResponse retrieveRandomExamBySkill(Long examId, PartType type) {
        final Long userId = this.securityUtils.getLoggedUserId();
        if (Objects.nonNull(examId)) { // kiểm tra xem user có quyền tham gia bài thi này không
            Exam exam = this.examRepository.findById(examId).orElse(null);
            if (Objects.isNull(exam)) {
                throw new NotFoundException(ValidationConstants.EXAM_NOT_FOUND);
            }
            if (!Objects.equals(userId, exam.getUserId())) {
                throw new BadRequestException(ValidationConstants.CANNOT_JOIN_EXAM);
            }
        } else {
            // TODO: kiểm tra xem còn bài thi nào user chưa hoàn thành (submitted) không?
            // TODO: nếu có và bài thi đó chưa hết hạn (cho một giới hạn là mấy tiếng đồng hồ chẳng hạn) thì không cho tạo bài thi mới
            Exam exam = Exam.builder()
                    .userId(userId)
                    .build();
            examId = this.examRepository.save(exam).getId();
        }

        DisplayAllDataResponse displayAllDataResponse = new DisplayAllDataResponse();
        displayAllDataResponse.setExamId(examId);
        List<Long> testIds = testRepository.allActiveTestIds(type);
        if (testIds.isEmpty()) {
            throw new NotFoundException(ValidationConstants.TEST_NOT_FOUND);
        }

        Random rand = new Random();
        Long testId = testIds.get(rand.nextInt(testIds.size()));
        List<ComponentWithPartNumber> testComponents = componentRepository.findByTestId(testId);

        final Map<Long, List<Component>> partComponents = new HashMap<>();
        for (ComponentWithPartNumber component : testComponents) {
            Long partNumber = component.getPartNumber();
            List<Component> components = partComponents
                    .computeIfAbsent(partNumber, k -> new ArrayList<>());
            components.add(component.getComponent());
        }

        final Long id = examId; // change to final variable
        final Long examTestId = this.examTestRepository.save(ExamTest.builder()
                .examId(id)
                .testId(testId)
                .build()).getId();
        displayAllDataResponse.setExamTestId(examTestId);

        Map<String, List<String>> listTypeQuestionInPart = new HashMap<>();

        final Map<Long, DisplayQuestionDataResponse> displayDataMap = new HashMap<>();
        for (Map.Entry<Long, List<Component>> entry : partComponents.entrySet()) {
            Long partNumber = entry.getKey();

            final List<ComponentRange> componentRanges = new ArrayList<>();
            ComponentRange currentComponentRange = ComponentRange.builder()
                    .position(ComponentPosition.LEFT)
                    .componentTypeBuilder(new ClientComponentTypeBuilder())
                    .range(Range.NONE)
                    .components(new ArrayList<>())
                    .build();
            componentRanges.add(currentComponentRange);
            List<Component> components = entry.getValue();
            for (Component component : components) {
                if (ComponentType.RANGE.equals(component.getType())) {
                    currentComponentRange = ComponentRange.builder()
                            .position(ComponentPosition.of(component.getPosition()))
                            .componentTypeBuilder(new ClientComponentTypeBuilder())
                            .range((Range) component.getValue())
                            .components(new ArrayList<>())
                            .build();
                    componentRanges.add(currentComponentRange);
                } else {
                    currentComponentRange.getComponents().add(component);
                    if (ComponentType.QUESTION.equals(component.getType())) {
                        currentComponentRange.getComponentTypeBuilder().setHasQuestion(true);
                        if (Objects.nonNull(component.getOptions())) {
                            currentComponentRange.getComponentTypeBuilder().setHasOptions(true);
                        }
                    } if (ComponentType.BOX.equals(component.getType())) {
                        currentComponentRange.getComponentTypeBuilder().setHasBox(true);
                    }
                }
            }

            List<ComponentDataResponse> rightContent = new ArrayList<>();
            List<ComponentDataResponse> leftContent = new ArrayList<>();
            final ClosureValue<Long> numberOrder = new ClosureValue<>(1L);
            List<String> listTypeQuestion = new ArrayList<>();

            final String clientComponentTypeSuffix = PartType.LISTENING.equals(type)
                    ? "-listening"
                    : "";

            for (ComponentRange componentRange : componentRanges) {
                if (componentRange.getComponents().isEmpty()) {
                    continue;
                }
                ComponentPosition position = componentRange.getPosition();
                ClientComponentType componentType = componentRange.getComponentTypeBuilder().build();
                List<ComponentDataResponse> componentDataResponses = new ArrayList<>();
                if (ClientComponentType.CHOOSE_ANSWER.equals(componentType)) {
                    final ClosureValue<String> clientComponentType = new ClosureValue<>(null);
                    componentDataResponses.addAll(componentRange.getComponents().stream()
                            .filter(component -> ComponentType.QUESTION.equals(component.getType()))
                            .flatMap(this::processChooseAnswerComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .peek(componentDataResponse -> componentDataResponse.setNumberOrder(numberOrder.getValue()))
                            .peek(componentDataResponse -> componentDataResponse.setType(
                                    componentDataResponse.getType() + clientComponentTypeSuffix))
                            .peek(componentDataResponse -> clientComponentType.setValue(componentDataResponse.getType()))
                            .collect(Collectors.toList()));
                    listTypeQuestion.add(clientComponentType.getValue());
                } else if (ClientComponentType.ANSWER_PARAGRAPH.equals(componentType)) {
                    List<Component> convertedComponents /* danh sách các components sau khi chuyển đổi thằng QUESTION về TEXT */ = componentRange.getComponents().stream()
                            .flatMap(component -> {
                                if (ComponentType.QUESTION.equals(component.getType()) && Objects.isNull(component.getOptions())) {
                                    Component textComponent = new Component();
                                    String text = String.format("**%s** %s", component.getKei(), replaceAllBreakLines(component.getValue().toString()));
                                    textComponent.setPartId(component.getPartId());
                                    textComponent.setType(ComponentType.TEXT);
                                    textComponent.setValue(new Raw(text));
                                    Component boxComponent = new Component();
                                    boxComponent.setPartId(component.getPartId());
                                    boxComponent.setType(ComponentType.BOX);
                                    boxComponent.setKei(component.getKei());
                                    return Stream.of(textComponent, boxComponent); // thêm ô trống ở sau
                                }
                                return Stream.of(component);
                            })
                            .collect(Collectors.toList());
                    convertedComponents.add(Component.builder()
                            .type(ComponentType.TEXT)
                            .value(new Raw(""))
                            .build());

                    List<Component> mergedComponents = new ArrayList<>();
                    Component current = null;
                    for (int i = 0; i < convertedComponents.size() - 1; i++) {
                        if (Objects.isNull(current)) {
                            current = convertedComponents.get(i);
                            mergedComponents.add(current);
                        }
                        Component next = convertedComponents.get(i + 1);
                        if (ComponentType.TEXT.equals(next.getType())) { // nếu thằng tiếp theo là TEXT thì gộp chuỗi lại
                            current.setValue(new Raw(
                                    String.format("%s %s", current.getValue().toString(), next.getValue().toString())
                            ));
                        } else if (ComponentType.BOX.equals(next.getType())) { // nếu thằng tiếp theo là BOX thì gán id
                            if (Objects.isNull(current)) { // trường hợp hai BOX đứng cạnh nhau
                                current = Component.builder()
                                        .type(ComponentType.TEXT)
                                        .value(new Raw(""))
                                        .build();
                                mergedComponents.add(current);
                            }
                            current.setKei(next.getKei());
                            current = null;
                            i++;
                        } else if (ComponentType.TITLE.equals(next.getType())) {
                            current.setValue(new Raw(
                                    String.format("%s\n\n**%s**\n\n", current.getValue().toString(), next.getValue().toString())
                            ));
                        } else if (ComponentType.IMAGE.equals(next.getType())) {
                            current.setValue(new Raw(
                                    String.format("%s\n\n![%s](%s)\n\n", current.getValue().toString(), next.getKei(), next.getValue().toString())
                            ));
                        }
                    }

                    List<ComponentDataResponse> tempResponses = mergedComponents.stream()
                            .map(this::processAnswerParagraphComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .peek(componentDataResponse -> componentDataResponse.setNumberOrder(numberOrder.getValue()))
                            .peek(componentDataResponse -> componentDataResponse.setType(
                                    componentDataResponse.getType() + clientComponentTypeSuffix))
                            .collect(Collectors.toList());

                    if (!tempResponses.isEmpty()) {
                        ComponentDataResponse lastComponent = tempResponses.get(tempResponses.size() - 1);
                        if (Objects.isNull(lastComponent.getId())) {
                            tempResponses.forEach(comp -> comp.setLastText(lastComponent.getText()));
                            tempResponses.remove(lastComponent);
                        }
                    }

                    componentDataResponses.addAll(tempResponses);
                    listTypeQuestion.add(ClientComponentType.ANSWER_PARAGRAPH.getValue() + clientComponentTypeSuffix);

                } else {
                    List<Component> convertedComponents = componentRange.getComponents();
                    convertedComponents.add(Component.builder()
                            .type(ComponentType.TEXT)
                            .value(new Raw(""))
                            .build());

                    List<Component> mergedComponents = new ArrayList<>();
                    Component current = convertedComponents.get(0);
                    mergedComponents.add(current);
                    for (int i = 0; i < convertedComponents.size() - 1; i++) {
                        Component next = convertedComponents.get(i + 1);
                        if (ComponentType.TEXT.equals(next.getType())) { // nếu thằng tiếp theo là TEXT thì gộp chuỗi lại
                            current.setValue(new Raw(
                                    String.format("%s %s", current.getValue().toString(), next.getValue().toString())
                            ));
                        } else if (ComponentType.TITLE.equals(next.getType())) {
                            current.setValue(new Raw(
                                    String.format("%s\n**%s**\n", current.getValue().toString(), next.getValue().toString())
                            ));
                        } else if (ComponentType.IMAGE.equals(next.getType())) {
                            current.setValue(new Raw(
                                    String.format("%s\n![%s](%s)\n", current.getValue().toString(), next.getKei(), next.getValue().toString())
                            ));
                        }
                    }

                    componentDataResponses.addAll(mergedComponents.stream()
                            .map(this::processUnknownComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .peek(componentDataResponse -> componentDataResponse.setNumberOrder(numberOrder.getValue()))
                            .peek(componentDataResponse -> componentDataResponse.setType(
                                    componentDataResponse.getType() + clientComponentTypeSuffix))
                            .collect(Collectors.toList()));
                    listTypeQuestion.add(ClientComponentType.UNKNOWN.getValue() + clientComponentTypeSuffix);
                }

                if (ComponentPosition.LEFT.equals(position)) {
                    leftContent.addAll(componentDataResponses);
                } else {
                    rightContent.addAll(componentDataResponses);
                }
                numberOrder.setValue(numberOrder.getValue() + 1); // tăng numberId cho range tiếp theo
            }
            listTypeQuestionInPart.put("part"+partNumber, listTypeQuestion );

            DisplayQuestionDataResponse displayData = new DisplayQuestionDataResponse();
            displayData.setLeftContent(leftContent);
            displayData.setRightContent(rightContent);
            displayDataMap.put(partNumber, displayData);

        }
        displayAllDataResponse.setListTypeQuestion(listTypeQuestionInPart);
        displayAllDataResponse.setDisplayQuestionDataResponse(displayDataMap);
        return displayAllDataResponse;
    }

    @Override
    public Map<String, Long> checkAnswer(Long examId) {
        List<String> skills = Arrays.asList(
                PartType.READING.getValue(),
                PartType.LISTENING.getValue());
        List<Tuple> userAnsAndTrueAns = testRepository.getUserAnswerAndTrueAnswer(skills, examId);
        List<UserAnswerAndTrueAnswerDto> userAnswerAndTrueAnswers = userAnsAndTrueAns.stream()
                .map(t -> new UserAnswerAndTrueAnswerDto(
                        t.get(0, String.class),
                        t.get(1, String.class),
                        t.get(2, String.class),
                        t.get(3, String.class)
                )).collect(Collectors.toList());
        Map<String, Long> correctAnswersForSkill = new HashMap<>();
        for (String skill : skills) {
            correctAnswersForSkill.put(skill, 0L);
        }
        for (UserAnswerAndTrueAnswerDto item : userAnswerAndTrueAnswers) {
            String skill = item.getSkill();
            Long correctAnswers = correctAnswersForSkill.computeIfAbsent(skill, k -> 0L);
            final String kei = item.getQuestion();
            if (Objects.isNull(kei)) {
                continue; // bỏ qua câu hỏi có kei là null
            }
            if (kei.contains("-")) { // kiểm tra xem câu hỏi có phải dạng câu chọn nhiều đáp án không
                // TODO: tách đáp án thành dạng mảng
                try {
                    Set<String> trueAnswers = Set.of(new ObjectMapper().readValue(item.getTrueAnswer(), String[].class));
                    Set<String> userAnswers = Set.of(new ObjectMapper().readValue(item.getUserAnswer(), String[].class));
                    for (String answer : userAnswers) {
                        if (trueAnswers.contains(answer)) {
                            correctAnswers++;
                        }
                    }
                } catch (JsonProcessingException ex) {
                    //
                }
            } else if(item.getTrueAnswer().equalsIgnoreCase(item.getUserAnswer())){
                correctAnswers++;
            }
            correctAnswersForSkill.put(skill, correctAnswers);
        }
        return correctAnswersForSkill;
    }

    private Stream<ComponentDataResponse> processChooseAnswerComponent(@NotNull Component component) {
        long from = 0L;
        long to = 0L;

        try {
            if (Objects.isNull(component.getSize())) {
                from = Long.valueOf(component.getKei());
                to = from;
            } else { // dạng nhiều câu hỏi gộp lại, chọn nhiều đáp án
                String[] ids = component.getKei().split("-");
                from = Long.valueOf(ids[0]);
                to = Long.valueOf(ids[1]);
            }
        } catch (NumberFormatException ex) {
            // bỏ qua
        }
        ClientComponentType clientComponentType = from == to
                ? ClientComponentType.CHOOSE_ANSWER
                : ClientComponentType.CHOOSE_TWO_ANSWER;
        final List<ComponentDataResponse> questions = new ArrayList<>();
        for (long questionId = from; questionId <= to; questionId++) {
            final ComponentDataResponse question = new ComponentDataResponse();
            question.setId(questionId);
            question.setType(clientComponentType.getValue());
            question.setNumberOrder(null);
            question.setQuestionTitle(((Raw) component.getValue()).getValue());
            final List<OptionResponse> options = new ArrayList<>();
            final Set<String> keys = component.getOptions().keys();
            for (String key : keys) {
                String value = component.getOptions().get(key);
                OptionResponse option = new OptionResponse();
                option.setTitle(value);
                option.setValue(key);
                options.add(option);
            }
            question.setSubId(from); // subId là id của câu hỏi đầu tiên
            question.setOptions(options);
            questions.add(question);
        }
        return questions.stream();
    }

    private ComponentDataResponse processAnswerParagraphComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        try {
            long questionId = Long.valueOf(component.getKei());
            response.setId(questionId);
            response.setSubId(questionId);
        } catch (NumberFormatException ex) {
            // bỏ qua
        }
        response.setType(ClientComponentType.ANSWER_PARAGRAPH.getValue());
        response.setLastText("");
        response.setIsDownLine(Boolean.FALSE);
        response.setText(component.getValue().toString());
        return response;
    }

    private ComponentDataResponse processUnknownComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        response.setType(ClientComponentType.UNKNOWN.getValue());
        response.setLastText("");
        response.setIsDownLine(Boolean.FALSE);
        response.setText(replaceAllBreakLines(component.getValue().toString()));
        return response;
    }

    private static String replaceAllBreakLines(@NotNull String text) {
        return text.replaceAll("\n*(<br>)+\n*", "\n\n");
    }
}
