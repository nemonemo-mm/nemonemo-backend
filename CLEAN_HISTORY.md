# Git 히스토리에서 비밀번호 제거하기

## 방법 1: BFG Repo-Cleaner (권장, 가장 빠름)

### 1. BFG 다운로드
```bash
# Windows에서
# https://rtyley.github.io/bfg-repo-cleaner/ 에서 bfg.jar 다운로드
# 또는 Chocolatey 사용: choco install bfg
```

### 2. 비밀번호 제거
```bash
# 1. 원격 저장소 백업 (안전을 위해)
git clone --mirror https://github.com/nemonemo-mm/nemonemo-backend.git backup.git

# 2. 비밀번호 제거
java -jar bfg.jar --replace-text passwords.txt

# passwords.txt 파일 내용:
# you67vr1!==>REMOVED_PASSWORD

# 3. 히스토리 정리
cd backup.git
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 4. Force push (주의!)
git push --force
```

## 방법 2: git filter-branch (기본 제공)

```bash
# 비밀번호를 포함한 모든 파일에서 제거
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch src/main/resources/application-prod.properties && \
   git rm --cached --ignore-unmatch scripts/setup_database.sh" \
  --prune-empty --tag-name-filter cat -- --all

# 히스토리 정리
git for-each-ref --format="delete %(refname)" refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push origin --force --all
git push origin --force --tags
```

## 방법 3: 새 저장소로 시작 (가장 간단하지만 히스토리 완전 삭제)

```bash
# 1. 현재 코드만 새 브랜치로
git checkout --orphan new-main
git add .
git commit -m "Initial commit (cleaned history)"

# 2. 기존 main 삭제하고 새 main으로
git branch -D main
git branch -m main

# 3. Force push
git push origin main --force
```

## ⚠️ 주의사항

1. **백업 필수**: 작업 전 반드시 원격 저장소 백업
2. **Force push**: 이미 푸시된 경우 `--force` 필요
3. **팀원 협의**: 팀원이 있으면 반드시 협의 후 진행
4. **복구 불가**: 히스토리 정리 후 복구 불가능

