# Production Deployment Guide
## Budgetix — Self-Hosted Production Setup

---

## 1. Prerequisites

| Tool | Minimum Version |
|---|---|
| Docker Engine | 24.x |
| Docker Compose | 2.x |
| Domain name | With DNS A record pointing to your server |
| SSL certificate | Let's Encrypt (Certbot) or a commercial certificate |
| Server | 2 vCPU, 4 GB RAM, 20 GB disk minimum |

---

## 2. Environment Variables

Copy `.env.example` to `.env` and fill in every value before deploying.

```bash
cp .env.example .env
```

**Critical values to change from defaults:**

```env
# Database
POSTGRES_USER=budgetix_prod
POSTGRES_PASSWORD=<strong-random-password>
POSTGRES_DB=budgetix

# Application datasource (matches above)
DATABASE_URL=jdbc:postgresql://postgres:5432/budgetix

# JWT secrets — must be at least 32 characters, cryptographically random
JWT_ACCESS_SECRET=<64-char-random-string>
JWT_REFRESH_SECRET=<64-char-random-string>

# URLs
FRONTEND_URL=https://yourdomain.com
PORT=3000

# Email (production SMTP)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USER=apikey
MAIL_PASS=<sendgrid-api-key>
MAIL_FROM=noreply@yourdomain.com

# File storage
UPLOAD_DIR=/app/uploads
MAX_FILE_SIZE=10MB
```

Generate strong secrets:
```bash
openssl rand -base64 48    # run twice — one for access, one for refresh
```

---

## 3. Production Docker Compose

Create `docker-compose.prod.yml` at the project root:

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    container_name: budgetix_postgres
    restart: always
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - budgetix_net
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U ${POSTGRES_USER}']
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: budgetix_redis
    restart: always
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    networks:
      - budgetix_net

  backend:
    image: budgetix-backend:latest
    container_name: budgetix_backend
    restart: always
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_ACCESS_SECRET: ${JWT_ACCESS_SECRET}
      JWT_REFRESH_SECRET: ${JWT_REFRESH_SECRET}
      FRONTEND_URL: ${FRONTEND_URL}
      MAIL_HOST: ${MAIL_HOST}
      MAIL_PORT: ${MAIL_PORT}
      MAIL_USER: ${MAIL_USER}
      MAIL_PASS: ${MAIL_PASS}
      MAIL_FROM: ${MAIL_FROM}
      UPLOAD_DIR: /app/uploads
      MAX_FILE_SIZE: ${MAX_FILE_SIZE}
      PORT: 3000
    volumes:
      - uploads:/app/uploads
    networks:
      - budgetix_net

  frontend:
    image: budgetix-frontend:latest
    container_name: budgetix_frontend
    restart: always
    networks:
      - budgetix_net

  nginx:
    image: nginx:alpine
    container_name: budgetix_nginx
    restart: always
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    depends_on:
      - backend
      - frontend
    networks:
      - budgetix_net

volumes:
  postgres_data:
  redis_data:
  uploads:

networks:
  budgetix_net:
    driver: bridge
```

---

## 4. Build Docker Images

### 4.1 Backend Dockerfile

Create `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/budgetix-*.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build:
```bash
cd backend
mvn clean package -DskipTests
docker build -t budgetix-backend:latest .
```

### 4.2 Frontend Dockerfile

Create `frontend/Dockerfile`:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx-spa.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

Create `frontend/nginx-spa.conf`:
```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;
  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

Build:
```bash
cd frontend
docker build -t budgetix-frontend:latest .
```

---

## 5. Nginx Reverse Proxy

Create `nginx/nginx.conf`:

```nginx
worker_processes auto;

events { worker_connections 1024; }

http {
  # Rate limiting
  limit_req_zone $binary_remote_addr zone=api:10m rate=30r/m;

  upstream backend  { server budgetix_backend:3000; }
  upstream frontend { server budgetix_frontend:80; }

  # Redirect HTTP → HTTPS
  server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$host$request_uri;
  }

  server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    ssl_session_cache   shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-Frame-Options DENY always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    client_max_body_size 10M;

    # API
    location /api/ {
      limit_req zone=api burst=60 nodelay;
      proxy_pass         http://backend;
      proxy_set_header   Host              $host;
      proxy_set_header   X-Real-IP         $remote_addr;
      proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
      proxy_set_header   X-Forwarded-Proto $scheme;
      proxy_read_timeout 60s;
    }

    # Frontend SPA
    location / {
      proxy_pass       http://frontend;
      proxy_set_header Host $host;
    }
  }
}
```

---

## 6. SSL Certificate (Let's Encrypt)

```bash
# Install certbot on the host
sudo apt install certbot

# Stop nginx temporarily if running
docker-compose -f docker-compose.prod.yml stop nginx

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Certificates saved to /etc/letsencrypt/live/yourdomain.com/

# Auto-renew cron
echo "0 3 * * * certbot renew --quiet && docker-compose -f /path/to/docker-compose.prod.yml exec nginx nginx -s reload" | sudo crontab -
```

---

## 7. Deploy

```bash
# Pull/build images on the server
docker-compose -f docker-compose.prod.yml pull
# or build locally and push to a registry

# Start all services
docker-compose -f docker-compose.prod.yml --env-file .env up -d

# Watch logs
docker-compose -f docker-compose.prod.yml logs -f backend
```

**Flyway migrations run automatically** on backend startup — the database schema will be created on first launch.

---

## 8. Database Backups

### Automated daily backup

```bash
#!/bin/bash
# /etc/cron.daily/budgetix-backup

BACKUP_DIR="/backups/budgetix"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p "$BACKUP_DIR"

docker exec budgetix_postgres pg_dump \
  -U "${POSTGRES_USER}" \
  "${POSTGRES_DB}" \
  | gzip > "${BACKUP_DIR}/backup_${TIMESTAMP}.sql.gz"

# Keep last 30 days
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +30 -delete
```

```bash
chmod +x /etc/cron.daily/budgetix-backup
```

### Restore from backup

```bash
gunzip -c backup_20260501_030000.sql.gz | \
  docker exec -i budgetix_postgres psql -U budgetix budgetix
```

---

## 9. Monitoring

### Health check endpoint (for uptime monitors)

```
GET https://yourdomain.com/api/actuator/health
Expected: { "status": "UP" }
```

### Container resource monitoring

```bash
docker stats budgetix_backend budgetix_postgres budgetix_frontend
```

### Application logs

```bash
# Live backend logs
docker logs -f budgetix_backend

# Save to file
docker logs budgetix_backend > backend.log 2>&1
```

### Log levels (application.yml)

```yaml
logging:
  level:
    com.budgetix: INFO       # change to DEBUG for troubleshooting
    org.springframework.security: WARN
```

---

## 10. Scaling

### Horizontal backend scaling

The API is fully stateless (JWT-based, no server-side session). Add multiple backend instances behind a load balancer:

```yaml
# docker-compose.prod.yml
backend:
  deploy:
    replicas: 3
```

Ensure all instances share the same `UPLOAD_DIR` volume (use NFS or cloud object storage for multi-node deployments).

### Database connection pool

Tune in `application.yml` based on instance count:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10        # per instance
      minimum-idle: 2
      connection-timeout: 30000
```

---

## 11. Security Hardening Checklist

- [ ] All `.env` secrets are unique, random, and never committed to git
- [ ] `JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` are ≥ 64 characters
- [ ] PostgreSQL port is NOT exposed to the internet (only internal Docker network)
- [ ] Redis port is NOT exposed to the internet
- [ ] Nginx rate limiting is enabled on `/api/`
- [ ] HTTPS enforced — HTTP redirects to HTTPS
- [ ] HSTS header set with `max-age=31536000`
- [ ] `X-Frame-Options: DENY` to prevent clickjacking
- [ ] `client_max_body_size` limits file upload size at proxy level
- [ ] MailHog container is removed (use real SMTP in production)
- [ ] Swagger UI disabled in production or protected by IP allow-list
- [ ] Regular automated database backups running and tested

### Disable Swagger UI in production

In `application.yml` (or via env override):

```yaml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

---

## 12. Updating / Rolling Deployments

```bash
# 1. Build new images
cd backend && mvn clean package -DskipTests && docker build -t budgetix-backend:latest .
cd ../frontend && docker build -t budgetix-frontend:latest .

# 2. Rolling restart (zero downtime if replicas > 1)
docker-compose -f docker-compose.prod.yml up -d --no-deps --build backend
docker-compose -f docker-compose.prod.yml up -d --no-deps --build frontend

# 3. Verify health
curl https://yourdomain.com/api/actuator/health
```

Flyway will automatically apply any new migrations on backend startup.

---

## 13. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Backend fails to start | DB not ready | Ensure `depends_on: condition: service_healthy` |
| `AccessDeniedException` on `.class` | Windows dev only — file lock from killed JVM | Delete `target/` and rebuild |
| 502 Bad Gateway from Nginx | Backend not running | `docker logs budgetix_backend` |
| Email not received | Wrong SMTP settings | Check `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER` |
| JWT `invalid signature` | Secret mismatch | Ensure all instances use same `JWT_ACCESS_SECRET` |
| Migrations fail on startup | Dirty Flyway state | `DELETE FROM flyway_schema_history WHERE success=false` then restart |
| Upload returns 413 | File too large at Nginx | Increase `client_max_body_size` in nginx.conf |
