# GitHub Desktop 브랜치 관리 가이드

## ✅ 브랜치 만들기

1. GitHub Desktop 실행
2. 왼쪽 상단 `Current Branch` 클릭
3. `New Branch` 클릭
4. 브랜치 이름 입력 (예: `feature/login`)
5. `Create Branch` 클릭 → 자동으로 해당 브랜치로 이동됨

---
## ✅ 브랜치에서 작업하고 커밋, 푸시

1. 코드 수정 후 GitHub Desktop으로 돌아오기
2. 변경 사항 확인 → 커밋 메시지 작성
3. `Commit to feature/브랜치명` 클릭
4. 오른쪽 위 `Push origin` 클릭 → GitHub에 업로드

---

## ✅ 브랜치를 main에 병합(Merge)

### GitHub 웹에서 Pull Request

- GitHub 웹사이트에서 `Compare & pull request` 클릭
- 설명 작성 후 `Create pull request` → `Merge pull request`


---

## 💡 팁과 규칙

- 브랜치는 기능 단위로: `feature/기능명`
- 한 브랜치에서는 하나의 기능만 개발
- `main`은 항상 안정된 코드만 유지
- Pull Request로 반드시 코드 리뷰 후 머지
