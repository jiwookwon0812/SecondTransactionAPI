package com.cocomo.secondhand_transaction.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendHtmlEmail(String emailTo, String subject, String html) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(emailTo);
        helper.setSubject(subject);
        helper.setText(html, true);
        javaMailSender.send(message);
    }

    // 거래 요청 알림 -> 구매자
    public void sendOrderRequest(String sellerEmail, String sellerNickName,
                                 String buyerNickName, String productName, String orderId) throws MessagingException {
        Context context = new Context();
        context.setVariable("sellerName", sellerNickName);
        context.setVariable("buyerName", buyerNickName);
        context.setVariable("productName", productName);
        context.setVariable("orderId", orderId);

        String htmlContent = templateEngine.process("requestOrder", context);
        sendHtmlEmail(sellerEmail, "[코코모] 새로운 거래 요청이 도착했습니다.", htmlContent);
    }

    public void approveOrder1(String buyerEmail, String sellerEmail, String buyerName, String sellerName,
                              String productName, String sellerNumber, String buyerNumber) throws MessagingException {

        // 구매자용 컨텍스트 설정
        Context buyerContext = new Context();
        buyerContext.setVariable("buyerName", buyerName);
        buyerContext.setVariable("sellerName", sellerName);
        buyerContext.setVariable("productName", productName);
        buyerContext.setVariable("sellerPhoneNumber", sellerNumber);

        // 판매자용 컨텍스트 설정
        Context sellerContext = new Context();
        sellerContext.setVariable("sellerName", sellerName);
        sellerContext.setVariable("buyerName", buyerName);
        sellerContext.setVariable("productName", productName);
        sellerContext.setVariable("buyerPhoneNumber", buyerNumber);

        // 템플릿 처리
        String buyerHtmlContent = templateEngine.process("approveRequestOrder1", buyerContext);
        String sellerHtmlContent = templateEngine.process("approveRequestOrder2", sellerContext);

        // 이메일 발송
        sendHtmlEmail(buyerEmail, "[코코모] 거래 요청 승인 및 결제가 완료되었습니다.", buyerHtmlContent);
        sendHtmlEmail(sellerEmail, "[코코모] 승인하신 거래의 결제가 완료되었습니다.", sellerHtmlContent);
    }

    // 거래 요청 거절 알림 -> 판매자
    public void rejectOrder(String buyerEmail, String buyerName,
                                     String sellerName, String productName) throws MessagingException {
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("productName", productName);

        String htmlContent = templateEngine.process("rejectRequestOrder", context);
        sendHtmlEmail(buyerEmail, "[코코모] 거래 요청이 거절되었습니다.", htmlContent);
    }

    // 거래 취소 요청 -> 판매자
    public void cancelOrder(String sellerEmail, String sellerName, String buyerName, String productName, String orderId) throws MessagingException {
        Context context = new Context();
        context.setVariable("sellerName", sellerName);
        context.setVariable("buyerName", buyerName);
        context.setVariable("productName", productName);
        context.setVariable("orderId", orderId);

        String htmlContent = templateEngine.process("cancelOrder", context);
        sendHtmlEmail(sellerEmail, "[코코모] 거래 취소 요청이 접수되었습니다.", htmlContent);
    }

    // 거래 요청 승인 -> 구매자
    public void approveCancel(String buyerEmail, String buyerName, String sellerName, String productName) throws MessagingException {
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("productName", productName);

        String htmlContent = templateEngine.process("approveCancel", context);
        sendHtmlEmail(buyerEmail, "[코코모] 거래 취소가 승인되었습니다.", htmlContent);
    }

    // 거래 요청 거절 -> 구매자
    public void rejectCancel(String buyerEmail, String buyerName, String sellerName, String productName) throws MessagingException {
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("productName", productName);

        String htmlContent = templateEngine.process("rejectCancel", context);
        sendHtmlEmail(buyerEmail, "[코코모] 거래 취소 요청이 거절되었습니다.", htmlContent);
    }

    // 거래 확정 -> 판매자, 구매자
    public void confirmOrder(String sellerEmail, String buyerEmail, String sellerName, String buyerName, String productName) throws MessagingException {
        // 판매자용 Context 설정
        Context sellerContext = new Context();
        sellerContext.setVariable("sellerName", sellerName);
        sellerContext.setVariable("productName", productName);

        // 구매자용 Context 설정
        Context buyerContext = new Context();
        buyerContext.setVariable("buyerName", buyerName);
        buyerContext.setVariable("productName", productName);

        // 템플릿 처리
        String sellerHtmlContent = templateEngine.process("confirmOrder2", sellerContext);
        String buyerHtmlContent = templateEngine.process("confirmOrder1", buyerContext);

        // 이메일 발송
        sendHtmlEmail(sellerEmail, "[코코모] 거래가 확정되었습니다.", sellerHtmlContent);
        sendHtmlEmail(buyerEmail, "[코코모] 거래가 확정되었습니다.", buyerHtmlContent);  // 구매자에게 발송
    }

    // 3일 뒤 거래 확정 요청 메일
    public void after3days(String buyerEmail, String buyerName, String sellerName, String productName, String orderId) throws MessagingException {
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("productName", productName);
        context.setVariable("orderId", orderId);

        String htmlContent = templateEngine.process("after3days", context);

        sendHtmlEmail(buyerEmail, "[코코모] 거래 확정을 눌러주세요.", htmlContent);
    }

    // 7일 뒤 자동 확정 후 메일
    public void after7days(String buyerEmail, String buyerName, String productName) throws MessagingException {
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("productName", productName);

        String htmlContent = templateEngine.process("after7days", context);

        sendHtmlEmail(buyerEmail, "[코코모] 거래가 자동으로 확정되었습니다.", htmlContent);
    }
}
