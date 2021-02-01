FROM java:8-jre-alpine
COPY build/libs/tourGuide-1.0.0.jar /srv
#EXPOSE 8083
WORKDIR /srv
CMD java -jar /srv/tourGuide-1.0.0.jar