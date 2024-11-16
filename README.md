# 코코모 - 직거래 위주의 중고 거래 플랫폼

## 📌 기획 배경

직거래 중고 거래에서는 불필요한 대화와 사기 위험으로 인해 거래가 번거롭고 불안할 수 있습니다. 특히 "가격 협상"이나 "판매 여부 확인" 같은 대화는 사용자에게 부담을 줄 수 있습니다. 이를 해결하기 위해, 직거래 시간을 미리 예약하고 즉시 결제하는 방식을 도입하여 시간 조율의 번거로움을 줄이고, 거래 확정 기능을 통해 거래 완료 시 안전하게 대금이 전달되도록 하여 사기 위험을 줄입니다. 결제 후에만 전화번호를 전달하도록 하여 불필요한 대화를 최소화하고 거래의 편리함과 신뢰성을 높이는 플랫폼을 목표로 합니다.

## 💡 해결 컨셉 및 기대 효과

- **직거래 예약 시스템**: 판매자가 직거래 가능한 시간과 장소를 등록하면, 구매자가 원하는 시간을 선택하여 거래를 예약합니다. 이를 통해 시간과 장소 조율에 대한 번거로움을 줄이고 사용자가 직거래를 더 간편하게 진행할 수 있습니다.
- **위치 기반 검색**: 위치 기반 검색 + 동네 검색 기능을 통해 사용자는 자신의 위치 근처에서 직거래 가능한 상품을 손쉽게 검색하고 반경 5km 이내의 상품만 볼 수 있어 거래의 편의성을 높입니다.
- **필요한 경우에만 대화 가능**: 결제 후에만 전화번호가 공유되도록 하여, 불필요한 대화를 최소화하고 거래의 편리함을 높입니다.

## 🛠️ 기능 명세서

### 1. 사용자 관리 관련 기능
- **회원 가입 및 로그인**: 회원 가입 및 로그인 기능을 제공합니다.
- **프로필 관리**: 사용자 정보 수정 기능을 지원합니다.

### 2. 상품 관리 관련 기능
- **상품 등록**: 판매자가 사진, 상품명, 설명, 가격, 직거래 가능한 시간과 장소 등을 입력하여 상품을 등록합니다.
- **상품 검색 및 필터링**: 카테고리별 검색, 가격 순, 최신 순 정렬 등 필터링 기능을 제공합니다.

### 3. 위치 기반 검색 기능
- **동네 기반 검색**: 구매자가 원하는 동네를 선택하여 해당 지역에서 직거래 가능한 상품 판매글 검색이 가능합니다.
- **위치 기반 검색**: 구매자의 반경 5km 이내에 있는 직거래 가능한 상품 판매글을 찾을 수 있도록 지원합니다.

### 4. 직거래 관련 기능
- **직거래 가능 시간/장소 등록**: 판매자가 직거래 가능한 시간과 장소를 등록할 수 있습니다.
- **직거래 예약 및 결제**: 구매자가 판매자가 설정한 시간 중 원하는 시간을 선택하고 결제를 진행합니다. 대금은 플랫폼에서 보관됩니다.
- **거래 확정 기능**: 구매자가 직거래 완료 후 거래 확정 버튼을 클릭하면 판매자에게 플랫폼에서 보관 중이던 대금을 전달합니다.

### 5. 대화 및 커뮤니케이션 기능
- **전화번호 교환 (대화 대체)**: 채팅 기능을 사용하지 않는 경우, 결제 후 전화번호를 서로 교환하여 직접 연락이 가능하도록 합니다. (이메일로 번호 전달 예정)

### 6. 대금 관리 및 결제 관련 기능
- **안전 거래**: 구매자가 거래 확정 시 대금을 전달받습니다.
- **환불 처리**: 직거래 실패 시 대금 환불 요청 및 처리 시스템을 갖추고 있습니다.

### 7. 리뷰 및 평점 관리 기능
- **거래 리뷰 작성**: 거래 완료 후 판매자가 서로에 대해 리뷰와 평점을 남길 수 있는 기능을 제공합니다.
- **리뷰 관리**: 각 사용자와 상품에 대해 평점을 볼 수 있어 신뢰도 평가가 가능합니다.

### 8. 알림 기능
- **이메일 알림**: 거래 예약, 결제 완료, 직거래 일정 등 중요한 이벤트 발생 시 사용자에게 알림을 제공합니다.
  - **이메일 발송**: 거래 요청, 거래 취소, 거래 확정 등의 이벤트에 대한 이메일을 사용자에게 발송합니다.
  - **스케줄러 알림**: 거래 요청 후 3일과 7일이 지난 거래에 대해 자동 알림을 전송하는 기능을 구현하였습니다. 이메일 내용은 동적으로 생성된 HTML로 Thymeleaf를 사용하여 발송합니다.
 <img width="650" alt="이메일사진" src="https://github.com/user-attachments/assets/951968e1-c582-4736-a5af-c626e4a1b615" style="border: 5px solid #000000;">

## 📡 API 설명

### 1. 사용자 API
- **회원가입**
    - `POST /signup`
    - Request Body:
      ```json
      {
        "email": "user@example.com",
        "nickname": "username",
        "password": "strongpassword",
        "phone_nb": "010-1234-5678",
        "authority": "ROLE_USER"
      }
      ```
    - Response:
      ```json
      {
        "message": "User registered successfully"
      }
      ```

- **로그인**
    - `POST /login`
    - Request Body:
      ```json
      {
        "email": "user@example.com",
        "password": "strongpassword"
      }
      ```
    - Response:
      ```json
      {
        "token": "jwt_token"
      }
      ```

### 2. 상품 API
- **상품 등록**
    - `POST /product/register`
    - Request Body:
      ```json
      {
        "pd_name": "상품명",
        "pd_img": "상품 이미지 URL",
        "pd_price": 10000,
        "pd_detail": "상품 설명",
        "location": "서울시 강남구",
        "latitude": 37.518568,
        "longitude": 127.024612,
        "place": "직거래 장소",
        "time": ["09:00", "18:00"],
        "category": "ELECTRONICS"
      }
      ```
    - Response:
      ```json
      {
        "message": "상품이 등록되었습니다."
      }
      ```

- **상품 검색**
    - `GET /product`
    - Query Parameters: `name`, `category`, `location`, `nickname`, `pdNum`
    - Response:
      ```json
      [
        {
          "pdNum": "12345",
          "pd_name": "상품명",
          "pd_img": "상품 이미지 URL",
          "pd_price": 10000,
          "pd_detail": "상품 설명"
        }
      ]
      ```

### 3. 거래 API
- **거래 요청**
    - `POST /order/request`
    - Request Body:
      ```json
      {
        "pdNum": "상품번호",
        "selectedTime": "2024-11-10-10-00"
      }
      ```
    - Response:
      ```json
      {
        "message": "거래가 요청 되었습니다."
      }
      ```
- **거래 확정**
    - `POST /order/confirm/{orderId}`
    - Response:
      ```json
      {
        "message": "거래가 확정되었습니다."
      }
      ```

### 4. 리뷰 API
- **리뷰 등록**
    - `POST /review/{orderId}`
    - Request Body:
      ```json
      {
        "pd_name": "상품명",
        "score": 5,
        "rv_detail": "리뷰 내용"
      }
      ```
    - Response:
      ```json
      {
        "message": "리뷰가 등록되었습니다."
      }
      ```

## 📅 프로젝트 구조
- **Controller**: API 요청을 처리하는 컨트롤러
- **Service**: 비즈니스 로직을 처리하는 서비스
- **Entity**: 데이터베이스와의 매핑을 위한 엔티티 클래스
- **DTO**: 데이터 전송 객체

# 🗂️ ERD

https://www.erdcloud.com/d/xR7eeweNiuoup998b

![image](https://github.com/user-attachments/assets/41f6ebc4-2140-4a64-858f-5906ec7f484d)
