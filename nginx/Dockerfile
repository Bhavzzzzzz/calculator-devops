FROM nginx:1.27-alpine
# Config is baked into the image (not bind-mounted) so the stack can be deployed
# from inside the Jenkins container, where host paths are not available
COPY nginx.conf /etc/nginx/conf.d/default.conf
