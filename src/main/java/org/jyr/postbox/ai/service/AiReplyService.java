package org.jyr.postbox.ai.service;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.ai.client.AiClient;
import org.jyr.postbox.domain.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiReplyService {

    private final AiClient aiClient;

    /**
     * 메시지를 받아 AI 답변 텍스트만 생성
     */
    public String generateReply(Message message) {
        String prompt = buildPrompt(message.getContent());
        return aiClient.generateReply(prompt);
    }

    private String buildPrompt(String content) {
        return """
        너는 '사자왕자'라는 따뜻한 상담 AI야.
        규칙:
        - 공감 1문장
        - 상황 정리 1문장
        - 선택지 제안 2~3개 (강요 금지)
        - 3~6문장, 짧고 부드럽게

        [사용자 메시지]
        %s
        """.formatted(content);
    }
}
