package uk.max.validator.Model;

import java.util.List;

// class that represents a validation results with only needed fields
public class ValidationResult {
    // make fields private
    private String id;
    private String focusNode;
    private String resultMessage;
    private String resultPath;
    private String resultSeverity;

    public ValidationResult() {
    }

    // constructor
    public ValidationResult(String id, String focusNode, String resultMessage, String resultPath, String resultSeverity) {
        this.id = id;
        this.focusNode = focusNode;
        this.resultMessage = resultMessage;
        this.resultPath = resultPath;
        this.resultSeverity = resultSeverity;
    }

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFocusNode() {
        return focusNode;
    }

    public void setFocusNode(String focusNode) {
        this.focusNode = focusNode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getResultSeverity() {
        return resultSeverity;
    }

    public void setResultSeverity(String resultSeverity) {
        this.resultSeverity = resultSeverity;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "id='" + id + '\'' +
                ", focusNode='" + focusNode + '\'' +
                ", resultMessage='" + resultMessage + '\'' +
                ", resultSeverity='" + resultSeverity + '\'' +
                ", resultPath='" + resultPath + '\'' +
                '}';
    }

    // method to build a json response consisting of ValidationResult objects
    public static String buildJsonResponse(List<ValidationResult> validationResults) {
        StringBuilder json = new StringBuilder("["); // open json array
        for (ValidationResult result : validationResults) {
            // build the string according to json syntax
            json.append("{")
                    .append("\"@id\": ").append(result.getId()).append(", ")
                    .append("\"focusNode\": ").append(result.getFocusNode()).append(", ")
                    .append("\"resultMessage\": ").append(result.getResultMessage()).append(", ")
                    .append("\"resultSeverity\": ").append(result.getResultSeverity()).append(", ")
                    .append("\"resultPath\": ").append(result.getResultPath()).append("")
                    .append("},");
        }
        // remove the trailing comma
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]"); // close the array
        return json.toString();
    }
}
