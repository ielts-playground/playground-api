package org.ielts.playground.service.impl;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.ComponentType;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.common.exception.InternalServerException;
import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.Part;
import org.ielts.playground.model.entity.PartAnswer;
import org.ielts.playground.model.entity.Test;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.TestCreationResponse;
import org.ielts.playground.repository.ComponentRepository;
import org.ielts.playground.repository.PartAnswerRepository;
import org.ielts.playground.repository.PartRepository;
import org.ielts.playground.repository.TestRepository;
import org.ielts.playground.service.TestService;
import org.ielts.playground.utils.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

@Service
public class TestServiceImpl implements TestService {
    private final TestRepository testRepository;
    private final PartRepository partRepository;
    private final ComponentRepository componentRepository;
    private final PartAnswerRepository partAnswerRepository;
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    public TestServiceImpl(
            TestRepository testRepository,
            PartRepository partRepository,
            ComponentRepository componentRepository,
            PartAnswerRepository partAnswerRepository,
            ModelMapper modelMapper, SecurityUtils securityUtils) {
        this.testRepository = testRepository;
        this.partRepository = partRepository;
        this.componentRepository = componentRepository;
        this.partAnswerRepository = partAnswerRepository;
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

        final Long testId = this.testRepository.save(Test.builder()
                .createdBy(this.securityUtils.getLoggedUserId())
                .active(Boolean.TRUE)
                .build()).getId();

        final Map<Long, Long> partIds = this.createComponents(testId, type, request.getComponents());
        this.createAnswers(partIds, request.getAnswers());

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
                this.componentRepository.saveAll(components);
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
}
