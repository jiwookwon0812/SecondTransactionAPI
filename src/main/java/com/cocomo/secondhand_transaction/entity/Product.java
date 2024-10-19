package com.cocomo.secondhand_transaction.entity;

import com.cocomo.secondhand_transaction.dto.ProductDto;
import com.cocomo.secondhand_transaction.entity.constant.Category;
import com.cocomo.secondhand_transaction.entity.constant.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "Product")
@Getter
@ToString
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String pd_name; // 상품명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 상품 등록한 유저 (1:N)

    @Column(nullable = false)
    private String pd_img; // 상품 이미지

    @Column(nullable = false)
    private Integer pd_price; // 가격

    private String pd_detail; // 상세 설명

    private String location; // 위치 (선택)

    private Double latitude;  // 위도 (선택)

    private Double longitude; // 경도 (선택)

    @Column(nullable = false)
    private String place; // 거래 장소

    @Column(nullable = false)
    @ElementCollection
    private List<String> time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime createdDt;
    // 등록일 한달이내에 판매 x 상태이면 삭제하도록
    // => 이건 한 3주 후에 알림 주도록

    @Column(nullable = false)
    private int request_buy;
    // 거래 요청 상태 (0: 기본, 1: 요청됨, 2: 승인됨, -1: 거절됨)

    @Column(nullable = false)
    private String pd_num; // 상품 등록 번호 (중복 없음) (이 번호로 상품 구별)

    @PrePersist
    public void prePersist(){
        this.createdDt = LocalDateTime.now(); // DB 저장 직전? 시간 설정
    }

    // 상품 등록번호 (중복x)
    public String generateProductNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    // 1. 생성자 (위도 경도 따로 입력할 필요 X 경우)
    public Product(ProductDto productDto, User user) {
        this.pd_name = productDto.getPd_name();
        this.user = user;
        this.pd_img = productDto.getPd_img();
        this.pd_price = productDto.getPd_price();
        this.pd_detail = productDto.getPd_detail();
        this.location = productDto.getLocation();
        this.latitude = productDto.getLatitude();
        this.longitude = productDto.getLongitude();
        this.place = productDto.getPlace();
        this.time = productDto.getTime();
        this.status = Status.AVAILABLE; // 상태 초기값 : 구매 가능
        this.category = productDto.getCategory();
        this.request_buy = 0; // 거래 요청 상태 기본값 : 0
        this.pd_num = generateProductNumber();
    }

    // 2. 생성자 (위치만 입력한 경우 -> 위도 경도 따로 넣어주기)
    public Product(ProductDto productDto, User user, double latitude, double longitude) {
        this.pd_name = productDto.getPd_name();
        this.user = user;
        this.pd_img = productDto.getPd_img();
        this.pd_price = productDto.getPd_price();
        this.pd_detail = productDto.getPd_detail();
        this.location = productDto.getLocation();
        this.latitude = latitude;
        this.longitude = longitude;
        this.place = productDto.getPlace();
        this.time = productDto.getTime();
        this.status = Status.AVAILABLE; // 상태 초기값 : 구매 가능
        this.category = productDto.getCategory();
        this.request_buy = 0; // 거래 요청 상태 기본값 : 0
        this.pd_num = generateProductNumber();
    }

    // 3. 생성자 (위치 기반으로 상품 등록 -> 위도 경도만 저장되기 때문에 위치는 따로 넣어주어야 함)
    public Product(ProductDto productDto, User user, String location) {
        this.pd_name = productDto.getPd_name();
        this.user = user;
        this.pd_img = productDto.getPd_img();
        this.pd_price = productDto.getPd_price();
        this.pd_detail = productDto.getPd_detail();
        this.location = location;
        this.latitude = productDto.getLatitude();
        this.longitude = productDto.getLongitude();
        this.place = productDto.getPlace();
        this.time = productDto.getTime();
        this.status = Status.AVAILABLE; // 상태 초기값 : 구매 가능
        this.category = productDto.getCategory();
        this.request_buy = 0; // 거래 요청 상태 기본값 : 0
        this.pd_num = generateProductNumber();
    }


}