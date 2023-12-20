![노션메인_최종_0914.jpg](https://github.com/Team-available/Will-U/blob/main/%EB%85%B8%EC%85%98%EB%A9%94%EC%9D%B8_%EC%B5%9C%EC%A2%85_0914.jpg?raw=true)

최종 프로젝트 `할래?`
===

### 내일배움 캠프 Spring 6기 1조 팀 된다

## 프로젝트 소개

---
> 👯‍♀️ 무엇이든 누군가와 함께 하고 싶을 때, 함께 할 친구를 찾을 수 있는 사이트


## [시연 영상](https://youtu.be/ls0F0HszD-s)

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

## 담당 기능

- SpringSecurity, JWT, Redis를 활용한 사용자 로그인/로그아웃 구현
- Redis를 활용한 RefeshToken 적용
- SSE를 활용한 실시간 알림기능 구현
- AWS RDS DB 환경 구축 (AZ+Replica)에 따른 부하 분산을 위한 Read/Write 분기
- AWS 서버 환경 구축(EC2 + ELB + Route53)
- ++ Javascript, JQuery, Ajax 등을 활용한 프론트엔드 개발 (기여도 전체의 30%)
---

## 주요 구현 기능 설명

### 사용자

- springSecurity 적용
  [DIR_security](https://github.com/Team-available/Will-U/tree/main/src/main/java/com/beteam/willu/common/security)
- JWT 적용
  [JwtUtil.java](https://github.com/Team-available/Will-U/blob/main/src/main/java/com/beteam/willu/common/jwt/JwtUtil.java)
- Redis
  적용 [Redis.java](https://github.com/Team-available/Will-U/blob/main/src/main/java/com/beteam/willu/common/redis/RedisUtil.java)

#### 사용자 회원가입

  ```java
  public class UserService {
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
}
  ```

1. RequestDto로 받은 입력값 중 중복 불가능한 항목의 중복여부 확인
2. 비밀번호 인코딩 후 회원가입(유저 생성)

---

#### 로그인

  ```java
  public class UserService {
    @Transactional
    public void userLogin(UserRequestDto requestDto, HttpServletResponse response) {
        String username = requestDto.getUsername();
        User user = findUser(username);

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("로그인 실패 비밀번호 틀립니다!");
        }

        String accessToken = jwtUtil.createAccessToken(username);
        String refreshToken = jwtUtil.createRefreshToken(username);

        redisUtil.saveRefreshToken(username, refreshToken);

        jwtUtil.addJwtToCookie(accessToken, JwtUtil.AUTHORIZATION_HEADER, response);
        jwtUtil.addJwtToCookie(refreshToken, JwtUtil.REFRESH_TOKEN_HEADER, response);
    }
}
  ```

* `@JsonInclude(JsonInclude.Include.NON_NULL)`를 사용하여 회원가입 시 사용한 RequestDto와 같은 dto지만 username과 password만 받아온다.

1. 입력받은 username에 해당하는 유저가 있는지 확인
2. 있다면 해당 유저의 비밀번호를 decode 한 값과 입력한 비밀번호가 일치하는 지 확인
3. 일치 시 AccessToken, RefreshToken 발급
4. 발급한 RefreshToken Redis에 저장
5. 발급한 토큰들 쿠키에 담기

#### 로그아웃

```java
public class UserService {
    public void logout(String accessToken, HttpServletResponse response, String username) {
        //쿠키에서 가져온 토큰추출
        accessToken = URLDecoder.decode(accessToken, StandardCharsets.UTF_8).substring(7);
        log.info("accessToken 값: " + accessToken);

        if (redisUtil.getRefreshToken(username) != null) {
            log.info("로그아웃 시 리프레시 토큰이 존재하면 지워준다.");
            redisUtil.deleteRefreshToken(username);
        }
        log.info("액세스 토큰 블랙리스트로 저장 : " + accessToken);
        redisUtil.addBlackList(accessToken, jwtUtil.getExpiration(accessToken));
        //쿠키 삭제
        jwtUtil.expireCookie(response, JwtUtil.AUTHORIZATION_HEADER);
        jwtUtil.expireCookie(response, JwtUtil.REFRESH_TOKEN_HEADER);
    }
}
```

1. 쿠키에서 AccessToken을 가져와 순수 토큰을 추출
2. `@AuthenticationPrincipal UserDetailsImpl userDetails`에 존재하는 username과 일치하는 RefreshToken이 있는지 확인하고 redis 저장소에서 삭제
3. AccessToken을 Redis의 blackList에 저장하고 현재 존재하는 쿠키(토큰들)을 만료시킴

---

### 2. 알림 기능

#### SSE 연결

- 클라이언트에서 SSE 연결 요청을 통해 서버와 지속적인 연결을 하는 길을 만듭니다.
- 연결 후 전송되는 알림이 없으면 503 에러가 발생하므로 연결하고 바로 더미데이터를 보내주도록 했습니다.
- 재연결이 거의 발생할 일이 없지만 재연결이 발생하면 기존 클라이언트가 갖고있는 마지막으로 받았던 Event의 Id를 사용해 연결이 끊긴 동안 수신된 알림들을 수신합니다.
- 튜터님의 피드백을 반영해 잦은 상호작용이 발생하는 알림이 되지 않도록 하기 위해 무조건적인 읽음 처리가 아닌 상호작용이 필요한 알림에만 읽음처리를 요하도록 변경

  ```java
      public class NotificationService{
          public SseEmitter subscribe(Long userId, String lastEventId) {
            log.info("SSE subscribe: USER ID: " + userId + "LastEVENTID: " + lastEventId);
            User user = userRepository.findById(userId).orElseThrow();
    
            String emitterId = makeTimeIncludeId(userId);
            SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
    
            emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
            emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
    
            // 503 에러를 방지하기 위한 더미 이벤트 전송
    
            Notification notification = Notification.builder()
                    .title("connect")
                    .content("EventStream Created. [userId=" + userId + "]")
                    .receiver(user)
                    .notificationType(NotificationType.MAKE_CONNECTION)
                    .build();
            String eventId = makeNotificationIdIncludeId(userId, notification.getId());
            sendNotification(emitter, eventId, emitterId, notification);
    
            // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
            // TODO TEST 정상 동작하는지 확인 필요
            if (hasLostData(lastEventId)) {
                log.info("미수신 데이터 있음");
                sendLostData(lastEventId, userId, emitterId, emitter);
            }
            return emitter;
      }
    }
    ``` 

#### 읽음 처리

- 승인/거절 버튼을 누르면 알림을 읽음처리하여 내 '상호작용이 필요한' 읽지 않은 알림 목록에서 사라집니다.

  ```java
    public class NotificationService {
    @Transactional
    public void updateRead(long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        notification.updateIsRead();
        emitterRepository.deleteAllEventCacheEndsWithNotificationId(id);
        log.info("읽었음으로 상태 변경" + notification.getIsRead().toString());
        log.info("cache 에서 notificationId 로끝나는 키를 가진 notification 삭제");
    }
  }
  ```

#### 알림 직접 보내기

- EventListener를 통한 알림 전송이 아닌 NotificationService를 사용한 알림만을 보내는 API로 독립적으로 알림 전송이 필요한 경우 사용됩니다.
- 알림 타입에 따라 다른 알림이 생성되어 전송되도록 했습니다.

  ```java
    public class NotificationService {
        public void send(User publisher, User receiver, NotificationType notificationType, String content, String title,
                     Long postId) {
        log.info("send 실행");
        Notification notification = notificationRepository.save(
                createNotification(publisher, receiver, notificationType, content, title, postId));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = makeNotificationIdIncludeId(receiver.getId(), notification.getId());
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithById(receiverId);
        log.info("emitters 크기: " + emitters.size());
        if (emitters.size() > 0) {
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(eventId, notification);
                        log.info("eventCache 저장 key: " + eventId + " notification: " + notification);
                        sendNotification(emitter, eventId, key, notification);
                    }
            );
        } else {
            log.info("연결된 Emitter가 없는 경우");
            log.info("eventCache 저장 key: " + eventId + " notification: " + notification);
            emitterRepository.saveEventCache(eventId, notification);
        }
    }
  }
  ```

#### API에 붙는 알림 보내기

- 알림 서비스와 타 서비스의 의존성을 높이지 않기 위해 EventListener를 사용했습니다.
- eventListener를 통해 타 서비스에서 이벤트를 통해 위 send() 메서드를 타고 알림이 전송됩니다.

```java
  public class ChatRoomService {
    @Transactional
    public void joinUserChatRoom(ChatroomJoinRequestDto requestDto, User loginUser) {
        //....유저가 채팅방에 들어오는 로직

        UserChatRoom guestChatRoom = UserChatRoom.builder().user(joiner).chatRooms(chatRoom).role("GUEST").build();
        //유저 채팅방 초대
        userChatRoomsRepository.save(guestChatRoom);

        //알림 발송
        NotificationEvent approveMessageEvent = NotificationEvent.builder()
                .title("참여 요청 승인").notificationType(NotificationType.APPROVE_REQUEST)
                .receiver(joiner).publisher(loginUser).content(post.getTitle() + " 게시글에 초대됐습니다.")
                .postId(postId).build();
        eventPublisher.publishEvent(approveMessageEvent);
        //추가 후 인원이 모두 찼는지 확인하고 알림 발송
        if (chatRoom.getUserChatRoomList().size() + 1 >= post.getMaxnum()) {
            post.setRecruitment(false);
            //기존 chatRoom에 있는 유저 목록
            List<User> users = new ArrayList<>(
                    chatRoom.getUserChatRoomList().stream().map(UserChatRoom::getUser).toList());
            users.add(joiner);
            for (User user : users) {
                NotificationEvent doneMessageEvent = NotificationEvent.builder()
                        .title("모집 완료 알림")
                        .notificationType(NotificationType.RECRUIT_DONE).receiver(user)
                        .publisher(loginUser).content(post.getTitle() + " 게시글 모집이 완료되었습니다.")
                        .postId(postId).build();
                eventPublisher.publishEvent(doneMessageEvent);
            }
        }
    }
}
```

#### 로그인한 유저에 대한 읽지 않은 알림 목록 조회

- 사용자는 본인이 확인하지 않은 동안에도 본인에게 온 알림을 저장하고 후에 확인할 수 있어야 한다고 생각했습니다.
- 따라서 유저가 알림목록에서 유저에게 온 알림 중 '상호작용이 필요한' 읽지 않은 알림은 계속 조회가 되도록 했습니다.

  ```java
  public class NotificationService {
    public List<NotificationResponseDto> getNotificationByUserId(Long userId) {
        return notificationRepository.findNotificationByReceiver_IdAndIsReadIsFalse(userId)
                .stream()
                .map(NotificationResponseDto::new)
                .toList();
    }
  }
  ```

### 🛠 Infra

#### READ & WRITE 분기

- 배포환경에서, `@Transactional(readOnly=true)` 가 붙었냐의 여부에 따라 읽기 작업은 replica로, 쓰기 작업은 기존 원본 DB 정보를 통해서 DataSource를 생성하도록 구현

```java

@Slf4j
@Profile("production")
@Configuration
public class DataSourceConfig {

    private final String MAIN_DATA_SOURCE = "mainDataSource";
    private final String REPLICA_DATA_SOURCE = "replicaDataSource";

    @Primary
    @Bean(MAIN_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mainDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Profile("production")
    @Bean(REPLICA_DATA_SOURCE)
    @ConfigurationProperties(prefix = "replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }
}
```

```java

@Profile("production")
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type;
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            type = DataSourceType.REPLICA;
        } else {
            type = DataSourceType.MAIN;
        }
        System.out.println("type = " + type.name());
        return type;

    }
}
```

```java

@Profile("production")
@Slf4j
@EnableJpaRepositories(
        basePackages = "com.beteam.willu"
)

@Configuration
public class RoutingDataSourceConfig {

    private final String ROUTING_DATA_SOURCE = "routingDataSource";
    private final String MAIN_DATA_SOURCE = "mainDataSource";
    private final String REPLICA_DATA_SOURCE = "replicaDataSource";
    private final String DATA_SOURCE = "dataSource";
    private final String NOROUTE_DATA_SOURCE = "norouteDataSource";

    @Value("${spring.jpa.properties.hibernate.format_sql}")
    String formatSQL;
    @Value("${spring.jpa.properties.hibernate.show_sql}")
    String showSQL;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    String ddl;

    @Profile("production")
    @Bean(ROUTING_DATA_SOURCE)
    public DataSource routingDataSource(
            @Qualifier(MAIN_DATA_SOURCE) final DataSource mainDataSource,
            @Qualifier(REPLICA_DATA_SOURCE) final DataSource replicaDataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.MAIN, mainDataSource);
        dataSourceMap.put(DataSourceType.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(mainDataSource);

        return routingDataSource;
    }

    @Profile("dev")
    @Bean("dataSource")
    public DataSource noroutingDataSource(
            @Qualifier(MAIN_DATA_SOURCE) final DataSource devDataSource) {
        return devDataSource;
    }

    @Profile("production")
    @Bean(DATA_SOURCE)
    public DataSource dataSource(
            @Qualifier(ROUTING_DATA_SOURCE) DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier(DATA_SOURCE) DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory
                = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("com.beteam.willu");
        entityManagerFactory.setJpaVendorAdapter(this.jpaVendorAdapter());
        entityManagerFactory.setPersistenceUnitName("entityManager");
        entityManagerFactory.setJpaProperties(additionalProperties());
        return entityManagerFactory;
    }

    private JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setShowSql(false);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        return hibernateJpaVendorAdapter;
    }

    @Bean("transactionManager")
    public PlatformTransactionManager platformTransactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return jpaTransactionManager;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, org.hibernate.dialect.MySQLDialect.class.getName());
        properties.setProperty(AvailableSettings.SHOW_SQL, showSQL);
        properties.setProperty(AvailableSettings.FORMAT_SQL, formatSQL);
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, ddl);
        // Naming Strategy 설정 (여기서는 ImprovedNamingStrategy 사용)
        properties.setProperty(AvailableSettings.PHYSICAL_NAMING_STRATEGY, org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy.class.getName());

        //암시적 전략은 default가 jpa properties.setProperty(AvailableSettings.IMPLICIT_NAMING_STRATEGY, org.hibernate.boot.model.naming.implicitnamingst"org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        return properties;
    }
}
```

`AWS` 를 활용한 서비스 환경 구축 과정 <br>

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

