package org.jyr.postbox.ai.client;

public interface AiClient {

    /**
     * 프롬프트를 받아 AI 답변을 생성한다.
     */
    String generateReply(String prompt);
}
