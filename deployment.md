# AWS EC2 Spring Boot HTTPS Deployment Guide

## Architecture

```text
Vercel (HTTPS)
      |
https://scrapper.duckdns.org/api
      |
    Nginx
      |
localhost:8080
      |
Spring Boot
```

## Prerequisites

- Amazon Linux 2023 EC2
- Spring Boot running as a systemd service (`my-spring-app`)
- DuckDNS hostname
- Ports 80 and 443 open in the EC2 Security Group

## 1. Create a DuckDNS hostname

1. Visit https://www.duckdns.org
2. Create a hostname such as `scrapper.duckdns.org`.
3. Point it to your EC2 public IP.

## 2. Install Nginx

```bash
sudo dnf update -y
sudo dnf install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx
```

## 3. Configure Nginx

Create `/etc/nginx/conf.d/myapp.conf`

```nginx
server {
    listen 80;
    server_name scrapper.duckdns.org;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Test:

```bash
sudo nginx -t
sudo systemctl restart nginx
```

## 4. Install Certbot

```bash
sudo dnf install certbot -y
```

## 5. Generate SSL certificate

```bash
sudo systemctl stop nginx
sudo certbot certonly --standalone -d scrapper.duckdns.org
sudo systemctl start nginx
```

## 6. Enable HTTPS

Replace `/etc/nginx/conf.d/myapp.conf` with:

```nginx
server {
    listen 80;
    server_name scrapper.duckdns.org;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name scrapper.duckdns.org;

    ssl_certificate /etc/letsencrypt/live/scrapper.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/scrapper.duckdns.org/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo nginx -t
sudo systemctl restart nginx
```

## 7. Update Vite

```env
VITE_API_URL=https://scrapper.duckdns.org/api
```

## 8. Configure Spring Boot CORS

Allow your Vercel frontend origin in your CORS configuration.

## Useful Commands

```bash
sudo systemctl restart my-spring-app
sudo systemctl restart nginx
sudo journalctl -u nginx -f
sudo journalctl -u my-spring-app -f
```
