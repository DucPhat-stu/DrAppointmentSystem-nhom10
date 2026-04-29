package com.healthcare.ai.web;

import com.healthcare.ai.dto.PromptTemplateRequest;
import com.healthcare.ai.dto.PromptTemplatePreviewRequest;
import com.healthcare.ai.dto.PromptTemplateResponse;
import com.healthcare.ai.service.PromptTemplateService;
import com.healthcare.shared.api.ApiResponse;
import com.healthcare.shared.common.web.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/templates")
@PreAuthorize("hasRole('ADMIN')")
public class PromptTemplateController {
    private final PromptTemplateService service;
    private final ApiResponseFactory apiResponseFactory;

    public PromptTemplateController(PromptTemplateService service, ApiResponseFactory apiResponseFactory) {
        this.service = service;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<PromptTemplateResponse>> list() {
        return apiResponseFactory.success("Prompt templates loaded", service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateResponse> get(@PathVariable UUID id) {
        return apiResponseFactory.success("Prompt template loaded", service.get(id));
    }

    @PostMapping("/preview")
    public ApiResponse<String> preview(@Valid @RequestBody PromptTemplatePreviewRequest request) {
        return apiResponseFactory.success("Prompt template preview completed", service.preview(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromptTemplateResponse> create(@Valid @RequestBody PromptTemplateRequest request) {
        return apiResponseFactory.success("Prompt template created", service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromptTemplateResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody PromptTemplateRequest request) {
        return apiResponseFactory.success("Prompt template updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return apiResponseFactory.success("Prompt template deleted", null);
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<PromptTemplateResponse> activate(@PathVariable UUID id) {
        return apiResponseFactory.success("Prompt template activated", service.activate(id));
    }
}
