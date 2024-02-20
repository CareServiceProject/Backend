//package com.github.backend.service;
//
//import com.github.backend.properties.CoolsmsProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.nurigo.sdk.NurigoApp;
//import net.nurigo.sdk.message.model.Message;
//import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
//import net.nurigo.sdk.message.response.SingleMessageSentResponse;
//import net.nurigo.sdk.message.service.DefaultMessageService;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.stereotype.Service;
//
//
//@Service
//@Slf4j
//@EnableConfigurationProperties(CoolsmsProperties.class)
//public class SendMessageService {
//    private final DefaultMessageService messageService;
//    private final CoolsmsProperties coolsmsProperties;
//
//    public SendMessageService(CoolsmsProperties coolsmsProperties){
//        this.messageService = NurigoApp.INSTANCE.initialize(coolsmsProperties.getApikey(), coolsmsProperties.getApiSecret(), "https://api.coolsms.co.kr");
//        this.coolsmsProperties = coolsmsProperties;
//    }
//    public void sendMessage(String phoneNum, String sendingMessage){
//        Message message = new Message();
//        message.setFrom(coolsmsProperties.getPhoneNum()); // 01012345678 형태여야 함.
//        message.setTo(phoneNum); // 01012345678 형태여야 함.
//        message.setText(sendingMessage);
//        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
//        log.info(phoneNum+"번호로 메시지가 정상적으로 전송되었습니다! ");
//    }
//}
