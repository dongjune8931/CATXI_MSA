# CATXI_MSA 

##  모놀리식 -> MSA

Catxi는 원래 **모놀리식 Spring Boot 애플리케이션** 구조로 개발되었으며, 채팅과 실시간 위치 공유 기능이 통합되어 있었습니다.  
하지만 다음과 같은 문제점이 있었습니다:

- 기능별 **독립 배포/스케일링 불가**
- 장애 발생 시 **전체 서비스 영향**
- 서비스 간 경계가 모호하여 **유지보수 비용 증가**
- 팀 간 병렬 개발 어려움

[기존 Catxi 프로젝트](https://github.com/Team-Catxi)
### 🔄 변경 후 구조
MSA 공부하면서 기존 프로젝트 아키텍처를 전환해보려 합니다~
이번 아키텍처 전환을 통해 **MSA 구조**로 리팩토링하였습니다.

| 항목            | 변경 전                           | 변경 후                                                 |
|-----------------|-----------------------------------|----------------------------------------------------------|
| 구조            | 단일 Spring Boot 애플리케이션     | 독립된 마이크로서비스 (User, Auth, Chat, Map, Notification) |
| 배포            | 수동 or 단일 서버 배포            | Jenkins 기반 CI/CD + K8s                                |
| 라우팅          | Nginx에서 직접 서비스 라우팅      | Spring Cloud Gateway + Eureka 기반 동적 라우팅          |
| 구성 관리       | local yml 직접 관리               | Spring Cloud Config Server 도입                         |
| 통신 방식       | 동기 API 호출                     | Kafka 기반 비동기 이벤트 처리 도입                      |
| 실시간 채팅     | 단일 서버 WebSocket               | Redis Pub/Sub 기반 멀티 서버 채팅 구현                  |
| 인증 방식       | 세션 or 토큰 직접 검증            | Gateway JWT 필터 처리 방식                              |

>  주요 핵심: **채팅 시스템은 Redis Pub/Sub을 이용해 멀티 인스턴스 확장이 가능한 구조로 개선됨**

---

##  전체 아키텍처 다이어그램

<img width="1356" height="601" alt="image" src="https://github.com/user-attachments/assets/39861dd5-d971-43fc-9ad8-06e4f4a35c77" />

---

##  서비스별 기능 요약

###  Auth Service
- 소셜 로그인 Kakao
- JWT 발급 및 갱신
- 인증 상태 확인
- Redis에 Refresh Token 저장

###  User Service
- 유저 프로필 관리
- 유저 차단 및 친구 목록 관리
- 유저 상태 정보 (ex. 온라인/오프라인)

###  Chat Service
- 채팅방 생성 / 입장 / 나가기 / 강퇴
- 채팅 메시지 전송 및 수신
- 메시지 읽음 처리
- Redis Pub/Sub 기반 다중 서버 브로드캐스팅
- 레디 버튼 기능:
채팅방의 방장이 레디 버튼을 누르면, 나머지 참여자들도 레디 버튼 UI가 활성화됨
모든 참여자가 레디 상태가 되면 준비 완료 상태로 전환
레디 상태는 Redis를 활용해 실시간으로 브로드캐스트되고 저장됨

###  Map Service
- 채팅방 내 서로의 위치 실시간 공유
- 위치 업데이트 전송 및 히스토리 저장
- 위치 이벤트 Kafka 발행
- Kakao Map API

###  Notification Service
- 채팅 메시지 도착 알림
- 위치 공유 시작 알림
- 템플릿 기반 예약 알림 자동 전송
- Kafka 이벤트 소비 기반 비동기 처리

---

##  기타 컴포넌트

| 구성 요소        | 설명                                                       |
|------------------|------------------------------------------------------------|
| **Gateway**      | Spring Cloud Gateway - 인증 필터 처리, 서비스 라우팅       |
| **Eureka Server**| 서비스 디스커버리 및 동적 라우팅                           |
| **Config Server**| 중앙 설정 관리                                             |
| **Kafka**        | 서비스 간 이벤트 기반 통신                                 |
| **Redis**        | 인증 캐시, 채팅 메시지 브로드캐스트                         |
| **Prometheus**   | 모니터링 메트릭 수집                                       |
| **Grafana**      | 실시간 대시보드 시각화                                     |
| **Jenkins**      | CI 자동화                                                  |
| **Kubernetes**   | 클러스터 오케스트레이션 및 배포 관리                       |
| **Docker**       | 서비스 컨테이너화                                           |

---

## 기술 요약

| 카테고리     | 기술 스택                             |
|--------------|----------------------------------------|
| Gateway      | Spring Cloud Gateway                   |
| Discovery    | Eureka Server                          |
| Config       | Spring Cloud Config                    |
| Messaging    | Apache Kafka                           |
| Realtime     | Redis Pub/Sub                          |
| Auth         | JWT + Redis                            |
| Deployment   | Jenkins + Docker + Kubernetes          |
| Monitoring   | Prometheus + Grafana                   |

---

