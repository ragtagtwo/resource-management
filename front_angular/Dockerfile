FROM node:20.15.0 AS planning

WORKDIR /app

COPY package*.json ./

RUN npm install

RUN npm install -g @angular/cli

COPY . .

RUN npm run build --output-path=dist/proto --configuration=production

FROM nginx:latest 

COPY --from=planning /app/dist/proto/browser /usr/share/nginx/html

COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
