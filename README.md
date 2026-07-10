# TermProject (Quiz App)

Firebase 기반 안드로이드 객관식 문제집/자체 시험 앱입니다. 사진(카메라/갤러리)에서 OCR로 문제 텍스트를 추출해 등록하고, OpenAI API로 유사 문제를 자동 생성하며, 폴더 단위로 문제를 모아 시험을 보고 정답률과 오답을 확인할 수 있습니다.

## 주요 기능

- **로그인** — Firebase Authentication(이메일/비밀번호) 로그인 (`LoginActivity`)
- **폴더 관리** — 주제별 폴더를 만들고 폴더 안에 문제를 저장 (`MainActivity`, `FolderAdapter`)
- **문제 만들기 + OCR** — 카메라로 촬영하거나 갤러리에서 사진을 선택하면 Tesseract(`tess-two`)로 텍스트를 추출해 문제 입력칸에 자동 채움 (`MakeTestActivity`)
- **AI 유사 문제 생성** — 기존 문제를 바탕으로 OpenAI Chat Completions API(`gpt-3.5-turbo`)에 프롬프트를 보내 오답 3개를 포함한 유사 문제를 생성하고 Firestore에 저장 (`AICreateActivity`)
- **시험 보기** — 폴더와 제한 시간을 선택해 문제를 풀고 답안을 Firestore에 세션 단위로 기록 (`TestReadyActivity`, `TakeTestActivity`)
- **정답률 확인** — 폴더별/전체 정답률을 코루틴으로 계산해 표시 (`ShowRateActivity`)
- **오답 노트** — 틀린 문제만 모아서 다시 보고, 오답을 바탕으로 AI 문제 생성으로 바로 연결 (`ShowWrongAnswerActivity`)
- **뽀모도로 학습 타이머** — 공부/휴식 사이클을 관리하는 타이머 (`StudyTimerActivity`)

## 기술 스택

- **언어/UI**: Kotlin, View Binding / Data Binding (XML 레이아웃, Jetpack Compose 미사용)
- **백엔드**: Firebase Authentication, Cloud Firestore, Firebase Storage, Firebase UI Auth
- **네트워킹**: Retrofit, OkHttp(+ Logging Interceptor), Gson Converter
- **OCR**: `tess-two` (Tesseract 안드로이드 바인딩, `kor`/`eng` 학습 데이터 포함)
- **AI**: OpenAI Chat Completions API (`gpt-3.5-turbo`) 직접 REST 호출
- **DI**: Hilt
- **비동기**: Kotlin Coroutines

## 프로젝트 구조

```
app/src/main/java/com/example/termproject/
├── LoginActivity.kt            # Firebase Auth 로그인
├── MainActivity.kt             # 폴더 목록 (홈 화면)
├── MakeTestActivity.kt         # 문제 등록 + OCR
├── AICreateActivity.kt         # OpenAI로 유사 문제 생성
├── SaveConfirmActivity.kt      # 문제 저장 확인
├── TestReadyActivity.kt        # 시험 폴더/시간 선택
├── TakeTestActivity.kt         # 시험 응시
├── AfterTestActivity.kt        # 시험 결과
├── ShowRateActivity.kt         # 정답률 통계
├── ShowWrongAnswerActivity.kt  # 오답 노트
├── StudyTimerActivity.kt       # 뽀모도로 타이머
├── FileDataActivity.kt         # 문제 상세 보기
├── FileAdapter.kt / FolderAdapter.kt / FileData.kt
```

## 시작하기

### 요구 사항

- Android Studio (최신 안정 버전)
- JDK 11
- `compileSdk`/`targetSdk` 35, `minSdk` 24

### 설정

1. Firebase 프로젝트를 생성하고 `google-services.json`을 `app/` 디렉터리에 추가합니다.
2. 프로젝트 루트에 `local.properties` 파일을 만들고 OpenAI API 키를 추가합니다.

   ```properties
   OPENAI_API_KEY=sk-...
   ```

   (빌드 시 `BuildConfig.OPENAI_API_KEY`로 주입되며, 값이 없으면 빌드가 실패합니다.)

3. Gradle Sync 후 앱을 실행합니다.

```
./gradlew assembleDebug
```

## 권한

- `INTERNET` — Firebase/OpenAI 통신
- `CAMERA` (선택) — 문제 사진 촬영
- `READ_MEDIA_STORAGE` — 갤러리에서 이미지 선택
