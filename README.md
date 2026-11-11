# 갈래말래 (Gallaemallae)

> 여러 대의 차가 함께 떠나는 여행에서, **일정 하나로 일행 모두를 같은 길로** 안내하는 Pleos Connect 차량용 앱

호스트가 여행 일정(목적지 목록)을 등록하면 일행 차량의 내비게이션에 같은 목적지가 순서대로 전달됩니다. 주행 중에는 일행 차량의 진행 상황 · 도착 예정 시간, 정차 요청(경유지 추가), 간편 메세지, 정차 중 게임을 차량 화면에서 다룹니다. 동승자는 QR로 접속하는 **모바일 웹**에서 같은 기능을 휴대폰으로 사용합니다.

| 구성 | 위치 | 설명 |
| --- | --- | --- |
| Android 앱 (Kotlin · Jetpack Compose) | [`android/`](android) | Pleos Connect에 설치하는 실제 앱 |
| 웹 프로토타입 (HTML/CSS/JS) | [`prototype/`](prototype) | 차량 화면 + 동승자 모바일 웹 시연용 |
| 설계 문서 | [`docs/`](docs) | Pleos 개발 가이드 반영 사항 |

---

## 주요 기능

- **일정 등록** · 목적지를 자유롭게 입력하면 AI가 순서대로 정리하고, 애매한 장소는 후보 중에서 고르기
- **일행 초대** · 동승자는 QR, 다른 차량은 초대 코드(`GLML-7K2Q`)로 참여
- **함께 안내** · 시스템 내비게이션(NaviHelper)으로 목적지 안내, 일행 차량에도 같은 목적지 전송
- **일행 현황** · 차량별 진행률 · 도착 예정 시간 · 내 차와의 거리
- **정차 요청** · 동승자 · 타 차량의 요청을 수락하면 경유지로 추가 (최대 2곳)
- **간편 메세지 · 게임** · 한 번 눌러 보내는 메세지, 정차 중 사다리타기 · 뽑기
- **주행 중 안전** · `CarUxRestrictions`에 따라 설정 화면 잠금, 긴 텍스트 줄임, 알림 개수 제한, 음성(Gleo AI)으로 수락/거절
- **동승자 모바일 웹** · 내비 화면 없이 일행 정보, 정차 요청, 그룹 메세지, 게임 진행 및 결과 확인

---

## 실행 방법

### 1. 웹 프로토타입 (설치 없이 바로 보기)

- 배포 주소: **https://gallaemallae-wwqv.vercel.app/**
- 로컬 실행

  ```bash
  cd prototype
  npx http-server -p 5173
  # 브라우저에서 http://localhost:5173 열기
  ```

- 사용법
  - 왼쪽 위 **내비게이션 화면 / 동승자 웹 / 함께 보기** 버튼으로 화면 전환
  - 오른쪽 위 **시연 패널**에서 정차 요청 받기, 목적지 도착, 네트워크 끊김, 주간/야간, 시뮬레이션 속도 등을 조작
  - 단축키: `V` 음성 “응”, `D` 시연 패널 숨기기
  - 홈의 **진행 중인 여정 이어하기** 또는 **새 여정 만들기 → 일정 입력 → 휴대폰에서 받기 → AI로 정리**로 시작

### 2. Android 앱

```powershell
cd android
# JDK 17 경로 지정 (예시)
$env:JAVA_HOME = "C:\path\to\jdk17"
.\gradlew.bat assembleDebug
```

- 빌드 결과: `android/app/build/outputs/apk/debug/app-debug.apk`
- 에뮬레이터에 설치

  ```bash
  adb install -r android/app/build/outputs/apk/debug/app-debug.apk
  adb shell am start -n com.gallaemallae.app/.MainActivity
  ```

- Android Studio에서 `android/` 폴더를 열고 Pleos Connect 에뮬레이터를 선택해 실행해도 됩니다.
- Debug 빌드에는 시연용 **디버그 패널**(정차 요청 받기, 주행 상태 강제 등)이 포함되고, Release 빌드에서는 빠집니다.

---

## 테스트 환경 및 필요한 설정

### 빌드 환경

| 항목 | 버전 |
| --- | --- |
| JDK | 17 |
| Gradle | 8.9 (Wrapper 포함) |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 2.0.20 (Compose Compiler 플러그인) |
| compileSdk / targetSdk | 34 |
| minSdk | 28 |
| Jetpack Compose BOM | 2024.09.03 |
| Pleos NaviHelper SDK | `ai.pleos.playground:NaviHelper:3.0.5` |

### 필요한 설정

- **Pleos Maven 저장소**: `settings.gradle.kts`에 등록되어 있습니다.

  ```kotlin
  maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
  ```

- **차량 API**: `build.gradle.kts`의 `useLibrary("android.car")`로 `CarUxRestrictionsManager`를 사용합니다. 차량 API가 없는 일반 기기에서는 시뮬레이션으로 대체됩니다.
- **권한 (AndroidManifest)**: `INTERNET`, `POST_NOTIFICATIONS`, `android.car.permission.CAR_DRIVING_STATE`, `pleos.car.permission.NAVI_ROUTE`, `pleos.car.permission.NAVI_ROUTE_SEARCH`. 첫 실행 시 알림 권한을 허용해 주세요.
- **주행 중 사용**: Activity에 `distractionOptimized` 메타데이터가 선언되어 있어 주행 중에도 앱이 표시됩니다.
- **내비게이션 연동**: NaviHelper 연결에 3초 안에 실패하면 내장 시뮬레이션 내비(`SimulatedNavi`)로 자동 전환되어, 실제 내비가 없는 환경에서도 흐름을 확인할 수 있습니다.
- **서버**: 현재는 일행 차량 · 동승자 통신을 앱 내부 시뮬레이션(`SimulatedTripServer`)으로 대체합니다. 별도 서버 설정은 필요 없습니다.
- **경로에 한글이 있는 경우**: `gradle.properties`의 `android.overridePathCheck=true`로 Windows 비 ASCII 경로 빌드를 허용합니다.

### 테스트 환경

| 항목 | 내용 |
| --- | --- |
| 웹 프로토타입 | Chrome / Edge 최신 버전 (데스크톱) |
| Android 앱 빌드 검증 | `assembleDebug` · `lintDebug` 통과 (Windows 11, JDK 17) |
| 실행 대상 | Pleos Connect Emulator (아래 참고) |

---

## Pleos Connect 에뮬레이터 버전과 화면 규격

| 항목 | 내용 |
| --- | --- |
| 에뮬레이터 | **Pleos Connect Emulator 2.1.0** |
| AVD 설정 | Automotive · Pleos Connect v2.1.0 · **API 34** · RAM 4GB · Cold boot |
| 화면 규격 | **2560 × 1440** (가로) |
| 앱 창 | 내비게이션과 화면을 나누는 앱 창 크기에 맞춰 반응형으로 배치 (세로 1창 기본) |
| 화면 구성 | Status bar 64px · Driving view(왼쪽) · 앱/내비 창 · GNB 116px |

에뮬레이터 설치 방법은 [Pleos Connect SDK · 에뮬레이터 설정 가이드](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)를 참고하세요.

---

## APK 파일 위치 / GitHub Release

| 버전 | 다운로드 |
| --- | --- |
| v0.2.0 | _(준비 중)_ |

---

## 폴더 구조

```
.
├─ android/                 # Kotlin · Jetpack Compose 앱
│  └─ app/src/main/java/com/gallaemallae/app/
│     ├─ platform/          # NaviHelper 연동, 주행 제한, 알림, 서버(시뮬레이션), 보안
│     ├─ data/              # 일정 파싱, 상태 저장
│     ├─ ui/                # 화면 · 컴포넌트 · 테마
│     └─ TripViewModel.kt
├─ prototype/               # 웹 프로토타입 (차량 화면 + 동승자 모바일 웹)
├─ docs/                    # 가이드 반영 사항
└─ vercel.json              # prototype/ 정적 배포 설정
```
