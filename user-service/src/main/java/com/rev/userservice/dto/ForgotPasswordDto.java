package com.rev.userservice.dto;

import java.util.List;

public class ForgotPasswordDto {

    private String usernameOrEmail;
    private String newPassword;
    private List<SecurityQuestionItem> securityQuestions;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public List<SecurityQuestionItem> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(List<SecurityQuestionItem> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }

    public static class SecurityQuestionItem {
        private String question;
        private String answer;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
