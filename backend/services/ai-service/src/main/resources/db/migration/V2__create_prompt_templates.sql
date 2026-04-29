CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    template TEXT NOT NULL,
    variables_list TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX ux_prompt_templates_active
    ON prompt_templates (is_active)
    WHERE is_active = TRUE;

INSERT INTO prompt_templates (
    id,
    name,
    template,
    variables_list,
    is_active,
    version
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Default structured symptom analysis',
    'Analyze the following structured health symptom intake and provide JSON output.
Symptoms: {{symptoms}}
Duration: {{duration}}
Additional description: {{description}}

Return ONLY valid JSON:
{
  "possible_conditions": ["condition1", "condition2"],
  "symptoms_detected": ["symptom1", "symptom2"],
  "recommended_specialty": "specialty_name",
  "advice": "brief advice"
}',
    '["symptoms","duration","description"]',
    TRUE,
    1
);
