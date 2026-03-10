package com.rev.userservice.dto;

import java.util.List;

public class SecurityAnswerUpdateDto {

    private String usernameOrEmail;
    private List<SecurityQuestionItem> securityQuestions;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
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
