![노션메인_최종_0914.jpg](Will-U\노션메인_최종_0914.jpg)

### 내일배움 캠프 Spring 6기 1조 팀 된다 최종 프로젝트 `할래?`

---

## 프로젝트 소개

---
> 👯‍♀️ 무엇이든 누군가와 함께 하고 싶을 때, 함께 할 친구를 찾을 수 있는 사이트

### 진행 기간  **2023/08/16 ~ 2023/09/18**

## ⚙ 개발환경

---

- `SpringBoot 3.1.0`
- `Java 17`
- `JDK 17.0.7`
- `MySQL 8.0`

## ⚙ 사용기술

---

- `JPA`
- `EC2`
- `S3`
- `RDS`
- `Redis`
- `Spring Security`
- `JWT`
- `Oauth2`
- `SSE`
- `STOMP`
- `Sring batch`
- `QueryDSL`

# ✨심형철

## 역할

---

팀장, 프로젝트 리더 <br>
프로젝트 일정 계획, 진행 관리, 설계 등을 주도했습니다.

## 구현 기능

- 알림기능
- refresh Token
- 로그인
- 회원가입
- 유저 상세 조회
- 프로필 수정
- 로그아웃
- AWS RDS DB 환경 구축 (AZ+Replica)
- 서버 환경 구축(EC2 + ELB + Route53)
- 테스트 코드 작성(RepositoryTest branch)

---

## 주요 구현 기능 설명

### 사용자

- springSecurity 적용
- JWT 적용
- Redis 적용

#### 사용자 회원가입

UserService

```
public void userSignup(UserRequestDto requestDto) {

        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("해당 유저가 이미 있습니다.");
        }

        if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("중복된 username 입니다");
        }

        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("중복된 email 입니다");
        }

        String password = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .username(requestDto.getUsername())
                .password(password)
                .nickname(requestDto.getNickname())
                .email(requestDto.getEmail())
                .picture("기본이미지")
                .build();

        userRepository.save(user);
    }
```

1. RequestDto로 받은 입력값 중 중복 불가능한 항목의 중복여부 확인
2. 비밀번호 인코딩 후 회원가입(유저 생성)

---

#### 로그인

UserService

```java
    @Transactional
public void userLogin(UserRequestDto requestDto,HttpServletResponse response){
        String username=requestDto.getUsername();
        User user=findUser(username);

        if(!passwordEncoder.matches(requestDto.getPassword(),user.getPassword())){
        throw new IllegalArgumentException("로그인 실패 비밀번호 틀립니다!");
        }

        String accessToken=jwtUtil.createAccessToken(username);
        String refreshToken=jwtUtil.createRefreshToken(username);

        redisUtil.saveRefreshToken(username,refreshToken);

        jwtUtil.addJwtToCookie(accessToken,JwtUtil.AUTHORIZATION_HEADER,response);
        jwtUtil.addJwtToCookie(refreshToken,JwtUtil.REFRESH_TOKEN_HEADER,response);
        }
```

```java
    public User findUser(String username){
        return userRepository.findByUsername(username)
        .orElseThrow(()->new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        }
```

* `@JsonInclude(JsonInclude.Include.NON_NULL)`를 사용하여 회원가입 시 사용한 RequestDto와 같은 dto지만 username과 password만 받아온다.

1.

#### 로그아웃

### 2. 알림 기능

- SSE 연결
    - 클라이언트에서 SSE 연결 요청을 통해 서버와 지속적인 연결을 하는 길을 만듭니다.
    - 연결 후 전송되는 알림이 없으면 503 에러가 발생하므로 연결하고 바로 더미데이터를 보내주도록 했습니다.
    - 재연결이 거의 발생할 일이 없지만 재연결이 발생하면 기존 클라이언트가 갖고있는 마지막으로 받았던 Event의 Id를 사용해 연결이 끊긴 동안 수신된 알림들을 수신합니다.
- 읽음 처리
    - 알림의 닫기나 승인/거절 버튼을 누르면 알림을 읽음처리하여 내 읽지 않은 알림 목록에서 사라집니다.
- 알림 직접 보내기
    - EventListener를 통한 알림 전송이 아닌 NotificationService를 사용한 알림만을 보내는 API로 독립적으로 알림 전송이 필요한 경우 사용됩니다.
    - 알림 타입에 따라 다른 알림이 생성되어 전송되도록 했습니다.
- API에 붙는 알림 보내기
    - 알림 서비스와 직접적으로 연결하는 걸 피하기 위해 EventListener를 사용했습니다. eventListener로 API에 필요한 알림을 만들어 전송할 수 있습니다.
- 로그인한 유저에 대한 읽지 않은 알림 목록 조회
    - 사용자는 본인이 확인하지 않은 동안에도 본인에게 온 알림을 저장하고 후에 확인할 수 있어야 한다고 생각했습니다.
    - 따라서 유저가 알림목록에서 유저에게 온 알림 중 읽지 않은 알림은 계속 조회가 되도록 했습니다.

### 🛠 Infra

`AWS` 를 활용한 서비스 환경 구축 <br>

1. EC2 인스턴스에 서버 배포 후 실행
2. EC2 인스턴스 2 개 복제 생성 후 ELB에 등록
3. Redis를 운영할 EC2 인스턴스 생성
4. RDS 생성, 가용성 확보를 위한 AZ 설정, 부하 분산을 위한 Replica 적용
5. Route 53 을 활용하여 도메인 등록, HTTPS 적용, ELB 연결

![할래 아키텍처](https://github.com/Team-available/Will-U/assets/131872877/8b49be94-da60-4b9f-b1d0-b9c5b76ffe60)

---

## 📄ERD

![Untitled](https://github.com/Team-available/Will-U/assets/131872877/a58f1684-829d-40ae-b477-4fa7d8824d93)


---

## 🌎배포

[willuapp.com](http://willuapp.com/)

---

## 📆개발 일정

---

> 8월 17일 ~ 8월 22일
>

- 역할 분담, 계획 수립 및 프로젝트 생성
- 기본 (필수) 기능 구현

> 8월 23일 ~ 27일
>

- 세부사항 조정
- 채팅 기능 구현
- 알림 기능 구현

> 8월 28일 ~ 8월 30일
>

- 프론트엔드 1차 구현 (APi 작동 확인)
- 세부사항 조정
- 1차 배포

> 8월 30일 ~ 9월 1일
>

- 프론트 엔드 2차 구현 (사용자 편의를 위한 조정)
- 세부사항 조정
- 2차 배포

> 9월 2일  ~ 9월 3일
>

- 중간 발표 준비
- 프론트 엔드 보강
- 채팅 기능 보강 (개별 사용자 에게 메세지 보내기)

> 9월 4일  ~ 9월 7일
>

- 중간 피드백 적용
    - 알림 기능 보강
- S3 사용 이미지 업로드

> 9월 8일 ~ 9월 12일
>

- 추가 기능 구현
    - 태그 검색 기능
    - 지도 API 적용 (게시글 장소 지정)
- 3차 배포 (유저 테스트)

> 9월 13일 ~ 15일
>

- 유저 피드백 반영 (프론트)
    - 신청 알림시 사이드바 활성화
    - 항목검색, 태그검색 모집중/모집완료 별 따로 검색
- 서버 고도화
    - 로드 밸런싱 적용
    - Replica 적용
    - 도메인 설정
    - HTTPS 적용
- 코드 리펙토링
    - QueryDSL 적용
    - 예외처리 AOP 적용

마지막 수정 날짜 : 2023-09-18

