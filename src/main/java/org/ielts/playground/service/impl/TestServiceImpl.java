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
import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.Part;
import org.ielts.playground.model.entity.PartAnswer;
import org.ielts.playground.model.entity.Test;
import org.ielts.playground.model.entity.TestAudio;
import org.ielts.playground.model.entity.type.Range;
import org.ielts.playground.model.entity.type.Raw;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.ComponentDataResponse;
import org.ielts.playground.model.response.DisplayData;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import java.util.Random;

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
            if (this.hasBox) {
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

    @Override
    public Map<Long, DisplayData> retrieveRandomReadingExam() {
        List<Long> testIds = testRepository.allActiveReadingTestIds();
        Random rand = new Random();
        Long testId = testIds.get(rand.nextInt(testIds.size()));
        List<Component> testComponents = componentRepository.findByTestId(testId);

        final Map<Long, List<Component>> partComponents = new HashMap<>();
        for (Component component : testComponents) {
            Long partNumber = component.getPartNumber();
            List<Component> components = partComponents
                    .computeIfAbsent(partNumber, k -> new ArrayList<>());
            components.add(component);
        }

        final Map<Long, DisplayData> displayDataMap = new HashMap<>();
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
                            .position(ComponentPosition.valueOf(component.getPosition()))
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
                            .collect(Collectors.toList()));
                } else if (ClientComponentType.ANSWER_PARAGRAPH.equals(componentType)) {
                    componentDataResponses.addAll(componentRange.getComponents().stream()
                            .map(this::processAnswerParagraphComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .collect(Collectors.toList()));
                } else {
                    componentDataResponses.addAll(componentRange.getComponents().stream()
                            .map(this::processUnknownComponent)
                            .peek(componentDataResponse -> componentDataResponse.setPart(partNumber))
                            .collect(Collectors.toList()));
                }

                if (ComponentPosition.LEFT.equals(position)) {
                    leftContent.addAll(componentDataResponses);
                } else {
                    rightContent.addAll(componentDataResponses);
                }
            }

            DisplayData displayData = new DisplayData();
            displayData.setLeftContent(leftContent);
            displayData.setRightContent(rightContent);
            displayDataMap.put(partNumber, displayData);
        }
        return displayDataMap;
    }

    private ComponentDataResponse processChooseAnswerComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        response.setId(Long.valueOf(component.getKei()));
        response.setSubId(Long.valueOf(component.getKei()));
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
        // TODO: ...
        return response;
    }

    private ComponentDataResponse processUnknownComponent(@NotNull Component component) {
        final ComponentDataResponse response = new ComponentDataResponse();
        // TODO: ...
        return response;
    }
}
