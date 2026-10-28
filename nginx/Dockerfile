# The config is baked into the image rather than bind-mounted, so the stack works the
# same from any directory (including the Jenkins workspace) and a config change
# produces a new image that `docker compose up --build` picks up automatically.
FROM nginx:1.28-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
