# ==========================================
# Stage 1: Build React SPA with Node 20
# ==========================================
FROM node:20-alpine AS builder
WORKDIR /app

# Cache npm dependencies layer
COPY package.json package-lock.json ./
RUN npm ci

# Copy sources and build SPA
COPY . .

# Pass API Base URL at build time (defaults to /api/v1 for reverse proxy)
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ==========================================
# Stage 2: Serve SPA with Nginx Alpine
# ==========================================
FROM nginx:1.27-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

