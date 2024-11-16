package com.cocomo.secondhand_transaction.service;

import com.cocomo.secondhand_transaction.dto.OrderDto;
import com.cocomo.secondhand_transaction.entity.Order;
import com.cocomo.secondhand_transaction.entity.Product;
import com.cocomo.secondhand_transaction.entity.User;
import com.cocomo.secondhand_transaction.entity.constant.OrderCancelRequestStatus;
import com.cocomo.secondhand_transaction.entity.constant.Payment;
import com.cocomo.secondhand_transaction.entity.constant.RequestOrder;
import com.cocomo.secondhand_transaction.repository.OrderRepository;
import com.cocomo.secondhand_transaction.repository.ProductRepository;
import com.cocomo.secondhand_transaction.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.cocomo.secondhand_transaction.entity.constant.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    // 상품 등록 유저 객체 찾기
    private User findUserByAuthentication(Authentication authentication) {
        String email = authentication.getName();  // 현재 로그인한 유저의 인증 정보에서 이메일 추출
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 거래 요청
    public void requestOrder(OrderDto.Order orderDto, Authentication authentication) throws MessagingException {
        // 구매자 User 객체 찾기
        User buyer = findUserByAuthentication(authentication);

        // 구매 Product 객체 찾기
        Product product = productRepository.findProductByPdNum(orderDto.getPdNum())
                .orElseThrow(() -> new RuntimeException("해당 물품의 정보가 없습니다."));
        if (product.getStatus().equals(RESERVED)) {
            throw new RuntimeException("이 상품은 예약 중입니다.");
        } else if (product.getStatus().equals(SOLD_OUT)) {
            throw new RuntimeException("이 상품은 판매 종료된 상품입니다.");
        } else if (product.getStatus().equals(REPORTED)) {
            throw new RuntimeException("이 상품은 현재 구매 불가합니다.");
        }

        // 판매자 User 객체 찾기
        User seller = product.getUser();

        // Order 객체 생성
        Order order = new Order(orderDto, seller, buyer, product);
        orderRepository.save(order);
        // 이메일 발송
        emailService.sendOrderRequest(seller.getEmail(), seller.getNickname(), buyer.getNickname(), product.getPd_name(), order.getOrderNum());

    }

    // 거래 요청 승인
    public void approveOrder(String orderNum, Authentication authentication) throws MessagingException {
        // order 찾기
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // order 의 판매자와 일치하는지 확인
        User seller = findUserByAuthentication(authentication);
        if (!seller.equals(order.getSeller())) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        order.updateRequestOrder(RequestOrder.APPROVED);
        // 결제 바로
        if (order.getRequestOrder().equals(RequestOrder.APPROVED) && order.getRequestCancel().equals(OrderCancelRequestStatus.NONE)){
            order.updatePayment(Payment.DEPOSITED);
        }
        orderRepository.save(order);
        // 이메일 발송
        emailService.approveOrder1(order.getBuyer().getEmail(), seller.getEmail(),
                order.getBuyer().getNickname(), seller.getNickname(), order.getProduct().getPd_name(), seller.getPhone_nb(), order.getBuyer().getPhone_nb());

    }

    // 거래 요청 거절
    public void rejectOrder(String orderNum, Authentication authentication) throws MessagingException {
        // order 찾기
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // order 의 판매자와 일치하는지 확인
        User seller = findUserByAuthentication(authentication);
        if (!seller.equals(order.getSeller())) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        order.updateRequestOrder(RequestOrder.REJECTED);
        orderRepository.save(order);

        // 물품 상태 바꾸기
        Product product = order.getProduct();
        product.updateProductStatus(AVAILABLE);
        productRepository.save(product);
        // 이메일 발송
        emailService.rejectOrder(order.getBuyer().getEmail(), order.getBuyer().getNickname(),
                seller.getNickname(), product.getPd_name());
    }

    // 거래 취소 요청
    public void cancelOrder(String orderNum, Authentication authentication) throws MessagingException {
        // order 찾기
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // product 찾기
        Product product = order.getProduct();

        // 구매자 확인
        User buyer = findUserByAuthentication(authentication);
        if (!buyer.equals(order.getBuyer())) {
            throw new RuntimeException("구매자 권한이 없습니다.");
        }

        // 거래 승인 및 결제 이전 -> 바로 취소 가능
        if (order.getPayment().equals(Payment.NONE)) {
            order.updateRequestOrder(RequestOrder.NONE);
            order.updateRequestCancelOrder(OrderCancelRequestStatus.APPROVED);
            order.updatePayment(Payment.REFUND);
            product.updateProductStatus(AVAILABLE);
            orderRepository.save(order);
            productRepository.save(product);
            // 이메일 발송
            emailService.approveCancel(buyer.getEmail(), buyer.getNickname(),
                    order.getSeller().getNickname(), product.getPd_name());
            return;
        }
        // 결제 후인 경우
        order.updateRequestCancelOrder(OrderCancelRequestStatus.REQUESTED);
        // 이 때 이메일 발송
        emailService.cancelOrder(order.getSeller().getEmail(), order.getSeller().getNickname(),
                buyer.getNickname(), product.getPd_name(), order.getOrderNum());
        orderRepository.save(order);
    }

    // 거래 취소 요청 승인
    public void approveCancel(String orderNum, Authentication authentication) throws MessagingException {
        User seller = findUserByAuthentication(authentication);
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다"));

        if (!seller.equals(order.getSeller())) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        Product product = order.getProduct();

        if (order.getRequestCancel().equals(OrderCancelRequestStatus.REQUESTED)) {
            order.updateRequestCancelOrder(OrderCancelRequestStatus.APPROVED);
            order.updateRequestOrder(RequestOrder.NONE);
            order.updatePayment(Payment.REFUND); // 바로 환불
            product.updateProductStatus(AVAILABLE);
            orderRepository.save(order);
            productRepository.save(product);
            // 이메일 발송
            emailService.approveCancel(order.getBuyer().getEmail(), order.getBuyer().getNickname(),
                    seller.getNickname(), product.getPd_name());
        } else {
            throw new RuntimeException("취소 요청된 주문이 아닙니다.");
        }
    }

    // 거래 취소 요청 거절
    public void rejectCancel(String orderNum, Authentication authentication) throws MessagingException {
        User seller = findUserByAuthentication(authentication);
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다"));

        if (!seller.equals(order.getSeller())) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        Product product = order.getProduct();

        if (order.getRequestCancel().equals(OrderCancelRequestStatus.REQUESTED)) {
            order.updateRequestCancelOrder(OrderCancelRequestStatus.REJECTED);
            // 이메일 발송
            orderRepository.save(order);
            emailService.rejectCancel(order.getBuyer().getEmail(), order.getBuyer().getNickname(),
                    seller.getNickname(), product.getPd_name());
        } else {
            throw new RuntimeException("취소 요청된 주문이 아닙니다.");
        }
    }

    // 거래 확정
    public void confirmOrder(String orderNum, Authentication authentication) throws MessagingException {
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        Product product = order.getProduct();

        User buyer = findUserByAuthentication(authentication);
        if (!buyer.equals(order.getBuyer())) {
            throw new RuntimeException("구매자 권한이 없습니다.");
        }

        if (!order.getRequestOrder().equals(RequestOrder.APPROVED) ||
        order.getRequestCancel().equals(OrderCancelRequestStatus.APPROVED) ||
        !order.getPayment().equals(Payment.DEPOSITED)) {
            throw new RuntimeException("거래 진행중 혹은 취소되었습니다.");
        }

        order.orderSuccess(); // 거래 확정

        emailService.confirmOrder(order.getSeller().getEmail(), buyer.getEmail(),
                order.getSeller().getNickname(), buyer.getNickname(), product.getPd_name());

        orderRepository.save(order);
        product.updateProductStatus(SOLD_OUT);
        productRepository.save(product);
    }


    // 거래 시간 문자열을 LocalDateTime 으로 변환
    private LocalDateTime parseSelectedTime(String selectedTime) {
        return LocalDateTime.parse(selectedTime, dateTimeFormatter);
    }

    // 스케줄러 - 3일이 지난 거래는 거래 확정 요청 알림 보내기 + 7일 지난 거래는 자동 거래 확정
    @Transactional
    @Scheduled(fixedRate = 86400000) // 하루마다 실행
    public void processOrderNotifications() throws MessagingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 3일이 지나고 알림을 보내지 않은 주문들 처리
        List<Order> notifyOrders = orderRepository.findBySuccessFalseAndNotifiedFalse();
        for (Order order : notifyOrders) {
            LocalDateTime selectedTime = parseSelectedTime(order.getSelectedTime());
            if (selectedTime.isBefore(threeDaysAgo)) {
                emailService.after3days(order.getBuyer().getEmail(), order.getBuyer().getNickname(),
                        order.getSeller().getNickname(), order.getProduct().getPd_name(), order.getOrderNum());
                order.orderNotified(); // notified = true
                orderRepository.save(order);
            }
        }

        // 7일이 지나고 확정되지 않은 주문들 처리
        List<Order> confirmOrders = orderRepository.findByReportedFalseAndSuccessFalseAndNotifiedTrue();
        for (Order order : confirmOrders) {
            LocalDateTime selectedTime = parseSelectedTime(order.getSelectedTime());
            if (selectedTime.isBefore(sevenDaysAgo)) {
                emailService.after7days(order.getBuyer().getEmail(), order.getBuyer().getNickname(),
                        order.getProduct().getPd_name());
                order.orderSuccess(); // 거래 확정
                orderRepository.save(order);
            }
        }
    }

    // 거래 조회
    public List<OrderDto.responseOrder> getMyOrder(Authentication authentication) {
        User user = findUserByAuthentication(authentication);
        List<Order> myOrders = orderRepository.findAllBySeller(user);
        List<Order> myOrders2 = orderRepository.findAllByBuyer(user);
        myOrders.addAll(myOrders2);

        return myOrders.stream().map(order -> {
            OrderDto.responseOrder responseOrder = new OrderDto.responseOrder();
            responseOrder.setPdNum(order.getProduct().getPdNum());
            responseOrder.setProductName(order.getProduct().getPd_name());
            responseOrder.setSellerNickname(order.getSeller().getNickname());
            responseOrder.setBuyerNickname(order.getBuyer().getNickname());
            responseOrder.setSelectedTime(order.getSelectedTime());
            responseOrder.setStatus(order.getRequestOrder().toString());
            return responseOrder;
        }).collect(Collectors.toList());
    }

    // 신고된 거래 중단
    public void reportOrder(String orderNum, Authentication authentication) {
        User buyer = findUserByAuthentication(authentication);
        Order order = orderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));
        if (!buyer.equals(order.getBuyer())) {
            throw new RuntimeException("구매자 권한이 없습니다.");
        }
        if (!order.isReported() || !order.isSuccess()
            || LocalDateTime.now().isBefore(parseSelectedTime(order.getSelectedTime()))) {
            throw new RuntimeException("신고할 수 없는 거래입니다.");
        }
        order.reportOrder();
        order.getProduct().updateProductStatus(REPORTED);
        order.orderNotified();
        productRepository.save(order.getProduct());
        orderRepository.save(order);
    }
}
