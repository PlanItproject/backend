FROM ubuntu:24.04
MAINTAINER Hyeonjun Ann <annhj980@gmail.com>

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update
RUN apt-get install -y apache2 # Install Apache web server (only 'yes')
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:ondrej/php
RUN apt-get update
RUN apt-get install -y php5.6

RUN apt-get install -y php5.6-mysql

EXPOSE 80

CMD ["apache2ctl", "-D", "FOREGROUND"]
