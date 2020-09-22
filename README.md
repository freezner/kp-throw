# 카카오페이 머니 뿌리기

## 1. 사용 기술
* Lang: Kotlin v1.3
* JDK: Java8
* Framework: Spring Boot v2.3.4
* Database: MySQL -> H2 (In memory)
* ORM: JPA + Hibernate (QueryDSL은 적용안함)
* IDE: InteliJ Ultimate
* Build: Gradle

## 2. 개발 요건
### 2-1. 공통
* 모든 API는 X-USER-ID와 X-ROOM-ID 헤더를 가진 상태에서 요청해야 함.
* 다수 서버 인스턴스에서도 기능에 문제가 없어야함.
* 테스트 코드 반드시 작성해야함.

### 2-2. 뿌리기 API
* Endpoint: [POST] /api/v1/splash
* 파라미터: 
  * amount: 뿌릴 금액
  * limit: 받을 수 있는 최대 인원
* 기능 구현 설명:
뿌리기 API를 호출 시 받기, 조회 API에서 사용될 토큰이 생성됩니다. 또한 in_money(뿌린 사람의 정보), out_money(뿌린 금액을 받을 인원과 금액의 정보)가 세팅됩니다. out_money에는 최대 인원 수 만큼 레코드가 쌓이며 이 레코드에는 이미 받을 금액이 계산되어 저장됩니다. 받을 사람의 아이디를 제외하고 대부분의 데이터가 뿌리기 API에서 설정됩니다.

### 2-3. 받기 API
* Endpoint: [PUT] /api/v1/take/:token
* 파라미터:
  * token: 뿌리기 API에서 생성된 토큰
* 기능 구현 설명:
뿌리기 API를 통해 생성된 토큰을 이용해 뿌린 금액을 받을 수 있습니다. 같은 방에서 2번이상 받을 수 없으며 토큰이 발행된지 10분 이후에도 받을 수 없습니다. 자신이 뿌린 건의 토큰으로 받을 수 없습니다. 받기 API를 호출하게되면 out_money에 해당 레코드에 아이디 정보가 저장되고 처리가 끝납니다. 받기 API에는 데드락, 타임아웃 등의 이슈를 가능한한 피하기 위해 로직을 최대한 가볍게 구성하는데 초점을 맞췄습니다.

### 2-4. 조회 API
* Endpoint: [GET] /api/v1/info/:token
* 파라미터:
  * token: 뿌리기 API에서 생성된 토큰
* 기능 구현 설명:
뿌린 자에게만 조회 API가 허용되며 뿌린 시각, 뿌린 금액, 받은 금액, 받은 아이디 정보를 확인 할 수 있습니다. 뿌린 후 7일간 조회가 가능합니다. 다른 토큰을 획득하더라도 아이디가 다르면 확인이 불가합니다. 조회 API를 호출하게되면 in_money(뿌린 사람의 정보), out_money(받은 인원 정보)를 함께 검색하여 표시해줍니다.

## 3. 에러 코드
API | 코드 | 발생 시점/메세지
--- | --- | -------
공통 | 1001 | 헤더 정보 불량
뿌리기 | 1002 | 토큰 생성 실패
뿌리기 | 2001 | 뿌리기 당 한 사용자는 한번만 받을 수 있습니다
뿌리기| 2002 | 존재하지 않는 토큰입니다
받기 | 2003 | 자신이 뿌리기한 건은 자신이 받을 수 없습니다
받기 | 2004 | 뿌린 자가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다
받기 | 2005 | 받을 수 있는 시간(10분)이 만료되었습니다
받기 | 2006 | 받을 수 있는 머니가 모두 소진되었습니다
조회 | 3001 | 조회 실패
조회 | 2003 | 조회 가능 기간이 만료됐습니다. 조회는 뿌리기 후 10일간 가능합니다
조회 | 3003 | 뿌린 사람 자신만 조회할 수 있습니다
