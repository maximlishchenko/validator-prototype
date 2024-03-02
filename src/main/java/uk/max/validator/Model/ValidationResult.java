package uk.max.validator.Model;

import java.util.List;

public class ValidationResult {
    private String id;
    private String focusNode;
    private String resultMessage;
    private String resultPath;
    private String resultSeverity;

    public ValidationResult() {
    }

    public ValidationResult(String id, String focusNode, String resultMessage, String resultPath, String resultSeverity) {
        this.id = id;
        this.focusNode = focusNode;
        this.resultMessage = resultMessage;
        this.resultPath = resultPath;
        this.resultSeverity = resultSeverity;
    }

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

    public static String buildJsonResponse(List<ValidationResult> validationResults) {
        StringBuilder json = new StringBuilder("[");
        for (ValidationResult result : validationResults) {
            json.append("{")
                    .append("\"@id\": ").append(result.getId()).append(", ")
                    .append("\"focusNode\": ").append(result.getFocusNode()).append(", ")
                    .append("\"resultMessage\": ").append(result.getResultMessage()).append(", ")
                    .append("\"resultSeverity\": ").append(result.getResultSeverity()).append(", ")
                    .append("\"resultPath\": ").append(result.getResultPath()).append("")
                    .append("},");
        }
        // Remove the trailing comma and close the array
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]");
        return json.toString();
    }
}
