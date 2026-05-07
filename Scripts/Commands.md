# Docker Start
docker compose up -d

# Docker down
docker compose down
docker compose down -v # stop + delete volumes

# verify infra health

docker compose ps

# Admin Credentials

Email: admin@shophub.com
password: Admin123!
Roles : USER, ADMIN

Login : http://localhost:3000/login

Admin Dashboard: http://localhost:3000/admin
