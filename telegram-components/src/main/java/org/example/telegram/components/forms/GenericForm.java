package org.example.telegram.components.forms;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericForm {
    private final List<FormStep<String>> steps;
    @Getter
    private final Map<String, String> userResponses = new HashMap<>();
    private int currentStepIndex = -1; // Start at -1 to handle the first message properly
    private final String firstMessage;
    private final String finalMessage;

    public GenericForm(List<FormStep<String>> steps, String firstMessage, String finalMessage) {
        this.steps = steps;
        this.firstMessage = firstMessage;
        this.finalMessage = finalMessage;
    }

    public boolean isCompleted() {
        return currentStepIndex == steps.size();
    }

    public String handleResponse(String input) {
        if (currentStepIndex == -1) {
            currentStepIndex++; // Move to the first real step
            return firstMessage + "\n\n" + steps.get(currentStepIndex).question();
        }

        if (isCompleted()) {
            return finalMessage;
        }

        if (input.equals("/back")) {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                return steps.get(currentStepIndex).question();
            } else {
                return "You are already at the first step.";
            }
        }

        FormStep<String> currentStep = steps.get(currentStepIndex);

        if (currentStep.validate(input.toLowerCase())) {
            userResponses.put(currentStep.fieldName(), input);
            currentStepIndex++;

            if (isCompleted()) {
                return currentStep.successMessage() + "\n\n" + finalMessage;
            } else {
                return currentStep.successMessage() + "\n\n" + steps.get(currentStepIndex).question();
            }
        } else {
            return currentStep.errorMessage();
        }
    }
}
