package com.healthcare.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ai.dto.PromptTemplateRequest;
import com.healthcare.ai.dto.PromptTemplatePreviewRequest;
import com.healthcare.ai.dto.PromptTemplateResponse;
import com.healthcare.ai.entity.PromptTemplateEntity;
import com.healthcare.ai.repository.PromptTemplateJpaRepository;
import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptTemplateService {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");

    private final PromptTemplateJpaRepository repository;
    private final ObjectMapper objectMapper;

    public PromptTemplateService(PromptTemplateJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromptTemplateResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public PromptTemplateResponse create(PromptTemplateRequest request) {
        List<String> variables = validate(request.template(), request.variables());
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setName(request.name().trim());
        entity.setTemplate(request.template().trim());
        entity.setVariablesList(writeVariables(variables));
        entity.setActive(false);
        entity.setVersion(1);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public PromptTemplateResponse update(UUID id, PromptTemplateRequest request) {
        List<String> variables = validate(request.template(), request.variables());
        PromptTemplateEntity entity = find(id);
        entity.setName(request.name().trim());
        entity.setTemplate(request.template().trim());
        entity.setVariablesList(writeVariables(variables));
        entity.setVersion(entity.getVersion() + 1);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PromptTemplateEntity entity = find(id);
        if (entity.isActive()) {
            throw new ApiException(ErrorCode.CONFLICT, "Active template cannot be deleted");
        }
        repository.delete(entity);
    }

    @Transactional
    public PromptTemplateResponse activate(UUID id) {
        PromptTemplateEntity target = find(id);
        repository.deactivateAll();
        target.setActive(true);
        return toResponse(repository.save(target));
    }

    public String preview(PromptTemplatePreviewRequest request) {
        String description = request.description() == null || request.description().isBlank()
                ? "None"
                : request.description().trim();
        String preview = request.template().trim()
                .replace("{{ symptoms }}", request.symptoms().trim())
                .replace("{{symptoms}}", request.symptoms().trim())
                .replace("{{ duration }}", request.duration().label())
                .replace("{{duration}}", request.duration().label())
                .replace("{{ description }}", description)
                .replace("{{description}}", description);

        if (preview.contains("{{") || preview.contains("}}")) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Template preview has unresolved placeholders");
        }

        return preview;
    }

    private PromptTemplateEntity find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Prompt template not found"));
    }

    private List<String> validate(String template, List<String> variables) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String variable : variables) {
            String candidate = variable == null ? "" : variable.trim();
            if (!VARIABLE_PATTERN.matcher(candidate).matches()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid template variable", List.of(candidate));
            }
            normalized.add(candidate);
        }

        Set<String> placeholders = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        if (!normalized.equals(placeholders)) {
            throw new ApiException(
                    ErrorCode.VALIDATION_ERROR,
                    "Template variables must match placeholders",
                    List.of("variables=" + normalized, "placeholders=" + placeholders)
            );
        }

        if (template.contains("{{") && placeholders.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Template contains invalid placeholders");
        }

        return List.copyOf(normalized);
    }

    private String writeVariables(List<String> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Could not serialize template variables");
        }
    }

    private List<String> readVariables(String variablesList) {
        try {
            return objectMapper.readValue(variablesList, new TypeReference<>() {
            });
        } catch (Exception exception) {
            return List.of();
        }
    }

    private PromptTemplateResponse toResponse(PromptTemplateEntity entity) {
        return new PromptTemplateResponse(
                entity.getId(),
                entity.getName(),
                entity.getTemplate(),
                readVariables(entity.getVariablesList()),
                entity.isActive(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
