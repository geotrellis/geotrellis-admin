FROM nginx

RUN apt-get update && apt-get install dnsmasq -y
RUN echo "user=root" >> /etc/dnsmasq.conf && \
    echo "address=/.dev/127.0.0.1" >> /etc/dnsmasq.conf
RUN update-rc.d dnsmasq enable
RUN service dnsmasq restart

RUN rm /etc/nginx/conf.d/default.conf

COPY deploy/nginx.conf /etc/nginx

ADD client/target/scala-2.11/classes/index-prod.html                /opt/app/index.html
ADD client/target/scala-2.11/classes/reset.css                      /opt/app/reset.css
ADD client/target/scala-2.11/classes/style.css                      /opt/app/style.css
ADD client/target/scala-2.11/classes/index-bundle.js                /opt/app/index-bundle.js
ADD client/target/scala-2.11/geotrellis-admin-client-opt.js         /opt/app/geotrellis-admin-client-opt.js
ADD client/target/scala-2.11/geotrellis-admin-client-jsdeps.min.js  /opt/app/geotrellis-admin-client-jsdeps.min.js

ADD deploy/favicons.tar.gz /opt/app

EXPOSE 8090

