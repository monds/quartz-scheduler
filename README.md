# quartz-scheduler

Spring Boot + Quartz 를 이용한 lite한 배치 프로그램
제공하는 기능은 다음과 같다.

- Trigger 이력 DB 적재
- Job 관련 REST API 제공
- DB 패스워드 암호화
- 설정 파일을 이용한 cron expression 변경
- HA 지원 (active + standby)


### 기본 설정
scan 대상이 되는 job class가 위치한 package 경로를 입력한다.
```yaml
scheduler:
  job-base-packages:
  - com.monds.job
```
