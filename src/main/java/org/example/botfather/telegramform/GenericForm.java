package org.example.botfather.telegramform;
import lombok.Getter;

import java.util.*;

public class GenericForm {
    private final List<FormStep<String>> steps;
    @Getter
    private final Map<String, String> userResponses = new HashMap<>();
    private int currentStepIndex = 0;

    public GenericForm(List<FormStep<String>> steps) {
        this.steps = steps;
    }

    public boolean isCompleted() {
        return currentStepIndex >= steps.size();
    }

    public String getCurrentQuestion() {
        if (isCompleted()) {
            return "" + userResponses;
        }
        return steps.get(currentStepIndex).question();
    }

    public String handleResponse(String input) {
        if (isCompleted()) {
            return "Form is already completed";
        }

        FormStep<String> currentStep = steps.get(currentStepIndex);

        if (currentStep.validate(input)) {
            userResponses.put(currentStep.fieldName(), input);
            currentStepIndex++;
            return currentStep.successMessage() + "\n\n" + getCurrentQuestion();
        } else {
            return currentStep.errorMessage();
        }
    }
}

