package org.ielts.playground.service.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.ComponentPosition;
import org.ielts.playground.common.enumeration.ComponentType;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.InternalServerException;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.entity.Component;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    public TestServiceImpl(
            TestRepository testRepository,
            PartRepository partRepository,
            ComponentWriteRepository componentWriteRepository,
            ComponentRepository componentRepository, PartAnswerRepository partAnswerRepository,
            TestAudioRepository testAudioRepository,
            ModelMapper modelMapper, SecurityUtils securityUtils) {
        this.testRepository = testRepository;
        this.partRepository = partRepository;
        this.componentWriteRepository = componentWriteRepository;
        this.componentRepository = componentRepository;
        this.partAnswerRepository = partAnswerRepository;
        this.testAudioRepository = testAudioRepository;
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
    static class ClosureLong {
        private long value;
    }

    @Override
    public DisplayAllDataResponse retrieveRandomReadingExam() {
        DisplayAllDataResponse displayAllDataResponse = new DisplayAllDataResponse();
        List<Long> testIds = testRepository.allActiveReadingTestIds();
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

        Map<String, Set<String>> listTypeQuestionInPart = new HashMap<>();

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
            final ClosureLong subId = new ClosureLong();
            subId.setValue(1);
            Set<String> listTypeQuestion = new HashSet<>();

            for (ComponentRange componentRange : componentRanges) {
                if (componentRange.getComponents().isEmpty()) {
                    continue;
                }
                ComponentPosition position = componentRange.getPosition();
                ClientComponentType componentType = componentRange.getComponentTypeBuilder().build();
                List<ComponentDataResponse> componentDataResponses = new ArrayList<>();
                if (ClientComponentType.CHOOSE_ANSWER.equals(componentType)) {
                    componentDataResponses.addAll(componentRange.getComponents().stream()
                            .filter(component -> ComponentType.QUESTION.equals(component.getType()))
                            .map(this::processChooseAnswerComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .peek(componentDataResponse -> componentDataResponse.setSubId(subId.getValue()))
                            .collect(Collectors.toList()));
                    listTypeQuestion.add(ClientComponentType.CHOOSE_ANSWER.getValue());
                } else if (ClientComponentType.ANSWER_PARAGRAPH.equals(componentType)) {
                    List<Component> convertedComponents /* danh sách các components sau khi chuyển đổi thằng QUESTION về TEXT */ = componentRange.getComponents().stream()
                            .flatMap(component -> {
                                if (ComponentType.QUESTION.equals(component.getType()) && Objects.isNull(component.getOptions())) {
                                    Component textComponent = new Component();
                                    String text = String.format("**%s** %s", component.getKei(), component.getValue().toString());
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
                                    String.format("%s\n**%s**\n", current.getValue().toString(), next.getValue().toString())
                            ));
                        } else if (ComponentType.IMAGE.equals(next.getType())) {
                            current.setValue(new Raw(
                                    String.format("%s\n![%s](%s)\n", current.getValue().toString(), next.getKei(), next.getValue().toString())
                            ));
                        }
                    }

                    componentDataResponses.addAll(mergedComponents.stream()
                            .map(this::processAnswerParagraphComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .peek(componentDataResponse -> componentDataResponse.setSubId(subId.getValue()))
                            .collect(Collectors.toList()));
                    listTypeQuestion.add(ClientComponentType.ANSWER_PARAGRAPH.getValue());

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
                            .peek(componentDataResponse -> componentDataResponse.setSubId(subId.getValue()))
                            .collect(Collectors.toList()));
                    listTypeQuestion.add(ClientComponentType.UNKNOWN.getValue());
                }

                if (ComponentPosition.LEFT.equals(position)) {
                    leftContent.addAll(componentDataResponses);
                } else {
                    rightContent.addAll(componentDataResponses);
                }
                subId.setValue(subId.getValue() + 1); // tăng subId cho range tiếp theo
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

    private ComponentDataResponse processChooseAnswerComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        try {
            if (Objects.isNull(component.getSize())) {
                response.setId(Long.valueOf(component.getKei()));
            } else { // dạng nhiều câu hỏi gộp lại, chọn nhiều đáp án
                String[] ids = component.getKei().split("-");
                Long from = Long.valueOf(ids[0]);
                Long to = Long.valueOf(ids[1]);
                // TODO: tách thành các câu hỏi theo yêu cầu của Front-end
            }
        } catch (NumberFormatException ex) {
            // bỏ qua
        }
        response.setType(ClientComponentType.CHOOSE_ANSWER.getValue());
        response.setNumberOrder(null);
        response.setQuestionTitle(((Raw) component.getValue()).getValue());
        final List<OptionResponse> options = new ArrayList<>();
        final Set<String> keys = component.getOptions().keys();
        for (String key : keys) {
            String value = component.getOptions().get(key);
            OptionResponse option = new OptionResponse();
            option.setTitle(value);
            option.setValue(key);
            options.add(option);
        }
        response.setOptions(options);
        return response;
    }

    private ComponentDataResponse processAnswerParagraphComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        try {
            response.setId(Long.valueOf(component.getKei()));
        } catch (NumberFormatException ex) {
            // bỏ qua
        }
        response.setType(ClientComponentType.ANSWER_PARAGRAPH.getValue());
        response.setNumberOrder(null);
        response.setQuestionTitle(null);
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
        response.setText(component.getValue().toString());
        return response;
    }
}
