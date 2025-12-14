package org.jyr.postbox.ai.client;

import org.springframework.stereotype.Component;

@Component
public class HttpAiClient implements AiClient {

    @Override
    public String generateReply(String prompt) {
        // ✅ 1차 MVP: 더미 응답 (외부 API 붙이기 전)
        return "🦁 사자왕자: 마음이 많이 무거웠겠다. 지금은 숨 고르는 게 먼저야. "
                + "1) 잠깐 물 한 잔  2) 5분만 쉬기  3) 내일 할 일 하나만 적기";
    }
}
