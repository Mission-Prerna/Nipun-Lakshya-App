package com.samagra.parent.ui.formlite.model;

public class ActionResult {
    private ResultStatus success;
    private ResultStatus failure;

    public ResultStatus getSuccess() {
        return success;
    }

    public void setSuccess(ResultStatus success) {
        this.success = success;
    }

    public ResultStatus getFailure() {
        return failure;
    }

    public void setFailure(ResultStatus failure) {
        this.failure = failure;
    }

    public class ResultStatus{
        private String label;
        private String color;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
