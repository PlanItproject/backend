package com.trip.planit;

import com.google.firebase.messaging.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FirebaseMessagingUnitTest {

    @Test
    void testSendNotificationWithFakeToken() throws FirebaseMessagingException {
        // 1. FirebaseMessaging mock 객체 생성
        FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);

        // 2. send 메소드가 호출되면 "fake-response" 반환하도록 설정
        when(mockMessaging.send(any(Message.class))).thenReturn("fake-response");

        // 3. 테스트용 메시지 생성
        Message message = Message.builder()
                .setToken("fake-token-for-test")
                .putData("title", "테스트 알림")
                .putData("body", "이건 가짜 토큰으로 테스트하는 중")
                .build();

        // 4. 메소드 호출
        String response = mockMessaging.send(message);

        // 5. 결과 검증
        assertEquals("fake-response", response);
        verify(mockMessaging, times(1)).send(any(Message.class));
    }
}
