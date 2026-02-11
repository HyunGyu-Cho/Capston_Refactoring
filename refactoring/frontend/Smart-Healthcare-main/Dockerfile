# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# package.json 및 package-lock.json 복사 (의존성 캐싱 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci --prefer-offline --no-audit

# 소스 코드 복사
COPY . .

# 프로덕션 빌드
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드된 파일 복사
COPY --from=build /app/build /usr/share/nginx/html

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]

